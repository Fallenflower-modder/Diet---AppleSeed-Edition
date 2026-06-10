package net.appleseed.appleseed.common.registry;

import net.appleseed.appleseed.AppleSeed;
import net.appleseed.appleseed.client.screen.DietMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Holds menu type registrations shared between client and server.
 * <p>
 * Separated from {@code ClientSetup} to avoid loading client-only classes
 * ({@code KeyMapping}) on the dedicated server, which would cause
 * {@link ExceptionInInitializerError}.
 */
public final class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, AppleSeed.MOD_ID);

    public static final Supplier<MenuType<DietMenu>> DIET_MENU =
            MENU_TYPES.register("diet", () -> IMenuTypeExtension.create(DietMenu::new));

    private ModMenuTypes() {
    }
}