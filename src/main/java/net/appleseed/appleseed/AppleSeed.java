package net.appleseed.appleseed;

import net.appleseed.appleseed.api.type.IDietGroup;
import net.appleseed.appleseed.client.ClientSetup;
import net.appleseed.appleseed.client.DietClientEvents;
import net.appleseed.appleseed.common.capability.DietData;
import net.appleseed.appleseed.common.capability.DietEffects;
import net.appleseed.appleseed.common.config.DietConfig;
import net.appleseed.appleseed.common.data.food.FoodNutritionAutoCalculator;
import net.appleseed.appleseed.common.data.food.FoodNutritionManager;
import net.appleseed.appleseed.common.data.group.DietGroup;
import net.appleseed.appleseed.common.data.group.DietGroups;
import net.appleseed.appleseed.common.data.suite.DietSuites;
import net.appleseed.appleseed.network.SyncDietConfigPacket;
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
import net.neoforged.fml.event.config.ModConfigEvent;
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

import java.util.Map;

@Mod(AppleSeed.MOD_ID)
public class AppleSeed {
    public static final String MOD_ID = "appleseed";

    public static GameRules.Key<GameRules.BooleanValue> RULE_KEEPNUTRITIONS;

    private static final java.util.Map<Player, Integer> prevFoodLevels = new java.util.WeakHashMap<>();
    private static final java.util.Map<java.util.UUID, java.util.Map<String, Float>> deathNutritionCache = new java.util.HashMap<>();

    public AppleSeed(IEventBus bus, ModContainer container) {
        ClientSetup.MENU_TYPES.register(bus);
        bus.addListener(this::commonSetup);
        bus.addListener(this::registerPayloads);
        bus.addListener(this::onConfigReload);
        NeoForge.EVENT_BUS.addListener(this::addReloadListener);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onDatapackSync);
        NeoForge.EVENT_BUS.addListener(this::onItemUseFinish);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(this::onPlayerDeath);
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
            float baseDecay = lost * 0.005f;
            for (IDietGroup group : DietGroups.getGroups(player.level())) {
                float decay = baseDecay * (float) group.getDecayMultiplier();
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
            float baseDecay = 0.001f;
            for (IDietGroup group : DietGroups.getGroups(player.level())) {
                float decay = baseDecay * (float) group.getDecayMultiplier();
                DietData.addValue(player, group.getName(), -decay);
            }
            DietData.syncToClient(player);
        }
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(AppleSeed.MOD_ID);
        registrar.playToClient(SyncDietPacket.TYPE, SyncDietPacket.STREAM_CODEC, SyncDietPacket::handle);
        registrar.playToClient(SyncDietConfigPacket.TYPE, SyncDietConfigPacket.STREAM_CODEC, SyncDietConfigPacket::handle);
    }

    private void onConfigReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getModId().equals(MOD_ID)) {
            DietEffects.clearCache();
        }
    }

    private void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        deathNutritionCache.remove(player.getUUID());

        syncDietConfigToClient((net.minecraft.server.level.ServerPlayer) player);

        for (IDietGroup group : DietGroups.getGroups(player.level())) {
            if (DietData.getValue(player, group.getName()) == 0.0f) {
                float initialValue = group.getDefaultValue();
                if (initialValue == 0.0f) {
                    initialValue = DietConfig.getInitialValue(group.getName());
                }
                DietData.setValue(player, group.getName(), initialValue);
            }
        }
        DietData.syncToClient(player);
    }

    private void syncDietConfigToClient(net.minecraft.server.level.ServerPlayer player) {
        java.util.List<SyncDietConfigPacket.GroupData> groupsData = new java.util.ArrayList<>();
        for (IDietGroup group : DietGroups.getGroups(player.level())) {
            if (group instanceof DietGroup dietGroup) {
                String iconId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(dietGroup.getIcon()).toString();
                groupsData.add(new SyncDietConfigPacket.GroupData(
                        dietGroup.getName(),
                        iconId,
                        dietGroup.getColor().toInt(),
                        dietGroup.getDefaultValue(),
                        dietGroup.getOrder(),
                        dietGroup.getGainMultiplier(),
                        dietGroup.getDecayMultiplier(),
                        dietGroup.isBeneficial(),
                        dietGroup.getTranslationKey()
                ));
            }
        }

        Map<String, Map<String, Float>> foodData = new java.util.HashMap<>();
        for (Map.Entry<net.minecraft.world.item.Item, Map<String, Float>> entry : FoodNutritionManager.INSTANCE.getAllFoodData().entrySet()) {
            String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(entry.getKey()).toString();
            foodData.put(itemId, entry.getValue());
        }

        SyncDietConfigPacket packet = new SyncDietConfigPacket(groupsData, foodData);
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, packet);
    }

    private void onPlayerDeath(final LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof Player player) {
            if (player.level().getGameRules().getBoolean(RULE_KEEPNUTRITIONS)) {
                java.util.Map<String, Float> nutritionValues = new java.util.HashMap<>();
                for (IDietGroup group : DietGroups.getGroups(player.level())) {
                    nutritionValues.put(group.getName(), DietData.getValue(player, group.getName()));
                }
                deathNutritionCache.put(player.getUUID(), nutritionValues);
            } else {
                deathNutritionCache.remove(player.getUUID());
            }
        }
    }

    private void onPlayerRespawn(final PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        if (player.level().getGameRules().getBoolean(RULE_KEEPNUTRITIONS)) {
            java.util.Map<String, Float> savedNutrition = deathNutritionCache.get(player.getUUID());
            if (savedNutrition != null) {
                for (java.util.Map.Entry<String, Float> entry : savedNutrition.entrySet()) {
                    DietData.setValue(player, entry.getKey(), entry.getValue());
                }
                deathNutritionCache.remove(player.getUUID());
            }
        } else {
            for (IDietGroup group : DietGroups.getGroups(player.level())) {
                float initialValue = group.getDefaultValue();
                if (initialValue == 0.0f) {
                    initialValue = DietConfig.getInitialValue(group.getName());
                }
                DietData.setValue(player, group.getName(), initialValue);
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
            event.getPlayerList().getServer().execute(() -> {
                for (net.minecraft.server.level.ServerPlayer player : event.getPlayerList().getPlayers()) {
                    initNewNutrientsForPlayer(player);
                    syncDietConfigToClient(player);
                }
            });
        } else {
            net.minecraft.server.level.ServerPlayer player = event.getPlayer();
            initNewNutrientsForPlayer(player);
            syncDietConfigToClient(player);
        }
    }

    private void initNewNutrientsForPlayer(net.minecraft.server.level.ServerPlayer player) {
        for (IDietGroup group : DietGroups.getGroups(player.level())) {
            if (DietData.getValue(player, group.getName()) == 0.0f) {
                float initialValue = group.getDefaultValue();
                if (initialValue == 0.0f) {
                    initialValue = DietConfig.getInitialValue(group.getName());
                }
                DietData.setValue(player, group.getName(), initialValue);
            }
        }
        DietData.syncToClient(player);
    }

    private void addReloadListener(final AddReloadListenerEvent event) {
        event.addListener(DietGroups.SERVER);
        event.addListener(DietSuites.SERVER);
        event.addListener(FoodNutritionManager.INSTANCE);
        event.addListener(FoodNutritionManager.CLIENT);
        DietEffects.clearCache();
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
