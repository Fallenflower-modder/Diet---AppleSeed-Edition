package net.appleseed.appleseed.common.data.effect;

import net.appleseed.appleseed.api.type.IDietCondition;

public class DietCondition implements IDietCondition {

    private final String group;
    private final float min;
    private final float max;

    public DietCondition(String group, float min, float max) {
        this.group = group;
        this.min = min;
        this.max = max;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public float getMin() {
        return min;
    }

    @Override
    public float getMax() {
        return max;
    }

    public boolean matches(float value) {
        return value >= min && value <= max;
    }
}
