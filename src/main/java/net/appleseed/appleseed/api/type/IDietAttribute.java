package net.appleseed.appleseed.api.type;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public interface IDietAttribute {

    String getAttribute();

    double getAmount();

    AttributeModifier.Operation getOperation();
}
