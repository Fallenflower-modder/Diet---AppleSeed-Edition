package net.appleseed.appleseed.common.data.food;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.appleseed.appleseed.AppleSeedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FoodNutritionAutoCalculator {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final java.nio.file.Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("apple_seed_foods");
    private static final Map<Item, Map<String, Float>> calculatedNutrition = new ConcurrentHashMap<>();
    private static final Set<Item> cycleDetected = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void ensureConfigDir() {
        File dir = CONFIG_DIR.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

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

        Set<Item> foodItems = new HashSet<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item != Items.AIR && item.getFoodProperties(new ItemStack(item), null) != null) {
                foodItems.add(item);
            }
        }

        AppleSeedConstants.LOG.info("Found {} food items, scanning recipes...", foodItems.size());

        RecipeManager recipeManager = server.getRecipeManager();
        Collection<RecipeHolder<?>> allRecipesCollection = recipeManager.getRecipes();

        Map<Item, List<RecipeHolder<?>>> allRecipes = new HashMap<>();
        int skippedRecipes = 0;
        for (RecipeHolder<?> holder : allRecipesCollection) {
            Recipe<?> recipe = holder.value();

            if (recipe instanceof SmithingTrimRecipe) {
                continue;
            }

            if (!isValidRecipeType(recipe)) {
                skippedRecipes++;
                continue;
            }

            Item resultItem;
            try {
                resultItem = recipe.getResultItem(server.registryAccess()).getItem();
            } catch (Exception e) {
                continue;
            }

            if (resultItem == Items.AIR) {
                continue;
            }

            if (foodItems.contains(resultItem)) {
                allRecipes.computeIfAbsent(resultItem, k -> new ArrayList<>()).add(holder);
            }
        }

        AppleSeedConstants.LOG.info("Skipped {} unsupported recipe types", skippedRecipes);

        Map<Item, List<RecipeHolder<?>>> foodRecipes = new HashMap<>();
        for (Map.Entry<Item, List<RecipeHolder<?>>> entry : allRecipes.entrySet()) {
            if (!FoodNutritionManager.INSTANCE.hasNutritionData(entry.getKey()) || overwriteExisting) {
                foodRecipes.put(entry.getKey(), entry.getValue());
            }
        }

        AppleSeedConstants.LOG.info("Found {} food items with valid recipes, {} need calculation{}",
                allRecipes.size(), foodRecipes.size(),
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
            if (saveToConfig(entry.getKey(), entry.getValue(), overwriteExisting)) {
                savedCount++;
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

        AppleSeedConstants.LOG.info("Auto-calculated and saved nutrition for {} foods to config/apple_seed_foods/", savedCount);
        if (zeroDataCount > 0) {
            AppleSeedConstants.LOG.info("Generated {} empty nutrition templates for foods without calculable recipes", zeroDataCount);
        }
    }

    private static Map<String, Float> calculateNutrition(Item item, Map<Item, List<RecipeHolder<?>>> allRecipes, Set<Item> visitStack, Set<Item> alreadyProcessed, MinecraftServer server) {
        alreadyProcessed.add(item);

        if (FoodNutritionManager.INSTANCE.hasNutritionData(item)) {
            return FoodNutritionManager.INSTANCE.getNutritions(item);
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
            if (recipes == null || recipes.isEmpty()) {
                return new HashMap<>();
            }

            for (RecipeHolder<?> holder : recipes) {
                Recipe<?> recipe = holder.value();
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
                        for (Map.Entry<String, Float> e : ingredientNutrition.entrySet()) {
                            sum.merge(e.getKey(), e.getValue(), Float::sum);
                        }
                        continue;
                    }

                    FoodProperties food = bestItem.getFoodProperties(new ItemStack(bestItem), null);
                    if (food == null) {
                        continue;
                    }

                    Map<String, Float> ingredientNutrition = calculateNutrition(bestItem, allRecipes, visitStack, alreadyProcessed, server);
                    for (Map.Entry<String, Float> e : ingredientNutrition.entrySet()) {
                        sum.merge(e.getKey(), e.getValue(), Float::sum);
                    }
                }

                if (!sum.isEmpty()) {
                    Map<String, Float> finalNutrition = new HashMap<>();
                    for (Map.Entry<String, Float> e : sum.entrySet()) {
                        finalNutrition.put(e.getKey(), e.getValue() / count);
                    }
                    calculatedNutrition.put(item, finalNutrition);
                    return finalNutrition;
                }
            }
        } finally {
            visitStack.remove(item);
        }

        return new HashMap<>();
    }

    private static boolean isValidRecipeType(Recipe<?> recipe) {
        return recipe instanceof CraftingRecipe
                || recipe instanceof SmeltingRecipe
                || recipe instanceof SmokingRecipe
                || recipe instanceof CampfireCookingRecipe
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
                nutritionsJson.addProperty("grains", 0.0f);
                nutritionsJson.addProperty("fruits", 0.0f);
                nutritionsJson.addProperty("vegetables", 0.0f);
                nutritionsJson.addProperty("proteins", 0.0f);
                nutritionsJson.addProperty("sugars", 0.0f);
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
