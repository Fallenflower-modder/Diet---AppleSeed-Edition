package net.appleseed.appleseed.common.data.group;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.appleseed.appleseed.AppleSeedConstants;
import net.appleseed.appleseed.api.type.IDietGroup;
import net.appleseed.appleseed.api.util.DietColor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class DietGroups extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    public static final DietGroups SERVER = new DietGroups();
    public static final DietGroups CLIENT = new DietGroups();

    private Map<String, IDietGroup> groups = new HashMap<>();

    public DietGroups() {
        super(GSON, "diet/groups");
    }

    public static Set<IDietGroup> getGroups(Level level) {
        DietGroups instance = level.isClientSide ? CLIENT : SERVER;
        return ImmutableSet.copyOf(instance.groups.values());
    }

    public static Optional<IDietGroup> getGroup(Level level, String name) {
        DietGroups instance = level.isClientSide ? CLIENT : SERVER;
        return Optional.ofNullable(instance.groups.get(name));
    }

    public Set<IDietGroup> getGroups() {
        return ImmutableSet.copyOf(this.groups.values());
    }

    public Optional<IDietGroup> getGroup(String name) {
        return Optional.ofNullable(this.groups.get(name));
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<String, IDietGroup> entry : this.groups.entrySet()) {
            tag.put(entry.getKey(), entry.getValue().save());
        }
        return tag;
    }

    public void load(CompoundTag tag) {
        Map<String, IDietGroup> loaded = new HashMap<>();
        for (String key : tag.getAllKeys()) {
            loaded.put(key, DietGroup.load((CompoundTag) Objects.requireNonNull(tag.get(key))));
        }
        this.groups = loaded;
    }

    @Override
    protected void apply(@Nonnull Map<ResourceLocation, JsonElement> object,
                         @Nonnull ResourceManager resourceManager,
                         @Nonnull ProfilerFiller profilerFiller) {
        Map<String, DietGroup.Builder> map = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();

            if (resourcelocation.getNamespace().equals(AppleSeedConstants.MOD_ID)) {
                try {
                    buildGroup(map.computeIfAbsent(resourcelocation.getPath(), DietGroup.Builder::new),
                            GsonHelper.convertToJsonObject(entry.getValue(), "top element"));
                } catch (IllegalArgumentException | JsonParseException e) {
                    AppleSeedConstants.LOG.error("Parsing error loading diet group {}", resourcelocation, e);
                }
            }
        }

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();

            if (resourcelocation.getPath().startsWith("_") ||
                    resourcelocation.getNamespace().equals(AppleSeedConstants.MOD_ID)) {
                continue;
            }

            try {
                buildGroup(map.computeIfAbsent(resourcelocation.getPath(), DietGroup.Builder::new),
                        GsonHelper.convertToJsonObject(entry.getValue(), "top element"));
            } catch (IllegalArgumentException | JsonParseException e) {
                AppleSeedConstants.LOG.error("Parsing error loading diet group {}", resourcelocation, e);
            }
        }

        groups = map.entrySet().stream()
                .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, entry -> entry.getValue().build()));
        AppleSeedConstants.LOG.info("Loaded {} diet groups", map.size());
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

        TagKey<Item> tag = TagKey.create(BuiltInRegistries.ITEM.key(),
                ResourceLocation.fromNamespaceAndPath(AppleSeedConstants.MOD_ID, builder.name));
        builder.tag(tag);
    }
}
