package net.appleseed.appleseed.api.hook;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;

public interface IDietRecipeFilterHook {

    boolean isValidRecipeType(Recipe<?> recipe);

    boolean shouldProcessItem(Item item);
}