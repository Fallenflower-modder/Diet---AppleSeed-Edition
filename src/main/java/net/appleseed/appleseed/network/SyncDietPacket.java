package net.appleseed.appleseed.network;

import io.netty.buffer.ByteBuf;
import net.appleseed.appleseed.AppleSeed;
import net.appleseed.appleseed.common.capability.DietData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record SyncDietPacket(Map<String, Float> values) implements CustomPacketPayload {

    public static final Type<SyncDietPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AppleSeed.MOD_ID, "sync_diet"));

    public static final StreamCodec<ByteBuf, SyncDietPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(
                    HashMap::new,
                    ByteBufCodecs.STRING_UTF8,
                    ByteBufCodecs.FLOAT
            ), SyncDietPacket::values,
            SyncDietPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final SyncDietPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                for (Map.Entry<String, Float> entry : packet.values().entrySet()) {
                    DietData.setValue(player, entry.getKey(), entry.getValue());
                }
            }
        });
    }
}
