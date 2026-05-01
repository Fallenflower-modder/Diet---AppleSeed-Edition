package net.appleseed.appleseed.client;

import net.appleseed.appleseed.AppleSeed;
import net.appleseed.appleseed.client.screen.DietMenu;
import net.appleseed.appleseed.client.screen.DietScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = AppleSeed.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, AppleSeed.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<DietMenu>> DIET_MENU =
            MENU_TYPES.register("diet_menu",
                    () -> new MenuType<>(DietMenu::new, FeatureFlags.DEFAULT_FLAGS));

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(DIET_MENU.get(), DietScreen::new);
    }

    public static void register() {
    }
}
