package net.appleseed.appleseed.api.type;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Represents an attribute modifier applied by a dietary effect.
 * <p>
 * When the effect's conditions are met, an attribute modifier is applied to the player
 * with the specified attribute name, amount, and operation.
 *
 * @see IDietEffect
 * @see AttributeModifier.Operation
 */
public interface IDietAttribute {

    /**
     * Returns the registry name of the attribute to modify.
     * <p>
     * Example: {@code "minecraft:generic.max_health"}.
     *
     * @return the attribute registry name
     */
    String getAttribute();

    /**
     * Returns the amount to apply for this modifier.
     * <p>
     * The meaning depends on the operation:
     * <ul>
     *   <li>{@link AttributeModifier.Operation#ADD_VALUE ADD_VALUE} — adds the raw value</li>
     *   <li>{@link AttributeModifier.Operation#ADD_MULTIPLIED_BASE ADD_MULTIPLIED_BASE} — adds {@code amount * base}</li>
     *   <li>{@link AttributeModifier.Operation#ADD_MULTIPLIED_TOTAL ADD_MULTIPLIED_TOTAL} — adds {@code amount * total}</li>
     * </ul>
     *
     * @return the modifier amount
     */
    double getAmount();

    /**
     * Returns the operation type for this modifier.
     *
     * @return the modifier operation
     */
    AttributeModifier.Operation getOperation();
}