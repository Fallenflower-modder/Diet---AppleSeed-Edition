package net.appleseed.appleseed.common.data.food;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.appleseed.appleseed.AppleSeedConstants;
import net.appleseed.appleseed.common.data.ServerDietConfig;
import net.appleseed.appleseed.common.data.group.DietGroups;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class FoodNutritionManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final java.nio.file.Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("apple_seed_foods");
    public static final FoodNutritionManager INSTANCE = new FoodNutritionManager();
    public static final FoodNutritionManager CLIENT = new FoodNutritionManager();

    final Map<Item, Map<String, Float>> foodNutrition = new HashMap<>();

    private FoodNutritionManager() {
        super(GSON, "diet/foods");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.foodNutrition.clear();

        int configCount = loadConfigFiles();
        AppleSeedConstants.LOG.info("Loaded {} config auto-generated food nutrition entries (lowest priority)", configCount);

        int resourceCount = 0;
        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            try {
                JsonObject json = entry.getValue().getAsJsonObject();
                String sourceItem = json.get("source_item").getAsString();
                ResourceLocation itemId = ResourceLocation.tryParse(sourceItem);
                Item item = itemId != null ? BuiltInRegistries.ITEM.get(itemId) : null;

                if (item == null || item == Items.AIR) {
                    AppleSeedConstants.LOG.warn("Skipping unknown food item '{}' in file {}", sourceItem, entry.getKey());
                    continue;
                }

                Map<String, Float> nutritions = new HashMap<>();
                if (json.has("nutritions")) {
                    JsonObject nutritionsJson = json.getAsJsonObject("nutritions");
                    for (String key : nutritionsJson.keySet()) {
                        float value = nutritionsJson.get(key).getAsFloat();
                        if (!DietGroups.isGroupDisabled(key, this == CLIENT)) {
                            nutritions.put(key, value);
                        }
                    }
                }
                this.foodNutrition.put(item, nutritions);
                resourceCount++;
            } catch (Exception e) {
                AppleSeedConstants.LOG.error("Failed to load food nutrition: {}", entry.getKey(), e);
            }
        }

        AppleSeedConstants.LOG.info("Loaded {} resource pack food nutrition entries (datapack > other mods > appleseed built-in)", resourceCount);
        AppleSeedConstants.LOG.info("Final priority: WORLD DATAPACK > OTHER MODS > APPLESEED BUILT-IN > CONFIG AUTO-GENERATED");
        AppleSeedConstants.LOG.info("Total food nutrition entries: {}", this.foodNutrition.size());
    }

    public void reloadConfigFiles() {
        int count = loadConfigFiles();
        AppleSeedConstants.LOG.info("Reloaded {} config auto-generated food nutrition entries", count);
    }

    private int loadConfigFiles() {
        File configDir = CONFIG_DIR.toFile();
        if (!configDir.exists() || !configDir.isDirectory()) {
            return 0;
        }

        int count = 0;
        File[] files = configDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) {
            return 0;
        }

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                String sourceItem = json.get("source_item").getAsString();
                ResourceLocation itemId = ResourceLocation.tryParse(sourceItem);
                Item item = itemId != null ? BuiltInRegistries.ITEM.get(itemId) : null;

                if (item == null || item == Items.AIR) {
                    AppleSeedConstants.LOG.warn("Skipping unknown food item '{}' in config file {}", sourceItem, file.getName());
                    continue;
                }

                Map<String, Float> nutritions = new HashMap<>();
                if (json.has("nutritions")) {
                    JsonObject nutritionsJson = json.getAsJsonObject("nutritions");
                    for (String key : nutritionsJson.keySet()) {
                        float value = nutritionsJson.get(key).getAsFloat();
                        if (!DietGroups.isGroupDisabled(key, this == CLIENT)) {
                            nutritions.put(key, value);
                        }
                    }
                }
                this.foodNutrition.put(item, nutritions);
                count++;
            } catch (Exception e) {
                AppleSeedConstants.LOG.error("Failed to load config food nutrition: {}", file.getName(), e);
            }
        }

        return count;
    }

    public float getNutritionValue(Item item, String group) {
        if (DietGroups.isGroupDisabled(group, this == CLIENT)) {
            return 0.0f;
        }
        Map<String, Float> nutritions = this.foodNutrition.get(item);
        if (nutritions != null) {
            return nutritions.getOrDefault(group, 0.0f);
        }
        return 0.0f;
    }

    public Map<String, Float> getNutritions(Item item) {
        Map<String, Float> result = new HashMap<>();
        Map<String, Float> all = this.foodNutrition.getOrDefault(item, new HashMap<>());
        for (Map.Entry<String, Float> entry : all.entrySet()) {
            if (!DietGroups.isGroupDisabled(entry.getKey(), this == CLIENT)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    public boolean hasNutritionData(Item item) {
        return this.foodNutrition.containsKey(item);
    }

    public Map<Item, Map<String, Float>> getAllFoodData() {
        return this.foodNutrition;
    }

    public static Map<String, Float> getNutritionsForClient(Item item, boolean isClientSide) {
        if (isClientSide && ServerDietConfig.isConnectedToDedicatedServer()) {
            String foodId = BuiltInRegistries.ITEM.getKey(item).toString();
            return ServerDietConfig.getServerFoodNutrition(foodId);
        }
        FoodNutritionManager instance = isClientSide ? CLIENT : INSTANCE;
        return instance.getNutritions(item);
    }
}
