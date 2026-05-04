package net.appleseed.appleseed.client;

import net.appleseed.appleseed.AppleSeed;
import net.appleseed.appleseed.client.screen.DietScreen;
import net.appleseed.appleseed.client.screen.DietMenu;
import net.appleseed.appleseed.common.data.food.FoodNutritionManager;
import net.appleseed.appleseed.common.data.group.DietGroups;
import net.appleseed.appleseed.common.data.suite.DietSuites;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.appleseed.appleseed.common.data.ServerDietConfig;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@EventBusSubscriber(modid = AppleSeed.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, AppleSeed.MOD_ID);

    public static final Supplier<MenuType<DietMenu>> DIET_MENU =
            MENU_TYPES.register("diet", () -> IMenuTypeExtension.create(DietMenu::new));

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
    }
}
