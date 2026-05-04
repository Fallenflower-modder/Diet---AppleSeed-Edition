package net.appleseed.appleseed.network;

import io.netty.buffer.ByteBuf;
import net.appleseed.appleseed.AppleSeed;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

public record SyncDietConfigPacket(List<GroupData> groups, Map<String, Map<String, Float>> foodData) implements CustomPacketPayload {

    public static final Type<SyncDietConfigPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AppleSeed.MOD_ID, "sync_diet_config"));

    public static final StreamCodec<ByteBuf, GroupData> GROUP_DATA_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public GroupData decode(ByteBuf buf) {
            return new GroupData(
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.DOUBLE.decode(buf),
                    ByteBufCodecs.DOUBLE.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf)
            );
        }

        @Override
        public void encode(ByteBuf buf, GroupData data) {
            ByteBufCodecs.STRING_UTF8.encode(buf, data.name());
            ByteBufCodecs.STRING_UTF8.encode(buf, data.iconId());
            ByteBufCodecs.INT.encode(buf, data.color());
            ByteBufCodecs.FLOAT.encode(buf, data.defaultValue());
            ByteBufCodecs.INT.encode(buf, data.order());
            ByteBufCodecs.DOUBLE.encode(buf, data.gainMultiplier());
            ByteBufCodecs.DOUBLE.encode(buf, data.decayMultiplier());
            ByteBufCodecs.BOOL.encode(buf, data.beneficial());
            ByteBufCodecs.STRING_UTF8.encode(buf, data.translationKey());
        }
    };

    private static final StreamCodec<ByteBuf, Map<String, Float>> NESTED_MAP_CODEC = ByteBufCodecs.map(
            HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.FLOAT
    );

    private static final StreamCodec<ByteBuf, Map<String, Map<String, Float>>> FOOD_DATA_CODEC = ByteBufCodecs.map(
            HashMap::new, ByteBufCodecs.STRING_UTF8, NESTED_MAP_CODEC
    );

    private static final IntFunction<List<GroupData>> LIST_FACTORY = ArrayList::new;

    public static final StreamCodec<ByteBuf, SyncDietConfigPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SyncDietConfigPacket decode(ByteBuf buf) {
            int size = ByteBufCodecs.VAR_INT.decode(buf);
            List<GroupData> groups = LIST_FACTORY.apply(size);
            for (int i = 0; i < size; i++) {
                groups.add(GROUP_DATA_STREAM_CODEC.decode(buf));
            }
            Map<String, Map<String, Float>> foodData = FOOD_DATA_CODEC.decode(buf);
            return new SyncDietConfigPacket(groups, foodData);
        }

        @Override
        public void encode(ByteBuf buf, SyncDietConfigPacket packet) {
            ByteBufCodecs.VAR_INT.encode(buf, packet.groups().size());
            for (GroupData group : packet.groups()) {
                GROUP_DATA_STREAM_CODEC.encode(buf, group);
            }
            FOOD_DATA_CODEC.encode(buf, packet.foodData());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static volatile MethodHandle handlePacket;

    private static MethodHandle getHandler() {
        if (handlePacket == null && FMLEnvironment.dist.isClient()) {
            synchronized (SyncDietConfigPacket.class) {
                if (handlePacket == null) {
                    try {
                        Class<?> clazz = Class.forName("net.appleseed.appleseed.network.ClientPacketHandler");
                        handlePacket = MethodHandles.lookup().findStatic(clazz, "handleConfigPacket",
                                MethodType.methodType(void.class, SyncDietConfigPacket.class, IPayloadContext.class));
                    } catch (Throwable e) {
                        handlePacket = null;
                    }
                }
            }
        }
        return handlePacket;
    }

    public static void handle(final SyncDietConfigPacket packet, final IPayloadContext context) {
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

    public static Item getItemFromId(String itemId) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id != null && BuiltInRegistries.ITEM.containsKey(id)) {
            return BuiltInRegistries.ITEM.get(id);
        }
        return Items.APPLE;
    }

    public record GroupData(String name, String iconId, int color, float defaultValue, int order,
                            double gainMultiplier, double decayMultiplier, boolean beneficial,
                            String translationKey) {
        public Map<String, Object> toMap() {
            Map<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("iconId", iconId);
            data.put("color", color);
            data.put("defaultValue", defaultValue);
            data.put("order", order);
            data.put("gainMultiplier", gainMultiplier);
            data.put("decayMultiplier", decayMultiplier);
            data.put("beneficial", beneficial);
            data.put("translationKey", translationKey);
            return data;
        }
    }
}
