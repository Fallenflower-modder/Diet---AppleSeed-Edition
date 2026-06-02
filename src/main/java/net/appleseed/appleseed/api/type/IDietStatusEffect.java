package net.appleseed.appleseed.api.type;

/**
 * Represents a status effect applied by a dietary effect.
 * <p>
 * When the effect's conditions are met, a Minecraft status effect is applied
 * to the player with the specified amplifier level.
 *
 * @see IDietEffect
 */
public interface IDietStatusEffect {

    /**
     * Returns the registry name of the status effect to apply.
     * <p>
     * Example: {@code "minecraft:regeneration"}.
     *
     * @return the status effect registry name
     */
    String getEffect();

    /**
     * Returns the amplifier level for this status effect.
     * <p>
     * Level 0 = effect level I, level 1 = effect level II, etc.
     *
     * @return the amplifier level (0-based)
     */
    int getAmplifier();
}