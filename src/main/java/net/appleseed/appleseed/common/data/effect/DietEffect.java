package net.appleseed.appleseed.common.data.effect;

import com.google.common.collect.ImmutableList;
import net.appleseed.appleseed.api.type.IDietAttribute;
import net.appleseed.appleseed.api.type.IDietCondition;
import net.appleseed.appleseed.api.type.IDietEffect;
import net.appleseed.appleseed.api.type.IDietStatusEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DietEffect implements IDietEffect {

    private final List<IDietCondition> conditions;
    private final List<IDietAttribute> attributes;
    private final List<IDietStatusEffect> statusEffects;
    private final UUID uuid;

    private DietEffect(List<IDietCondition> conditions, List<IDietAttribute> attributes,
                       List<IDietStatusEffect> statusEffects, UUID uuid) {
        this.conditions = ImmutableList.copyOf(conditions);
        this.attributes = ImmutableList.copyOf(attributes);
        this.statusEffects = ImmutableList.copyOf(statusEffects);
        this.uuid = uuid;
    }

    @Override
    public List<IDietCondition> getConditions() {
        return conditions;
    }

    @Override
    public List<IDietAttribute> getAttributes() {
        return attributes;
    }

    @Override
    public List<IDietStatusEffect> getStatusEffects() {
        return statusEffects;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    public static class Builder {

        private final List<IDietCondition> conditions = new ArrayList<>();
        private final List<IDietAttribute> attributes = new ArrayList<>();
        private final List<IDietStatusEffect> statusEffects = new ArrayList<>();
        private UUID uuid;

        public Builder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder addCondition(IDietCondition condition) {
            this.conditions.add(condition);
            return this;
        }

        public Builder addAttribute(IDietAttribute attribute) {
            this.attributes.add(attribute);
            return this;
        }

        public Builder addStatusEffect(IDietStatusEffect effect) {
            this.statusEffects.add(effect);
            return this;
        }

        public IDietEffect build() {
            if (this.uuid == null) {
                this.uuid = UUID.randomUUID();
            }
            return new DietEffect(this.conditions, this.attributes, this.statusEffects, this.uuid);
        }
    }
}
