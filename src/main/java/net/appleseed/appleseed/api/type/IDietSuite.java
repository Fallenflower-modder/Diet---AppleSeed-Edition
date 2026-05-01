package net.appleseed.appleseed.api.type;

import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.Set;

public interface IDietSuite {

    String getName();

    Set<IDietGroup> getGroups();

    List<IDietEffect> getEffects();

    CompoundTag save();
}
