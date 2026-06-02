package net.appleseed.appleseed.api.hook;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public interface IDietItemFoodEatHook {

    boolean shouldIntercept(Player player, ItemStack stack);

    Map<String, Float> modifyNutritionGains(Player player, ItemStack stack, Map<String, Float> originalGains);

    default void onAfterEat(Player player, ItemStack stack) {
    }
}