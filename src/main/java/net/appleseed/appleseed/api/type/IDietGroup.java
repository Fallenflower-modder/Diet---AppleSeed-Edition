package net.appleseed.appleseed.api.type;

import net.appleseed.appleseed.api.util.DietColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface IDietGroup {

    String getName();

    Item getIcon();

    DietColor getColor();

    float getDefaultValue();

    int getOrder();

    double getGainMultiplier();

    double getDecayMultiplier();

    boolean isBeneficial();

    TagKey<Item> getTag();

    String getTranslationKey();

    boolean contains(ItemStack stack);

    CompoundTag save();
}
