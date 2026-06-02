package net.appleseed.appleseed.common.event;

import net.appleseed.appleseed.AppleSeedConstants;
import net.appleseed.appleseed.common.capability.DietData;
import net.appleseed.appleseed.common.config.DietConfig;
import net.appleseed.appleseed.common.data.food.FoodNutritionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = AppleSeedConstants.MOD_ID)
public class BlockFoodEventHandler {

    private static final TagKey<Block> FOOD_BLOCKS_TAG = TagKey.create(
            BuiltInRegistries.BLOCK.key(),
            ResourceLocation.fromNamespaceAndPath(AppleSeedConstants.MOD_ID, "food_blocks")
    );

    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        if (event.isCanceled()) {
            return;
        }

        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        InteractionHand hand = event.getHand();

        AppleSeedConstants.LOG.debug("BlockFoodEventHandler: player={} block={} hand={}",
                player.getName().getString(), state.getBlock(), hand);

        if (!state.is(FOOD_BLOCKS_TAG)) {
            return;
        }

        AppleSeedConstants.LOG.debug("BlockFoodEventHandler: matched food_blocks tag for {}", state.getBlock());

        if (hand != InteractionHand.MAIN_HAND) {
            return;
        }

        boolean ignoreHunger = DietConfig.INSTANCE.ignoreHunger.get();
        if (!ignoreHunger && !player.canEat(false)) {
            AppleSeedConstants.LOG.debug("BlockFoodEventHandler: player cannot eat, skipping");
            return;
        }

        if (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            return;
        }

        Block block = state.getBlock();
        if (!FoodNutritionManager.INSTANCE.hasBlockNutritionData(block)) {
            AppleSeedConstants.LOG.warn("BlockFoodEventHandler: no nutrition data for block {}", block);
            return;
        }

        var nutritions = FoodNutritionManager.INSTANCE.getBlockNutritions(block);
        int bites = FoodNutritionManager.INSTANCE.getBlockBites(block);
        AppleSeedConstants.LOG.info("BlockFoodEventHandler: adding nutrition for block {} bites={}: {}", block, bites, nutritions);
        for (var entry : nutritions.entrySet()) {
            if (entry.getValue() > 0) {
                DietData.addValue(player, entry.getKey(), entry.getValue() / bites);
            }
        }
        DietData.syncToClient(player);
    }
}