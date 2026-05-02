package net.appleseed.appleseed.common.capability;

import net.appleseed.appleseed.api.type.IDietGroup;
import net.appleseed.appleseed.common.data.group.DietGroups;
import net.appleseed.appleseed.network.SyncDietPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DietData {

    private static final String TAG_KEY = "appleseed_diet";

    public static CompoundTag getDietTag(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(TAG_KEY)) {
            persistent.put(TAG_KEY, new CompoundTag());
        }
        return persistent.getCompound(TAG_KEY);
    }

    public static float getValue(Player player, String group) {
        CompoundTag tag = getDietTag(player);
        if (tag.contains(group)) {
            return tag.getFloat(group);
        }
        return 0.0f;
    }

    public static void setValue(Player player, String group, float value) {
        CompoundTag tag = getDietTag(player);
        tag.putFloat(group, Math.clamp(value, 0.0f, 1.0f));
    }

    public static void addValue(Player player, String group, float value) {
        IDietGroup groupObj = DietGroups.getGroup(player.level(), group).orElse(null);
        float multiplier = groupObj != null ? (float) groupObj.getGainMultiplier() : 1.0f;
        float current = getValue(player, group);
        setValue(player, group, current + value * multiplier);
    }

    public static Map<String, Float> getAllValues(Player player) {
        Map<String, Float> values = new HashMap<>();
        CompoundTag tag = getDietTag(player);
        Set<String> keys = tag.getAllKeys();
        for (String key : keys) {
            values.put(key, tag.getFloat(key));
        }
        return values;
    }

    public static void syncToClient(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            Map<String, Float> allValues = new HashMap<>();
            for (IDietGroup group : DietGroups.getGroups(player.level())) {
                allValues.put(group.getName(), getValue(player, group.getName()));
            }
            SyncDietPacket packet = new SyncDietPacket(allValues);
            PacketDistributor.sendToPlayer(serverPlayer, packet);
        }
    }
}

