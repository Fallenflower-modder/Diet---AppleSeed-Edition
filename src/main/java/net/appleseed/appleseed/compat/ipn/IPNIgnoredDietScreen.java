package net.appleseed.appleseed.compat.ipn;

import net.appleseed.appleseed.client.screen.DietMenu;
import net.appleseed.appleseed.client.screen.DietScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.anti_ad.mc.ipn.api.IPNIgnore;

/**
 * A {@link DietScreen} subclass annotated with {@code @IPNIgnore} to prevent
 * Inventory Profiles Next from sorting or otherwise interfering with the diet GUI.
 * <p>
 * This class is only loaded when IPN is detected on the classpath, avoiding
 * {@code NoClassDefFoundError} when the annotation is absent.
 */
@IPNIgnore
public class IPNIgnoredDietScreen extends DietScreen {

    public IPNIgnoredDietScreen(DietMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }
}