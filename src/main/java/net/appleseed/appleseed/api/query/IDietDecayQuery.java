package net.appleseed.appleseed.api.query;

import net.minecraft.world.entity.player.Player;

/**
 * Query interface for retrieving nutrition decay multipliers from game rules.
 * <p>
 * Provides access to the three decay channels controlled by server-side game rules:
 * <ul>
 *   <li><b>Hit</b> — triggered when the player takes damage</li>
 *   <li><b>Hunger</b> — triggered when the player's food level decreases</li>
 *   <li><b>Saturation</b> — triggered when the player's saturation level decreases</li>
 * </ul>
 * Each multiplier is a {@code double} that scales the base decay amount.
 * A value of {@code 0.0} effectively disables that decay channel.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * IDietDecayQuery query = DietDecayQuery.getInstance();
 * double hitMultiplier = query.getHitDecayMultiplier(player);
 * }</pre>
 *
 * @see DietDecayQuery
 */
public interface IDietDecayQuery {

    /**
     * Returns the hit decay multiplier from the game rule
     * {@code nutritionDecayByHitMultiplier}.
     * <p>
     * This multiplier scales the nutrition decay applied when the player takes damage.
     * Default value: {@code 0.001}.
     *
     * @param player the player whose game rule value to query (provides access to the level/server)
     * @return the hit decay multiplier
     */
    double getHitDecayMultiplier(Player player);

    /**
     * Returns the hunger decay multiplier from the game rule
     * {@code nutritionDecayByHungerMultiplier}.
     * <p>
     * This multiplier scales the nutrition decay applied per point of food level lost.
     * Default value: {@code 0.005}.
     *
     * @param player the player whose game rule value to query (provides access to the level/server)
     * @return the hunger decay multiplier
     */
    double getHungerDecayMultiplier(Player player);

    /**
     * Returns the saturation decay multiplier from the game rule
     * {@code nutritionDecayBySaturationMultiplier}.
     * <p>
     * This multiplier scales the nutrition decay applied per point of saturation lost.
     * Default value: {@code 0.0} (disabled by default).
     *
     * @param player the player whose game rule value to query (provides access to the level/server)
     * @return the saturation decay multiplier
     */
    double getSaturationDecayMultiplier(Player player);
}
