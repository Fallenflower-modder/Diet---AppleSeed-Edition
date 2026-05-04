package net.appleseed.appleseed.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.appleseed.appleseed.AppleSeed;
import net.appleseed.appleseed.api.type.IDietGroup;
import net.appleseed.appleseed.common.capability.DietData;
import net.appleseed.appleseed.common.capability.DietEffects;
import net.appleseed.appleseed.common.config.DietConfig;
import net.appleseed.appleseed.common.data.group.DietGroups;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class DietScreen extends AbstractContainerScreen<DietMenu> {

    public static final ResourceLocation BACKGROUND =
            ResourceLocation.withDefaultNamespace("textures/gui/demo_background.png");
    public static final ResourceLocation DIET_ICONS =
            ResourceLocation.fromNamespaceAndPath(AppleSeed.MOD_ID, "textures/gui/diet_info.png");
    public static final ResourceLocation PROGRESS_EMPTY =
            ResourceLocation.fromNamespaceAndPath(AppleSeed.MOD_ID, "textures/gui/progress_empty.png");
    public static final ResourceLocation PROGRESS_FULL =
            ResourceLocation.fromNamespaceAndPath(AppleSeed.MOD_ID, "textures/gui/progress_full.png");

    private int infoIconX;
    private int infoIconY;

    public DietScreen(DietMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 256;
        this.imageHeight = 166;
    }

    private List<IDietGroup> getSortedGroups() {
        List<IDietGroup> groups = new ArrayList<>(DietGroups.getGroups(this.menu.getPlayer().level()));
        groups.sort(Comparator.comparingInt(IDietGroup::getOrder));
        return groups;
    }

    @Override
    protected void init() {
        super.init();
        List<IDietGroup> sortedGroups = getSortedGroups();

        this.imageHeight = 110 + sortedGroups.size() * 18;

        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.infoIconX = this.leftPos + this.titleLabelX + this.font.width(this.title) + 8;
        this.infoIconY = this.topPos + 6;

        this.clearWidgets();
        if (sortedGroups.size() <= 6) {
            int buttonY = this.topPos + 128;
            this.addRenderableWidget(Button.builder(Component.translatable("gui.appleseed.close"), button -> {
                this.minecraft.player.closeContainer();
            }).bounds(this.leftPos + (this.imageWidth - 60) / 2, buttonY, 60, 20).build());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        List<IDietGroup> sortedGroups = getSortedGroups();
        int expectedHeight = 110 + sortedGroups.size() * 18;
        if (this.imageHeight != expectedHeight) {
            this.init();
        }

        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        if (mouseX >= infoIconX && mouseX <= infoIconX + 16 &&
                mouseY >= infoIconY && mouseY <= infoIconY + 16) {
            List<Component> tooltip = getActiveEffectsTooltip();
            guiGraphics.renderTooltip(this.font, tooltip, java.util.Optional.empty(), mouseX, mouseY);
        }
    }

    private List<Component> getActiveEffectsTooltip() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("tooltip.appleseed.effects"));

        Player player = this.menu.getPlayer();
        Map<String, Integer> mergedEffects = new HashMap<>();
        Map<String, Double> mergedAttributes = new HashMap<>();

        for (IDietGroup group : getSortedGroups()) {
            String groupName = group.getName();
            float value = DietData.getValue(player, groupName);
            List<? extends String> rangeStrs = getRangesForGroup(groupName);
            List<DietEffects.RangeEffects> ranges = DietEffects.parseRange(groupName, rangeStrs);

            for (DietEffects.RangeEffects range : ranges) {
                if (value >= range.min() && value <= range.max()) {
                    for (DietEffects.ParsedEffect pe : range.effects()) {
                        String effectKey = pe.effect().value().getDescriptionId();
                        mergedEffects.merge(effectKey, pe.amplifier(), Integer::max);
                    }
                    for (DietEffects.ParsedAttribute pa : range.attributes()) {
                        String attrKey = pa.attribute().value().getDescriptionId();
                        mergedAttributes.merge(attrKey, pa.amount(), Double::sum);
                    }
                }
            }
        }

        if (mergedEffects.isEmpty() && mergedAttributes.isEmpty()) {
            tooltip.add(Component.literal("暂无激活效果"));
        } else {
            for (Map.Entry<String, Integer> entry : mergedEffects.entrySet()) {
                Component effectName = Component.translatable(entry.getKey());
                String level = entry.getValue() > 0 ? toRoman(entry.getValue() + 1) : "";
                tooltip.add(Component.literal(effectName.getString() + " " + level));
            }
            for (Map.Entry<String, Double> entry : mergedAttributes.entrySet()) {
                Component attrName = Component.translatable(entry.getKey());
                String attrKey = entry.getKey();
                if (isSpeedAttribute(attrKey)) {
                    tooltip.add(Component.literal(String.format("%+.2f ", entry.getValue()) + attrName.getString()));
                } else {
                    tooltip.add(Component.literal(String.format("%+.0f ", entry.getValue()) + attrName.getString()));
                }
            }
        }

        return tooltip;
    }

    private List<? extends String> getRangesForGroup(String groupName) {
        if (DietConfig.hasEffectsOverride(groupName)) {
            return DietConfig.getEffectsOverride(groupName);
        }
        IDietGroup group = DietGroups.getGroup(this.menu.getPlayer().level(), groupName).orElse(null);
        if (group instanceof net.appleseed.appleseed.common.data.group.DietGroup dietGroup) {
            return dietGroup.getEffects();
        }
        return java.util.Collections.emptyList();
    }

    private boolean isSpeedAttribute(String attrKey) {
        return attrKey.contains("movement_speed") || attrKey.contains("attack_speed");
    }

    private String toRoman(int num) {
        return switch (num) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(num);
        };
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();

        RenderSystem.setShaderTexture(0, BACKGROUND);
        guiGraphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        RenderSystem.setShaderTexture(0, DIET_ICONS);
        guiGraphics.blit(DIET_ICONS, infoIconX, infoIconY, 160, 0, 16, 16, 16, 16);

        Player player = this.menu.getPlayer();
        List<IDietGroup> sortedGroups = getSortedGroups();

        int iconX = this.leftPos + 18;
        int nameX = this.leftPos + 42;
        int barX = this.leftPos + 82;
        int y = this.topPos + 28;

        for (int i = 0; i < sortedGroups.size(); i++) {
            IDietGroup group = sortedGroups.get(i);
            float value = DietData.getValue(player, group.getName());
            int barWidth = (int) (value * 122);
            int color = group.getColor().toInt();
            float r = ((color >> 16) & 255) / 255.0F;
            float g = ((color >> 8) & 255) / 255.0F;
            float b = (color & 255) / 255.0F;
            int rowY = y + i * 18;

            net.minecraft.world.item.Item iconItem = group.getIcon();
            if (iconItem == null) {
                iconItem = net.minecraft.world.item.Items.APPLE;
            }
            guiGraphics.renderFakeItem(new net.minecraft.world.item.ItemStack(iconItem), iconX, rowY - 5);

            Component groupName = Component.translatable(group.getTranslationKey());
            guiGraphics.drawString(this.font, groupName, nameX, rowY - 1, 0x404040, false);

            RenderSystem.setShaderColor(r, g, b, 1.0F);
            RenderSystem.setShaderTexture(0, PROGRESS_EMPTY);
            guiGraphics.blit(PROGRESS_EMPTY, barX, rowY, 0, 0, 122, 5, 122, 5);
            if (barWidth > 0) {
                RenderSystem.setShaderTexture(0, PROGRESS_FULL);
                guiGraphics.blit(PROGRESS_FULL, barX, rowY, 0, 0, barWidth, 5, 122, 5);
            }
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            String percentText = String.format("%.0f%%", value * 100);
            guiGraphics.drawString(this.font, percentText, barX + 125, rowY - 1, 0x404040, false);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, 8, 4210752, false);
    }
}
