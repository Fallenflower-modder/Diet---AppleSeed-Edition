package net.appleseed.appleseed.common.data.food;

import net.minecraft.world.level.material.Fluid;

/**
 * Snapshot of a fluid ingredient extracted from a recipe.
 * Contains the fluid and its amount in milli-buckets (mB).
 */
public record FluidIngredientSnapshot(Fluid fluid, long amount) {}