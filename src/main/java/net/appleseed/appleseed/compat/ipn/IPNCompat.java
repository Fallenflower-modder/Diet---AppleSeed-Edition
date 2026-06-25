package net.appleseed.appleseed.compat.ipn;

import net.appleseed.appleseed.client.screen.DietMenu;
import net.appleseed.appleseed.client.screen.DietScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.fml.ModList;

/**
 * Soft-dependency integration with Inventory Profiles Next.
 * <p>
 * When IPN is loaded, {@link #createScreen} returns an {@link IPNIgnoredDietScreen}
 * annotated with {@code @IPNIgnore} to prevent IPN from interfering with the diet GUI.
 * When IPN is absent, a plain {@link DietScreen} is returned.
 * <p>
 * The {@code IPNIgnoredDietScreen} class is never loaded unless IPN is present,
 * avoiding {@code NoClassDefFoundError} for the {@code @IPNIgnore} annotation.
 */
public final class IPNCompat {

    private static final boolean IPN_LOADED = ModList.get().isLoaded("inventoryprofilesnext");

    private IPNCompat() {
    }

    /**
     * Creates a DietScreen, optionally wrapped with {@code @IPNIgnore} if IPN is present.
     *
     * @param menu           the diet container menu
     * @param playerInventory the player's inventory
     * @param title          the screen title component
     * @return a {@link DietScreen} or {@link IPNIgnoredDietScreen} depending on IPN presence
     */
    public static DietScreen createScreen(DietMenu menu, Inventory playerInventory, Component title) {
        if (IPN_LOADED) {
            return new IPNIgnoredDietScreen(menu, playerInventory, title);
        }
        return new DietScreen(menu, playerInventory, title);
    }
}