package net.appleseed.appleseed.api.type;

import net.appleseed.appleseed.api.util.DietColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Represents a single nutrition group (e.g. "fruits", "vegetables", "proteins").
 * <p>
 * Each group defines its visual properties (icon, color), behavioral modifiers
 * (gain/decay multipliers), and matching rules (tag-based or custom).
 * Implementations are typically loaded from JSON data files.
 *
 * @see IDietSuite
 */
public interface IDietGroup {

    /**
     * Returns the unique name of this nutrition group.
     *
     * @return the group name, e.g. {@code "fruits"}
     */
    String getName();

    /**
     * Returns the icon item displayed in the UI for this group.
     *
     * @return the icon item
     */
    Item getIcon();

    /**
     * Returns the color used to render this group's bar and text.
     *
     * @return the display color
     */
    DietColor getColor();

    /**
     * Returns the default starting value for new players.
     *
     * @return the default value (0.0 ~ 1.0)
     */
    float getDefaultValue();

    /**
     * Returns the display order of this group in the nutrition screen.
     * <p>
     * Lower values appear first.
     *
     * @return the sort order
     */
    int getOrder();

    /**
     * Returns the multiplier applied to nutrition gains.
     * <p>
     * For example, {@code 1.0} means normal gain, {@code 2.0} means double.
     *
     * @return the gain multiplier
     */
    double getGainMultiplier();

    /**
     * Returns the multiplier applied to nutrition decay.
     * <p>
     * For example, {@code 1.0} means normal decay, {@code 0.5} means half decay.
     *
     * @return the decay multiplier
     */
    double getDecayMultiplier();

    /**
     * Returns whether this group is considered beneficial (positive effects).
     * <p>
     * Non-beneficial groups typically have negative effects when their values are too high.
     *
     * @return {@code true} if beneficial
     */
    boolean isBeneficial();

    /**
     * Returns the item tag used to match foods belonging to this group.
     * <p>
     * Items matching this tag are automatically assigned to this group during auto-calculation.
     *
     * @return the item tag key, or {@code null} if not tag-based
     */
    TagKey<Item> getTag();

    /**
     * Returns the translation key for this group's display name.
     *
     * @return the translation key
     */
    String getTranslationKey();

    /**
     * Returns whether this group is negative (penalizes the player).
     *
     * @return {@code true} if negative
     */
    boolean isNegative();

    /**
     * Returns whether this group should ignore attack damage as a decay source.
     *
     * @return {@code true} to ignore attack decay
     */
    boolean ignoreAttack();

    /**
     * Returns whether this group should ignore hunger as a decay source.
     *
     * @return {@code true} to ignore hunger decay
     */
    boolean ignoreHunger();

    /**
     * Checks whether the given item stack belongs to this nutrition group.
     * <p>
     * Default implementation checks against the group's tag.
     *
     * @param stack the item stack to check
     * @return {@code true} if the item belongs to this group
     */
    boolean contains(ItemStack stack);

    /**
     * Serializes this group's data to NBT for persistence.
     *
     * @return an NBT compound tag
     */
    CompoundTag save();
}