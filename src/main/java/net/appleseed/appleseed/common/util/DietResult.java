package net.appleseed.appleseed.common.util;

import net.appleseed.appleseed.api.type.IDietGroup;
import net.appleseed.appleseed.api.type.IDietResult;

import java.util.HashMap;
import java.util.Map;

public class DietResult implements IDietResult {

    public static final IDietResult EMPTY = Map::of;

    private final Map<IDietGroup, Float> values = new HashMap<>();

    public void add(IDietGroup group, float value) {
        values.put(group, value);
    }

    @Override
    public Map<IDietGroup, Float> get() {
        return values;
    }
}
