package net.appleseed.appleseed;

import net.appleseed.appleseed.api.type.IDietGroup;
import net.appleseed.appleseed.client.ClientSetup;
import net.appleseed.appleseed.client.DietClientEvents;
import net.appleseed.appleseed.common.capability.DietData;
import net.appleseed.appleseed.common.capability.DietEffects;
import net.appleseed.appleseed.common.config.DietConfig;
import net.appleseed.appleseed.common.data.food.FoodNutritionAutoCalculator;
import net.appleseed.appleseed.common.data.food.FoodNutritionManager;
import net.appleseed.appleseed.common.data.group.DietGroups;
import net.appleseed.appleseed.common.data.suite.DietSuites;
import net.appleseed.appleseed.network.SyncDietPacket;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(AppleSeed.MOD_ID)
public class AppleSeed {
    public static final String MOD_ID = "appleseed";

    public static GameRules.Key<GameRules.BooleanValue> RULE_KEEPNUTRITIONS;

    private static final java.util.Map<Player, Integer> prevFoodLevels = new java.util.WeakHashMap<>();

    public AppleSeed(IEventBus bus, ModContainer container) {
        ClientSetup.MENU_TYPES.register(bus);
        bus.addListener(this::commonSetup);
        bus.addListener(this::registerPayloads);
        NeoForge.EVENT_BUS.addListener(this::addReloadListener);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onDatapackSync);
        NeoForge.EVENT_BUS.addListener(this::onItemUseFinish);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(this::onPlayerRespawn);
        NeoForge.EVENT_BUS.addListener(this::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(this::onPlayerHurt);

        container.registerConfig(ModConfig.Type.COMMON, DietConfig.SPEC);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.register(DietClientEvents.class);
        }
    }

    private void onPlayerTick(final PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

        int currentFood = player.getFoodData().getFoodLevel();
        Integer prevFood = prevFoodLevels.get(player);
        if (prevFood != null && currentFood < prevFood) {
            int lost = prevFood - currentFood;
            float decay = lost * 0.005f;
            for (IDietGroup group : DietGroups.getGroups(player.level())) {
                DietData.addValue(player, group.getName(), -decay);
            }
            DietData.syncToClient(player);
        }
        prevFoodLevels.put(player, currentFood);

        if (player.tickCount % 20 == 0) {
            DietEffects.applyEffects(player);
        }
    }

    private void onPlayerHurt(final LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof Player player) {
            float decay = 0.001f;
            for (IDietGroup group : DietGroups.getGroups(player.level())) {
                DietData.addValue(player, group.getName(), -decay);
            }
            DietData.syncToClient(player);
        }
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(AppleSeed.MOD_ID);
        registrar.playToClient(SyncDietPacket.TYPE, SyncDietPacket.STREAM_CODEC, SyncDietPacket::handle);
    }

    private void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        for (IDietGroup group : DietGroups.getGroups(player.level())) {
            if (DietData.getValue(player, group.getName()) == 0.0f) {
                DietData.setValue(player, group.getName(), DietConfig.getInitialValue(group.getName()));
            }
        }
        DietData.syncToClient(player);
    }

    private void onPlayerRespawn(final PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        if (!player.level().getGameRules().getBoolean(RULE_KEEPNUTRITIONS)) {
            for (IDietGroup group : DietGroups.getGroups(player.level())) {
                DietData.setValue(player, group.getName(), DietConfig.getInitialValue(group.getName()));
            }
        }
        DietData.syncToClient(player);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        RULE_KEEPNUTRITIONS = GameRules.register("keepNutritions", GameRules.Category.PLAYER,
                GameRules.BooleanValue.create(false));
        FoodNutritionAutoCalculator.ensureConfigDir();
        AppleSeedConstants.LOG.info("AppleSeed initialized!");
    }

    private void onServerStarting(final ServerStartingEvent event) {
        FoodNutritionAutoCalculator.calculateAllAsync(event.getServer());
    }

    private void onDatapackSync(final OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            FoodNutritionAutoCalculator.calculateAllAsync(event.getPlayerList().getServer(), true);
        }
    }

    private void addReloadListener(final AddReloadListenerEvent event) {
        event.addListener(DietGroups.SERVER);
        event.addListener(DietSuites.SERVER);
        event.addListener(FoodNutritionManager.INSTANCE);
        event.addListener(FoodNutritionManager.CLIENT);
    }

    private void onItemUseFinish(final LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        if (entity instanceof Player player) {
            ItemStack stack = event.getItem();
            if (stack.getFoodProperties(player) != null) {
                for (IDietGroup group : DietGroups.getGroups(player.level())) {
                    float gain = FoodNutritionManager.INSTANCE.getNutritionValue(stack.getItem(), group.getName());
                    if (gain > 0) {
                        DietData.addValue(player, group.getName(), gain);
                    }
                }
                DietData.syncToClient(player);
            }
        }
    }
}
