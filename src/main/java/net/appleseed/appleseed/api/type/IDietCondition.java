package net.appleseed.appleseed.api.type;

/**
 * Represents a single condition for activating a dietary effect.
 * <p>
 * Each condition specifies a nutrition group and a value range [min, max].
 * The condition is satisfied when the player's nutrition value for that group
 * falls within the specified range.
 * <p>
 * All conditions in an effect are evaluated with AND logic — every condition
 * must be satisfied for the effect to activate.
 *
 * @see IDietEffect
 */
public interface IDietCondition {

    /**
     * Returns the name of the nutrition group this condition checks.
     *
     * @return the nutrition group name
     */
    String getGroup();

    /**
     * Returns the minimum value required for this condition.
     * <p>
     * The player's nutrition value must be {@code >= min}.
     *
     * @return the minimum value (0.0 ~ 1.0)
     */
    float getMin();

    /**
     * Returns the maximum value allowed for this condition.
     * <p>
     * The player's nutrition value must be {@code <= max}.
     *
     * @return the maximum value (0.0 ~ 1.0)
     */
    float getMax();
}