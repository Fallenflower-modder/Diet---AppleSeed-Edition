package net.appleseed.appleseed.api.hook;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/**
 * Hook interface for controlling which items display nutrition tooltips and what content is shown.
 * <p>
 * Register implementations via {@link DietHookRegistry#registerTooltipFilterHook(IDietTooltipFilterHook)}.
 * For a tooltip to be displayed, <strong>all</strong> registered hooks must return {@code true}
 * from {@link #shouldShowTooltip} (AND logic).
 * <p>
 * {@link #modifyTooltipNutrition} is called in registration order, with each hook receiving
 * the result of the previous one.
 *
 * @see DietHookRegistry
 */
public interface IDietTooltipFilterHook {

    /**
     * Determines whether the nutrition tooltip should be displayed for the given item stack.
     * <p>
     * All hooks must return {@code true} (AND logic) for the tooltip to be shown.
     * This is called before any nutrition data is resolved.
     *
     * @param stack  the ItemStack being rendered
     * @param player the player viewing the tooltip
     * @return {@code true} to show the tooltip; {@code false} to hide it
     */
    boolean shouldShowTooltip(ItemStack stack, Player player);

    /**
     * Modifies the nutrition values displayed in the tooltip.
     * <p>
     * Called in registration order. Use this to hide specific groups, adjust displayed values,
     * or add custom entries. The returned map is what gets rendered in the tooltip.
     *
     * @param stack              the ItemStack being rendered
     * @param player             the player viewing the tooltip
     * @param originalNutritions the original nutrition data (group name to value)
     * @return the modified nutrition data to display
     */
    Map<String, Float> modifyTooltipNutrition(ItemStack stack, Player player, Map<String, Float> originalNutritions);
}