package net.appleseed.appleseed.api.type;

import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.Set;

/**
 * Represents a collection of nutrition groups that together form a dietary suite.
 * <p>
 * Each suite defines a set of groups and associated effects that are applied
 * when the player's nutrition values meet certain conditions.
 *
 * @see IDietGroup
 * @see IDietEffect
 */
public interface IDietSuite {

    /**
     * Returns the unique name of this suite.
     *
     * @return the suite name
     */
    String getName();

    /**
     * Returns the set of nutrition groups that belong to this suite.
     *
     * @return an unmodifiable set of groups
     */
    Set<IDietGroup> getGroups();

    /**
     * Returns the list of effects defined in this suite.
     * <p>
     * Effects are evaluated in order and applied when their conditions are met.
     *
     * @return list of effects
     */
    List<IDietEffect> getEffects();

    /**
     * Serializes this suite's data to NBT for persistence.
     *
     * @return an NBT compound tag
     */
    CompoundTag save();
}