package net.appleseed.appleseed.api.type;

import java.util.List;
import java.util.UUID;

/**
 * Represents a single dietary effect within a suite.
 * <p>
 * Each effect consists of conditions (must ALL be met), and a list of
 * attribute modifiers and status effects to apply when conditions are satisfied.
 * A unique UUID is used to identify and manage the effect's application.
 *
 * @see IDietCondition
 * @see IDietAttribute
 * @see IDietStatusEffect
 */
public interface IDietEffect {

    /**
     * Returns the conditions that must be met for this effect to activate.
     * <p>
     * All conditions are evaluated with AND logic — every condition must be satisfied.
     *
     * @return list of conditions
     */
    List<IDietCondition> getConditions();

    /**
     * Returns the attribute modifiers to apply when this effect is active.
     *
     * @return list of attribute modifiers
     */
    List<IDietAttribute> getAttributes();

    /**
     * Returns the status effects to apply when this effect is active.
     *
     * @return list of status effects
     */
    List<IDietStatusEffect> getStatusEffects();

    /**
     * Returns the unique identifier for this effect.
     * <p>
     * Used to track and remove the effect when conditions are no longer met.
     *
     * @return the effect's UUID
     */
    UUID getUuid();
}