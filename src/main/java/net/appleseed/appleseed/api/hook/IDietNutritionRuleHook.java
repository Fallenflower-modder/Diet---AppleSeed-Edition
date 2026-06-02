package net.appleseed.appleseed.api.hook;

import net.minecraft.world.entity.player.Player;

public interface IDietNutritionRuleHook {

    float onBeforeAdd(Player player, String group, float value);

    float onBeforeSet(Player player, String group, float value);

    float onBeforeDecay(Player player, String group, float decay);

    default void onAfterChange(Player player, String group, float oldValue, float newValue) {
    }
}