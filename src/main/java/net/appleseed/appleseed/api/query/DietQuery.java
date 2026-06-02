package net.appleseed.appleseed.api.query;

/**
 * Static accessor for the nutrition data query API.
 * <p>
 * Provides a single entry point {@link #getInstance()} that returns the current
 * {@link IDietFoodQuery} implementation. The instance is set automatically by
 * AppleSeed during mod initialization and should not be changed by external mods.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * IDietFoodQuery query = DietQuery.getInstance();
 * if (query.hasNutritionData(Items.APPLE)) {
 *     float value = query.getNutritionValue(Items.APPLE, "fruits");
 * }
 * }</pre>
 *
 * @see IDietFoodQuery
 */
public final class DietQuery {

    private static IDietFoodQuery instance;

    private DietQuery() {
    }

    /**
     * Sets the query implementation. Called internally by AppleSeed during initialization.
     *
     * @param query the implementation to set
     */
    public static void setInstance(IDietFoodQuery query) {
        instance = query;
    }

    /**
     * Returns the current nutrition data query implementation.
     * <p>
     * Safe to call from any thread after mod initialization is complete.
     *
     * @return the current query implementation, or {@code null} if not yet initialized
     */
    public static IDietFoodQuery getInstance() {
        return instance;
    }
}