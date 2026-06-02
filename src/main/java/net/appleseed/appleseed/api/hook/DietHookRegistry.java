package net.appleseed.appleseed.api.hook;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DietHookRegistry {

    private static final List<IDietItemFoodEatHook> itemFoodEatHooks = new ArrayList<>();
    private static final List<IDietBlockFoodEatHook> blockFoodEatHooks = new ArrayList<>();
    private static final List<IDietRecipeFilterHook> recipeFilterHooks = new ArrayList<>();
    private static final List<IDietTooltipFilterHook> tooltipFilterHooks = new ArrayList<>();
    private static final List<IDietNutritionRuleHook> nutritionRuleHooks = new ArrayList<>();

    private DietHookRegistry() {
    }

    public static void registerItemFoodEatHook(IDietItemFoodEatHook hook) {
        itemFoodEatHooks.add(hook);
    }

    public static void unregisterItemFoodEatHook(IDietItemFoodEatHook hook) {
        itemFoodEatHooks.remove(hook);
    }

    public static void registerBlockFoodEatHook(IDietBlockFoodEatHook hook) {
        blockFoodEatHooks.add(hook);
    }

    public static void unregisterBlockFoodEatHook(IDietBlockFoodEatHook hook) {
        blockFoodEatHooks.remove(hook);
    }

    public static void registerRecipeFilterHook(IDietRecipeFilterHook hook) {
        recipeFilterHooks.add(hook);
    }

    public static void unregisterRecipeFilterHook(IDietRecipeFilterHook hook) {
        recipeFilterHooks.remove(hook);
    }

    public static void registerTooltipFilterHook(IDietTooltipFilterHook hook) {
        tooltipFilterHooks.add(hook);
    }

    public static void unregisterTooltipFilterHook(IDietTooltipFilterHook hook) {
        tooltipFilterHooks.remove(hook);
    }

    public static void registerNutritionRuleHook(IDietNutritionRuleHook hook) {
        nutritionRuleHooks.add(hook);
    }

    public static void unregisterNutritionRuleHook(IDietNutritionRuleHook hook) {
        nutritionRuleHooks.remove(hook);
    }

    public static boolean shouldInterceptItemFood(Player player, ItemStack stack) {
        for (IDietItemFoodEatHook hook : itemFoodEatHooks) {
            if (hook.shouldIntercept(player, stack)) {
                return true;
            }
        }
        return false;
    }

    public static Map<String, Float> modifyItemFoodGains(Player player, ItemStack stack, Map<String, Float> originalGains) {
        Map<String, Float> result = new HashMap<>(originalGains);
        for (IDietItemFoodEatHook hook : itemFoodEatHooks) {
            result = hook.modifyNutritionGains(player, stack, result);
        }
        return result;
    }

    public static void onAfterItemFoodEat(Player player, ItemStack stack) {
        for (IDietItemFoodEatHook hook : itemFoodEatHooks) {
            hook.onAfterEat(player, stack);
        }
    }

    public static boolean shouldProcessBlockFood(Player player, Block block, BlockPos pos) {
        for (IDietBlockFoodEatHook hook : blockFoodEatHooks) {
            if (!hook.shouldProcessBlock(player, block, pos)) {
                return false;
            }
        }
        return true;
    }

    public static Map<String, Float> modifyBlockFoodGains(Player player, Block block, Map<String, Float> originalGains) {
        Map<String, Float> result = new HashMap<>(originalGains);
        for (IDietBlockFoodEatHook hook : blockFoodEatHooks) {
            result = hook.modifyNutritionGains(player, block, result);
        }
        return result;
    }

    public static void onAfterBlockFoodEat(Player player, Block block) {
        for (IDietBlockFoodEatHook hook : blockFoodEatHooks) {
            hook.onAfterEat(player, block);
        }
    }

    public static boolean isValidRecipeType(Recipe<?> recipe) {
        for (IDietRecipeFilterHook hook : recipeFilterHooks) {
            if (!hook.isValidRecipeType(recipe)) {
                return false;
            }
        }
        return true;
    }

    public static boolean shouldProcessItem(Item item) {
        for (IDietRecipeFilterHook hook : recipeFilterHooks) {
            if (!hook.shouldProcessItem(item)) {
                return false;
            }
        }
        return true;
    }

    public static boolean shouldShowTooltip(ItemStack stack, Player player) {
        for (IDietTooltipFilterHook hook : tooltipFilterHooks) {
            if (!hook.shouldShowTooltip(stack, player)) {
                return false;
            }
        }
        return true;
    }

    public static Map<String, Float> modifyTooltipNutrition(ItemStack stack, Player player, Map<String, Float> originalNutritions) {
        Map<String, Float> result = new HashMap<>(originalNutritions);
        for (IDietTooltipFilterHook hook : tooltipFilterHooks) {
            result = hook.modifyTooltipNutrition(stack, player, result);
        }
        return result;
    }

    public static float processBeforeAdd(Player player, String group, float value) {
        float result = value;
        for (IDietNutritionRuleHook hook : nutritionRuleHooks) {
            result = hook.onBeforeAdd(player, group, result);
        }
        return result;
    }

    public static float processBeforeSet(Player player, String group, float value) {
        float result = value;
        for (IDietNutritionRuleHook hook : nutritionRuleHooks) {
            result = hook.onBeforeSet(player, group, result);
        }
        return result;
    }

    public static float processBeforeDecay(Player player, String group, float decay) {
        float result = decay;
        for (IDietNutritionRuleHook hook : nutritionRuleHooks) {
            result = hook.onBeforeDecay(player, group, result);
        }
        return result;
    }

    public static void processAfterChange(Player player, String group, float oldValue, float newValue) {
        for (IDietNutritionRuleHook hook : nutritionRuleHooks) {
            hook.onAfterChange(player, group, oldValue, newValue);
        }
    }
}