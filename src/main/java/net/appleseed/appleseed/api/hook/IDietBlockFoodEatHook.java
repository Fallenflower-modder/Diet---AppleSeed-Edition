package net.appleseed.appleseed.api.hook;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

import java.util.Map;

/**
 * Hook interface for intercepting and overriding block food (e.g. cake) eating detection logic.
 * <p>
 * Register implementations via {@link DietHookRegistry#registerBlockFoodEatHook(IDietBlockFoodEatHook)}.
 * For block food processing to proceed, <strong>all</strong> registered hooks must return {@code true}
 * from {@link #shouldProcessBlock} (AND logic).
 * <p>
 * The original gains passed to {@link #modifyNutritionGains} are already divided by the number of bites.
 *
 * @see DietHookRegistry
 */
public interface IDietBlockFoodEatHook {

    /**
     * Determines whether the block food interaction should be processed.
     * <p>
     * Called before nutrition lookup. All registered hooks must return {@code true} (AND logic)
     * for processing to continue.
     *
     * @param player the player interacting with the block
     * @param block  the block being interacted with
     * @param pos    the position of the block
     * @return {@code true} to allow processing; {@code false} to cancel
     */
    boolean shouldProcessBlock(Player player, Block block, BlockPos pos);

    /**
     * Modifies the per-bite nutrition gains for block food.
     * <p>
     * The original gains are already divided by the number of bites (e.g. cake has 7 bites).
     * Each hook receives the result of the previous hook in registration order.
     *
     * @param player        the player eating the block food
     * @param block         the block being eaten
     * @param originalGains the per-bite nutrition gain map (group name to value)
     * @return the modified per-bite nutrition gain map
     */
    Map<String, Float> modifyNutritionGains(Player player, Block block, Map<String, Float> originalGains);

    /**
     * Callback invoked after block food nutrition has been applied to the player.
     * <p>
     * Default implementation does nothing.
     *
     * @param player the player who ate
     * @param block  the block food that was eaten from
     */
    default void onAfterEat(Player player, Block block) {
    }
}