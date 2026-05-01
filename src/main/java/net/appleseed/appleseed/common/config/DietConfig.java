package net.appleseed.appleseed.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
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

        List<String> defaultGrains = Arrays.asList(
                "0-25:effect(minecraft:slowness,0)",
                "61-70:attribute(minecraft:generic.max_health,4.0)",
                "71-80:attribute(minecraft:generic.max_health,6.0),effect(minecraft:regeneration,0)",
                "81-100:attribute(minecraft:generic.max_health,6.0),attribute(minecraft:generic.attack_damage,1.0),effect(minecraft:regeneration,0)"
        );
        grainsRanges = builder
                .comment("谷物营养值奖励节点设置")
                .defineList("grains_ranges", defaultGrains, o -> o instanceof String);

        List<String> defaultFruits = Arrays.asList(
                "0-25:effect(minecraft:mining_fatigue,0)",
                "61-70:attribute(minecraft:generic.max_health,2.0),attribute(minecraft:generic.attack_speed,0.05)",
                "71-80:attribute(minecraft:generic.max_health,4.0),attribute(minecraft:generic.attack_speed,0.1)",
                "81-100:attribute(minecraft:generic.max_health,6.0),attribute(minecraft:generic.attack_speed,0.2)"
        );
        fruitsRanges = builder
                .comment("水果营养值奖励节点设置")
                .defineList("fruits_ranges", defaultFruits, o -> o instanceof String);

        List<String> defaultVegetables = Arrays.asList(
                "0-25:effect(minecraft:nausea,0)",
                "61-70:attribute(minecraft:generic.max_health,2.0),attribute(minecraft:generic.armor_toughness,2.0)",
                "71-80:attribute(minecraft:generic.max_health,2.0),attribute(minecraft:generic.armor_toughness,3.0)",
                "81-100:attribute(minecraft:generic.max_health,2.0),attribute(minecraft:generic.armor_toughness,4.0),effect(minecraft:haste,0)"
        );
        vegetablesRanges = builder
                .comment("蔬菜营养值奖励节点设置")
                .defineList("vegetables_ranges", defaultVegetables, o -> o instanceof String);

        List<String> defaultProteins = Arrays.asList(
                "0-25:effect(minecraft:weakness,0)",
                "61-70:attribute(minecraft:generic.max_health,2.0),attribute(minecraft:generic.armor,1.0)",
                "71-80:attribute(minecraft:generic.max_health,4.0),attribute(minecraft:generic.armor,2.0)",
                "81-100:attribute(minecraft:generic.max_health,6.0),attribute(minecraft:generic.armor,4.0),effect(minecraft:resistance,0)"
        );
        proteinsRanges = builder
                .comment("蛋白质营养值奖励节点设置")
                .defineList("proteins_ranges", defaultProteins, o -> o instanceof String);

        List<String> defaultSugars = Arrays.asList(
                "51-60:effect(minecraft:speed,0)",
                "61-80:effect(minecraft:speed,1)",
                "81-100:effect(minecraft:speed,1),effect(minecraft:hunger,4)"
        );
        sugarsRanges = builder
                .comment("糖分营养值奖励节点设置")
                .defineList("sugars_ranges", defaultSugars, o -> o instanceof String);

        builder.pop();

        builder.comment("Initial Values Settings").push("Initial_Values_Settings");

        grainsInitial = builder
                .comment("谷物营养值初始百分比，默认50%")
                .defineInRange("grains_initial", 0.5, 0.0, 1.0);

        fruitsInitial = builder
                .comment("水果营养值初始百分比，默认50%")
                .defineInRange("fruits_initial", 0.5, 0.0, 1.0);

        vegetablesInitial = builder
                .comment("蔬菜营养值初始百分比，默认50%")
                .defineInRange("vegetables_initial", 0.5, 0.0, 1.0);

        proteinsInitial = builder
                .comment("蛋白质营养值初始百分比，默认50%")
                .defineInRange("proteins_initial", 0.5, 0.0, 1.0);

        sugarsInitial = builder
                .comment("糖分营养值初始百分比，默认50%")
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

    public static List<String> getRanges(String group) {
        return switch (group) {
            case "grains" -> (List<String>) INSTANCE.grainsRanges.get();
            case "fruits" -> (List<String>) INSTANCE.fruitsRanges.get();
            case "vegetables" -> (List<String>) INSTANCE.vegetablesRanges.get();
            case "proteins" -> (List<String>) INSTANCE.proteinsRanges.get();
            case "sugars" -> (List<String>) INSTANCE.sugarsRanges.get();
            default -> List.of();
        };
    }
}
