package net.appleseed.appleseed.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;

public class DietConfig {

    public static final DietConfig INSTANCE;
    public static final ModConfigSpec SPEC;

    public final ModConfigSpec.BooleanValue ignoreHunger;

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
                .define("ignore_hunger", false);

        builder.pop();

        builder.comment("预设营养素字段覆盖",
                "可在此覆盖5种预设营养素(grains/fruits/vegetables/proteins/sugars)的以下字段：",
                "is_negative - 是否为负面营养素（按配方计算食物营养值时忽略此营养素）",
                "ignore_attack - 受到攻击时该营养素是否不减少",
                "ignore_hunger - 饱食度降低时该营养素是否不减少（注意：与上方General_Settings中的全局ignore_hunger语义不同，此处仅控制衰减行为）")
                .push("Group_Overrides");

        grainsIsNegative = builder
                .comment("覆盖谷物的 is_negative 字段")
                .define("grains_is_negative", false);
        grainsIgnoreAttack = builder
                .comment("覆盖谷物的 ignore_attack 字段")
                .define("grains_ignore_attack", false);
        grainsIgnoreHunger = builder
                .comment("覆盖谷物的 ignore_hunger 字段")
                .define("grains_ignore_hunger", false);

        fruitsIsNegative = builder
                .comment("覆盖水果的 is_negative 字段")
                .define("fruits_is_negative", false);
        fruitsIgnoreAttack = builder
                .comment("覆盖水果的 ignore_attack 字段")
                .define("fruits_ignore_attack", false);
        fruitsIgnoreHunger = builder
                .comment("覆盖水果的 ignore_hunger 字段")
                .define("fruits_ignore_hunger", false);

        vegetablesIsNegative = builder
                .comment("覆盖蔬菜的 is_negative 字段")
                .define("vegetables_is_negative", false);
        vegetablesIgnoreAttack = builder
                .comment("覆盖蔬菜的 ignore_attack 字段")
                .define("vegetables_ignore_attack", false);
        vegetablesIgnoreHunger = builder
                .comment("覆盖蔬菜的 ignore_hunger 字段")
                .define("vegetables_ignore_hunger", false);

        proteinsIsNegative = builder
                .comment("覆盖蛋白质的 is_negative 字段")
                .define("proteins_is_negative", false);
        proteinsIgnoreAttack = builder
                .comment("覆盖蛋白质的 ignore_attack 字段")
                .define("proteins_ignore_attack", false);
        proteinsIgnoreHunger = builder
                .comment("覆盖蛋白质的 ignore_hunger 字段")
                .define("proteins_ignore_hunger", false);

        sugarsIsNegative = builder
                .comment("覆盖糖类的 is_negative 字段")
                .define("sugars_is_negative", false);
        sugarsIgnoreAttack = builder
                .comment("覆盖糖类的 ignore_attack 字段")
                .define("sugars_ignore_attack", false);
        sugarsIgnoreHunger = builder
                .comment("覆盖糖类的 ignore_hunger 字段")
                .define("sugars_ignore_hunger", false);

        builder.pop();

        builder.comment("Nutritions Settings").push("Nutritions_Settings");

        builder.comment("注意：效果范围配置现在默认从营养素数据文件读取。",
                "如需覆盖，请删除前面的注释并取消#号注释并配置列表。",
                "示例：grains_ranges = [\"0-25:effect(minecraft:slowness,0)\"]",
                "优先级：此处配置（非注释且非空时） > 营养素数据文件配置").push("Effects_Override");

        grainsRanges = builder
                .comment("谷物营养值奖励节点设置 - 默认使用数据文件配置")
                .defineList("grains_ranges", Collections.emptyList(), o -> o instanceof String);

        fruitsRanges = builder
                .comment("水果营养值奖励节点设置 - 默认使用数据文件配置")
                .defineList("fruits_ranges", Collections.emptyList(), o -> o instanceof String);

        vegetablesRanges = builder
                .comment("蔬菜营养值奖励节点设置 - 默认使用数据文件配置")
                .defineList("vegetables_ranges", Collections.emptyList(), o -> o instanceof String);

        proteinsRanges = builder
                .comment("蛋白质营养值奖励节点设置 - 默认使用数据文件配置")
                .defineList("proteins_ranges", Collections.emptyList(), o -> o instanceof String);

        sugarsRanges = builder
                .comment("糖类营养值奖励节点设置 - 默认使用数据文件配置")
                .defineList("sugars_ranges", Collections.emptyList(), o -> o instanceof String);

        builder.pop();

        grainsInitial = builder
                .comment("谷物初始营养值设置 默认50%")
                .defineInRange("grains_initial", 0.5, 0.0, 1.0);

        fruitsInitial = builder
                .comment("水果初始营养值设置 默认50%")
                .defineInRange("fruits_initial", 0.5, 0.0, 1.0);

        vegetablesInitial = builder
                .comment("蔬菜初始营养值设置 默认50%")
                .defineInRange("vegetables_initial", 0.5, 0.0, 1.0);

        proteinsInitial = builder
                .comment("蛋白质初始营养值设置 默认50%")
                .defineInRange("proteins_initial", 0.5, 0.0, 1.0);

        sugarsInitial = builder
                .comment("糖类初始营养值设置 默认50%")
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
}
