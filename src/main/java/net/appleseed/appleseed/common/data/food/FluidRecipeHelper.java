package net.appleseed.appleseed.common.data.food;

import net.appleseed.appleseed.AppleSeedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluid;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Central utility for extracting fluid ingredients from recipes of any mod.
 * <p>
 * Maintains a registry of {@link IFluidRecipeHandler} implementations.
 * Built-in handlers cover common mods (Create, Farmer's Delight).
 * External mods can register additional handlers via {@link #registerHandler(IFluidRecipeHandler)}.
 * Falls back to reflection-based generic handling for unknown recipe types.
 * <p>
 * <b>Extending for new mods:</b>
 * <pre>{@code
 * FluidRecipeHelper.registerHandler(recipe -> {
 *     if (recipe instanceof MyModRecipe myRecipe) {
 *         return myRecipe.getFluidInputs().stream()
 *             .map(fi -> new FluidIngredientSnapshot(fi.getFluid(), fi.getAmount()))
 *             .toList();
 *     }
 *     return List.of();
 * });
 * }</pre>
 */
public final class FluidRecipeHelper {

    private static final List<IFluidRecipeHandler> handlers = new ArrayList<>();

    private FluidRecipeHelper() {}

    /**
     * Registers a custom fluid recipe handler.
     * Handlers are tried in registration order; the first non-empty result wins.
     *
     * @param handler the handler to register
     */
    public static void registerHandler(IFluidRecipeHandler handler) {
        handlers.add(handler);
    }

    /**
     * Extracts fluid ingredients from a recipe using registered handlers.
     * Falls back to reflection-based generic handling if no handler matches.
     *
     * @param recipe the recipe to extract fluid ingredients from
     * @return list of fluid ingredient snapshots, never null
     */
    public static List<FluidIngredientSnapshot> extractFluidIngredients(Recipe<?> recipe) {
        // Try registered handlers first (first match wins)
        for (IFluidRecipeHandler handler : handlers) {
            List<FluidIngredientSnapshot> result = handler.extractFluidIngredients(recipe);
            if (!result.isEmpty()) {
                return result;
            }
        }

        // Fall back to generic reflection-based handling
        return extractViaReflection(recipe);
    }

    // ---- Built-in handlers (registered in static initializer) ----

    static {
        // Create mod: ProcessingRecipe subclasses have getFluidIngredients()
        registerHandler(recipe -> {
            String className = recipe.getClass().getName();
            if (!className.startsWith("com.simibubi.create.")) {
                return List.of();
            }
            return extractViaMethod(recipe, "getFluidIngredients");
        });

        // Farmer's Delight: CookingPotRecipe has getFluidIngredients() or getFluidInputs()
        registerHandler(recipe -> {
            String className = recipe.getClass().getName();
            if (!className.startsWith("vectorwing.farmersdelight.")) {
                return List.of();
            }
            List<FluidIngredientSnapshot> result = extractViaMethod(recipe, "getFluidIngredients");
            if (!result.isEmpty()) return result;
            return extractViaMethod(recipe, "getFluidInputs");
        });
    }

    // ---- Reflection-based fallback for unknown recipe types ----

    private static List<FluidIngredientSnapshot> extractViaReflection(Recipe<?> recipe) {
        // Skip vanilla Minecraft and standard library recipes — they don't have fluid ingredients
        String className = recipe.getClass().getName();
        if (className.startsWith("net.minecraft.") || className.startsWith("java.") || className.startsWith("com.mojang.")) {
            return List.of();
        }

        // Try common method names used by mods for fluid ingredient access
        for (String methodName : new String[]{"getFluidIngredients", "getFluidInputs"}) {
            List<FluidIngredientSnapshot> result = extractViaMethod(recipe, methodName);
            if (!result.isEmpty()) {
                return result;
            }
        }
        return List.of();
    }

    private static List<FluidIngredientSnapshot> extractViaMethod(Recipe<?> recipe, String methodName) {
        try {
            Method method = recipe.getClass().getMethod(methodName);
            Object result = method.invoke(recipe);
            if (!(result instanceof List<?> list)) {
                return List.of();
            }

            List<FluidIngredientSnapshot> ingredients = new ArrayList<>();
            for (Object element : list) {
                FluidIngredientSnapshot snapshot = extractFromIngredientObject(element);
                if (snapshot != null) {
                    ingredients.add(snapshot);
                }
            }
            return ingredients;
        } catch (NoSuchMethodException e) {
            return List.of();
        } catch (Exception e) {
            AppleSeedConstants.LOG.debug("[FluidRecipeHelper] Failed to invoke {} on {}: {}",
                    methodName, recipe.getClass().getSimpleName(), e.getMessage());
            return List.of();
        }
    }

    /**
     * Attempts to extract a FluidIngredientSnapshot from a single ingredient object
     * by calling {@code getFluid()} and {@code getAmount()} via reflection.
     */
    private static FluidIngredientSnapshot extractFromIngredientObject(Object ingredient) {
        try {
            Method getFluid = ingredient.getClass().getMethod("getFluid");
            Method getAmount = ingredient.getClass().getMethod("getAmount");

            Object fluid = getFluid.invoke(ingredient);
            if (fluid == null) return null;

            long amount = ((Number) getAmount.invoke(ingredient)).longValue();

            if (fluid instanceof Fluid mcFluid) {
                return new FluidIngredientSnapshot(mcFluid, amount);
            }
        } catch (Exception e) {
            // Silently skip — this ingredient object doesn't follow the expected pattern
        }
        return null;
    }

    // ---- Fluid-to-item resolution ----

    /**
     * Resolves the bucket item for a given fluid.
     */
    public static Item getBucketItem(Fluid fluid) {
        if (fluid == null) return Items.AIR;
        try {
            return fluid.getBucket();
        } catch (Exception e) {
            return Items.AIR;
        }
    }

    /**
     * Attempts to resolve a bucket item from a fluid ResourceLocation.
     */
    public static Item findBucketItem(ResourceLocation fluidId) {
        try {
            Fluid fluid = BuiltInRegistries.FLUID.get(fluidId);
            if (fluid != null) {
                return fluid.getBucket();
            }
        } catch (Exception e) {
            AppleSeedConstants.LOG.warn("[FluidRecipeHelper] Could not resolve fluid bucket for {}", fluidId, e);
        }
        return Items.AIR;
    }
}