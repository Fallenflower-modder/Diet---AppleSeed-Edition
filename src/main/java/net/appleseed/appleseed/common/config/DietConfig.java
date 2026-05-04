package net.appleseed.appleseed.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;

public class DietConfig {

    public static final DietConfig INSTANCE;
    public static final ModConfigSpec SPEC;

    public final ModConfigSpec.BooleanValue ignoreHunger;

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
}
