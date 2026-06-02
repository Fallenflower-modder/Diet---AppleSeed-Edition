package net.appleseed.appleseed.common.data.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.appleseed.appleseed.common.recipe.RecipeRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class SimulateRecipe implements Recipe<RecipeInput> {
    private final List<SimulateIngredient> inputs;
    private final List<SimulateIngredient> outputs;

    public SimulateRecipe(List<SimulateIngredient> inputs, List<SimulateIngredient> outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public List<SimulateIngredient> getInputs() {
        return inputs;
    }

    public List<SimulateIngredient> getOutputs() {
        return outputs;
    }

    @Override
    public boolean matches(RecipeInput container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput container, HolderLookup.Provider registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeRegistry.SIMULATE_RECIPE_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeRegistry.SIMULATE_RECIPE_TYPE;
    }

    public record SimulateIngredient(String type, ResourceLocation id, int count) {
        public static final Codec<SimulateIngredient> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.optionalFieldOf("type", "item").forGetter(SimulateIngredient::type),
                        ResourceLocation.CODEC.fieldOf("id").forGetter(SimulateIngredient::id),
                        Codec.INT.optionalFieldOf("count", 1).forGetter(SimulateIngredient::count)
                ).apply(instance, SimulateIngredient::new)
        );

        public boolean isItem() {
            return "item".equals(type);
        }

        public boolean isFluid() {
            return "fluid".equals(type);
        }
    }

    public static class Serializer implements RecipeSerializer<SimulateRecipe> {
        public static final MapCodec<SimulateRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        SimulateIngredient.CODEC.listOf().fieldOf("inputs").forGetter(SimulateRecipe::getInputs),
                        SimulateIngredient.CODEC.listOf().fieldOf("outputs").forGetter(SimulateRecipe::getOutputs)
                ).apply(instance, SimulateRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, SimulateRecipe> STREAM_CODEC = StreamCodec.of(
                (buf, recipe) -> {
                    buf.writeVarInt(recipe.inputs.size());
                    for (SimulateIngredient ing : recipe.inputs) {
                        buf.writeUtf(ing.type());
                        buf.writeResourceLocation(ing.id());
                        buf.writeVarInt(ing.count());
                    }
                    buf.writeVarInt(recipe.outputs.size());
                    for (SimulateIngredient ing : recipe.outputs) {
                        buf.writeUtf(ing.type());
                        buf.writeResourceLocation(ing.id());
                        buf.writeVarInt(ing.count());
                    }
                },
                buf -> {
                    int inputSize = buf.readVarInt();
                    List<SimulateIngredient> inputs = new ArrayList<>();
                    for (int i = 0; i < inputSize; i++) {
                        inputs.add(new SimulateIngredient(buf.readUtf(), buf.readResourceLocation(), buf.readVarInt()));
                    }
                    int outputSize = buf.readVarInt();
                    List<SimulateIngredient> outputs = new ArrayList<>();
                    for (int i = 0; i < outputSize; i++) {
                        outputs.add(new SimulateIngredient(buf.readUtf(), buf.readResourceLocation(), buf.readVarInt()));
                    }
                    return new SimulateRecipe(inputs, outputs);
                }
        );

        @Override
        public MapCodec<SimulateRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SimulateRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}