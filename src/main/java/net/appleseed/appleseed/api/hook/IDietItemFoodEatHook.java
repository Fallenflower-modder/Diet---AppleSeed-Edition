package net.appleseed.appleseed.api.hook;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/**
 * Hook interface for intercepting and overriding item food eating detection logic.
 * <p>
 * Register implementations via {@link DietHookRegistry#registerItemFoodEatHook(IDietItemFoodEatHook)}.
 * When any registered hook's {@link #shouldIntercept} returns {@code true}, AppleSeed skips
 * its default nutrition lookup and uses {@link #modifyNutritionGains} exclusively.
 * <p>
 * Hooks are invoked in registration order. All hooks' {@link #modifyNutritionGains} are called
 * sequentially, with each hook receiving the result of the previous one.
 *
 * @see DietHookRegistry
 */
public interface IDietItemFoodEatHook {

    /**
     * Determines whether this hook should take over nutrition calculation for the given food item.
     * <p>
     * When any registered hook returns {@code true}, AppleSeed's default nutrition lookup is bypassed
     * and {@link #modifyNutritionGains} is used as the sole source of nutrition data.
     *
     * @param player the player who is eating
     * @param stack  the food ItemStack being consumed
     * @return {@code true} to intercept and use custom nutrition; {@code false} to use default logic
     */
    boolean shouldIntercept(Player player, ItemStack stack);

    /**
     * Provides or modifies the nutrition gain map.
     * <p>
     * Called regardless of whether {@link #shouldIntercept} returned {@code true}.
     * If {@code shouldIntercept} was {@code true}, {@code originalGains} will be empty.
     * The returned map's keys are nutrition group names and values are in the range {@code [0.0, 1.0]}.
     *
     * @param player        the player who is eating
     * @param stack         the food ItemStack being consumed
     * @param originalGains the default nutrition gain map (group name to value), or empty if intercepted
     * @return the modified nutrition gain map
     */
    Map<String, Float> modifyNutritionGains(Player player, ItemStack stack, Map<String, Float> originalGains);

    /**
     * Callback invoked after nutrition values have been applied to the player.
     * <p>
     * Use for side effects such as sending messages, triggering achievements, or playing sounds.
     * Default implementation does nothing.
     *
     * @param player the player who ate
     * @param stack  the food ItemStack that was consumed
     */
    default void onAfterEat(Player player, ItemStack stack) {
    }
}