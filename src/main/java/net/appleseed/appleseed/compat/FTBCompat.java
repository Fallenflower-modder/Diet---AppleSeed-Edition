package net.appleseed.appleseed.compat;

import net.appleseed.appleseed.AppleSeed;
import net.appleseed.appleseed.AppleSeedConstants;
import net.appleseed.appleseed.client.screen.DietMenu;
import net.appleseed.appleseed.client.screen.DietScreen;
import net.appleseed.appleseed.common.config.DietConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;
import java.util.Optional;

public class FTBCompat {

    private static final boolean ftbLoaded;

    static {
        ftbLoaded = ModList.get().isLoaded("ftblibrary");
    }

    public static boolean isFTBLoaded() {
        return ftbLoaded;
    }

    public static void updateButtonVisibility() {
        if (!ftbLoaded) {
            return;
        }
        try {
            boolean shouldShow = DietConfig.getEffectiveEntranceVisibility() == DietConfig.EntranceVisibility.FTB_COMPACT;

            Class<?> managerClass = Class.forName("dev.ftb.mods.ftblibrary.sidebar.SidebarButtonManager");
            Object manager = managerClass.getField("INSTANCE").get(null);
            Method getButtonMethod = managerClass.getMethod("getButton", ResourceLocation.class);

            ResourceLocation buttonId = ResourceLocation.fromNamespaceAndPath(AppleSeed.MOD_ID, "diet");
            Optional<?> buttonOpt = (Optional<?>) getButtonMethod.invoke(manager, buttonId);

            if (buttonOpt.isPresent()) {
                Object button = buttonOpt.get();
                Method setForceHidden = button.getClass().getMethod("setForceHidden", boolean.class);
                setForceHidden.invoke(button, !shouldShow);
            }
        } catch (Exception e) {
            AppleSeedConstants.LOG.warn("Failed to update FTB Library sidebar button visibility: {}", e.getMessage());
        }
    }

    static void openDietScreen() {
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