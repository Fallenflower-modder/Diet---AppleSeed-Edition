package net.appleseed.appleseed.common.data.food;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.appleseed.appleseed.AppleSeedConstants;
import net.appleseed.appleseed.api.query.IDietFoodQuery;
import net.appleseed.appleseed.common.capability.DietData;
import net.appleseed.appleseed.common.data.ServerDietConfig;
import net.appleseed.appleseed.common.data.group.DietGroups;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.GsonHelper;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class FoodNutritionManager extends SimpleJsonResourceReloadListener implements IDietFoodQuery {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final java.nio.file.Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("apple_seed_foods");
    private static final java.nio.file.Path CONFIG_BLOCKS_DIR = FMLPaths.CONFIGDIR.get().resolve("apple_seed_blocks");
    public static final FoodNutritionManager INSTANCE = new FoodNutritionManager();
    public static final FoodNutritionManager CLIENT = new FoodNutritionManager();

    final Map<Item, Map<String, Float>> foodNutrition = new HashMap<>();
    final Map<Block, Map<String, Float>> blockNutrition = new HashMap<>();
    final Map<Block, Integer> blockBites = new HashMap<>();

    private FoodNutritionManager() {
        super(GSON, "diet/foods");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.foodNutrition.clear();
        this.blockNutrition.clear();
        this.blockBites.clear();

        int configCount = loadConfigFiles();
        AppleSeedConstants.LOG.info("Loaded {} config auto-generated food nutrition entries (lowest priority)", configCount);

        int resourceCount = 0;
        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            try {
                JsonElement element = entry.getValue();
                if (element.isJsonArray()) {
                    JsonArray array = element.getAsJsonArray();
                    for (JsonElement item : array) {
                        if (item.isJsonObject() && parseFoodEntry(item.getAsJsonObject(), entry.getKey().toString() + " (array)")) {
                            resourceCount++;
                        }
                    }
                } else if (element.isJsonObject()) {
                    if (parseFoodEntry(element.getAsJsonObject(), entry.getKey().toString())) {
                        resourceCount++;
                    }
                }
            } catch (Exception e) {
                AppleSeedConstants.LOG.error("Failed to load food nutrition: {}", entry.getKey(), e);
            }
        }

        AppleSeedConstants.LOG.info("Loaded {} resource pack food nutrition entries (datapack > other mods > appleseed built-in)", resourceCount);

        int blockResourceCount = loadBlocksFromResources(resourceManager);
        AppleSeedConstants.LOG.info("Loaded {} resource pack block food entries", blockResourceCount);

        int blockConfigCount = loadBlockConfigFiles();
        AppleSeedConstants.LOG.info("Loaded {} config auto-generated block food entries", blockConfigCount);

        int appleSeedCount = loadAppleSeedData();
        AppleSeedConstants.LOG.info("Loaded {} appleseed_data.json entries (highest priority)", appleSeedCount);
        AppleSeedConstants.LOG.info("Final priority: APPLESEED_DATA.JSON > DATAPACK > OTHER MODS > APPLESEED BUILT-IN > CONFIG AUTO-GENERATED");
        AppleSeedConstants.LOG.info("Total food nutrition entries: {} items + {} blocks", this.foodNutrition.size(), this.blockNutrition.size());
    }

    public void reloadConfigFiles() {
        int configCount = loadConfigFiles();
        int blockConfigCount = loadBlockConfigFiles();
        int appleSeedCount = loadAppleSeedData();
        AppleSeedConstants.LOG.info("Reloaded {} config entries + {} block config entries + {} appleseed_data.json entries", configCount, blockConfigCount, appleSeedCount);
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
                JsonElement element = GSON.fromJson(reader, JsonElement.class);
                if (element.isJsonArray()) {
                    JsonArray array = element.getAsJsonArray();
                    for (JsonElement item : array) {
                        if (item.isJsonObject() && parseFoodEntry(item.getAsJsonObject(), file.getName() + " (array)")) {
                            count++;
                        }
                    }
                } else if (element.isJsonObject()) {
                    if (parseFoodEntry(element.getAsJsonObject(), file.getName())) {
                        count++;
                    }
                }
            } catch (Exception e) {
                AppleSeedConstants.LOG.error("Failed to load config food nutrition: {}", file.getName(), e);
            }
        }

        return count;
    }

    private int loadAppleSeedData() {
        java.nio.file.Path appleSeedData = FMLPaths.CONFIGDIR.get().resolve("appleseed_data.json");
        File file = appleSeedData.toFile();
        if (!file.exists() || !file.isFile()) {
            return 0;
        }

        int count = 0;
        try (FileReader reader = new FileReader(file)) {
            JsonElement element = GSON.fromJson(reader, JsonElement.class);
            if (element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                for (JsonElement item : array) {
                    if (item.isJsonObject()) {
                        JsonObject obj = item.getAsJsonObject();
                        if (obj.has("source_item") && parseFoodEntry(obj, "appleseed_data.json")) {
                            count++;
                        } else if (obj.has("source_block") && parseBlockEntry(obj, "appleseed_data.json")) {
                            count++;
                        }
                    }
                }
            } else if (element.isJsonObject()) {
                JsonObject root = element.getAsJsonObject();
                if (root.has("items") && root.get("items").isJsonArray()) {
                    JsonArray items = root.getAsJsonArray("items");
                    for (JsonElement item : items) {
                        if (item.isJsonObject() && parseFoodEntry(item.getAsJsonObject(), "appleseed_data.json")) {
                            count++;
                        }
                    }
                } else if (root.has("source_item")) {
                    if (parseFoodEntry(root, "appleseed_data.json")) {
                        count++;
                    }
                }
                if (root.has("blocks") && root.get("blocks").isJsonArray()) {
                    JsonArray blocks = root.getAsJsonArray("blocks");
                    for (JsonElement block : blocks) {
                        if (block.isJsonObject() && parseBlockEntry(block.getAsJsonObject(), "appleseed_data.json")) {
                            count++;
                        }
                    }
                } else if (root.has("source_block")) {
                    if (parseBlockEntry(root, "appleseed_data.json")) {
                        count++;
                    }
                }
            }
        } catch (Exception e) {
            AppleSeedConstants.LOG.error("Failed to load appleseed_data.json", e);
        }

        return count;
    }

    private int loadBlocksFromResources(ResourceManager resourceManager) {
        int count = 0;
        try {
            Map<ResourceLocation, Resource> resources = resourceManager.listResources(
                    "diet/blocks",
                    path -> path.getPath().endsWith(".json")
            );
            AppleSeedConstants.LOG.debug("FoodNutritionManager: loadBlocksFromResources found {} resources at diet/blocks", resources.size());
            for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
                AppleSeedConstants.LOG.debug("FoodNutritionManager: loading block food from {}", entry.getKey());
                try (Reader reader = new InputStreamReader(entry.getValue().open())) {
                    JsonElement element = GSON.fromJson(reader, JsonElement.class);
                    if (element.isJsonArray()) {
                        JsonArray array = element.getAsJsonArray();
                        for (JsonElement item : array) {
                            if (item.isJsonObject() && parseBlockEntry(item.getAsJsonObject(), entry.getKey().toString() + " (array)")) {
                                count++;
                            }
                        }
                    } else if (element.isJsonObject() && parseBlockEntry(element.getAsJsonObject(), entry.getKey().toString())) {
                        count++;
                    }
                } catch (Exception e) {
                    AppleSeedConstants.LOG.error("Failed to load block food: {}", entry.getKey(), e);
                }
            }
        } catch (Exception e) {
            AppleSeedConstants.LOG.warn("Could not list diet/blocks resources: {}", e.getMessage());
        }
        return count;
    }

    private int loadBlockConfigFiles() {
        File configDir = CONFIG_BLOCKS_DIR.toFile();
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
                JsonElement element = GSON.fromJson(reader, JsonElement.class);
                if (element.isJsonArray()) {
                    JsonArray array = element.getAsJsonArray();
                    for (JsonElement item : array) {
                        if (item.isJsonObject() && parseBlockEntry(item.getAsJsonObject(), file.getName() + " (array)")) {
                            count++;
                        }
                    }
                } else if (element.isJsonObject()) {
                    if (parseBlockEntry(element.getAsJsonObject(), file.getName())) {
                        count++;
                    }
                }
            } catch (Exception e) {
                AppleSeedConstants.LOG.error("Failed to load block config: {}", file.getName(), e);
            }
        }

        return count;
    }

    private boolean parseBlockEntry(JsonObject json, String source) {
        try {
            String sourceBlock = json.get("source_block").getAsString();
            ResourceLocation blockId = ResourceLocation.tryParse(sourceBlock);
            Block block = blockId != null ? BuiltInRegistries.BLOCK.get(blockId) : null;

            if (block == null || block == net.minecraft.world.level.block.Blocks.AIR) {
                AppleSeedConstants.LOG.warn("Skipping unknown food block '{}' from {}", sourceBlock, source);
                return false;
            }

            int bites = GsonHelper.getAsInt(json, "bites", 1);

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
            this.blockNutrition.put(block, nutritions);
            this.blockBites.put(block, bites);
            return true;
        } catch (Exception e) {
            AppleSeedConstants.LOG.error("Failed to parse block food entry from {}", source, e);
            return false;
        }
    }

    private boolean parseFoodEntry(JsonObject json, String source) {
        try {
            boolean autoCalculated = GsonHelper.getAsBoolean(json, "auto_calculated", false);
            String sourceItem = json.get("source_item").getAsString();
            ResourceLocation itemId = ResourceLocation.tryParse(sourceItem);
            Item item = itemId != null ? BuiltInRegistries.ITEM.get(itemId) : null;

            if (item == null || item == Items.AIR) {
                AppleSeedConstants.LOG.warn("Skipping unknown food item '{}' from {}", sourceItem, source);
                return false;
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
            return true;
        } catch (Exception e) {
            AppleSeedConstants.LOG.error("Failed to parse food entry from {}", source, e);
            return false;
        }
    }

    public float getNutritionValue(Item item, String group) {
        if (DietGroups.isGroupDisabled(group, this == CLIENT)) {
            return 0.0f;
        }
        Map<String, Float> nutritions = this.foodNutrition.get(item);
        if (nutritions == null && item instanceof BlockItem blockItem) {
            nutritions = this.blockNutrition.get(blockItem.getBlock());
        }
        if (nutritions != null) {
            return nutritions.getOrDefault(group, 0.0f);
        }
        return 0.0f;
    }

    public Map<String, Float> getNutritions(Item item) {
        Map<String, Float> result = new HashMap<>();
        Map<String, Float> all = this.foodNutrition.getOrDefault(item, new HashMap<>());
        if (all.isEmpty() && item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            all = this.blockNutrition.getOrDefault(block, new HashMap<>());
            AppleSeedConstants.LOG.debug("FoodNutritionManager: blockNutrition fallback for item={} block={} size={} data={}",
                    item, block, this.blockNutrition.size(), all);
        }
        for (Map.Entry<String, Float> entry : all.entrySet()) {
            if (!DietGroups.isGroupDisabled(entry.getKey(), this == CLIENT)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    public boolean hasNutritionData(Item item) {
        if (this.foodNutrition.containsKey(item)) {
            return true;
        }
        if (item instanceof BlockItem blockItem) {
            return this.blockNutrition.containsKey(blockItem.getBlock());
        }
        return false;
    }

    public void putNutritionData(Item item, String group, float value) {
        this.foodNutrition.computeIfAbsent(item, k -> new HashMap<>()).put(group, value);
    }

    public Map<Item, Map<String, Float>> getAllFoodData() {
        return this.foodNutrition;
    }

    // ---- Block food methods ----

    public float getBlockNutritionValue(Block block, String group) {
        if (DietGroups.isGroupDisabled(group, this == CLIENT)) {
            return 0.0f;
        }
        Map<String, Float> nutritions = this.blockNutrition.get(block);
        if (nutritions != null) {
            return nutritions.getOrDefault(group, 0.0f);
        }
        return 0.0f;
    }

    public Map<String, Float> getBlockNutritions(Block block) {
        Map<String, Float> result = new HashMap<>();
        Map<String, Float> all = this.blockNutrition.getOrDefault(block, new HashMap<>());
        for (Map.Entry<String, Float> entry : all.entrySet()) {
            if (!DietGroups.isGroupDisabled(entry.getKey(), this == CLIENT)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    public boolean hasBlockNutritionData(Block block) {
        return this.blockNutrition.containsKey(block);
    }

    public int getBlockBites(Block block) {
        return this.blockBites.getOrDefault(block, 1);
    }

    public void putBlockNutritionData(Block block, String group, float value) {
        this.blockNutrition.computeIfAbsent(block, k -> new HashMap<>()).put(group, value);
    }

    public void putBlockBites(Block block, int bites) {
        this.blockBites.put(block, bites);
    }

    public Map<Block, Map<String, Float>> getAllBlockData() {
        return this.blockNutrition;
    }

    public static Map<String, Float> getNutritionsForClient(Item item, boolean isClientSide) {
        if (isClientSide && ServerDietConfig.isConnectedToDedicatedServer()) {
            String foodId = BuiltInRegistries.ITEM.getKey(item).toString();
            Map<String, Float> serverData = ServerDietConfig.getServerFoodNutrition(foodId);
            if (!serverData.isEmpty()) {
                return serverData;
            }
            AppleSeedConstants.LOG.debug("FoodNutritionManager: ServerDietConfig has no data for {}, falling back to CLIENT", foodId);
            return CLIENT.getNutritions(item);
        }
        FoodNutritionManager instance = isClientSide ? CLIENT : INSTANCE;
        return instance.getNutritions(item);
    }

    @Override
    public void applyNutrition(ItemStack stack, int quality, Player player) {
        if (player.level().isClientSide()) {
            return;
        }
        Item item = stack.getItem();
        Map<String, Float> nutritions = getNutritions(item);
        if (nutritions.isEmpty()) {
            return;
        }
        float multiplier = 1.0f + 0.1f * quality;
        for (Map.Entry<String, Float> entry : nutritions.entrySet()) {
            float value = entry.getValue() * multiplier;
            if (value > 0) {
                DietData.addValue(player, entry.getKey(), value);
            }
        }
        DietData.syncToClient(player);
    }

    @Override
    public void applyNutrition(BlockState state, int quality, Player player) {
        if (player.level().isClientSide()) {
            return;
        }
        Block block = state.getBlock();
        Map<String, Float> blockNutritions = getBlockNutritions(block);
        if (blockNutritions.isEmpty()) {
            return;
        }
        int bites = getBlockBites(block);
        float multiplier = (1.0f + 0.1f * quality) / bites;
        for (Map.Entry<String, Float> entry : blockNutritions.entrySet()) {
            float value = entry.getValue() * multiplier;
            if (value > 0) {
                DietData.addValue(player, entry.getKey(), value);
            }
        }
        DietData.syncToClient(player);
    }
}
