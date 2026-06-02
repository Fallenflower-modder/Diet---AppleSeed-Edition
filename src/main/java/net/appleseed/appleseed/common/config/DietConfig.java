package net.appleseed.appleseed.common.config;

import net.appleseed.appleseed.AppleSeedConstants;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DietConfig {

    public static final DietConfig INSTANCE;
    public static final ModConfigSpec SPEC;

    public enum EntranceVisibility {
        INVISIBLE,
        DEFAULT,
        FTB_COMPACT;

        public static EntranceVisibility fromString(String value) {
            if (value == null || value.isBlank()) {
                return DEFAULT;
            }
            try {
                return EntranceVisibility.valueOf(value.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                AppleSeedConstants.LOG.warn("Invalid entrance_visibility value '{}', falling back to DEFAULT", value);
                return DEFAULT;
            }
        }
    }

    public final ModConfigSpec.BooleanValue ignoreHunger;
    public final ModConfigSpec.ConfigValue<String> entranceVisibility;
    public final ModConfigSpec.IntValue craftChainSearchDepth;

    // 预设营养素字段覆盖 - 用于覆盖数据文件中对应字段的值
    // 注意：此处的 ignore_hunger 指"饱食度降低时该营养素是否不减少"，与 General_Settings 中的全局 ignore_hunger（控制饱食度满时是否计算摄入）语义不同
    public final ModConfigSpec.BooleanValue grainsIsNegative;
    public final ModConfigSpec.BooleanValue grainsIgnoreAttack;
    public final ModConfigSpec.BooleanValue grainsIgnoreHunger;

    public final ModConfigSpec.BooleanValue fruitsIsNegative;
    public final ModConfigSpec.BooleanValue fruitsIgnoreAttack;
    public final ModConfigSpec.BooleanValue fruitsIgnoreHunger;

    public final ModConfigSpec.BooleanValue vegetablesIsNegative;
    public final ModConfigSpec.BooleanValue vegetablesIgnoreAttack;
    public final ModConfigSpec.BooleanValue vegetablesIgnoreHunger;

    public final ModConfigSpec.BooleanValue proteinsIsNegative;
    public final ModConfigSpec.BooleanValue proteinsIgnoreAttack;
    public final ModConfigSpec.BooleanValue proteinsIgnoreHunger;

    public final ModConfigSpec.BooleanValue sugarsIsNegative;
    public final ModConfigSpec.BooleanValue sugarsIgnoreAttack;
    public final ModConfigSpec.BooleanValue sugarsIgnoreHunger;

    public final ModConfigSpec.ConfigValue<List<? extends String>> grainsRanges;
    public final ModConfigSpec.ConfigValue<List<? extends String>> fruitsRanges;
    public final ModConfigSpec.ConfigValue<List<? extends String>> vegetablesRanges;
    public final ModConfigSpec.ConfigValue<List<? extends String>> proteinsRanges;
    public final ModConfigSpec.ConfigValue<List<? extends String>> sugarsRanges;

    public final ModConfigSpec.DoubleValue grainsInitial;
    public final ModConfigSpec.DoubleValue fruitsInitial;
    public final ModConfigSpec.DoubleValue vegetablesInitial;
    public final ModConfigSpec.DoubleValue proteinsInitial;
    public final ModConfigSpec.DoubleValue sugarsInitial;

    static {
        Pair<DietConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(DietConfig::new);
        INSTANCE = specPair.getLeft();
        SPEC = specPair.getRight();
    }

    private DietConfig(ModConfigSpec.Builder builder) {
        builder.comment("General Settings").push("General_Settings");

        ignoreHunger = builder
                .comment("控制饱食度满时食用食物是否还计算营养值，默认false")
                .comment("Control whether to calculate nutrition when the player is full, default false.")
                .define("ignore_hunger", false);

        entranceVisibility = builder
                .comment("控制膳食均衡入口按钮的可见性",
                        "可选值: invisible（不显示入口）, default（物品栏按钮）, ftb_compact（FTB Library侧边栏按钮，需安装FTB Library）",
                        "非法值将自动回退为 default")
                .comment("Control the visibility of the diet balance button in the inventory.",
                        "Available values: invisible, default, ftb_compat",
                        "Default: default"
                )
                .define("entrance_visibility", "default", o -> o instanceof String);

        craftChainSearchDepth = builder
                .comment("非食物/流体物品配方链的最大递归搜索深度，默认3。",
                        "0表示不递归处理非食物/流体物品。",
                        "更高的值意味着自动计算能覆盖更多配方，但可能造成性能下降。")
                .comment("Control the maximum recursive search depth for non food items.",
                        "0 means no recursive search.",
                        "The higher value means more recipes will be searched. But it may affect performance.",
                        "Default: 3"
                )
                .defineInRange("craft_chain_search_depth", 3, 0, 100);

        builder.pop();

        builder.comment("预设营养素字段覆盖",
                "可在此覆盖5种预设营养素(grains/fruits/vegetables/proteins/sugars)的以下字段：",
                "is_negative - 是否为负面营养素（按配方计算食物营养值时忽略此营养素）",
                "ignore_attack - 受到攻击时该营养素是否不减少",
                "ignore_hunger - 饱食度降低时该营养素是否不减少（注意：与上方General_Settings中的全局ignore_hunger语义不同，此处仅控制衰减行为）",
                "",
                "Group Override Settings:",
                "Override fields for the 5 preset nutrition groups (grains/fruits/vegetables/proteins/sugars):",
                "is_negative - Whether this is a negative nutrition (ignored when calculating food nutrition via recipe).",
                "ignore_attack - Whether this nutrition does not decay when the player takes damage.",
                "ignore_hunger - Whether this nutrition does not decay when hunger decreases.",
                "Note: The ignore_hunger here is different from the global ignore_hunger in General_Settings; it only controls decay behavior.")
                .push("Group_Overrides");

        grainsIsNegative = builder
                .comment("覆盖谷物的 is_negative 字段", "Override grains is_negative field.")
                .define("grains_is_negative", false);
        grainsIgnoreAttack = builder
                .comment("覆盖谷物的 ignore_attack 字段", "Override grains ignore_attack field.")
                .define("grains_ignore_attack", false);
        grainsIgnoreHunger = builder
                .comment("覆盖谷物的 ignore_hunger 字段", "Override grains ignore_hunger field.")
                .define("grains_ignore_hunger", false);

        fruitsIsNegative = builder
                .comment("覆盖水果的 is_negative 字段", "Override fruits is_negative field.")
                .define("fruits_is_negative", false);
        fruitsIgnoreAttack = builder
                .comment("覆盖水果的 ignore_attack 字段", "Override fruits ignore_attack field.")
                .define("fruits_ignore_attack", false);
        fruitsIgnoreHunger = builder
                .comment("覆盖水果的 ignore_hunger 字段", "Override fruits ignore_hunger field.")
                .define("fruits_ignore_hunger", false);

        vegetablesIsNegative = builder
                .comment("覆盖蔬菜的 is_negative 字段", "Override vegetables is_negative field.")
                .define("vegetables_is_negative", false);
        vegetablesIgnoreAttack = builder
                .comment("覆盖蔬菜的 ignore_attack 字段", "Override vegetables ignore_attack field.")
                .define("vegetables_ignore_attack", false);
        vegetablesIgnoreHunger = builder
                .comment("覆盖蔬菜的 ignore_hunger 字段", "Override vegetables ignore_hunger field.")
                .define("vegetables_ignore_hunger", false);

        proteinsIsNegative = builder
                .comment("覆盖蛋白质的 is_negative 字段", "Override proteins is_negative field.")
                .define("proteins_is_negative", false);
        proteinsIgnoreAttack = builder
                .comment("覆盖蛋白质的 ignore_attack 字段", "Override proteins ignore_attack field.")
                .define("proteins_ignore_attack", false);
        proteinsIgnoreHunger = builder
                .comment("覆盖蛋白质的 ignore_hunger 字段", "Override proteins ignore_hunger field.")
                .define("proteins_ignore_hunger", false);

        sugarsIsNegative = builder
                .comment("覆盖糖类的 is_negative 字段", "Override sugars is_negative field.")
                .define("sugars_is_negative", false);
        sugarsIgnoreAttack = builder
                .comment("覆盖糖类的 ignore_attack 字段", "Override sugars ignore_attack field.")
                .define("sugars_ignore_attack", false);
        sugarsIgnoreHunger = builder
                .comment("覆盖糖类的 ignore_hunger 字段", "Override sugars ignore_hunger field.")
                .define("sugars_ignore_hunger", false);

        builder.pop();

        builder.comment("Nutritions Settings").push("Nutritions_Settings");

        builder.comment("效果范围覆盖设置",
                "注意：效果范围配置现在默认从营养素数据文件读取。",
                "如需覆盖，请删除前面的注释并取消#号注释并配置列表。",
                "示例：grains_ranges = [\"0-25:effect(minecraft:slowness,0)\"]",
                "优先级：此处配置（非注释且非空时） > 营养素数据文件配置",
                "",
                "Effect Range Override Settings:",
                "Effect ranges are loaded from nutrition group data files by default.",
                "To override, remove the comment markers above and configure the list.",
                "Example: grains_ranges = [\"0-25:effect(minecraft:slowness,0)\"]",
                "Priority: config file (when not commented) > nutrition group data file")
                .push("Effects_Override");

        grainsRanges = builder
                .comment("谷物营养值奖励节点设置 - 默认使用数据文件配置", "Grains effect range settings. Uses data file config by default.")
                .defineList("grains_ranges", Collections.emptyList(), o -> o instanceof String);

        fruitsRanges = builder
                .comment("水果营养值奖励节点设置 - 默认使用数据文件配置", "Fruits effect range settings. Uses data file config by default.")
                .defineList("fruits_ranges", Collections.emptyList(), o -> o instanceof String);

        vegetablesRanges = builder
                .comment("蔬菜营养值奖励节点设置 - 默认使用数据文件配置", "Vegetables effect range settings. Uses data file config by default.")
                .defineList("vegetables_ranges", Collections.emptyList(), o -> o instanceof String);

        proteinsRanges = builder
                .comment("蛋白质营养值奖励节点设置 - 默认使用数据文件配置", "Proteins effect range settings. Uses data file config by default.")
                .defineList("proteins_ranges", Collections.emptyList(), o -> o instanceof String);

        sugarsRanges = builder
                .comment("糖类营养值奖励节点设置 - 默认使用数据文件配置", "Sugars effect range settings. Uses data file config by default.")
                .defineList("sugars_ranges", Collections.emptyList(), o -> o instanceof String);

        builder.pop();

        grainsInitial = builder
                .comment("谷物初始营养值设置 默认50%", "Grains initial nutrition value. Default: 50%")
                .defineInRange("grains_initial", 0.5, 0.0, 1.0);

        fruitsInitial = builder
                .comment("水果初始营养值设置 默认50%", "Fruits initial nutrition value. Default: 50%")
                .defineInRange("fruits_initial", 0.5, 0.0, 1.0);

        vegetablesInitial = builder
                .comment("蔬菜初始营养值设置 默认50%", "Vegetables initial nutrition value. Default: 50%")
                .defineInRange("vegetables_initial", 0.5, 0.0, 1.0);

        proteinsInitial = builder
                .comment("蛋白质初始营养值设置 默认50%", "Proteins initial nutrition value. Default: 50%")
                .defineInRange("proteins_initial", 0.5, 0.0, 1.0);

        sugarsInitial = builder
                .comment("糖类初始营养值设置 默认50%", "Sugars initial nutrition value. Default: 50%")
                .defineInRange("sugars_initial", 0.5, 0.0, 1.0);

        builder.pop();
    }

    public static float getInitialValue(String group) {
        return switch (group) {
            case "grains" -> INSTANCE.grainsInitial.get().floatValue();
            case "fruits" -> INSTANCE.fruitsInitial.get().floatValue();
            case "vegetables" -> INSTANCE.vegetablesInitial.get().floatValue();
            case "proteins" -> INSTANCE.proteinsInitial.get().floatValue();
            case "sugars" -> INSTANCE.sugarsInitial.get().floatValue();
            default -> 0.5f;
        };
    }

    public static boolean hasEffectsOverride(String groupName) {
        List<? extends String> configRanges = switch (groupName) {
            case "grains" -> INSTANCE.grainsRanges.get();
            case "fruits" -> INSTANCE.fruitsRanges.get();
            case "vegetables" -> INSTANCE.vegetablesRanges.get();
            case "proteins" -> INSTANCE.proteinsRanges.get();
            case "sugars" -> INSTANCE.sugarsRanges.get();
            default -> null;
        };
        return configRanges != null && !configRanges.isEmpty();
    }

    public static List<? extends String> getEffectsOverride(String groupName) {
        return switch (groupName) {
            case "grains" -> INSTANCE.grainsRanges.get();
            case "fruits" -> INSTANCE.fruitsRanges.get();
            case "vegetables" -> INSTANCE.vegetablesRanges.get();
            case "proteins" -> INSTANCE.proteinsRanges.get();
            case "sugars" -> INSTANCE.sugarsRanges.get();
            default -> Collections.emptyList();
        };
    }

    /**
     * 获取 is_negative 的有效值。
     * 对于预设营养素，配置文件中的 Group_Overrides 会覆盖数据文件中的值。
     */
    public static boolean isGroupNegative(String groupName, boolean dataFileValue) {
        return switch (groupName) {
            case "grains" -> INSTANCE.grainsIsNegative.get();
            case "fruits" -> INSTANCE.fruitsIsNegative.get();
            case "vegetables" -> INSTANCE.vegetablesIsNegative.get();
            case "proteins" -> INSTANCE.proteinsIsNegative.get();
            case "sugars" -> INSTANCE.sugarsIsNegative.get();
            default -> dataFileValue;
        };
    }

    /**
     * 获取 ignore_attack 的有效值。
     * 对于预设营养素，配置文件中的 Group_Overrides 会覆盖数据文件中的值。
     */
    public static boolean isGroupIgnoreAttack(String groupName, boolean dataFileValue) {
        return switch (groupName) {
            case "grains" -> INSTANCE.grainsIgnoreAttack.get();
            case "fruits" -> INSTANCE.fruitsIgnoreAttack.get();
            case "vegetables" -> INSTANCE.vegetablesIgnoreAttack.get();
            case "proteins" -> INSTANCE.proteinsIgnoreAttack.get();
            case "sugars" -> INSTANCE.sugarsIgnoreAttack.get();
            default -> dataFileValue;
        };
    }

    /**
     * 获取 ignore_hunger 的有效值（指饱食度降低时该营养素是否不减少）。
     * 对于预设营养素，配置文件中的 Group_Overrides 会覆盖数据文件中的值。
     * 注意：此字段与 General_Settings 中的全局 ignore_hunger 语义不同。
     */
    public static boolean isGroupIgnoreHunger(String groupName, boolean dataFileValue) {
        return switch (groupName) {
            case "grains" -> INSTANCE.grainsIgnoreHunger.get();
            case "fruits" -> INSTANCE.fruitsIgnoreHunger.get();
            case "vegetables" -> INSTANCE.vegetablesIgnoreHunger.get();
            case "proteins" -> INSTANCE.proteinsIgnoreHunger.get();
            case "sugars" -> INSTANCE.sugarsIgnoreHunger.get();
            default -> dataFileValue;
        };
    }

    public static EntranceVisibility getEntranceVisibility() {
        return EntranceVisibility.fromString(INSTANCE.entranceVisibility.get());
    }

    public static EntranceVisibility getEffectiveEntranceVisibility() {
        EntranceVisibility raw = getEntranceVisibility();
        if (raw == EntranceVisibility.FTB_COMPACT && !ModList.get().isLoaded("ftblibrary")) {
            AppleSeedConstants.LOG.warn("entrance_visibility is set to ftb_compact, but FTB Library is not loaded. Falling back to DEFAULT.");
            return EntranceVisibility.DEFAULT;
        }
        return raw;
    }
}
