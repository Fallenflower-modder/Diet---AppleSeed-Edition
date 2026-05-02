package net.appleseed.appleseed.client;

import net.appleseed.appleseed.AppleSeed;
import net.appleseed.appleseed.client.screen.InventoryScreenButton;
import net.appleseed.appleseed.common.data.food.FoodNutritionManager;
import net.appleseed.appleseed.common.data.group.DietGroups;
import net.appleseed.appleseed.common.data.suite.DietSuites;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = AppleSeed.MOD_ID, value = Dist.CLIENT)
public class DietClientEvents {

    private static InventoryScreenButton dietButton;

    @SubscribeEvent
    public static void onGuiInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof InventoryScreen screen) {
            int x = screen.getGuiLeft() + 128;
            int y = screen.getGuiTop() + 61;
            dietButton = new InventoryScreenButton(x, y);
            event.addListener(dietButton);
        }
    }

    @SubscribeEvent
    public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        if (event.getScreen() instanceof InventoryScreen screen && dietButton != null) {
            int x = screen.getGuiLeft() + 128;
            int y = screen.getGuiTop() + 61;
            dietButton.setPosition(x, y);
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Player player = event.getEntity();
        if (player == null || player.level() == null) {
            return;
        }

        FoodProperties food = stack.getFoodProperties(player);
        if (food == null) {
            return;
        }

        Map<String, Float> nutritions = FoodNutritionManager.CLIENT.getNutritions(stack.getItem());
        if (nutritions.isEmpty()) {
            return;
        }

        boolean hasAny = false;
        for (float value : nutritions.values()) {
            if (value > 0) {
                hasAny = true;
                break;
            }
        }

        if (!hasAny) {
            return;
        }

        List<Component> tooltip = event.getToolTip();
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.appleseed.nutrition"));

        DietGroups.getGroups(player.level()).forEach(group -> {
            float value = nutritions.getOrDefault(group.getName(), 0.0f);
            if (value > 0) {
                float percent = value * 100;
                tooltip.add(Component.literal(String.format("  %s: +%.1f%%",
                        Component.translatable(group.getTranslationKey()).getString(), percent))
                        .withStyle(s -> s.withColor(group.getColor().toInt())));
            }
        });
    }

    @SubscribeEvent
    public static void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(DietGroups.CLIENT);
        event.addListener(DietSuites.CLIENT);
        event.addListener(FoodNutritionManager.CLIENT);
    }
}
