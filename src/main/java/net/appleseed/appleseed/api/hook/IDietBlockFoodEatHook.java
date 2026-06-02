package net.appleseed.appleseed.api.hook;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

import java.util.Map;

public interface IDietBlockFoodEatHook {

    boolean shouldProcessBlock(Player player, Block block, BlockPos pos);

    Map<String, Float> modifyNutritionGains(Player player, Block block, Map<String, Float> originalGains);

    default void onAfterEat(Player player, Block block) {
    }
}