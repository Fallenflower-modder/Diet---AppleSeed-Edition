package net.appleseed.appleseed.network;

import io.netty.buffer.ByteBuf;
import net.appleseed.appleseed.AppleSeed;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public record OpenDietScreenPacket() implements CustomPacketPayload {

    public static final Type<OpenDietScreenPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AppleSeed.MOD_ID, "open_diet_screen"));

    public static final StreamCodec<ByteBuf, OpenDietScreenPacket> STREAM_CODEC =
            StreamCodec.unit(new OpenDietScreenPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static volatile MethodHandle handlePacket;

    private static MethodHandle getHandler() {
        if (handlePacket == null && FMLEnvironment.dist.isClient()) {
            synchronized (OpenDietScreenPacket.class) {
                if (handlePacket == null) {
                    try {
                        Class<?> clazz = Class.forName("net.appleseed.appleseed.network.ClientPacketHandler");
                        handlePacket = MethodHandles.lookup().findStatic(clazz, "handleOpenScreen",
                                MethodType.methodType(void.class, OpenDietScreenPacket.class, IPayloadContext.class));
                    } catch (Throwable e) {
                        handlePacket = null;
                    }
                }
            }
        }
        return handlePacket;
    }

    public static void handle(final OpenDietScreenPacket packet, final IPayloadContext context) {
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