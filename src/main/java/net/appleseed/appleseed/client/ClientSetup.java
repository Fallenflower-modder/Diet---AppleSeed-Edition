package net.appleseed.appleseed.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.appleseed.appleseed.AppleSeed;
import net.appleseed.appleseed.client.screen.DietScreen;
import net.appleseed.appleseed.client.screen.DietMenu;
import net.appleseed.appleseed.common.data.food.FoodNutritionManager;
import net.appleseed.appleseed.common.data.group.DietGroups;
import net.appleseed.appleseed.common.data.suite.DietSuites;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.appleseed.appleseed.common.data.ServerDietConfig;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lwjgl.glfw.GLFW;

import java.util.function.Supplier;

@EventBusSubscriber(modid = AppleSeed.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, AppleSeed.MOD_ID);

    public static final Supplier<MenuType<DietMenu>> DIET_MENU =
            MENU_TYPES.register("diet", () -> IMenuTypeExtension.create(DietMenu::new));

    public static final KeyMapping DIET_KEY = new KeyMapping(
            "key.appleseed.diet",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "key.categories.appleseed"
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(DIET_KEY);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(DIET_MENU.get(), DietScreen::new);
    }

    @EventBusSubscriber(modid = AppleSeed.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class ClientGameEvents {
        @SubscribeEvent
        public static void addReloadListener(AddReloadListenerEvent event) {
            event.addListener(DietGroups.CLIENT);
            event.addListener(DietSuites.CLIENT);
            event.addListener(FoodNutritionManager.CLIENT);
        }

        @SubscribeEvent
        public static void onPlayerDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
            ServerDietConfig.clear();
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            while (DIET_KEY.consumeClick()) {
                var player = Minecraft.getInstance().player;
                if (player != null) {
                    Minecraft.getInstance().setScreen(new DietScreen(
                            new DietMenu(0, player.getInventory()),
                            player.getInventory(),
                            Component.translatable("gui.appleseed.title")
                    ));
                }
            }
        }
    }
}
