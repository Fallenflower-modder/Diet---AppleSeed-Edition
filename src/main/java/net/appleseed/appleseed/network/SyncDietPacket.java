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

public record SyncDietPacket(float grains, float fruits, float vegetables, float proteins, float sugars) implements CustomPacketPayload {

    public static final Type<SyncDietPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AppleSeed.MOD_ID, "sync_diet"));

    public static final StreamCodec<ByteBuf, SyncDietPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SyncDietPacket::grains,
            ByteBufCodecs.FLOAT, SyncDietPacket::fruits,
            ByteBufCodecs.FLOAT, SyncDietPacket::vegetables,
            ByteBufCodecs.FLOAT, SyncDietPacket::proteins,
            ByteBufCodecs.FLOAT, SyncDietPacket::sugars,
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
                DietData.setValue(player, "grains", packet.grains());
                DietData.setValue(player, "fruits", packet.fruits());
                DietData.setValue(player, "vegetables", packet.vegetables());
                DietData.setValue(player, "proteins", packet.proteins());
                DietData.setValue(player, "sugars", packet.sugars());
            }
        });
    }
}
