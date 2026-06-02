package net.appleseed.appleseed.api.hook;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Hook interface for filtering which recipes and items participate in the automatic nutrition
 * calculation system.
 * <p>
 * Register implementations via {@link DietHookRegistry#registerRecipeFilterHook(IDietRecipeFilterHook)}.
 * For a recipe or item to be included, <strong>all</strong> registered hooks must return {@code true}
 * from their respective methods (AND logic).
 * <p>
 * This is evaluated <em>in addition to</em> AppleSeed's built-in type checks (CraftingRecipe,
 * SmeltingRecipe, SmokerRecipe, CampfireCookingRecipe, SimulateRecipe, and Create/Farmer's Delight recipes).
 *
 * @see DietHookRegistry
 */
public interface IDietRecipeFilterHook {

    /**
     * Filters which recipe types are considered during automatic nutrition calculation.
     * <p>
     * All hooks must return {@code true} (AND logic) for a recipe to be included.
     * This check runs after AppleSeed's built-in type checks.
     *
     * @param recipe the recipe to evaluate
     * @return {@code true} to include this recipe type; {@code false} to exclude it
     */
    boolean isValidRecipeType(Recipe<?> recipe);

    /**
     * Filters which food items are included in the automatic calculation set.
     * <p>
     * All hooks must return {@code true} (AND logic) for an item to be processed.
     * This is called during the initial item collection phase.
     *
     * @param item the item to evaluate
     * @return {@code true} to include this item; {@code false} to exclude it
     */
    boolean shouldProcessItem(Item item);
}