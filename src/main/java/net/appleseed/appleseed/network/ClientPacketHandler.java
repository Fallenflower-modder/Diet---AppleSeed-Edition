package net.appleseed.appleseed.network;

import net.appleseed.appleseed.common.capability.DietData;
import net.appleseed.appleseed.common.data.ServerDietConfig;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;

public class ClientPacketHandler {

    @OnlyIn(Dist.CLIENT)
    public static void handlePacket(final SyncDietPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                for (Map.Entry<String, Float> entry : packet.values().entrySet()) {
                    DietData.setValue(player, entry.getKey(), entry.getValue());
                }
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public static void handleConfigPacket(final SyncDietConfigPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerDietConfig.setServerGroups(packet.groups());
            ServerDietConfig.setServerFoodData(packet.foodData());
        });
    }
}
