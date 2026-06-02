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

/**
 * Central registry for all Hook interfaces.
 * <p>
 * Provides static {@code register} and {@code unregister} methods for each Hook type.
 * All methods are thread-safe and maintain insertion order. Hooks are invoked in
 * registration order — the first registered hook runs first.
 * <p>
 * <b>Registration Timing:</b> Hooks should be registered during mod initialization
 * (e.g. in your {@code @Mod} constructor or on {@code FMLCommonSetupEvent}).
 * They take effect immediately and persist until explicitly unregistered.
 *
 * <h3>Hook Invocation Logic</h3>
 * <table border="1">
 *   <tr><th>Hook Type</th><th>Logic</th><th>Description</th></tr>
 *   <tr><td>Item Food Eat</td><td>OR</td><td>Any hook returning {@code true} intercepts</td></tr>
 *   <tr><td>Block Food Eat</td><td>AND</td><td>All hooks must return {@code true}</td></tr>
 *   <tr><td>Recipe Filter</td><td>AND</td><td>All hooks must return {@code true}</td></tr>
 *   <tr><td>Tooltip Filter</td><td>AND</td><td>All hooks must return {@code true}</td></tr>
 *   <tr><td>Nutrition Rule</td><td>Chain</td><td>Each hook receives the previous result</td></tr>
 * </table>
 */
public final class DietHookRegistry {

    private static final List<IDietItemFoodEatHook> itemFoodEatHooks = new ArrayList<>();
    private static final List<IDietBlockFoodEatHook> blockFoodEatHooks = new ArrayList<>();
    private static final List<IDietRecipeFilterHook> recipeFilterHooks = new ArrayList<>();
    private static final List<IDietTooltipFilterHook> tooltipFilterHooks = new ArrayList<>();
    private static final List<IDietNutritionRuleHook> nutritionRuleHooks = new ArrayList<>();

    private DietHookRegistry() {
    }

    /**
     * Registers an item food eat hook. Hooks are invoked in registration order.
     *
     * @param hook the hook to register
     */
    public static void registerItemFoodEatHook(IDietItemFoodEatHook hook) {
        itemFoodEatHooks.add(hook);
    }

    /**
     * Unregisters a previously registered item food eat hook.
     *
     * @param hook the hook to unregister
     */
    public static void unregisterItemFoodEatHook(IDietItemFoodEatHook hook) {
        itemFoodEatHooks.remove(hook);
    }

    /**
     * Registers a block food eat hook. Hooks are invoked in registration order.
     *
     * @param hook the hook to register
     */
    public static void registerBlockFoodEatHook(IDietBlockFoodEatHook hook) {
        blockFoodEatHooks.add(hook);
    }

    /**
     * Unregisters a previously registered block food eat hook.
     *
     * @param hook the hook to unregister
     */
    public static void unregisterBlockFoodEatHook(IDietBlockFoodEatHook hook) {
        blockFoodEatHooks.remove(hook);
    }

    /**
     * Registers a recipe filter hook. Hooks are invoked in registration order.
     *
     * @param hook the hook to register
     */
    public static void registerRecipeFilterHook(IDietRecipeFilterHook hook) {
        recipeFilterHooks.add(hook);
    }

    /**
     * Unregisters a previously registered recipe filter hook.
     *
     * @param hook the hook to unregister
     */
    public static void unregisterRecipeFilterHook(IDietRecipeFilterHook hook) {
        recipeFilterHooks.remove(hook);
    }

    /**
     * Registers a tooltip filter hook. Hooks are invoked in registration order.
     *
     * @param hook the hook to register
     */
    public static void registerTooltipFilterHook(IDietTooltipFilterHook hook) {
        tooltipFilterHooks.add(hook);
    }

    /**
     * Unregisters a previously registered tooltip filter hook.
     *
     * @param hook the hook to unregister
     */
    public static void unregisterTooltipFilterHook(IDietTooltipFilterHook hook) {
        tooltipFilterHooks.remove(hook);
    }

    /**
     * Registers a nutrition rule hook. Hooks are invoked in registration order.
     *
     * @param hook the hook to register
     */
    public static void registerNutritionRuleHook(IDietNutritionRuleHook hook) {
        nutritionRuleHooks.add(hook);
    }

    /**
     * Unregisters a previously registered nutrition rule hook.
     *
     * @param hook the hook to unregister
     */
    public static void unregisterNutritionRuleHook(IDietNutritionRuleHook hook) {
        nutritionRuleHooks.remove(hook);
    }

    /**
     * Checks whether any registered hook wants to intercept food eating for the given item.
     * <p>
     * Uses OR logic: returns {@code true} as soon as any hook returns {@code true}.
     *
     * @param player the player who is eating
     * @param stack  the food ItemStack being consumed
     * @return {@code true} if any hook wants to intercept
     */
    public static boolean shouldInterceptItemFood(Player player, ItemStack stack) {
        for (IDietItemFoodEatHook hook : itemFoodEatHooks) {
            if (hook.shouldIntercept(player, stack)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Invokes all registered item food eat hooks to modify nutrition gains.
     * <p>
     * Uses chain logic: each hook receives the result of the previous one.
     *
     * @param player        the player who is eating
     * @param stack         the food ItemStack being consumed
     * @param originalGains the initial nutrition gain map
     * @return the final modified nutrition gain map
     */
    public static Map<String, Float> modifyItemFoodGains(Player player, ItemStack stack, Map<String, Float> originalGains) {
        Map<String, Float> result = new HashMap<>(originalGains);
        for (IDietItemFoodEatHook hook : itemFoodEatHooks) {
            result = hook.modifyNutritionGains(player, stack, result);
        }
        return result;
    }

    /**
     * Invokes all registered item food eat hooks' after-eat callbacks.
     *
     * @param player the player who ate
     * @param stack  the food ItemStack that was consumed
     */
    public static void onAfterItemFoodEat(Player player, ItemStack stack) {
        for (IDietItemFoodEatHook hook : itemFoodEatHooks) {
            hook.onAfterEat(player, stack);
        }
    }

    /**
     * Checks whether all registered block food hooks allow processing.
     * <p>
     * Uses AND logic: returns {@code false} as soon as any hook returns {@code false}.
     *
     * @param player the player interacting with the block
     * @param block  the block being interacted with
     * @param pos    the position of the block
     * @return {@code true} if all hooks allow processing
     */
    public static boolean shouldProcessBlockFood(Player player, Block block, BlockPos pos) {
        for (IDietBlockFoodEatHook hook : blockFoodEatHooks) {
            if (!hook.shouldProcessBlock(player, block, pos)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Invokes all registered block food eat hooks to modify block food gains.
     * <p>
     * Uses chain logic: each hook receives the result of the previous one.
     *
     * @param player        the player eating the block food
     * @param block         the block being eaten
     * @param originalGains the initial per-bite nutrition gain map
     * @return the final modified per-bite nutrition gain map
     */
    public static Map<String, Float> modifyBlockFoodGains(Player player, Block block, Map<String, Float> originalGains) {
        Map<String, Float> result = new HashMap<>(originalGains);
        for (IDietBlockFoodEatHook hook : blockFoodEatHooks) {
            result = hook.modifyNutritionGains(player, block, result);
        }
        return result;
    }

    /**
     * Invokes all registered block food eat hooks' after-eat callbacks.
     *
     * @param player the player who ate
     * @param block  the block food that was eaten from
     */
    public static void onAfterBlockFoodEat(Player player, Block block) {
        for (IDietBlockFoodEatHook hook : blockFoodEatHooks) {
            hook.onAfterEat(player, block);
        }
    }

    /**
     * Checks whether all registered recipe filter hooks consider this recipe type valid.
     * <p>
     * Uses AND logic: returns {@code false} as soon as any hook returns {@code false}.
     * This runs after AppleSeed's built-in type checks.
     *
     * @param recipe the recipe to evaluate
     * @return {@code true} if all hooks consider this recipe type valid
     */
    public static boolean isValidRecipeType(Recipe<?> recipe) {
        for (IDietRecipeFilterHook hook : recipeFilterHooks) {
            if (!hook.isValidRecipeType(recipe)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether all registered recipe filter hooks consider this item processable.
     * <p>
     * Uses AND logic: returns {@code false} as soon as any hook returns {@code false}.
     *
     * @param item the item to evaluate
     * @return {@code true} if all hooks consider this item processable
     */
    public static boolean shouldProcessItem(Item item) {
        for (IDietRecipeFilterHook hook : recipeFilterHooks) {
            if (!hook.shouldProcessItem(item)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether all registered tooltip filter hooks want to show the tooltip.
     * <p>
     * Uses AND logic: returns {@code false} as soon as any hook returns {@code false}.
     *
     * @param stack  the ItemStack being rendered
     * @param player the player viewing the tooltip
     * @return {@code true} if all hooks want to show the tooltip
     */
    public static boolean shouldShowTooltip(ItemStack stack, Player player) {
        for (IDietTooltipFilterHook hook : tooltipFilterHooks) {
            if (!hook.shouldShowTooltip(stack, player)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Invokes all registered tooltip filter hooks to modify displayed nutrition values.
     * <p>
     * Uses chain logic: each hook receives the result of the previous one.
     *
     * @param stack              the ItemStack being rendered
     * @param player             the player viewing the tooltip
     * @param originalNutritions the original nutrition data to display
     * @return the final modified nutrition data to display
     */
    public static Map<String, Float> modifyTooltipNutrition(ItemStack stack, Player player, Map<String, Float> originalNutritions) {
        Map<String, Float> result = new HashMap<>(originalNutritions);
        for (IDietTooltipFilterHook hook : tooltipFilterHooks) {
            result = hook.modifyTooltipNutrition(stack, player, result);
        }
        return result;
    }

    /**
     * Invokes all registered nutrition rule hooks' {@code onBeforeAdd} methods.
     * <p>
     * Uses chain logic: each hook receives the result of the previous one.
     *
     * @param player the player whose nutrition is changing
     * @param group  the nutrition group name
     * @param value  the amount to add
     * @return the final modified value to add
     */
    public static float processBeforeAdd(Player player, String group, float value) {
        float result = value;
        for (IDietNutritionRuleHook hook : nutritionRuleHooks) {
            result = hook.onBeforeAdd(player, group, result);
        }
        return result;
    }

    /**
     * Invokes all registered nutrition rule hooks' {@code onBeforeSet} methods.
     * <p>
     * Uses chain logic: each hook receives the result of the previous one.
     *
     * @param player the player whose nutrition is being set
     * @param group  the nutrition group name
     * @param value  the value to set
     * @return the final modified value to set
     */
    public static float processBeforeSet(Player player, String group, float value) {
        float result = value;
        for (IDietNutritionRuleHook hook : nutritionRuleHooks) {
            result = hook.onBeforeSet(player, group, result);
        }
        return result;
    }

    /**
     * Invokes all registered nutrition rule hooks' {@code onBeforeDecay} methods.
     * <p>
     * Uses chain logic: each hook receives the result of the previous one.
     *
     * @param player the player whose nutrition is decaying
     * @param group  the nutrition group name
     * @param decay  the base decay amount
     * @return the final modified decay amount
     */
    public static float processBeforeDecay(Player player, String group, float decay) {
        float result = decay;
        for (IDietNutritionRuleHook hook : nutritionRuleHooks) {
            result = hook.onBeforeDecay(player, group, result);
        }
        return result;
    }

    /**
     * Invokes all registered nutrition rule hooks' {@code onAfterChange} callbacks.
     *
     * @param player   the player whose nutrition changed
     * @param group    the nutrition group name
     * @param oldValue the value before the change
     * @param newValue the value after the change
     */
    public static void processAfterChange(Player player, String group, float oldValue, float newValue) {
        for (IDietNutritionRuleHook hook : nutritionRuleHooks) {
            hook.onAfterChange(player, group, oldValue, newValue);
        }
    }
}