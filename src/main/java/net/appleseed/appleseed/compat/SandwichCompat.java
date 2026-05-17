package net.appleseed.appleseed.compat;

import net.appleseed.appleseed.common.data.food.FoodNutritionManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SandwichCompat {

    private static final String SAR_MOD_ID = "someassemblyrequired";
    private static final String SANDWICH_ID = "someassemblyrequired:sandwich";
    private static final String CONTENTS_TAG = "someassemblyrequired:sandwich_contents";

    private static boolean initialized;
    private static boolean enabled;

    private static void ensureInitialized() {
        if (!initialized) {
            initialized = true;
            try {
                enabled = net.neoforged.fml.ModList.get().isLoaded(SAR_MOD_ID);
            } catch (Exception e) {
                enabled = false;
            }
        }
    }

    public static boolean isEnabled() {
        ensureInitialized();
        return enabled;
    }

    public static boolean isSandwich(ItemStack stack) {
        if (!isEnabled()) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return SANDWICH_ID.equals(id.toString());
    }

    public static Map<String, Float> calculateNutrition(ItemStack stack, Level level) {
        if (!isSandwich(stack)) {
            return Collections.emptyMap();
        }

        CompoundTag fullTag = (CompoundTag) stack.save(level.registryAccess());
        CompoundTag components = fullTag.getCompound("components");

        CompoundTag source = findContentsSource(components);
        if (source == null) {
            return Collections.emptyMap();
        }

        ListTag contents = source.getList(CONTENTS_TAG, Tag.TAG_COMPOUND);
        if (contents.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Float> total = new HashMap<>();

        for (int i = 0; i < contents.size(); i++) {
            CompoundTag entry = contents.getCompound(i);
            String itemId = entry.getString("id");
            int count = entry.getInt("count");

            if (count <= 0) {
                continue;
            }

            ResourceLocation rl = ResourceLocation.tryParse(itemId);
            if (rl == null) {
                continue;
            }

            Item ingredient = BuiltInRegistries.ITEM.get(rl);
            if (ingredient == null || ingredient == Items.AIR) {
                continue;
            }

            Map<String, Float> ingredientNutrition = FoodNutritionManager.getNutritionsForClient(
                    ingredient, level.isClientSide());

            for (Map.Entry<String, Float> e : ingredientNutrition.entrySet()) {
                total.merge(e.getKey(), e.getValue() * count, Float::sum);
            }
        }

        return total;
    }

    private static CompoundTag findContentsSource(CompoundTag components) {
        if (components.contains(CONTENTS_TAG, Tag.TAG_LIST)) {
            return components;
        }
        CompoundTag customData = components.getCompound("minecraft:custom_data");
        if (customData.contains(CONTENTS_TAG, Tag.TAG_LIST)) {
            return customData;
        }
        for (String key : components.getAllKeys()) {
            if (key.endsWith("sandwich_contents") || key.contains("sandwich_contents")) {
                CompoundTag nested = components.getCompound(key);
                if (nested.contains(CONTENTS_TAG, Tag.TAG_LIST)) {
                    return nested;
                }
                if (components.get(key) instanceof CompoundTag ct && ct.contains(CONTENTS_TAG, Tag.TAG_LIST)) {
                    return ct;
                }
            }
        }
        return null;
    }
}
