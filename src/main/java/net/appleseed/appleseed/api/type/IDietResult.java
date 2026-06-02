package net.appleseed.appleseed.api.type;

import java.util.HashMap;
import java.util.Map;

@FunctionalInterface
public interface IDietResult {

    IDietResult EMPTY = Map::of;

    Map<IDietGroup, Float> get();

    default IDietResult merge(IDietResult other) {
        Map<IDietGroup, Float> merged = new HashMap<>(get());
        other.get().forEach((group, value) -> merged.merge(group, value, Float::sum));
        return () -> merged;
    }

    default IDietResult add(IDietGroup group, float value) {
        Map<IDietGroup, Float> copy = new HashMap<>(get());
        copy.merge(group, value, Float::sum);
        return () -> copy;
    }

    default IDietResult scale(float multiplier) {
        Map<IDietGroup, Float> scaled = new HashMap<>();
        get().forEach((group, value) -> scaled.put(group, value * multiplier));
        return () -> scaled;
    }

    default boolean isEmpty() {
        return get().isEmpty();
    }

    default float getValue(IDietGroup group) {
        return get().getOrDefault(group, 0.0f);
    }
}
