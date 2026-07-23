package net.appleseed.appleseed.api.query;

/**
 * Static accessor for the nutrition decay multiplier query API.
 * <p>
 * Provides a single entry point {@link #getInstance()} that returns the current
 * {@link IDietDecayQuery} implementation. The instance is set automatically by
 * AppleSeed during mod initialization and should not be changed by external mods.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * IDietDecayQuery query = DietDecayQuery.getInstance();
 * if (query != null) {
 *     double hitMult = query.getHitDecayMultiplier(player);
 * }
 * }</pre>
 *
 * @see IDietDecayQuery
 */
public final class DietDecayQuery {

    private static IDietDecayQuery instance;

    private DietDecayQuery() {
    }

    /**
     * Sets the query implementation. Called internally by AppleSeed during initialization.
     *
     * @param query the implementation to set
     */
    public static void setInstance(IDietDecayQuery query) {
        instance = query;
    }

    /**
     * Returns the current decay multiplier query implementation.
     * <p>
     * Safe to call from any thread after mod initialization is complete.
     *
     * @return the current query implementation, or {@code null} if not yet initialized
     */
    public static IDietDecayQuery getInstance() {
        return instance;
    }
}
