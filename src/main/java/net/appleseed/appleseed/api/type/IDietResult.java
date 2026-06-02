package net.appleseed.appleseed.api.type;

import java.util.HashMap;
import java.util.Map;

/**
 * Immutable snapshot of a player's nutrition values across all groups.
 * <p>
 * This is a {@link FunctionalInterface} with a single abstract method {@link #get()}
 * that returns the nutrition map. Use the static constant {@link #EMPTY} for an empty
 * result and the default methods {@link #merge(IDietResult)}, {@link #add(IDietGroup, float)},
 * and {@link #scale(float)} to derive new results.
 * <p>
 * <b>Example:</b>
 * <pre>{@code
 * IDietResult result = IDietResult.EMPTY
 *     .add(fruitsGroup, 0.5f)
 *     .add(vegetablesGroup, 0.3f);
 * float fruitValue = result.getValue(fruitsGroup); // 0.5f
 * }</pre>
 */
@FunctionalInterface
public interface IDietResult {

    /**
     * A singleton empty result with no nutrition values.
     */
    IDietResult EMPTY = Map::of;

    /**
     * Returns the nutrition data as an immutable map of group to value.
     *
     * @return map of nutrition groups to their current values (0.0 ~ 1.0)
     */
    Map<IDietGroup, Float> get();

    /**
     * Creates a new result by merging this one with another.
     * <p>
     * Values for the same group are summed.
     *
     * @param other the other result to merge with
     * @return a new merged result
     */
    default IDietResult merge(IDietResult other) {
        Map<IDietGroup, Float> merged = new HashMap<>(get());
        other.get().forEach((group, value) -> merged.merge(group, value, Float::sum));
        return () -> merged;
    }

    /**
     * Creates a new result by adding a value to a specific group.
     * <p>
     * If the group already exists, the value is added to the existing value.
     *
     * @param group the nutrition group
     * @param value the value to add (may be negative)
     * @return a new result with the added value
     */
    default IDietResult add(IDietGroup group, float value) {
        Map<IDietGroup, Float> copy = new HashMap<>(get());
        copy.merge(group, value, Float::sum);
        return () -> copy;
    }

    /**
     * Creates a new result by scaling all values by a multiplier.
     *
     * @param multiplier the scale factor
     * @return a new scaled result
     */
    default IDietResult scale(float multiplier) {
        Map<IDietGroup, Float> scaled = new HashMap<>();
        get().forEach((group, value) -> scaled.put(group, value * multiplier));
        return () -> scaled;
    }

    /**
     * Checks whether this result contains any values.
     *
     * @return {@code true} if empty
     */
    default boolean isEmpty() {
        return get().isEmpty();
    }

    /**
     * Returns the value for a specific group, or {@code 0.0f} if not present.
     *
     * @param group the nutrition group
     * @return the value, or {@code 0.0f}
     */
    default float getValue(IDietGroup group) {
        return get().getOrDefault(group, 0.0f);
    }
}