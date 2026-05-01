package net.appleseed.appleseed.common.capability;

import net.appleseed.appleseed.network.SyncDietPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

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
        float current = getValue(player, group);
        setValue(player, group, current + value);
    }

    public static void syncToClient(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            SyncDietPacket packet = new SyncDietPacket(
                    getValue(player, "grains"),
                    getValue(player, "fruits"),
                    getValue(player, "vegetables"),
                    getValue(player, "proteins"),
                    getValue(player, "sugars")
            );
            PacketDistributor.sendToPlayer(serverPlayer, packet);
        }
    }
}
