package net.appleseed.appleseed.network;

import io.netty.buffer.ByteBuf;
import net.appleseed.appleseed.AppleSeed;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
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

    private static volatile MethodHandle handlePacket;

    private static MethodHandle getHandler() {
        if (handlePacket == null && FMLEnvironment.dist.isClient()) {
            synchronized (SyncDietPacket.class) {
                if (handlePacket == null) {
                    try {
                        Class<?> clazz = Class.forName("net.appleseed.appleseed.network.ClientPacketHandler");
                        handlePacket = MethodHandles.lookup().findStatic(clazz, "handlePacket",
                                MethodType.methodType(void.class, SyncDietPacket.class, IPayloadContext.class));
                    } catch (Throwable e) {
                        handlePacket = null;
                    }
                }
            }
        }
        return handlePacket;
    }

    public static void handle(final SyncDietPacket packet, final IPayloadContext context) {
        if (FMLEnvironment.dist.isClient()) {
            MethodHandle handler = getHandler();
            if (handler != null) {
                try {
                    handler.invokeExact(packet, context);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
