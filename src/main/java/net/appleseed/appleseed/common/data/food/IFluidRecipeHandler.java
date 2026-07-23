package net.appleseed.appleseed.common.data.food;

import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

/**
 * Interface for extracting fluid ingredients from recipes.
 * <p>
 * Implementations handle specific recipe types (e.g., Create, Farmer's Delight).
 * Register via {@link FluidRecipeHelper#registerHandler(IFluidRecipeHandler)}.
 * <p>
 * <b>Usage:</b> Return an empty list if the handler does not recognize the recipe type.
 * The registry will try the next handler or fall back to reflection.
 */
@FunctionalInterface
public interface IFluidRecipeHandler {
    /**
     * Attempts to extract fluid ingredients from a recipe.
     *
     * @param recipe the recipe to inspect
     * @return list of fluid ingredient snapshots, or empty list if this handler does not apply
     */
    List<FluidIngredientSnapshot> extractFluidIngredients(Recipe<?> recipe);
}