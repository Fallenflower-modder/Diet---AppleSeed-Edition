package net.appleseed.appleseed.api.type;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface IDietTracker {

    void tick();

    void initSuite();

    void consume(ItemStack stack);

    void consume(ItemStack stack, int healing, float saturationModifier);

    void consume(List<ItemStack> stacks, int healing, float saturationModifier);

    float getValue(String group);

    void setValue(String group, float amount);

    Map<String, Float> getValues();

    void setValues(Map<String, Float> groups);

    String getSuite();

    void setSuite(String name);

    Map<Holder<Attribute>, Set<UUID>> getModifiers();

    void setModifiers(Map<Holder<Attribute>, Set<UUID>> modifiers);

    boolean isActive();

    void setActive(boolean active);

    Player getPlayer();

    void sync();

    void captureStack(ItemStack stack);

    ItemStack getCapturedStack();

    void addEaten(Item item);

    Set<Item> getEaten();

    void setEaten(Set<Item> foods);

    void save(CompoundTag tag);

    void load(CompoundTag tag);

    void copy(Player oldPlayer, boolean wasDeath);
}
