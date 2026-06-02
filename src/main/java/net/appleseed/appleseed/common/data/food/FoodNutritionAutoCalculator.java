package net.appleseed.appleseed.common.data.food;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.appleseed.appleseed.AppleSeedConstants;
import net.appleseed.appleseed.api.type.IDietGroup;
import net.appleseed.appleseed.common.config.DietConfig;
import net.appleseed.appleseed.common.data.group.DietGroups;
import net.appleseed.appleseed.common.data.recipe.SimulateRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FoodNutritionAutoCalculator {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final java.nio.file.Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("apple_seed_foods");
    private static final Map<Item, Map<String, Float>> calculatedNutrition = new ConcurrentHashMap<>();
    private static final Set<Item> cycleDetected = Collections.newSetFromMap(new ConcurrentHashMap<>());

    static {
        AppleSeedConstants.LOG.debug("[FoodNutritionAutoCalculator] Static initializer loaded");
    }

    public static void ensureConfigDir() {
        File dir = CONFIG_DIR.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private static void loadConfigSimulateRecipes(Map<Item, List<RecipeHolder<?>>> allRecipes) {
        java.nio.file.Path simConfigDir = FMLPaths.CONFIGDIR.get().resolve("apple_seed_simulate");
        File configDir = simConfigDir.toFile();
        if (!configDir.exists() || !configDir.isDirectory()) {
            AppleSeedConstants.LOG.debug("[loadConfigSimulateRecipes] Config directory does not exist: {}", simConfigDir);
            return;
        }

        File[] files = configDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            AppleSeedConstants.LOG.debug("[loadConfigSimulateRecipes] No JSON files found in {}", simConfigDir);
            return;
        }

        AppleSeedConstants.LOG.debug("[loadConfigSimulateRecipes] Found {} JSON files in {}", files.length, simConfigDir);
        int configCount = 0;
        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                JsonElement element = GSON.fromJson(reader, JsonElement.class);
                if (element.isJsonObject()) {
                    parseSimulateRecipeJson(element.getAsJsonObject(), file.getName(), allRecipes);
                    configCount++;
                } else if (element.isJsonArray()) {
                    JsonArray array = element.getAsJsonArray();
                    for (JsonElement item : array) {
                        if (item.isJsonObject()) {
                            parseSimulateRecipeJson(item.getAsJsonObject(), file.getName() + " (array)", allRecipes);
                            configCount++;
                        }
                    }
                }
            } catch (Exception e) {
                AppleSeedConstants.LOG.error("Failed to load simulated recipe config: {}", file.getName(), e);
            }
        }
        if (configCount > 0) {
            AppleSeedConstants.LOG.debug("[loadConfigSimulateRecipes] Loaded {} simulated recipes from config into allRecipes", configCount);
        }
    }

    private static void parseSimulateRecipeJson(JsonObject json, String source, Map<Item, List<RecipeHolder<?>>> allRecipes) {
        try {
            if (!json.has("inputs") || !json.has("outputs")) {
                AppleSeedConstants.LOG.warn("Skipping simulated recipe with missing inputs/outputs from {}", source);
                return;
            }

            List<SimulateRecipe.SimulateIngredient> inputs = new ArrayList<>();
            JsonArray inputsArray = json.getAsJsonArray("inputs");
            for (JsonElement elem : inputsArray) {
                if (elem.isJsonObject()) {
                    JsonObject ingJson = elem.getAsJsonObject();
                    String type = ingJson.has("type") ? ingJson.get("type").getAsString() : "item";
                    ResourceLocation id = ResourceLocation.tryParse(ingJson.get("id").getAsString());
                    int count = ingJson.has("count") ? ingJson.get("count").getAsInt() : 1;
                    if (id != null) {
                        inputs.add(new SimulateRecipe.SimulateIngredient(type, id, count));
                    }
                }
            }

            List<SimulateRecipe.SimulateIngredient> outputs = new ArrayList<>();
            JsonArray outputsArray = json.getAsJsonArray("outputs");
            for (JsonElement elem : outputsArray) {
                if (elem.isJsonObject()) {
                    JsonObject ingJson = elem.getAsJsonObject();
                    String type = ingJson.has("type") ? ingJson.get("type").getAsString() : "item";
                    ResourceLocation id = ResourceLocation.tryParse(ingJson.get("id").getAsString());
                    int count = ingJson.has("count") ? ingJson.get("count").getAsInt() : 1;
                    if (id != null) {
                        outputs.add(new SimulateRecipe.SimulateIngredient(type, id, count));
                    }
                }
            }

            AppleSeedConstants.LOG.debug("[parseSimulateRecipeJson] Parsing recipe from '{}': inputs={} outputs={}",
                    source, inputsArray.size(), outputsArray.size());

            if (inputs.isEmpty() || outputs.isEmpty()) {
                AppleSeedConstants.LOG.warn("Skipping simulated recipe with empty inputs/outputs from {}", source);
                return;
            }

            SimulateRecipe recipe = new SimulateRecipe(inputs, outputs);
            ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(AppleSeedConstants.MOD_ID,
                    "config_simulate/" + System.currentTimeMillis() + "_" + configCountStatic);
            RecipeHolder<SimulateRecipe> holder = new RecipeHolder<>(recipeId, recipe);

            for (SimulateRecipe.SimulateIngredient output : outputs) {
                if (output.isItem()) {
                    Item item = BuiltInRegistries.ITEM.get(output.id());
                    if (item != null && item != Items.AIR) {
                        allRecipes.computeIfAbsent(item, k -> new ArrayList<>()).add(holder);
                        AppleSeedConstants.LOG.debug("[parseSimulateRecipeJson] Config sim recipe -> output item: {} (count={})",
                                output.id(), output.count());
                    }
                }
            }
        } catch (Exception e) {
            AppleSeedConstants.LOG.error("Failed to parse simulated recipe from {}", source, e);
        }
    }

    private static int configCountStatic = 0;

    public static void calculateAllAsync(MinecraftServer server) {
        calculateAllAsync(server, false);
    }

    public static void calculateAllAsync(MinecraftServer server, boolean isReload) {
        boolean overwriteExisting = isReload;

        CompletableFuture.runAsync(() -> {
            calculateAll(server, overwriteExisting, isReload);
        }).whenComplete((v, ex) -> {
            if (ex != null) {
                AppleSeedConstants.LOG.error("Failed to calculate food nutrition", ex);
            }
            server.execute(() -> {
                FoodNutritionManager.INSTANCE.reloadConfigFiles();
                FoodNutritionManager.CLIENT.reloadConfigFiles();
                if (isReload) {
                    sendMessageToAll(server, Component.translatable("appleseed.calculation.complete"));
                }
                AppleSeedConstants.LOG.info("Nutrition calculation complete! Config reloaded!");
            });
        });
    }

    private static void sendMessageToAll(MinecraftServer server, Component message) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(message);
        }
    }

    private static void calculateAll(MinecraftServer server, boolean overwriteExisting, boolean isReload) {
        ensureConfigDir();
        calculatedNutrition.clear();
        cycleDetected.clear();
        configCountStatic = 0;

        RecipeManager recipeManager = server.getRecipeManager();
        Collection<RecipeHolder<?>> allRecipesCollection = recipeManager.getRecipes();
        AppleSeedConstants.LOG.debug("[calculateAll] isReload={} overwriteExisting={} totalRecipesInManager={}",
                isReload, overwriteExisting, allRecipesCollection.size());

        Set<Item> foodItems = new HashSet<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item != Items.AIR && item.getFoodProperties(new ItemStack(item), null) != null) {
                foodItems.add(item);
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
                    blockFoodCount++;
                }
            }
        }

        if (blockFoodCount > 0) {
            AppleSeedConstants.LOG.debug("[calculateAll] Added {} food block items to calculation set", blockFoodCount);
        }

        AppleSeedConstants.LOG.debug("[calculateAll] Total food items (including blocks): {}", foodItems.size());

        Map<Item, List<RecipeHolder<?>>> allRecipes = new HashMap<>();
        int skippedRecipes = 0;
        int simRecipeCount = 0;
        int simOutputItemCount = 0;
        for (RecipeHolder<?> holder : allRecipesCollection) {
            Recipe<?> recipe = holder.value();

            if (recipe instanceof SmithingTrimRecipe) {
                continue;
            }

            if (!isValidRecipeType(recipe)) {
                skippedRecipes++;
                continue;
            }

            if (recipe instanceof SimulateRecipe simRecipe) {
                simRecipeCount++;
                AppleSeedConstants.LOG.debug("[calculateAll] Native SimulateRecipe id={} inputs={} outputs={}",
                        holder.id(), simRecipe.getInputs().size(), simRecipe.getOutputs().size());
                for (SimulateRecipe.SimulateIngredient output : simRecipe.getOutputs()) {
                    if (output.isItem()) {
                        Item item = BuiltInRegistries.ITEM.get(output.id());
                        if (item != null && item != Items.AIR) {
                            allRecipes.computeIfAbsent(item, k -> new ArrayList<>()).add(holder);
                            simOutputItemCount++;
                            AppleSeedConstants.LOG.debug("[calculateAll]   -> output item: {} (count={})",
                                    output.id(), output.count());
                        }
                    }
                }
                continue;
            }

            Item resultItem;
            try {
                resultItem = recipe.getResultItem(server.registryAccess()).getItem();
            } catch (Exception e) {
                AppleSeedConstants.LOG.debug("[calculateAll] Failed to get result item for recipe {}: {}",
                        holder.id(), e.getMessage());
                continue;
            }

            if (resultItem == Items.AIR) {
                continue;
            }

            allRecipes.computeIfAbsent(resultItem, k -> new ArrayList<>()).add(holder);
        }

        AppleSeedConstants.LOG.debug("[calculateAll] Collected {} items with valid recipes ({} SimulateRecipes with {} output items, skipped {} unsupported types)",
                allRecipes.size(), simRecipeCount, simOutputItemCount, skippedRecipes);

        int beforeConfigCount = allRecipes.size();
        loadConfigSimulateRecipes(allRecipes);
        int configAdded = allRecipes.size() - beforeConfigCount;
        if (configAdded > 0) {
            AppleSeedConstants.LOG.debug("[calculateAll] Config-based recipes added {} new items to allRecipes", configAdded);
        }

        Map<Item, List<RecipeHolder<?>>> foodRecipes = new HashMap<>();
        for (Map.Entry<Item, List<RecipeHolder<?>>> entry : allRecipes.entrySet()) {
            Item item = entry.getKey();
            if (item.getFoodProperties(new ItemStack(item), null) == null) {
                continue;
            }
            boolean hasExistingData = FoodNutritionManager.INSTANCE.hasNutritionData(item);
            boolean needsCalc = !hasExistingData || overwriteExisting;
            if (needsCalc) {
                foodRecipes.put(item, entry.getValue());
            }
        }

        AppleSeedConstants.LOG.debug("[calculateAll] Food items with valid recipes: {}, need calculation: {}{}",
                allRecipes.entrySet().stream().filter(e -> e.getKey().getFoodProperties(new ItemStack(e.getKey()), null) != null).count(),
                foodRecipes.size(),
                overwriteExisting ? " (overwrite mode)" : " (no built-in data)");

        Set<Item> alreadyProcessed = new HashSet<>();
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

            calculateNutrition(food, allRecipes, new HashSet<>(), alreadyProcessed, server);

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

        int savedCount = 0;
        for (Map.Entry<Item, Map<String, Float>> entry : calculatedNutrition.entrySet()) {
            Item item = entry.getKey();
            if (item.getFoodProperties(new ItemStack(item), null) == null) {
                continue;
            }
            if (saveToConfig(item, entry.getValue(), overwriteExisting)) {
                savedCount++;
                if (entry.getValue().isEmpty()) {
                    AppleSeedConstants.LOG.debug("[calculateAll] Saved empty nutrition for: {}", BuiltInRegistries.ITEM.getKey(item));
                }
            }
        }

        int zeroDataCount = 0;
        for (Item food : foodRecipes.keySet()) {
            if (!FoodNutritionManager.INSTANCE.hasNutritionData(food) && !calculatedNutrition.containsKey(food)) {
                if (saveToConfig(food, new HashMap<>(), overwriteExisting)) {
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

    private static Map<String, Float> calculateNutrition(Item item, Map<Item, List<RecipeHolder<?>>> allRecipes, Set<Item> visitStack, Set<Item> alreadyProcessed, MinecraftServer server) {
        return calculateNutrition(item, allRecipes, visitStack, alreadyProcessed, 0, server);
    }

    private static Map<String, Float> calculateNutrition(Item item, Map<Item, List<RecipeHolder<?>>> allRecipes, Set<Item> visitStack, Set<Item> alreadyProcessed, int nonFoodDepth, MinecraftServer server) {
        alreadyProcessed.add(item);

        if (FoodNutritionManager.INSTANCE.hasNutritionData(item)) {
            Map<String, Float> stored = FoodNutritionManager.INSTANCE.getNutritions(item);
            Map<String, Float> filtered = new HashMap<>();
            for (Map.Entry<String, Float> e : stored.entrySet()) {
                if (!isNegativeGroup(e.getKey())) {
                    filtered.put(e.getKey(), e.getValue());
                }
            }
            if (!filtered.isEmpty()) {
                return filtered;
            }
        }

        if (calculatedNutrition.containsKey(item)) {
            return calculatedNutrition.get(item);
        }

        if (cycleDetected.contains(item)) {
            return new HashMap<>();
        }

        if (visitStack.contains(item)) {
            cycleDetected.add(item);
            AppleSeedConstants.LOG.warn("Cycle detected in recipe chain for {}", BuiltInRegistries.ITEM.getKey(item));
            return new HashMap<>();
        }

        visitStack.add(item);

        try {
            List<RecipeHolder<?>> recipes = allRecipes.get(item);

            if (recipes != null && !recipes.isEmpty()) {
                AppleSeedConstants.LOG.debug("[calculateNutrition] Item '{}' has {} recipes, depth={}",
                        BuiltInRegistries.ITEM.getKey(item), recipes.size(), nonFoodDepth);

                for (RecipeHolder<?> holder : recipes) {
                    Recipe<?> recipe = holder.value();

                    if (recipe instanceof SimulateRecipe simRecipe) {
                        Map<String, Float> simSum = processSimulatedRecipe(simRecipe, item, allRecipes, visitStack, alreadyProcessed, nonFoodDepth, server);
                        if (simSum != null && !simSum.isEmpty()) {
                            int totalOutputCount = getSimOutputCount(simRecipe, item);
                            if (totalOutputCount == 0) totalOutputCount = 1;

                            Map<String, Float> finalSimNutrition = new HashMap<>();
                            for (Map.Entry<String, Float> e : simSum.entrySet()) {
                                finalSimNutrition.put(e.getKey(), e.getValue() / totalOutputCount);
                            }
                            calculatedNutrition.put(item, finalSimNutrition);
                            AppleSeedConstants.LOG.debug("[calculateNutrition] SimulateRecipe for '{}' returned {} nutrients (outputCount={})",
                                    BuiltInRegistries.ITEM.getKey(item), finalSimNutrition.size(), totalOutputCount);
                            return finalSimNutrition;
                        }
                        continue;
                    }

                    int count = recipe.getResultItem(server.registryAccess()).getCount();

                    if (!isValidRecipeType(recipe)) {
                        continue;
                    }

                    Map<String, Float> sum = new HashMap<>();

                    for (Ingredient ingredient : recipe.getIngredients()) {
                        ItemStack[] matchingItems = ingredient.getItems();
                        if (matchingItems.length == 0) {
                            continue;
                        }

                        Item bestItem = null;
                        for (ItemStack match : matchingItems) {
                            if (match.getItem() != Items.AIR) {
                                bestItem = match.getItem();
                                break;
                            }
                        }

                        if (bestItem == null) {
                            continue;
                        }

                        if (FoodNutritionManager.INSTANCE.hasNutritionData(bestItem)) {
                            Map<String, Float> ingredientNutrition = FoodNutritionManager.INSTANCE.getNutritions(bestItem);
                            boolean contributed = false;
                            for (Map.Entry<String, Float> e : ingredientNutrition.entrySet()) {
                                if (!isNegativeGroup(e.getKey())) {
                                    sum.merge(e.getKey(), e.getValue(), Float::sum);
                                    contributed = true;
                                }
                            }
                            if (contributed) {
                                continue;
                            }
                        }

                        int maxChainDepth = DietConfig.INSTANCE.craftChainSearchDepth.get();
                        boolean bestItemIsFood = bestItem.getFoodProperties(new ItemStack(bestItem), null) != null;
                        int nextDepth = bestItemIsFood ? 0 : nonFoodDepth + 1;
                        if (nextDepth > maxChainDepth) {
                            AppleSeedConstants.LOG.debug("[calculateNutrition] Skipping ingredient '{}' depth {} > max {}",
                                    BuiltInRegistries.ITEM.getKey(bestItem), nextDepth, maxChainDepth);
                            continue;
                        }

                        Map<String, Float> ingredientNutrition = calculateNutrition(bestItem, allRecipes, visitStack, alreadyProcessed, nextDepth, server);
                        for (Map.Entry<String, Float> e : ingredientNutrition.entrySet()) {
                            if (!isNegativeGroup(e.getKey())) {
                                sum.merge(e.getKey(), e.getValue(), Float::sum);
                            }
                        }
                    }

                    addFluidIngredientNutrition(recipe, sum, allRecipes, visitStack, alreadyProcessed, nonFoodDepth, server);

                    if (!sum.isEmpty()) {
                        Map<String, Float> finalNutrition = new HashMap<>();
                        for (Map.Entry<String, Float> e : sum.entrySet()) {
                            finalNutrition.put(e.getKey(), e.getValue() / count);
                        }
                        calculatedNutrition.put(item, finalNutrition);
                        return finalNutrition;
                    }
                }
            } else {
                AppleSeedConstants.LOG.debug("[calculateNutrition] Item '{}' has NO recipes in allRecipes", BuiltInRegistries.ITEM.getKey(item));
            }
        } finally {
            visitStack.remove(item);
        }

        return new HashMap<>();
    }

    private static void addFluidIngredientNutrition(Recipe<?> recipe, Map<String, Float> sum,
                                                    Map<Item, List<RecipeHolder<?>>> allRecipes,
                                                    Set<Item> visitStack, Set<Item> alreadyProcessed,
                                                    int nonFoodDepth, MinecraftServer server) {
        int maxDepth = DietConfig.INSTANCE.craftChainSearchDepth.get();
        int fluidDepth = nonFoodDepth + 1;
        if (fluidDepth > maxDepth) {
            return;
        }

        List<?> fluidIngredients = getFluidIngredients(recipe);
        if (fluidIngredients == null || fluidIngredients.isEmpty()) {
            return;
        }

        for (Object ingredient : fluidIngredients) {
            try {
                Method getFluidMethod = findMethod(ingredient.getClass(), "getFluid");
                Method getAmountMethod = findMethod(ingredient.getClass(), "getAmount");
                if (getFluidMethod == null || getAmountMethod == null) {
                    continue;
                }

                Object fluid = getFluidMethod.invoke(ingredient);
                if (fluid == null) {
                    continue;
                }

                Number amountNum = (Number) getAmountMethod.invoke(ingredient);
                long fluidAmount = amountNum.longValue();

                Method getBucketMethod = findMethod(fluid.getClass(), "getBucket");
                if (getBucketMethod == null) {
                    continue;
                }

                Item bucketItem = (Item) getBucketMethod.invoke(fluid);
                if (bucketItem == null || bucketItem == Items.AIR) {
                    continue;
                }

                Map<String, Float> bucketNutrition = calculateNutrition(bucketItem, allRecipes, visitStack, alreadyProcessed, fluidDepth, server);
                double ratio = (double) fluidAmount / 1000.0;

                for (Map.Entry<String, Float> e : bucketNutrition.entrySet()) {
                    if (!isNegativeGroup(e.getKey())) {
                        sum.merge(e.getKey(), (float) (e.getValue() * ratio), Float::sum);
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    private static Map<String, Float> processSimulatedRecipe(SimulateRecipe simRecipe, Item targetItem,
                                                             Map<Item, List<RecipeHolder<?>>> allRecipes,
                                                             Set<Item> visitStack, Set<Item> alreadyProcessed,
                                                             int nonFoodDepth, MinecraftServer server) {
        Map<String, Float> sum = new HashMap<>();
        int maxChainDepth = DietConfig.INSTANCE.craftChainSearchDepth.get();

        AppleSeedConstants.LOG.debug("[processSimulatedRecipe] Processing SimulateRecipe for target '{}' with {} inputs, depth={}",
                BuiltInRegistries.ITEM.getKey(targetItem), simRecipe.getInputs().size(), nonFoodDepth);

        for (SimulateRecipe.SimulateIngredient input : simRecipe.getInputs()) {
            if (input.isItem()) {
                Item inputItem = BuiltInRegistries.ITEM.get(input.id());
                if (inputItem == null || inputItem == Items.AIR) {
                    AppleSeedConstants.LOG.debug("[processSimulatedRecipe]   input item '{}' not found", input.id());
                    continue;
                }

                AppleSeedConstants.LOG.debug("[processSimulatedRecipe]   processing input item: {} (count={})",
                        input.id(), input.count());

                if (FoodNutritionManager.INSTANCE.hasNutritionData(inputItem)) {
                    Map<String, Float> inputNutrition = FoodNutritionManager.INSTANCE.getNutritions(inputItem);
                    boolean contributed = false;
                    for (Map.Entry<String, Float> e : inputNutrition.entrySet()) {
                        if (!isNegativeGroup(e.getKey())) {
                            sum.merge(e.getKey(), e.getValue() * input.count(), Float::sum);
                            contributed = true;
                        }
                    }
                    if (contributed) {
                        continue;
                    }
                }

                boolean itemIsFood = inputItem.getFoodProperties(new ItemStack(inputItem), null) != null;
                int nextDepth = itemIsFood ? 0 : nonFoodDepth + 1;
                if (nextDepth > maxChainDepth) {
                    AppleSeedConstants.LOG.debug("[processSimulatedRecipe]   skipping input '{}' depth {} > max {}",
                            input.id(), nextDepth, maxChainDepth);
                    continue;
                }

                Map<String, Float> ingredientNutrition = calculateNutrition(
                        inputItem, allRecipes, visitStack, alreadyProcessed, nextDepth, server);
                for (Map.Entry<String, Float> e : ingredientNutrition.entrySet()) {
                    if (!isNegativeGroup(e.getKey())) {
                        sum.merge(e.getKey(), e.getValue() * input.count(), Float::sum);
                    }
                }
            } else if (input.isFluid()) {
                int fluidDepth = nonFoodDepth + 1;
                if (fluidDepth > maxChainDepth) {
                    continue;
                }

                ResourceLocation fluidId = input.id();
                Item bucketItem = findBucketForFluid(fluidId);
                if (bucketItem == null || bucketItem == Items.AIR) {
                    continue;
                }

                Map<String, Float> bucketNutrition = calculateNutrition(
                        bucketItem, allRecipes, visitStack, alreadyProcessed, fluidDepth, server);
                double ratio = (double) input.count() / 1000.0;

                for (Map.Entry<String, Float> e : bucketNutrition.entrySet()) {
                    if (!isNegativeGroup(e.getKey())) {
                        sum.merge(e.getKey(), (float) (e.getValue() * ratio), Float::sum);
                    }
                }
            }
        }

        return sum;
    }

    private static int getSimOutputCount(SimulateRecipe simRecipe, Item targetItem) {
        ResourceLocation targetId = BuiltInRegistries.ITEM.getKey(targetItem);
        for (SimulateRecipe.SimulateIngredient output : simRecipe.getOutputs()) {
            if (output.isItem() && output.id().equals(targetId)) {
                return output.count();
            }
        }
        return 1;
    }

    private static Item findBucketForFluid(ResourceLocation fluidId) {
        try {
            var fluid = BuiltInRegistries.FLUID.get(fluidId);
            if (fluid != null) {
                return fluid.getBucket();
            }
        } catch (Exception e) {
            AppleSeedConstants.LOG.warn("Could not resolve fluid bucket for {}", fluidId, e);
        }
        return null;
    }

    private static List<?> getFluidIngredients(Recipe<?> recipe) {
        String[] methodNames = {"getFluidIngredients", "getFluidInputs"};
        for (String name : methodNames) {
            try {
                Method method = recipe.getClass().getMethod(name);
                Object result = method.invoke(recipe);
                if (result instanceof List<?>) {
                    return (List<?>) result;
                }
            } catch (NoSuchMethodException ignored) {
            } catch (Exception e) {
                break;
            }
        }
        return null;
    }

    private static Method findMethod(Class<?> clazz, String methodName) {
        try {
            return clazz.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static boolean isNegativeGroup(String groupName) {
        boolean dataFileValue = DietGroups.SERVER.getGroup(groupName)
                .map(IDietGroup::isNegative)
                .orElse(false);
        return net.appleseed.appleseed.common.config.DietConfig.isGroupNegative(groupName, dataFileValue);
    }

    private static boolean isValidRecipeType(Recipe<?> recipe) {
        return recipe instanceof CraftingRecipe
                || recipe instanceof SmeltingRecipe
                || recipe instanceof SmokingRecipe
                || recipe instanceof CampfireCookingRecipe
                || recipe instanceof SimulateRecipe
                || recipe.getClass().getName().toLowerCase().contains("create")
                || recipe.getClass().getName().toLowerCase().contains("farmersdelight")
                || recipe.getClass().getName().toLowerCase().contains("farmer_delight");
    }

    private static boolean saveToConfig(Item item, Map<String, Float> nutritions, boolean overwriteExisting) {
        try {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            String fileName = itemId.getNamespace() + "_" + itemId.getPath() + ".json";
            File file = CONFIG_DIR.resolve(fileName).toFile();

            if (!overwriteExisting && file.exists()) {
                return false;
            }

            JsonObject json = new JsonObject();
            json.addProperty("source_item", itemId.toString());
            json.addProperty("auto_calculated", nutritions.isEmpty());
            json.addProperty("comment", "Edit this file to add custom nutrition values");

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
}