package net.appleseed.appleseed.common.util;

import net.appleseed.appleseed.api.type.IDietGroup;
import net.appleseed.appleseed.api.type.IDietResult;

import java.util.HashMap;
import java.util.Map;

public class DietResult implements IDietResult {

    public static final IDietResult EMPTY = Map::of;

    private final Map<IDietGroup, Float> values = new HashMap<>();

    @Override
    public IDietResult add(IDietGroup group, float value) {
        values.merge(group, value, Float::sum);
        return this;
    }

    @Override
    public Map<IDietGroup, Float> get() {
        return values;
    }
}
