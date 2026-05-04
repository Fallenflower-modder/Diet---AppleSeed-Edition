package net.appleseed.appleseed.common.capability;

import net.appleseed.appleseed.api.type.IDietGroup;
import net.appleseed.appleseed.common.config.DietConfig;
import net.appleseed.appleseed.common.data.group.DietGroup;
import net.appleseed.appleseed.common.data.group.DietGroups;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class DietEffects {

    public record ParsedEffect(Holder<MobEffect> effect, int amplifier) {}

    public record ParsedAttribute(Holder<Attribute> attribute, double amount) {}

    public record RangeEffects(float min, float max, List<ParsedEffect> effects, List<ParsedAttribute> attributes) {}

    private static final Map<String, List<RangeEffects>> CACHED_EFFECTS = new HashMap<>();

    public static List<RangeEffects> parseRange(String groupName, List<? extends String> ranges) {
        if (CACHED_EFFECTS.containsKey(groupName)) {
            return CACHED_EFFECTS.get(groupName);
        }

        List<RangeEffects> result = new ArrayList<>();
        for (String rangeStr : ranges) {
            try {
                String[] parts = rangeStr.split(":", 2);
                String[] minMax = parts[0].split("-");
                float min = Float.parseFloat(minMax[0]) / 100.0f;
                float max = Float.parseFloat(minMax[1]) / 100.0f;

                List<ParsedEffect> effects = new ArrayList<>();
                List<ParsedAttribute> attributes = new ArrayList<>();
                String effectsPart = parts[1];

                for (String effectStr : effectsPart.split(",\\s*(?=effect|attribute)")) {
                    if (effectStr.startsWith("effect(")) {
                        String content = effectStr.substring(7, effectStr.length() - 1);
                        String[] effectParts = content.split(",");
                        ResourceLocation id = ResourceLocation.tryParse(effectParts[0]);
                        int amplifier = effectParts.length > 1 ? Integer.parseInt(effectParts[1]) : 0;
                        if (id != null) {
                            Holder<MobEffect> effect = BuiltInRegistries.MOB_EFFECT.getHolder(id).orElse(null);
                            if (effect != null) {
                                effects.add(new ParsedEffect(effect, amplifier));
                            }
                        }
                    } else if (effectStr.startsWith("attribute(")) {
                        String content = effectStr.substring(10, effectStr.length() - 1);
                        String[] attrParts = content.split(",");
                        ResourceLocation id = ResourceLocation.tryParse(attrParts[0]);
                        double amount = attrParts.length > 1 ? Double.parseDouble(attrParts[1]) : 0;
                        if (id != null) {
                            Holder<Attribute> attr = BuiltInRegistries.ATTRIBUTE.getHolder(id).orElse(null);
                            if (attr != null) {
                                attributes.add(new ParsedAttribute(attr, amount));
                            }
                        }
                    }
                }
                result.add(new RangeEffects(min, max, effects, attributes));
            } catch (Exception e) {
            }
        }

        CACHED_EFFECTS.put(groupName, result);
        return result;
    }

    public static void applyEffects(Player player) {
        if (player.level().isClientSide()) {
            return;
        }

        Map<Holder<MobEffect>, Integer> mergedEffects = new HashMap<>();
        Map<Holder<Attribute>, Double> mergedAttributes = new HashMap<>();

        for (IDietGroup group : DietGroups.getGroups(player.level())) {
            List<? extends String> ranges = getRangesForGroup(group.getName(), group);
            mergeGroupEffects(player, group.getName(), ranges, mergedEffects, mergedAttributes);
        }

        for (Map.Entry<Holder<MobEffect>, Integer> entry : mergedEffects.entrySet()) {
            Holder<MobEffect> effect = entry.getKey();
            int targetAmplifier = entry.getValue();

            MobEffectInstance existing = player.getEffect(effect);
            if (existing == null || existing.getAmplifier() < targetAmplifier) {
                player.addEffect(new MobEffectInstance(effect, 400, targetAmplifier, false, false, true));
            } else if (existing.getAmplifier() == targetAmplifier) {
                if (existing.getDuration() <= 380) {
                    player.addEffect(new MobEffectInstance(effect, 400, targetAmplifier, false, false, true));
                }
            }
        }

        for (Holder.Reference<Attribute> attr : BuiltInRegistries.ATTRIBUTE.holders().toList()) {
            var instance = player.getAttribute(attr);
            if (instance != null) {
                String attrName = attr.key().location().toString();
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                        net.appleseed.appleseed.AppleSeed.MOD_ID,
                        "diet_bonus_" + attrName.replace(':', '_')
                );
                instance.removeModifier(id);
            }
        }

        for (Map.Entry<Holder<Attribute>, Double> entry : mergedAttributes.entrySet()) {
            var instance = player.getAttribute(entry.getKey());
            if (instance != null) {
                String attrName = entry.getKey().getRegisteredName();
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                        net.appleseed.appleseed.AppleSeed.MOD_ID,
                        "diet_bonus_" + attrName.replace(':', '_')
                );
                instance.addTransientModifier(new AttributeModifier(
                        id, entry.getValue(), AttributeModifier.Operation.ADD_VALUE
                ));
            }
        }
    }

    private static List<? extends String> getRangesForGroup(String groupName, IDietGroup group) {
        if (DietConfig.hasEffectsOverride(groupName)) {
            return DietConfig.getEffectsOverride(groupName);
        }
        if (group instanceof DietGroup dietGroup) {
            return dietGroup.getEffects();
        }
        return Collections.emptyList();
    }

    private static void mergeGroupEffects(Player player, String group, List<? extends String> configRanges,
                                           Map<Holder<MobEffect>, Integer> effects,
                                           Map<Holder<Attribute>, Double> attributes) {
        float value = DietData.getValue(player, group);
        IDietGroup groupObj = DietGroups.getGroup(player.level(), group).orElse(null);
        float decayMultiplier = groupObj != null ? (float) groupObj.getDecayMultiplier() : 1.0f;
        List<RangeEffects> ranges = parseRange(group, configRanges);

        for (RangeEffects range : ranges) {
            if (value >= range.min() && value <= range.max()) {
                for (ParsedEffect pe : range.effects()) {
                    effects.merge(pe.effect(), pe.amplifier(), Integer::max);
                }
                for (ParsedAttribute pa : range.attributes()) {
                    attributes.merge(pa.attribute(), pa.amount() * decayMultiplier, Double::sum);
                }
            }
        }
    }

    public static void clearCache() {
        CACHED_EFFECTS.clear();
    }
}
