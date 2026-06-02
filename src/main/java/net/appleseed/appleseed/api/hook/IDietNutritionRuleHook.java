package net.appleseed.appleseed.api.hook;

import net.minecraft.world.entity.player.Player;

/**
 * Hook interface for overriding nutrition value add/set/decay rules at the finest granularity.
 * <p>
 * Register implementations via {@link DietHookRegistry#registerNutritionRuleHook(IDietNutritionRuleHook)}.
 * These hooks are called <em>before</em> values are actually written to player data.
 * Returned values replace the original values. Final values are always clamped to {@code [0.0, 1.0]}.
 * <p>
 * Hooks are invoked in registration order, with each hook receiving the result of the previous one.
 *
 * @see DietHookRegistry
 */
public interface IDietNutritionRuleHook {

    /**
     * Called before a nutrition value is added (accumulated).
     * <p>
     * Triggered for both positive gains (eating) and negative gains (hunger/attack decay).
     * Return the value to use instead of the original.
     *
     * @param player the player whose nutrition is changing
     * @param group  the nutrition group name
     * @param value  the amount to add (positive for gain, negative for decay)
     * @return the modified value to add
     */
    float onBeforeAdd(Player player, String group, float value);

    /**
     * Called before a nutrition value is set directly (overwritten).
     * <p>
     * The final value will be clamped to {@code [0.0, 1.0]} after this hook returns.
     *
     * @param player the player whose nutrition is being set
     * @param group  the nutrition group name
     * @param value  the value to set
     * @return the modified value to set
     */
    float onBeforeSet(Player player, String group, float value);

    /**
     * Called before decay is applied (hunger loss or damage taken).
     * <p>
     * Return {@code 0} to completely prevent decay for this tick.
     * The returned value is further multiplied by the group's decay multiplier.
     *
     * @param player the player whose nutrition is decaying
     * @param group  the nutrition group name
     * @param decay  the base decay amount (always positive)
     * @return the modified decay amount
     */
    float onBeforeDecay(Player player, String group, float decay);

    /**
     * Callback invoked after a nutrition value has changed.
     * <p>
     * Only called when the value actually changed ({@code oldValue != newValue}).
     * Default implementation does nothing.
     *
     * @param player   the player whose nutrition changed
     * @param group    the nutrition group name
     * @param oldValue the value before the change
     * @param newValue the value after the change (already clamped to {@code [0.0, 1.0]})
     */
    default void onAfterChange(Player player, String group, float oldValue, float newValue) {
    }
}