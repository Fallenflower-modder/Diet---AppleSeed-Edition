package net.appleseed.appleseed.api.hook;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public interface IDietTooltipFilterHook {

    boolean shouldShowTooltip(ItemStack stack, Player player);

    Map<String, Float> modifyTooltipNutrition(ItemStack stack, Player player, Map<String, Float> originalNutritions);
}