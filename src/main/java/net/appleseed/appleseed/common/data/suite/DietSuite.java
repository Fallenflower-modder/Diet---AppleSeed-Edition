package net.appleseed.appleseed.common.data.suite;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.appleseed.appleseed.api.type.IDietEffect;
import net.appleseed.appleseed.api.type.IDietGroup;
import net.appleseed.appleseed.api.type.IDietSuite;
import net.appleseed.appleseed.common.data.group.DietGroup;
import net.minecraft.nbt.CompoundTag;

import java.util.*;

public final class DietSuite implements IDietSuite {

    private final String name;
    private final Set<IDietGroup> groups;
    private final List<IDietEffect> effects;

    private DietSuite(String name, Set<IDietGroup> groups, List<IDietEffect> effects) {
        this.name = name;
        TreeSet<IDietGroup> sorted = new TreeSet<>(
                Comparator.comparingInt(IDietGroup::getOrder).thenComparing(IDietGroup::getName));
        sorted.addAll(groups);
        this.groups = ImmutableSet.copyOf(sorted);
        this.effects = ImmutableList.copyOf(effects);
    }

    public static IDietSuite load(CompoundTag tag) {
        Set<IDietGroup> set = new HashSet<>();
        CompoundTag groups = tag.getCompound("Groups");

        for (String key : groups.getAllKeys()) {
            set.add(DietGroup.load(groups.getCompound(key)));
        }
        return new DietSuite(tag.getString("Name"), set, new ArrayList<>());
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Set<IDietGroup> getGroups() {
        return this.groups;
    }

    @Override
    public List<IDietEffect> getEffects() {
        return this.effects;
    }

    @Override
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Name", this.name);
        CompoundTag groups = new CompoundTag();

        for (IDietGroup group : this.groups) {
            groups.put(group.getName(), group.save());
        }
        tag.put("Groups", groups);
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DietSuite dietSuite = (DietSuite) o;
        return Objects.equals(name, dietSuite.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public static class Builder {

        private final String name;
        private final Set<IDietGroup> groups = new HashSet<>();
        private final List<IDietEffect> effects = new ArrayList<>();

        public Builder(String name) {
            this.name = name;
        }

        public Builder addGroup(IDietGroup group) {
            this.groups.add(group);
            return this;
        }

        public Builder addEffect(IDietEffect effect) {
            this.effects.add(effect);
            return this;
        }

        public IDietSuite build() {
            return new DietSuite(this.name, this.groups, this.effects);
        }
    }
}
