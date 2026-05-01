package net.appleseed.appleseed.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.appleseed.appleseed.AppleSeed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class InventoryScreenButton extends AbstractButton {

    private static final ResourceLocation BUTTON_NORMAL =
            ResourceLocation.fromNamespaceAndPath(AppleSeed.MOD_ID, "textures/gui/diet_button_normal.png");
    private static final ResourceLocation BUTTON_PRESSED =
            ResourceLocation.fromNamespaceAndPath(AppleSeed.MOD_ID, "textures/gui/diet_button_pressed.png");

    public InventoryScreenButton(int x, int y) {
        super(x, y, 20, 20, Component.translatable("gui.appleseed.button"));
    }

    @Override
    public void onPress() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            Minecraft.getInstance().setScreen(new DietScreen(
                    new DietMenu(0, player.getInventory()),
                    player.getInventory(),
                    Component.translatable("gui.appleseed.title")
            ));
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        ResourceLocation texture = this.isHoveredOrFocused() ? BUTTON_PRESSED : BUTTON_NORMAL;
        RenderSystem.setShaderTexture(0, texture);
        guiGraphics.blit(texture, this.getX(), this.getY(), 0, 0, 20, 20, 20, 20);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}
