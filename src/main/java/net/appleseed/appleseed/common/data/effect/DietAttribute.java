package net.appleseed.appleseed.common.data.effect;

import net.appleseed.appleseed.api.type.IDietAttribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class DietAttribute implements IDietAttribute {

    private final String attribute;
    private final double amount;
    private final AttributeModifier.Operation operation;

    public DietAttribute(String attribute, double amount, AttributeModifier.Operation operation) {
        this.attribute = attribute;
        this.amount = amount;
        this.operation = operation;
    }

    @Override
    public String getAttribute() {
        return attribute;
    }

    @Override
    public double getAmount() {
        return amount;
    }

    @Override
    public AttributeModifier.Operation getOperation() {
        return operation;
    }
}
