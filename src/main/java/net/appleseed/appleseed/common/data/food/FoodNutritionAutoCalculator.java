package net.appleseed.appleseed.common.data.food;

import com.cloudworks.api.recipeparser.RecipeParserAPI;
import com.cloudworks.api.recipeparser.model.Ingredient;
import com.cloudworks.api.recipeparser.model.Product;
import com.cloudworks.api.recipeparser.model.QueryMode;
import com.cloudworks.api.recipeparser.model.RecipeData;
import com.cloudworks.api.recipeparser.model.RecipeParseResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.appleseed.appleseed.AppleSeedConstants;
import net.appleseed.appleseed.api.hook.DietHookRegistry;
import net.appleseed.appleseed.api.type.IDietGroup;
import net.appleseed.appleseed.common.config.DietConfig;
import net.appleseed.appleseed.common.data.group.DietGroups;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class FoodNutritionAutoCalculator {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final java.nio.file.Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("apple_seed_foods");
    private static final java.nio.file.Path CONFIG_ITEMS_DIR = CONFIG_DIR.resolve("items");
    private static final java.nio.file.Path CONFIG_BLOCKS_DIR = CONFIG_DIR.resolve("blocks");
    private static final java.nio.file.Path BANNED_RECIPES_FILE = FMLPaths.CONFIGDIR.get().resolve("appleseed_banned_recipe.json");

    // 物品营养缓存（按个数），最终会持久化保存
    private static final Map<Item, Map<String, Float>> calculatedNutrition = new ConcurrentHashMap<>();
    // 流体营养缓存（每 mB 的营养值），不持久化保存
    private static final Map<ResourceLocation, Map<String, Float>> fluidCalculatedNutrition = new ConcurrentHashMap<>();

    private static final Set<Item> blockFoodItems = new HashSet<>();
    private static final List<java.util.regex.Pattern> bannedRecipePatterns = new ArrayList<>();

    private static final Map<String, Float> EMPTY_NUTRITION = Collections.emptyMap();

    static {
        AppleSeedConstants.LOG.debug("[FoodNutritionAutoCalculator] Static initializer loaded");
    }

    // ========================================================================
    //  计算上下文与访问栈
    // ========================================================================

    /** 计算上下文，封装计算过程中不变的依赖 */
    private static class CalcContext {
        final Map<Item, List<RecipeParseResult>> itemRecipes;
        final Map<ResourceLocation, List<RecipeParseResult>> fluidRecipes;
        final Set<Item> alreadyProcessed;
        final MinecraftServer server;

        CalcContext(Map<Item, List<RecipeParseResult>> itemRecipes,
                    Map<ResourceLocation, List<RecipeParseResult>> fluidRecipes,
                    Set<Item> alreadyProcessed, MinecraftServer server) {
            this.itemRecipes = itemRecipes;
            this.fluidRecipes = fluidRecipes;
            this.alreadyProcessed = alreadyProcessed;
            this.server = server;
        }
    }

    /** 递归访问栈，用于循环检测。物品和流体各自独立追踪 */
    private static class VisitStack {
        final Set<Item> items = new HashSet<>();
        final Set<ResourceLocation> fluids = new HashSet<>();
    }

    // ========================================================================
    //  Public API — 异步配方收集 + 异步营养计算
    // ========================================================================
    //
    //  线程模型：
    //    [服务器线程] 准备 + 提交异步配方查询
    //        → [CloudWorks 工作线程] 配方解析（parseProduceRecipeAsync）
    //        → [服务器线程] 回调：存储结果，完成 Future
    //        → [ForkJoinPool] 营养值计算 + 保存结果
    //        → [服务器线程] 重载配置 + 通知玩家
    //
    // ========================================================================

    public static void ensureConfigDir() {
        CONFIG_ITEMS_DIR.toFile().mkdirs();
        CONFIG_BLOCKS_DIR.toFile().mkdirs();
    }

    public static void calculateAllAsync(MinecraftServer server) {
        calculateAllAsync(server, false);
    }

    /**
     * 启动异步配方收集与营养值计算流程。
     * <p>
     * 阶段 1（服务器线程）：清理状态、收集食物物品/流体 ID、提交异步配方查询
     * 阶段 2（CloudWorks 工作线程）：配方解析
     * 阶段 3（ForkJoinPool）：营养值递归计算 + 配置文件保存
     * 阶段 4（服务器线程）：重载配置文件、通知玩家
     */
    public static void calculateAllAsync(MinecraftServer server, boolean isReload) {
        boolean overwriteExisting = isReload;

        // --- 阶段 1：准备（服务器线程） ---
        ensureConfigDir();
        loadBannedRecipes();
        calculatedNutrition.clear();
        fluidCalculatedNutrition.clear();
        blockFoodItems.clear();

        Set<Item> foodItems = collectFoodItems();
        AppleSeedConstants.LOG.debug("[calculateAllAsync] Total food items (including blocks): {}", foodItems.size());

        // 收集所有需要查询配方的物品 ID
        List<Item> itemsToQuery = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item != Items.AIR) {
                itemsToQuery.add(item);
            }
        }

        // 收集所有需要查询配方的流体 ID
        List<ResourceLocation> fluidIdsToQuery = new ArrayList<>();
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            if (fluid == Fluids.EMPTY) {
                continue;
            }
            ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluid);
            if (fluidId != null && !fluidId.equals(BuiltInRegistries.FLUID.getDefaultKey())) {
                fluidIdsToQuery.add(fluidId);
            }
        }

        AppleSeedConstants.LOG.info("[calculateAllAsync] Submitting async recipe queries: {} items, {} fluids",
                itemsToQuery.size(), fluidIdsToQuery.size());

        // 配方结果映射（回调在服务器线程写入，计算阶段在 ForkJoinPool 读取）
        Map<Item, List<RecipeParseResult>> allItemRecipes = new ConcurrentHashMap<>();
        Map<ResourceLocation, List<RecipeParseResult>> allFluidRecipes = new ConcurrentHashMap<>();

        RecipeManager recipeManager = server.getRecipeManager();
        List<CompletableFuture<Void>> queryFutures = new ArrayList<>();

        // 为每个物品提交异步配方查询
        for (Item item : itemsToQuery) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            CompletableFuture<Void> future = new CompletableFuture<>();
            queryFutures.add(future);

            RecipeParserAPI.parseProduceRecipeAsync(
                itemId, QueryMode.ITEM, recipeManager,
                results -> {
                    List<RecipeParseResult> filtered = filterBannedRecipes(results);
                    if (!filtered.isEmpty()) {
                        allItemRecipes.put(item, filtered);
                    }
                    future.complete(null);
                },
                error -> {
                    AppleSeedConstants.LOG.debug("[calculateAllAsync] Failed to parse recipes for item {}: {}",
                            itemId, error);
                    future.complete(null);
                },
                server
            );
        }

        // 为每个流体提交异步配方查询
        for (ResourceLocation fluidId : fluidIdsToQuery) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            queryFutures.add(future);

            RecipeParserAPI.parseProduceRecipeAsync(
                fluidId, QueryMode.FLUID, recipeManager,
                results -> {
                    List<RecipeParseResult> filtered = filterBannedRecipes(results);
                    if (!filtered.isEmpty()) {
                        allFluidRecipes.put(fluidId, filtered);
                    }
                    future.complete(null);
                },
                error -> {
                    AppleSeedConstants.LOG.debug("[calculateAllAsync] Failed to parse recipes for fluid {}: {}",
                            fluidId, error);
                    future.complete(null);
                },
                server
            );
        }

        // --- 阶段 2→3→4：等待所有查询完成，然后计算营养值 ---
        CompletableFuture.allOf(queryFutures.toArray(new CompletableFuture[0]))
            .thenRunAsync(() -> {
                AppleSeedConstants.LOG.info("[calculateAllAsync] Async recipe collection complete: {} item recipes, {} fluid recipes",
                        allItemRecipes.size(), allFluidRecipes.size());

                calculateAllWithRecipes(server, overwriteExisting, isReload,
                        foodItems, allItemRecipes, allFluidRecipes);
            })
            .exceptionally(ex -> {
                AppleSeedConstants.LOG.error("[calculateAllAsync] Failed during nutrition calculation", ex);
                server.execute(() -> {
                    if (isReload) {
                        sendMessageToAll(server, Component.translatable("appleseed.calculation.failed"));
                    }
                });
                return null;
            });
    }

    // ========================================================================
    //  Nutrition Calculation Flow（在 ForkJoinPool 上运行）
    // ========================================================================

    private static void calculateAllWithRecipes(MinecraftServer server, boolean overwriteExisting,
                                                 boolean isReload, Set<Item> foodItems,
                                                 Map<Item, List<RecipeParseResult>> allItemRecipes,
                                                 Map<ResourceLocation, List<RecipeParseResult>> allFluidRecipes) {
        Map<Item, List<RecipeParseResult>> foodRecipes = filterFoodRecipes(allItemRecipes, overwriteExisting);

        AppleSeedConstants.LOG.debug("[calculateAllWithRecipes] Food items with valid recipes: {}, need calculation: {}{}",
                allItemRecipes.entrySet().stream().filter(e -> e.getKey().getFoodProperties(new ItemStack(e.getKey()), null) != null).count(),
                foodRecipes.size(),
                overwriteExisting ? " (overwrite mode)" : " (no built-in data)");

        calculateFoodNutrition(foodRecipes, allItemRecipes, allFluidRecipes, overwriteExisting, isReload, server);
        saveResults(overwriteExisting, foodRecipes);

        // 阶段 4：回到服务器线程重载配置和通知
        server.execute(() -> {
            FoodNutritionManager.INSTANCE.reloadConfigFiles();
            FoodNutritionManager.CLIENT.reloadConfigFiles();
            if (isReload) {
                sendMessageToAll(server, Component.translatable("appleseed.calculation.complete"));
            }
            AppleSeedConstants.LOG.info("Nutrition calculation complete! Config reloaded!");
        });
    }

    // ========================================================================
    //  Food Item Collection
    // ========================================================================

    private static Set<Item> collectFoodItems() {
        Set<Item> foodItems = new HashSet<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item != Items.AIR && item.getFoodProperties(new ItemStack(item), null) != null) {
                if (DietHookRegistry.shouldProcessItem(item)) {
                    foodItems.add(item);
                }
            }
        }
        AppleSeedConstants.LOG.debug("[calculateAll] Found {} items with FoodProperties", foodItems.size());

        TagKey<Block> foodBlocksTag = TagKey.create(
                BuiltInRegistries.BLOCK.key(),
                ResourceLocation.fromNamespaceAndPath(AppleSeedConstants.MOD_ID, "food_blocks")
        );

        int blockFoodCount = 0;
        for (Block block : BuiltInRegistries.BLOCK) {
            if (block.defaultBlockState().is(foodBlocksTag)) {
                Item blockItem = block.asItem();
                if (blockItem != Items.AIR && !foodItems.contains(blockItem)) {
                    foodItems.add(blockItem);
                    blockFoodItems.add(blockItem);
                    blockFoodCount++;
                }
            }
        }

        if (blockFoodCount > 0) {
            AppleSeedConstants.LOG.debug("[calculateAll] Added {} food block items to calculation set", blockFoodCount);
        }

        return foodItems;
    }

    private static Map<Item, List<RecipeParseResult>> filterFoodRecipes(
            Map<Item, List<RecipeParseResult>> allRecipes, boolean overwriteExisting) {
        Map<Item, List<RecipeParseResult>> foodRecipes = new HashMap<>();
        for (Map.Entry<Item, List<RecipeParseResult>> entry : allRecipes.entrySet()) {
            Item item = entry.getKey();
            boolean isBlockFood = blockFoodItems.contains(item);
            if (item.getFoodProperties(new ItemStack(item), null) == null && !isBlockFood) {
                continue;
            }
            boolean hasExistingData = FoodNutritionManager.INSTANCE.hasNutritionData(item);
            boolean needsCalc = !hasExistingData || overwriteExisting;
            if (needsCalc) {
                foodRecipes.put(item, entry.getValue());
            }
        }
        return foodRecipes;
    }

    // ========================================================================
    //  Batch Calculation Loop
    // ========================================================================

    private static void calculateFoodNutrition(Map<Item, List<RecipeParseResult>> foodRecipes,
                                               Map<Item, List<RecipeParseResult>> allItemRecipes,
                                               Map<ResourceLocation, List<RecipeParseResult>> allFluidRecipes,
                                               boolean overwriteExisting, boolean isReload,
                                               MinecraftServer server) {
        Set<Item> alreadyProcessed = new HashSet<>();
        CalcContext ctx = new CalcContext(allItemRecipes, allFluidRecipes, alreadyProcessed, server);
        AtomicInteger processed = new AtomicInteger(0);
        int total = foodRecipes.size();

        final long[] lastProgressTime = {System.currentTimeMillis()};

        for (Item food : foodRecipes.keySet()) {
            if (!overwriteExisting && FoodNutritionManager.INSTANCE.hasNutritionData(food)) {
                alreadyProcessed.add(food);
                continue;
            }
            if (calculatedNutrition.containsKey(food) || alreadyProcessed.contains(food)) {
                continue;
            }

            // 每个食物使用独立的 VisitStack，因为不同食物的计算路径相互独立
            calculateNutrition(food, ctx, new VisitStack(), 0);

            int current = processed.incrementAndGet();
            long now = System.currentTimeMillis();
            if ((now - lastProgressTime[0] >= 2000 && current > 0) || current == total) {
                lastProgressTime[0] = now;
                int finalCurrent = current;
                server.execute(() -> {
                    if (isReload) {
                        sendMessageToAll(server, Component.translatable("appleseed.calculation.progress",
                                finalCurrent, total, calculatedNutrition.size()));
                    }
                    AppleSeedConstants.LOG.info("Calculating food nutrition: {}/{} ({} succeeded)",
                            finalCurrent, total, calculatedNutrition.size());
                });
            }
        }
    }

    private static void saveResults(boolean overwriteExisting, Map<Item, List<RecipeParseResult>> foodRecipes) {
        int savedCount = 0;
        for (Map.Entry<Item, Map<String, Float>> entry : calculatedNutrition.entrySet()) {
            Item item = entry.getKey();
            boolean isBlockFood = blockFoodItems.contains(item);
            if (item.getFoodProperties(new ItemStack(item), null) == null && !isBlockFood) {
                continue;
            }
            if (saveToConfig(item, entry.getValue(), overwriteExisting, isBlockFood)) {
                savedCount++;
                if (entry.getValue().isEmpty()) {
                    AppleSeedConstants.LOG.debug("[calculateAll] Saved empty nutrition for: {}", BuiltInRegistries.ITEM.getKey(item));
                }
            }
        }

        int zeroDataCount = 0;
        for (Item food : foodRecipes.keySet()) {
            if (!FoodNutritionManager.INSTANCE.hasNutritionData(food) && !calculatedNutrition.containsKey(food)) {
                boolean isBlockFood = blockFoodItems.contains(food);
                if (saveToConfig(food, new HashMap<>(), overwriteExisting, isBlockFood)) {
                    zeroDataCount++;
                }
            }
        }

        for (Item blockFood : blockFoodItems) {
            if (!foodRecipes.containsKey(blockFood)
                    && !FoodNutritionManager.INSTANCE.hasNutritionData(blockFood)
                    && !calculatedNutrition.containsKey(blockFood)) {
                if (saveToConfig(blockFood, new HashMap<>(), overwriteExisting, true)) {
                    zeroDataCount++;
                }
            }
        }

        AppleSeedConstants.LOG.debug("[calculateAll] Summary: {} foods calculated, {} empty templates, {} total processed",
                savedCount, zeroDataCount, savedCount + zeroDataCount);
        if (savedCount == 0 && zeroDataCount == 0 && foodRecipes.size() > 0) {
            AppleSeedConstants.LOG.warn("[calculateAll] No foods were calculated! Check if recipes are available in RecipeManager.");
        }
    }

    // ========================================================================
    //  Core Nutrition Calculation — Item (Recursive with cycle handling)
    // ========================================================================

    /**
     * 计算物品的营养值（按个数）。
     * <p>
     * 循环处理策略：当检测到循环（当前物品已在 visitStack 中）时返回空，
     * 调用方会自动尝试该物品的其他配方。只有所有配方都失败时才真正返回空。
     */
    private static Map<String, Float> calculateNutrition(Item item, CalcContext ctx, VisitStack visitStack, int nonFoodDepth) {
        ctx.alreadyProcessed.add(item);

        // 优先检查预定义营养数据
        Map<String, Float> fromData = getExistingNutritionData(item);
        if (fromData != null) {
            return fromData;
        }

        // 检查缓存
        if (calculatedNutrition.containsKey(item)) {
            return calculatedNutrition.get(item);
        }

        // 循环检测：如果当前物品已在访问路径上，返回空让调用方尝试其他配方
        if (visitStack.items.contains(item)) {
            AppleSeedConstants.LOG.debug("[calculateNutrition] Cycle detected for item {}, skipping this recipe",
                    BuiltInRegistries.ITEM.getKey(item));
            return EMPTY_NUTRITION;
        }

        visitStack.items.add(item);
        try {
            List<RecipeParseResult> recipes = ctx.itemRecipes.get(item);

            if (recipes != null && !recipes.isEmpty()) {
                AppleSeedConstants.LOG.debug("[calculateNutrition] Item '{}' has {} recipes, depth={}",
                        BuiltInRegistries.ITEM.getKey(item), recipes.size(), nonFoodDepth);

                // 依次尝试每个配方，直到找到一个成功的
                for (RecipeParseResult result : recipes) {
                    Map<String, Float> nutrition = calculateFromRecipeData(
                            result.getData(), item, ctx, visitStack, nonFoodDepth);
                    if (!nutrition.isEmpty()) {
                        calculatedNutrition.put(item, nutrition);
                        return nutrition;
                    }
                }
                // 所有配方都失败（可能都是循环或缺少营养源）
                AppleSeedConstants.LOG.debug("[calculateNutrition] All {} recipes failed for item '{}'",
                        recipes.size(), BuiltInRegistries.ITEM.getKey(item));
            } else {
                AppleSeedConstants.LOG.debug("[calculateNutrition] Item '{}' has NO recipes", BuiltInRegistries.ITEM.getKey(item));
            }
        } finally {
            visitStack.items.remove(item);
        }

        return EMPTY_NUTRITION;
    }

    /**
     * 从 RecipeData 计算物品配方的营养值。
     * 累加所有输入的营养贡献，然后除以目标物品的产出数量。
     */
    private static Map<String, Float> calculateFromRecipeData(RecipeData recipeData, Item targetItem,
                                                               CalcContext ctx, VisitStack visitStack, int nonFoodDepth) {
        Map<String, Float> sum = new HashMap<>();

        for (Ingredient ingredient : recipeData.getInputs()) {
            resolveIngredient(ingredient, sum, ctx, visitStack, nonFoodDepth);
        }

        if (sum.isEmpty()) {
            return EMPTY_NUTRITION;
        }

        double totalOutputCount = getTotalItemOutputCount(recipeData, targetItem);
        if (totalOutputCount <= 0) {
            totalOutputCount = 1.0;
        }

        Map<String, Float> finalNutrition = new HashMap<>();
        for (Map.Entry<String, Float> e : sum.entrySet()) {
            finalNutrition.put(e.getKey(), e.getValue() / (float) totalOutputCount);
        }

        AppleSeedConstants.LOG.debug("[calculateFromRecipeData] Recipe for '{}' returned {} nutrients (outputCount={})",
                BuiltInRegistries.ITEM.getKey(targetItem), finalNutrition.size(), totalOutputCount);
        return finalNutrition;
    }

    // ========================================================================
    //  Core Nutrition Calculation — Fluid (Recursive, per mB, not persisted)
    // ========================================================================

    /**
     * 计算流体的营养值（每 mB 的营养值，不持久化保存）。
     * <p>
     * 营养来源优先级：
     * <ol>
     *   <li>缓存中已有</li>
     *   <li>从对应桶物品推导：桶物品营养 / 1000（1 桶 = 1000 mB）</li>
     *   <li>从产出该流体的配方推导：总输入营养 / 流体产出量(mB)</li>
     * </ol>
     * 循环处理策略与物品相同：循环时返回空，调用方尝试其他配方。
     */
    private static Map<String, Float> calculateFluidNutrition(ResourceLocation fluidId, CalcContext ctx,
                                                               VisitStack visitStack, int nonFoodDepth) {
        // 检查缓存
        if (fluidCalculatedNutrition.containsKey(fluidId)) {
            return fluidCalculatedNutrition.get(fluidId);
        }

        // 循环检测
        if (visitStack.fluids.contains(fluidId)) {
            AppleSeedConstants.LOG.debug("[calculateFluidNutrition] Cycle detected for fluid {}, skipping this recipe", fluidId);
            return EMPTY_NUTRITION;
        }

        visitStack.fluids.add(fluidId);
        try {
            // 方法1：从桶物品推导（桶物品营养 / 1000）
            Item bucketItem = FluidRecipeHelper.findBucketItem(fluidId);
            if (bucketItem != Items.AIR) {
                Map<String, Float> bucketNutrition = calculateNutrition(bucketItem, ctx, visitStack, nonFoodDepth);
                if (!bucketNutrition.isEmpty()) {
                    Map<String, Float> fluidNutrition = new HashMap<>();
                    for (Map.Entry<String, Float> e : bucketNutrition.entrySet()) {
                        fluidNutrition.put(e.getKey(), e.getValue() / 1000.0f);
                    }
                    fluidCalculatedNutrition.put(fluidId, fluidNutrition);
                    AppleSeedConstants.LOG.debug("[calculateFluidNutrition] Fluid '{}' nutrition from bucket '{}': {} per mB",
                            fluidId, BuiltInRegistries.ITEM.getKey(bucketItem), fluidNutrition);
                    return fluidNutrition;
                }
            }

            // 方法2：从产出该流体的配方推导
            List<RecipeParseResult> fluidRecipes = ctx.fluidRecipes.get(fluidId);
            if (fluidRecipes != null && !fluidRecipes.isEmpty()) {
                for (RecipeParseResult result : fluidRecipes) {
                    Map<String, Float> nutrition = calculateFluidFromRecipeData(
                            result.getData(), fluidId, ctx, visitStack, nonFoodDepth);
                    if (!nutrition.isEmpty()) {
                        fluidCalculatedNutrition.put(fluidId, nutrition);
                        AppleSeedConstants.LOG.debug("[calculateFluidNutrition] Fluid '{}' nutrition from recipe: {} per mB",
                                fluidId, nutrition);
                        return nutrition;
                    }
                }
            }
        } finally {
            visitStack.fluids.remove(fluidId);
        }

        return EMPTY_NUTRITION;
    }

    /**
     * 从 RecipeData 计算流体配方的营养值（每 mB）。
     * 累加所有输入的营养贡献，然后除以目标流体的产出量(mB)。
     */
    private static Map<String, Float> calculateFluidFromRecipeData(RecipeData recipeData, ResourceLocation targetFluidId,
                                                                    CalcContext ctx, VisitStack visitStack, int nonFoodDepth) {
        Map<String, Float> sum = new HashMap<>();

        for (Ingredient ingredient : recipeData.getInputs()) {
            resolveIngredient(ingredient, sum, ctx, visitStack, nonFoodDepth);
        }

        if (sum.isEmpty()) {
            return EMPTY_NUTRITION;
        }

        double totalFluidOutput = getTotalFluidOutputCount(recipeData, targetFluidId);
        if (totalFluidOutput <= 0) {
            totalFluidOutput = 1.0;
        }

        Map<String, Float> finalNutrition = new HashMap<>();
        for (Map.Entry<String, Float> e : sum.entrySet()) {
            finalNutrition.put(e.getKey(), e.getValue() / (float) totalFluidOutput);
        }

        return finalNutrition;
    }

    // ========================================================================
    //  Ingredient Resolution
    // ========================================================================

    /**
     * 解析配方输入成分的营养贡献。
     * 根据 unit 类型分别处理物品和流体。
     */
    private static void resolveIngredient(Ingredient ingredient, Map<String, Float> sum,
                                          CalcContext ctx, VisitStack visitStack, int nonFoodDepth) {
        double count = ingredient.getCount();
        if (count <= 0) {
            return;
        }

        String unit = ingredient.getUnit();
        String id = ingredient.getId();

        if ("item".equals(unit)) {
            resolveItemIngredient(id, count, sum, ctx, visitStack, nonFoodDepth);
        } else if ("fluid".equals(unit)) {
            resolveFluidIngredient(id, count, sum, ctx, visitStack, nonFoodDepth);
        }
    }

    /**
     * 解析物品输入的营养贡献 = 物品营养（按个数）× 数量。
     */
    private static void resolveItemIngredient(String id, double count, Map<String, Float> sum,
                                               CalcContext ctx, VisitStack visitStack, int nonFoodDepth) {
        ResourceLocation itemId = ResourceLocation.tryParse(id);
        if (itemId == null) {
            return;
        }

        Item inputItem = BuiltInRegistries.ITEM.get(itemId);
        if (inputItem == null || inputItem == Items.AIR) {
            AppleSeedConstants.LOG.debug("[resolveItemIngredient] Input item '{}' not found", id);
            return;
        }

        AppleSeedConstants.LOG.debug("[resolveItemIngredient] Processing input item: {} (count={})", id, count);

        // 优先使用预定义营养数据
        if (FoodNutritionManager.INSTANCE.hasNutritionData(inputItem)) {
            Map<String, Float> inputNutrition = FoodNutritionManager.INSTANCE.getNutritions(inputItem);
            boolean contributed = false;
            for (Map.Entry<String, Float> e : inputNutrition.entrySet()) {
                if (!isNegativeGroup(e.getKey())) {
                    sum.merge(e.getKey(), e.getValue() * (float) count, Float::sum);
                    contributed = true;
                }
            }
            if (contributed) {
                return;
            }
        }

        // 递归计算
        boolean itemIsFood = inputItem.getFoodProperties(new ItemStack(inputItem), null) != null;
        int nextDepth = itemIsFood ? 0 : nonFoodDepth + 1;
        int maxChainDepth = DietConfig.INSTANCE.craftChainSearchDepth.get();

        if (nextDepth > maxChainDepth) {
            AppleSeedConstants.LOG.debug("[resolveItemIngredient] Skipping input '{}' depth {} > max {}",
                    id, nextDepth, maxChainDepth);
            return;
        }

        Map<String, Float> ingredientNutrition = calculateNutrition(inputItem, ctx, visitStack, nextDepth);
        for (Map.Entry<String, Float> e : ingredientNutrition.entrySet()) {
            if (!isNegativeGroup(e.getKey())) {
                sum.merge(e.getKey(), e.getValue() * (float) count, Float::sum);
            }
        }
    }

    /**
     * 解析流体输入的营养贡献 = 流体营养（每 mB）× 用量(mB)。
     * 流体营养不持久化，通过 {@link #calculateFluidNutrition} 递归计算。
     */
    private static void resolveFluidIngredient(String id, double count, Map<String, Float> sum,
                                                CalcContext ctx, VisitStack visitStack, int nonFoodDepth) {
        ResourceLocation fluidId = ResourceLocation.tryParse(id);
        if (fluidId == null) {
            return;
        }

        int maxChainDepth = DietConfig.INSTANCE.craftChainSearchDepth.get();
        int fluidDepth = nonFoodDepth + 1;
        if (fluidDepth > maxChainDepth) {
            return;
        }

        AppleSeedConstants.LOG.debug("[resolveFluidIngredient] Processing input fluid: {} (count={} mB)", id, count);

        // 获取流体每 mB 的营养值
        Map<String, Float> fluidNutrition = calculateFluidNutrition(fluidId, ctx, visitStack, fluidDepth);

        // 按用量(mB)贡献营养
        for (Map.Entry<String, Float> e : fluidNutrition.entrySet()) {
            if (!isNegativeGroup(e.getKey())) {
                sum.merge(e.getKey(), e.getValue() * (float) count, Float::sum);
            }
        }
    }

    // ========================================================================
    //  Output Count Helpers
    // ========================================================================

    private static double getTotalItemOutputCount(RecipeData recipeData, Item targetItem) {
        ResourceLocation targetId = BuiltInRegistries.ITEM.getKey(targetItem);
        double totalCount = 0.0;

        for (Product product : recipeData.getOutputs()) {
            if ("item".equals(product.getUnit()) && targetId.toString().equals(product.getId())) {
                totalCount += product.getCount();
            }
        }

        return totalCount;
    }

    private static double getTotalFluidOutputCount(RecipeData recipeData, ResourceLocation targetFluidId) {
        String targetId = targetFluidId.toString();
        double totalCount = 0.0;

        for (Product product : recipeData.getOutputs()) {
            if ("fluid".equals(product.getUnit()) && targetId.equals(product.getId())) {
                totalCount += product.getCount();
            }
        }

        return totalCount;
    }

    // ========================================================================
    //  Existing Data Lookup
    // ========================================================================

    private static Map<String, Float> getExistingNutritionData(Item item) {
        if (!FoodNutritionManager.INSTANCE.hasNutritionData(item)) {
            return null;
        }
        Map<String, Float> stored = FoodNutritionManager.INSTANCE.getNutritions(item);
        Map<String, Float> filtered = new HashMap<>();
        for (Map.Entry<String, Float> e : stored.entrySet()) {
            if (!isNegativeGroup(e.getKey())) {
                filtered.put(e.getKey(), e.getValue());
            }
        }
        return filtered.isEmpty() ? null : filtered;
    }

    // ========================================================================
    //  Banned Recipe Management
    // ========================================================================

    private static List<RecipeParseResult> filterBannedRecipes(List<RecipeParseResult> results) {
        List<RecipeParseResult> filtered = new ArrayList<>();
        for (RecipeParseResult result : results) {
            if (!isBannedRecipe(result.getRecipeId())) {
                filtered.add(result);
            } else {
                AppleSeedConstants.LOG.debug("[filterBannedRecipes] Skipping banned recipe: {}", result.getRecipeId());
            }
        }
        return filtered;
    }

    private static void loadBannedRecipes() {
        bannedRecipePatterns.clear();
        File file = BANNED_RECIPES_FILE.toFile();
        if (!file.exists()) {
            AppleSeedConstants.LOG.debug("[loadBannedRecipes] No banned recipe config found at {}", BANNED_RECIPES_FILE);
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            if (json.has("banned_recipes")) {
                var arr = json.getAsJsonArray("banned_recipes");
                for (var elem : arr) {
                    String pattern = elem.getAsString();
                    String[] parts = pattern.split("\\*", -1);
                    StringBuilder regexBuilder = new StringBuilder();
                    for (int i = 0; i < parts.length; i++) {
                        if (i > 0) {
                            regexBuilder.append(".*");
                        }
                        regexBuilder.append(java.util.regex.Pattern.quote(parts[i]));
                    }
                    bannedRecipePatterns.add(java.util.regex.Pattern.compile(regexBuilder.toString()));
                }
            }
            AppleSeedConstants.LOG.info("[loadBannedRecipes] Loaded {} banned recipe patterns: {}",
                    bannedRecipePatterns.size(), bannedRecipePatterns.stream()
                            .map(java.util.regex.Pattern::pattern)
                            .map(s -> s.replace("\\Q", "").replace("\\E", ""))
                            .toList());
        } catch (Exception e) {
            AppleSeedConstants.LOG.error("[loadBannedRecipes] Failed to load banned recipes: {}", e.getMessage());
        }
    }

    private static boolean isBannedRecipe(ResourceLocation recipeId) {
        String idString = recipeId.toString();
        for (java.util.regex.Pattern pattern : bannedRecipePatterns) {
            if (pattern.matcher(idString).matches()) {
                return true;
            }
        }
        return false;
    }

    // ========================================================================
    //  Config Persistence (items only, fluids are not persisted)
    // ========================================================================

    private static boolean saveToConfig(Item item, Map<String, Float> nutritions, boolean overwriteExisting, boolean isBlockFood) {
        try {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            String fileName = itemId.getNamespace() + "_" + itemId.getPath() + ".json";

            java.nio.file.Path targetDir = isBlockFood ? CONFIG_BLOCKS_DIR : CONFIG_ITEMS_DIR;
            File file = targetDir.resolve(fileName).toFile();

            if (!overwriteExisting && file.exists()) {
                return false;
            }

            JsonObject json = new JsonObject();

            if (isBlockFood) {
                Block block = item instanceof BlockItem blockItem ? blockItem.getBlock() : null;
                ResourceLocation blockId = block != null ? BuiltInRegistries.BLOCK.getKey(block) : itemId;
                json.addProperty("source_block", blockId.toString());
                json.addProperty("bites", 1);
                json.addProperty("comment", "Edit this file to add custom nutrition values");
            } else {
                json.addProperty("source_item", itemId.toString());
                json.addProperty("comment", "Edit this file to add custom nutrition values");
            }
            json.addProperty("auto_calculated", nutritions.isEmpty());

            JsonObject nutritionsJson = new JsonObject();
            for (Map.Entry<String, Float> e : nutritions.entrySet()) {
                if (e.getValue() > 0.0001f) {
                    nutritionsJson.addProperty(e.getKey(), Math.round(e.getValue() * 10000) / 10000.0f);
                }
            }
            if (nutritions.isEmpty() || nutritionsJson.size() == 0) {
                for (IDietGroup group : DietGroups.SERVER.getGroups()) {
                    nutritionsJson.addProperty(group.getName(), 0.0f);
                }
            }

            json.add("nutritions", nutritionsJson);

            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(json, writer);
            }
            return true;
        } catch (Exception e) {
            AppleSeedConstants.LOG.error("Failed to save nutrition config for {}", BuiltInRegistries.ITEM.getKey(item), e);
            return false;
        }
    }

    // ========================================================================
    //  Utility Methods
    // ========================================================================

    private static boolean isNegativeGroup(String groupName) {
        boolean dataFileValue = DietGroups.SERVER.getGroup(groupName)
                .map(IDietGroup::isNegative)
                .orElse(false);
        return net.appleseed.appleseed.common.config.DietConfig.isGroupNegative(groupName, dataFileValue);
    }

    private static void sendMessageToAll(MinecraftServer server, Component message) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(message);
        }
    }
}