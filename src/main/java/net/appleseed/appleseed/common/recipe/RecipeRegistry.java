package net.appleseed.appleseed.common.recipe;

import net.appleseed.appleseed.AppleSeedConstants;
import net.appleseed.appleseed.common.data.recipe.SimulateRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

public class RecipeRegistry {
    public static final RecipeType<SimulateRecipe> SIMULATE_RECIPE_TYPE = new RecipeType<SimulateRecipe>() {
        @Override
        public String toString() {
            return "appleseed:simulate_recipe";
        }
    };

    public static final RecipeSerializer<SimulateRecipe> SIMULATE_RECIPE_SERIALIZER = new SimulateRecipe.Serializer();

    @SubscribeEvent
    public static void registerRecipes(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.RECIPE_TYPE)) {
            Registry.register(BuiltInRegistries.RECIPE_TYPE,
                    ResourceLocation.fromNamespaceAndPath(AppleSeedConstants.MOD_ID, "simulate_recipe"),
                    SIMULATE_RECIPE_TYPE);
            AppleSeedConstants.LOG.debug("Registered RecipeType: appleseed:simulate_recipe");
        }
        if (event.getRegistryKey().equals(Registries.RECIPE_SERIALIZER)) {
            Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                    ResourceLocation.fromNamespaceAndPath(AppleSeedConstants.MOD_ID, "simulate_recipe"),
                    SIMULATE_RECIPE_SERIALIZER);
            AppleSeedConstants.LOG.debug("Registered RecipeSerializer: appleseed:simulate_recipe");
        }
    }
}