package net.appleseed.appleseed.common.registry;

import net.appleseed.appleseed.AppleSeed;
import net.appleseed.appleseed.client.screen.DietMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

// Common (both-dist) menu registry. Kept out of client.ClientSetup so the dedicated server can
// register the diet container without initializing that client-only class -- its KeyMapping field
// trips NeoForge's RuntimeDistCleaner ("invalid dist DEDICATED_SERVER"). DietMenu is a plain
// AbstractContainerMenu and is server-safe. The registered id is unchanged: appleseed:diet.
public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, AppleSeed.MOD_ID);

    public static final Supplier<MenuType<DietMenu>> DIET_MENU =
            MENU_TYPES.register("diet", () -> IMenuTypeExtension.create(DietMenu::new));
}
