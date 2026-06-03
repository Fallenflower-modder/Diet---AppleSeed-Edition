package net.appleseed.appleseed.api.query;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/**
 * Query interface for retrieving nutrition data for items and blocks.
 * <p>
 * Implemented by {@code FoodNutritionManager} and accessible via
 * {@link DietQuery#getInstance()}. Mods should use this interface to query
 * nutrition data without depending on internal implementation classes.
 * <p>
 * <b>Item methods:</b> When the item is a {@code BlockItem}, query methods
 * automatically fall back to block nutrition data if no direct item data exists.
 *
 * @see DietQuery
 */
public interface IDietFoodQuery {

    /**
     * Returns all nutrition data for the specified item.
     * <p>
     * If the item is a {@code BlockItem} and has no direct item data,
     * falls back to block nutrition data.
     *
     * @param item the item to query
     * @return a map of nutrition group names to values (0.0 ~ 1.0), never null
     */
    Map<String, Float> getNutritions(Item item);

    /**
     * Returns the nutrition value for a specific group.
     *
     * @param item  the item to query
     * @param group the nutrition group name
     * @return the nutrition value, or {@code 0.0f} if no data exists
     */
    float getNutritionValue(Item item, String group);

    /**
     * Checks whether the specified item has registered nutrition data.
     * <p>
     * For {@code BlockItem} instances, also checks block nutrition data.
     *
     * @param item the item to check
     * @return {@code true} if nutrition data exists for this item
     */
    boolean hasNutritionData(Item item);

    /**
     * Returns all registered food nutrition data.
     * <p>
     * <b>Warning:</b> The returned map is a direct reference to the internal data structure.
     * Modifying it during iteration may cause {@code ConcurrentModificationException}.
     *
     * @return a map of all items to their nutrition data
     */
    Map<Item, Map<String, Float>> getAllFoodData();

    /**
     * Returns all nutrition data for the specified block food.
     * <p>
     * Values represent the <em>total</em> nutrition for the entire block (not per bite).
     * Divide by {@link #getBlockBites(Block)} to get per-bite values.
     *
     * @param block the block to query
     * @return a map of nutrition group names to values (0.0 ~ 1.0), never null
     */
    Map<String, Float> getBlockNutritions(Block block);

    /**
     * Returns the block food nutrition value for a specific group.
     * <p>
     * Value is the total nutrition for the entire block (not per bite).
     *
     * @param block the block to query
     * @param group the nutrition group name
     * @return the nutrition value, or {@code 0.0f} if no data exists
     */
    float getBlockNutritionValue(Block block, String group);

    /**
     * Checks whether the specified block has registered nutrition data.
     *
     * @param block the block to check
     * @return {@code true} if nutrition data exists for this block
     */
    boolean hasBlockNutritionData(Block block);

    /**
     * Returns the number of bites for the specified block food.
     * <p>
     * Each bite provides {@code totalNutrition / bites} nutrition.
     * Cake typically has 7 bites.
     *
     * @param block the block to query
     * @return the number of bites, or {@code 1} if no data exists
     */
    int getBlockBites(Block block);

    /**
     * Returns all registered block food nutrition data.
     * <p>
     * <b>Warning:</b> The returned map is a direct reference to the internal data structure.
     *
     * @return a map of all blocks to their nutrition data
     */
    Map<Block, Map<String, Float>> getAllBlockData();

    /**
     * Looks up nutrition data for the given item stack, applies the quality multiplier,
     * and adds the resulting values to the specified player.
     * <p>
     * The final value for each nutrition group is calculated as:
     * <pre>{@code value * (1.0 + 0.1 * quality)}</pre>
     * A quality of {@code 0} applies the raw nutrition values unchanged.
     * <p>
     * If the item is a {@code BlockItem}, falls back to block nutrition data (per-bite).
     * This method respects Hook mechanisms — {@link net.appleseed.appleseed.api.hook.DietHookRegistry}
     * callbacks are invoked during value application.
     *
     * @param stack   the item stack to look up nutrition data from
     * @param quality the quality level (0 = normal, higher = bonus multiplier)
     * @param player  the player to apply nutrition to
     */
    void applyNutrition(ItemStack stack, int quality, Player player);

    /**
     * Looks up nutrition data for the given block state, applies the quality multiplier,
     * and adds the resulting values (per-bite) to the specified player.
     * <p>
     * The total block nutrition is divided by the number of bites, then the quality
     * multiplier is applied:
     * <pre>{@code (totalBlockValue / bites) * (1.0 + 0.1 * quality)}</pre>
     * A quality of {@code 0} applies the raw per-bite nutrition values unchanged.
     * <p>
     * This method respects Hook mechanisms — {@link net.appleseed.appleseed.api.hook.DietHookRegistry}
     * callbacks are invoked during value application.
     *
     * @param state   the block state to look up nutrition data from
     * @param quality the quality level (0 = normal, higher = bonus multiplier)
     * @param player  the player to apply nutrition to
     */
    void applyNutrition(BlockState state, int quality, Player player);
}