package net.appleseed.appleseed.common.data.group;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonArray;
import net.appleseed.appleseed.AppleSeedConstants;
import net.appleseed.appleseed.api.type.IDietGroup;
import net.appleseed.appleseed.api.util.DietColor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.*;

public class DietGroups extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    public static final DietGroups SERVER = new DietGroups();
    public static final DietGroups CLIENT = new DietGroups();

    private Map<String, IDietGroup> groups = new HashMap<>();
    private static final Set<String> disabledGroups = new HashSet<>();

    public DietGroups() {
        super(GSON, "diet/groups");
    }

    public static boolean isGroupDisabled(String groupName) {
        return disabledGroups.contains(groupName);
    }

    public static Set<IDietGroup> getGroups(Level level) {
        DietGroups instance = level.isClientSide ? CLIENT : SERVER;
        Set<IDietGroup> result = new HashSet<>();
        for (IDietGroup group : instance.groups.values()) {
            if (!disabledGroups.contains(group.getName())) {
                result.add(group);
            }
        }
        return ImmutableSet.copyOf(result);
    }

    public static Optional<IDietGroup> getGroup(Level level, String name) {
        if (disabledGroups.contains(name)) {
            return Optional.empty();
        }
        DietGroups instance = level.isClientSide ? CLIENT : SERVER;
        return Optional.ofNullable(instance.groups.get(name));
    }

    public Set<IDietGroup> getGroups() {
        Set<IDietGroup> result = new HashSet<>();
        for (IDietGroup group : this.groups.values()) {
            if (!disabledGroups.contains(group.getName())) {
                result.add(group);
            }
        }
        return ImmutableSet.copyOf(result);
    }

    public Optional<IDietGroup> getGroup(String name) {
        if (disabledGroups.contains(name)) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.groups.get(name));
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<String, IDietGroup> entry : this.groups.entrySet()) {
            if (!disabledGroups.contains(entry.getKey())) {
                tag.put(entry.getKey(), entry.getValue().save());
            }
        }
        return tag;
    }

    public void load(CompoundTag tag) {
        Map<String, IDietGroup> loaded = new HashMap<>();
        for (String key : tag.getAllKeys()) {
            if (!disabledGroups.contains(key)) {
                loaded.put(key, DietGroup.load((CompoundTag) Objects.requireNonNull(tag.get(key))));
            }
        }
        this.groups = loaded;
    }

    @Override
    protected void apply(@Nonnull Map<ResourceLocation, JsonElement> object,
                         @Nonnull ResourceManager resourceManager,
                         @Nonnull ProfilerFiller profilerFiller) {
        disabledGroups.clear();

        processDisabledGroups(object);

        Map<String, java.util.AbstractMap.SimpleEntry<Integer, DietGroup.Builder>> groupMap = new HashMap<>();
        Set<String> processedNames = new HashSet<>();

        List<Map.Entry<ResourceLocation, JsonElement>> entries = new ArrayList<>(object.entrySet());
        entries.sort((a, b) -> {
            int prioA = getFilePriority(a.getKey());
            int prioB = getFilePriority(b.getKey());
            return Integer.compare(prioB, prioA);
        });

        for (Map.Entry<ResourceLocation, JsonElement> entry : entries) {
            ResourceLocation resourcelocation = entry.getKey();
            String path = resourcelocation.getPath();

            if (path.equals("disabled_groups") || path.startsWith("_") || path.contains("disabled_groups")) {
                continue;
            }

            int filePriority = getFilePriority(resourcelocation);

            if (processedNames.contains(path)) {
                java.util.AbstractMap.SimpleEntry<Integer, DietGroup.Builder> existing = groupMap.get(path);
                if (existing != null && existing.getKey() >= filePriority) {
                    continue;
                }
            }

            try {
                JsonElement json = entry.getValue();
                if (!json.isJsonObject()) {
                    AppleSeedConstants.LOG.warn("Skipping non-object diet group file {}", resourcelocation);
                    continue;
                }

                DietGroup.Builder builder = new DietGroup.Builder(path);
                buildGroup(builder, GsonHelper.convertToJsonObject(json, "top element"));

                if (processedNames.contains(path)) {
                    java.util.AbstractMap.SimpleEntry<Integer, DietGroup.Builder> existing = groupMap.get(path);
                    AppleSeedConstants.LOG.info("Diet group '{}' overridden by {} (priority {} > {})",
                            path, resourcelocation, filePriority, existing.getKey());
                }

                groupMap.put(path, new java.util.AbstractMap.SimpleEntry<>(filePriority, builder));
                processedNames.add(path);

            } catch (IllegalArgumentException | JsonParseException e) {
                groupMap.remove(path);
                processedNames.remove(path);
                AppleSeedConstants.LOG.error("Parsing error loading diet group {}, removed from map", resourcelocation, e);
            }
        }

        Map<String, IDietGroup> finalGroups = new HashMap<>();
        for (Map.Entry<String, java.util.AbstractMap.SimpleEntry<Integer, DietGroup.Builder>> entry : groupMap.entrySet()) {
            if (!disabledGroups.contains(entry.getKey())) {
                finalGroups.put(entry.getKey(), entry.getValue().getValue().build());
            }
        }
        groups = ImmutableMap.copyOf(finalGroups);
        AppleSeedConstants.LOG.info("Loaded {} diet groups ({} disabled)", groups.size(), disabledGroups.size());
    }

    private int getFilePriority(ResourceLocation location) {
        String namespace = location.getNamespace();
        if (namespace.equals(AppleSeedConstants.MOD_ID)) {
            return 1;
        }
        return 2;
    }

    private void processDisabledGroups(Map<ResourceLocation, JsonElement> object) {
        Set<String> highestPriorityDisabled = null;
        int highestPriority = -1;

        Map<Integer, Set<String>> samePriorityDisabled = new LinkedHashMap<>();

        List<Map.Entry<ResourceLocation, JsonElement>> entries = new ArrayList<>(object.entrySet());
        entries.sort((a, b) -> {
            int prioA = getFilePriority(a.getKey());
            int prioB = getFilePriority(b.getKey());
            return Integer.compare(prioB, prioA);
        });

        for (Map.Entry<ResourceLocation, JsonElement> entry : entries) {
            ResourceLocation resourcelocation = entry.getKey();
            if (!resourcelocation.getPath().equals("disabled_groups")) {
                continue;
            }

            try {
                JsonElement json = entry.getValue();
                if (!json.isJsonArray()) {
                    AppleSeedConstants.LOG.warn("Invalid disabled_groups format in {}, expected JSON array, skipping", resourcelocation);
                    continue;
                }

                JsonArray array = json.getAsJsonArray();
                Set<String> disabled = new HashSet<>();
                for (JsonElement element : array) {
                    String groupId = element.getAsString();
                    disabled.add(groupId);
                }

                int priority = getFilePriority(resourcelocation);

                if (highestPriority == -1 || priority > highestPriority) {
                    highestPriority = priority;
                    highestPriorityDisabled = disabled;
                    samePriorityDisabled.clear();
                    samePriorityDisabled.put(priority, new HashSet<>(disabled));
                } else if (priority == highestPriority) {
                    if (samePriorityDisabled.containsKey(priority)) {
                        samePriorityDisabled.get(priority).addAll(disabled);
                    } else {
                        samePriorityDisabled.put(priority, new HashSet<>(disabled));
                    }
                }

                AppleSeedConstants.LOG.info("Found disabled_groups from {} (priority {}): {} groups",
                        resourcelocation, priority, disabled.size());

            } catch (Exception e) {
                AppleSeedConstants.LOG.error("Failed to process disabled_groups: {}", resourcelocation, e);
            }
        }

        if (!samePriorityDisabled.isEmpty()) {
            disabledGroups.clear();
            for (Set<String> groups : samePriorityDisabled.values()) {
                disabledGroups.addAll(groups);
            }
            AppleSeedConstants.LOG.info("Processed disabled_groups, total {} disabled groups", disabledGroups.size());
        }

        for (String groupId : new HashSet<>(disabledGroups)) {
            boolean exists = false;
            for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
                if (entry.getKey().getPath().equals(groupId)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                AppleSeedConstants.LOG.warn("Disabled group '{}' does not exist, skipping", groupId);
                disabledGroups.remove(groupId);
            }
        }
    }

    private void buildGroup(DietGroup.Builder builder, JsonObject json) {
        String iconName = GsonHelper.getAsString(json, "icon");
        Item icon = BuiltInRegistries.ITEM.get(ResourceLocation.parse(iconName));
        if (icon == Items.AIR) {
            icon = Items.APPLE;
        }
        builder.icon(icon);

        if (json.has("color")) {
            builder.color(DietColor.fromHex(GsonHelper.getAsString(json, "color")));
        }
        builder.order(GsonHelper.getAsInt(json, "order", 0));
        builder.defaultValue(GsonHelper.getAsFloat(json, "default_value", 0.0f));
        builder.gainMultiplier(GsonHelper.getAsDouble(json, "gain_multiplier", 1.0));
        builder.decayMultiplier(GsonHelper.getAsDouble(json, "decay_multiplier", 1.0));
        builder.beneficial(GsonHelper.getAsBoolean(json, "beneficial", true));

        if (json.has("translation_key")) {
            builder.translationKey(GsonHelper.getAsString(json, "translation_key"));
        }

        TagKey<Item> tag = TagKey.create(BuiltInRegistries.ITEM.key(),
                ResourceLocation.fromNamespaceAndPath(AppleSeedConstants.MOD_ID, builder.name));
        builder.tag(tag);
    }
}
