package net.appleseed.appleseed.common.data.effect;

import net.appleseed.appleseed.api.type.IDietStatusEffect;

public class DietStatusEffect implements IDietStatusEffect {

    private final String effect;
    private final int amplifier;

    public DietStatusEffect(String effect, int amplifier) {
        this.effect = effect;
        this.amplifier = amplifier;
    }

    @Override
    public String getEffect() {
        return effect;
    }

    @Override
    public int getAmplifier() {
        return amplifier;
    }
}
