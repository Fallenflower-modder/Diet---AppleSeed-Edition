package net.appleseed.appleseed.common.data.group;

import net.appleseed.appleseed.api.type.IDietGroup;
import net.appleseed.appleseed.api.util.DietColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DietGroup implements IDietGroup {

    private final String name;
    private final Item icon;
    private final DietColor color;
    private final float defaultValue;
    private final int order;
    private final double gainMultiplier;
    private final double decayMultiplier;
    private final boolean beneficial;
    private final TagKey<Item> tag;
    private final String translationKey;
    private final List<String> effects;

    DietGroup(String name, Item icon, DietColor color, float defaultValue, int order,
              double gainMultiplier, double decayMultiplier, boolean beneficial, TagKey<Item> tag,
              String translationKey, List<String> effects) {
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.defaultValue = defaultValue;
        this.order = order;
        this.gainMultiplier = gainMultiplier;
        this.decayMultiplier = decayMultiplier;
        this.beneficial = beneficial;
        this.tag = tag;
        this.translationKey = translationKey;
        this.effects = effects;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Item getIcon() {
        return icon;
    }

    @Override
    public DietColor getColor() {
        return color;
    }

    @Override
    public float getDefaultValue() {
        return defaultValue;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public double getGainMultiplier() {
        return gainMultiplier;
    }

    @Override
    public double getDecayMultiplier() {
        return decayMultiplier;
    }

    @Override
    public boolean isBeneficial() {
        return beneficial;
    }

    @Override
    public TagKey<Item> getTag() {
        return tag;
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }

    public List<String> getEffects() {
        return effects;
    }

    @Override
    public boolean contains(ItemStack stack) {
        return tag != null && stack.is(tag);
    }

    @Override
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Name", name);
        tag.putInt("Color", color.toInt());
        tag.putFloat("DefaultValue", defaultValue);
        tag.putInt("Order", order);
        tag.putDouble("GainMultiplier", gainMultiplier);
        tag.putDouble("DecayMultiplier", decayMultiplier);
        tag.putBoolean("Beneficial", beneficial);
        tag.putString("TranslationKey", translationKey);
        return tag;
    }

    public static DietGroup load(CompoundTag tag) {
        return new DietGroup(
                tag.getString("Name"),
                net.minecraft.world.item.Items.APPLE,
                DietColor.fromInt(tag.getInt("Color")),
                tag.getFloat("DefaultValue"),
                tag.getInt("Order"),
                tag.getDouble("GainMultiplier"),
                tag.getDouble("DecayMultiplier"),
                tag.getBoolean("Beneficial"),
                null,
                tag.getString("TranslationKey"),
                Collections.emptyList()
        );
    }

    public static DietGroup fromSyncData(Map<String, Object> data) {
        try {
            Item icon = net.appleseed.appleseed.network.SyncDietConfigPacket.getItemFromId((String) data.get("iconId"));
            return new DietGroup(
                    (String) data.get("name"),
                    icon,
                    DietColor.fromInt(((Number) data.get("color")).intValue()),
                    ((Number) data.get("defaultValue")).floatValue(),
                    ((Number) data.get("order")).intValue(),
                    ((Number) data.get("gainMultiplier")).doubleValue(),
                    ((Number) data.get("decayMultiplier")).doubleValue(),
                    (Boolean) data.get("beneficial"),
                    null,
                    (String) data.get("translationKey"),
                    Collections.emptyList()
            );
        } catch (Exception e) {
            return null;
        }
    }

    public Map<String, Object> toSyncData() {
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("name", name);
        data.put("color", color.toInt());
        data.put("defaultValue", defaultValue);
        data.put("order", order);
        data.put("gainMultiplier", gainMultiplier);
        data.put("decayMultiplier", decayMultiplier);
        data.put("beneficial", beneficial);
        data.put("translationKey", translationKey);
        return data;
    }

    public static class Builder {

        public final String name;
        private Item icon;
        private DietColor color = new DietColor(255, 255, 255, 255);
        private float defaultValue = 0.0f;
        private int order = 0;
        private double gainMultiplier = 1.0;
        private double decayMultiplier = 1.0;
        private boolean beneficial = true;
        private TagKey<Item> tag;
        private String translationKey;
        private List<String> effects = Collections.emptyList();

        public Builder(String name) {
            this.name = name;
            this.translationKey = "diet.group." + name;
        }

        public Builder icon(Item icon) {
            this.icon = icon;
            return this;
        }

        public Builder color(DietColor color) {
            this.color = color;
            return this;
        }

        public Builder defaultValue(float defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder gainMultiplier(double gainMultiplier) {
            this.gainMultiplier = gainMultiplier;
            return this;
        }

        public Builder decayMultiplier(double decayMultiplier) {
            this.decayMultiplier = decayMultiplier;
            return this;
        }

        public Builder beneficial(boolean beneficial) {
            this.beneficial = beneficial;
            return this;
        }

        public Builder tag(TagKey<Item> tag) {
            this.tag = tag;
            return this;
        }

        public Builder translationKey(String translationKey) {
            this.translationKey = translationKey;
            return this;
        }

        public Builder effects(List<String> effects) {
            this.effects = effects;
            return this;
        }

        public DietGroup build() {
            return new DietGroup(name, icon, color, defaultValue, order,
                    gainMultiplier, decayMultiplier, beneficial, tag, translationKey, effects);
        }
    }
}
