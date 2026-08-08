package net.appleseed.appleseed;

import net.appleseed.appleseed.api.hook.DietHookRegistry;
import net.appleseed.appleseed.api.query.DietDecayQuery;
import net.appleseed.appleseed.api.query.DietQuery;
import net.appleseed.appleseed.api.query.IDietDecayQuery;
import net.appleseed.appleseed.api.type.IDietGroup;
import net.appleseed.appleseed.common.registry.ModMenuTypes;
import net.appleseed.appleseed.client.DietClientEvents;
import net.appleseed.appleseed.common.event.BlockFoodEventHandler;
import net.appleseed.appleseed.common.capability.DietData;
import net.appleseed.appleseed.common.capability.DietEffects;
import net.appleseed.appleseed.common.config.DietConfig;
import net.appleseed.appleseed.common.data.food.FoodNutritionAutoCalculator;
import net.appleseed.appleseed.common.data.food.FoodNutritionManager;
import net.appleseed.appleseed.common.data.group.DietGroup;
import net.appleseed.appleseed.common.data.group.DietGroups;
import net.appleseed.appleseed.common.recipe.RecipeRegistry;
import net.appleseed.appleseed.common.data.suite.DietSuites;
import net.appleseed.appleseed.compat.SandwichCompat;
import net.appleseed.appleseed.network.OpenDietScreenPacket;
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
import net.neoforged.fml.config.ConfigTracker;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod(AppleSeed.MOD_ID)
public class AppleSeed {
    public static final String MOD_ID = "appleseed";

    public static GameRules.Key<GameRules.BooleanValue> RULE_KEEPNUTRITIONS;
    public static GameRules.Key<GameRules.IntegerValue> RULE_DECAY_BY_HIT;
    public static GameRules.Key<GameRules.IntegerValue> RULE_DECAY_BY_HUNGER;
    public static GameRules.Key<GameRules.IntegerValue> RULE_DECAY_BY_SATURATION;

    /** 游戏规则以整数存储，实际系数 = 整数值 / DECAY_MULTIPLIER_SCALE */
    public static final int DECAY_MULTIPLIER_SCALE = 1_000_000;

    private static final java.util.Map<Player, Integer> prevFoodLevels = new java.util.WeakHashMap<>();
    private static final java.util.Map<Player, Float> prevSaturationLevels = new java.util.WeakHashMap<>();
    private static final java.util.Map<java.util.UUID, java.util.Map<String, Float>> deathNutritionCache = new java.util.HashMap<>();

    /** 配置文件引用，用于版本迁移 */
    private static ModConfig commonConfig;

    public AppleSeed(IEventBus bus, ModContainer container) {
        bus.register(RecipeRegistry.class);
        ModMenuTypes.MENU_TYPES.register(bus);
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
        NeoForge.EVENT_BUS.register(BlockFoodEventHandler.class);

        commonConfig = ConfigTracker.INSTANCE.registerConfig(ModConfig.Type.COMMON, DietConfig.SPEC, container);

        DietQuery.setInstance(FoodNutritionManager.INSTANCE);
        DietDecayQuery.setInstance(new IDietDecayQuery() {
            @Override
            public double getHitDecayMultiplier(Player player) {
                return DietConfig.getEffectiveHitDecayMultiplier(
                        player.level().getGameRules().getRule(RULE_DECAY_BY_HIT).get()) / (double) DECAY_MULTIPLIER_SCALE;
            }

            @Override
            public double getHungerDecayMultiplier(Player player) {
                return DietConfig.getEffectiveHungerDecayMultiplier(
                        player.level().getGameRules().getRule(RULE_DECAY_BY_HUNGER).get()) / (double) DECAY_MULTIPLIER_SCALE;
            }

            @Override
            public double getSaturationDecayMultiplier(Player player) {
                return DietConfig.getEffectiveSaturationDecayMultiplier(
                        player.level().getGameRules().getRule(RULE_DECAY_BY_SATURATION).get()) / (double) DECAY_MULTIPLIER_SCALE;
            }
        });

        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.register(DietClientEvents.class);
        }
    }

    private void onPlayerTick(final PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

        boolean changed = false;

        // --- 饥饿衰减渠道 ---
        int currentFood = player.getFoodData().getFoodLevel();
        Integer prevFood = prevFoodLevels.get(player);
        if (prevFood != null && currentFood < prevFood) {
            int lost = prevFood - currentFood;
            double hungerMultiplier = DietConfig.getEffectiveHungerDecayMultiplier(
                    player.level().getGameRules().getRule(RULE_DECAY_BY_HUNGER).get()) / (double) DECAY_MULTIPLIER_SCALE;
            float baseDecay = lost * (float) hungerMultiplier;
            for (IDietGroup group : DietGroups.getGroups(player.level())) {
                if (DietConfig.isGroupIgnoreHunger(group.getName(), group.ignoreHunger())) {
                    continue;
                }
                float decay = baseDecay * (float) group.getDecayMultiplier();
                decay = DietHookRegistry.processBeforeDecay(player, group.getName(), decay);
                if (decay > 0) {
                    float oldValue = DietData.getValue(player, group.getName());
                    DietData.addValue(player, group.getName(), -decay);
                    DietHookRegistry.processAfterChange(player, group.getName(), oldValue, DietData.getValue(player, group.getName()));
                    changed = true;
                }
            }
        }
        prevFoodLevels.put(player, currentFood);

        // --- 饱和度衰减渠道 ---
        float currentSaturation = player.getFoodData().getSaturationLevel();
        Float prevSaturation = prevSaturationLevels.get(player);
        if (prevSaturation != null && currentSaturation < prevSaturation) {
            float lost = prevSaturation - currentSaturation;
            double saturationMultiplier = DietConfig.getEffectiveSaturationDecayMultiplier(
                    player.level().getGameRules().getRule(RULE_DECAY_BY_SATURATION).get()) / (double) DECAY_MULTIPLIER_SCALE;
            float baseDecay = lost * (float) saturationMultiplier;
            for (IDietGroup group : DietGroups.getGroups(player.level())) {
                if (DietConfig.isGroupIgnoreSaturation(group.getName(), group.ignoreSaturation())) {
                    continue;
                }
                float decay = baseDecay * (float) group.getDecayMultiplier();
                decay = DietHookRegistry.processBeforeDecay(player, group.getName(), decay);
                if (decay > 0) {
                    float oldValue = DietData.getValue(player, group.getName());
                    DietData.addValue(player, group.getName(), -decay);
                    DietHookRegistry.processAfterChange(player, group.getName(), oldValue, DietData.getValue(player, group.getName()));
                    changed = true;
                }
            }
        }
        prevSaturationLevels.put(player, currentSaturation);

        if (changed) {
            DietData.syncToClient(player);
        }

        if (player.tickCount % 20 == 0) {
            DietEffects.applyEffects(player);
        }
    }

    private void onPlayerHurt(final LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof Player player) {
            double hitMultiplier = DietConfig.getEffectiveHitDecayMultiplier(
                    player.level().getGameRules().getRule(RULE_DECAY_BY_HIT).get()) / (double) DECAY_MULTIPLIER_SCALE;
            float baseDecay = (float) hitMultiplier;
            for (IDietGroup group : DietGroups.getGroups(player.level())) {
                if (DietConfig.isGroupIgnoreAttack(group.getName(), group.ignoreAttack())) {
                    continue;
                }
                float decay = baseDecay * (float) group.getDecayMultiplier();
                decay = DietHookRegistry.processBeforeDecay(player, group.getName(), decay);
                if (decay > 0) {
                    float oldValue = DietData.getValue(player, group.getName());
                    DietData.addValue(player, group.getName(), -decay);
                    DietHookRegistry.processAfterChange(player, group.getName(), oldValue, DietData.getValue(player, group.getName()));
                }
            }
            DietData.syncToClient(player);
        }
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(AppleSeed.MOD_ID);
        registrar.playToClient(SyncDietPacket.TYPE, SyncDietPacket.STREAM_CODEC, SyncDietPacket::handle);
        registrar.playToClient(SyncDietConfigPacket.TYPE, SyncDietConfigPacket.STREAM_CODEC, SyncDietConfigPacket::handle);
        registrar.playToClient(OpenDietScreenPacket.TYPE, OpenDietScreenPacket.STREAM_CODEC, OpenDietScreenPacket::handle);
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
                        dietGroup.isNegative(),
                        dietGroup.ignoreAttack(),
                        dietGroup.ignoreHunger(),
                        dietGroup.ignoreSaturation(),
                        dietGroup.getTranslationKey()
                ));
            }
        }

        Map<String, Map<String, Float>> foodData = new java.util.HashMap<>();
        for (Map.Entry<net.minecraft.world.item.Item, Map<String, Float>> entry : FoodNutritionManager.INSTANCE.getAllFoodData().entrySet()) {
            String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(entry.getKey()).toString();
            foodData.put(itemId, entry.getValue());
        }
        for (Map.Entry<net.minecraft.world.level.block.Block, Map<String, Float>> entry : FoodNutritionManager.INSTANCE.getAllBlockData().entrySet()) {
            net.minecraft.world.item.Item blockItem = entry.getKey().asItem();
            if (blockItem != net.minecraft.world.item.Items.AIR) {
                String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(blockItem).toString();
                foodData.putIfAbsent(itemId, entry.getValue());
            }
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
        RULE_DECAY_BY_HIT = GameRules.register("nutritionDecayByHitMultiplier", GameRules.Category.PLAYER,
                GameRules.IntegerValue.create(DietConfig.getDefaultHitDecayMultiplier()));
        RULE_DECAY_BY_HUNGER = GameRules.register("nutritionDecayByHungerMultiplier", GameRules.Category.PLAYER,
                GameRules.IntegerValue.create(DietConfig.getDefaultHungerDecayMultiplier()));
        RULE_DECAY_BY_SATURATION = GameRules.register("nutritionDecayBySaturationMultiplier", GameRules.Category.PLAYER,
                GameRules.IntegerValue.create(DietConfig.getDefaultSaturationDecayMultiplier()));

        // 配置文件版本迁移检查
        performConfigVersionMigration();

        FoodNutritionAutoCalculator.ensureConfigDir();
        AppleSeedConstants.LOG.info("AppleSeed initialized!");
    }

    private void onServerStarting(final ServerStartingEvent event) {
        AppleSeedConstants.LOG.debug("Server starting, deferring nutrition calculation to datapack sync");
    }

    private void onDatapackSync(final OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            AppleSeedConstants.LOG.debug("DatapackSync (all players): starting nutrition auto-calculation");
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

    /**
     * 检查配置文件版本，若版本不匹配则执行迁移。
     * 迁移流程：读取原始TOML文件检测version字段 → 保存当前内存配置值 → 覆盖写入配置文件。
     */
    private void performConfigVersionMigration() {
        if (commonConfig == null) {
            AppleSeedConstants.LOG.warn("[ConfigMigration] commonConfig is null, skipping version check");
            return;
        }

        String configVersion = readVersionFromConfigFile();
        if (configVersion == null) {
            AppleSeedConstants.LOG.info("[ConfigMigration] No version field found in config file. "
                    + "Performing migration to version {}.", AppleSeedConstants.MOD_VERSION);
            saveConfigFile();
            AppleSeedConstants.LOG.info("[ConfigMigration] Config file has been migrated to version {}",
                    AppleSeedConstants.MOD_VERSION);
        } else if (!AppleSeedConstants.MOD_VERSION.equals(configVersion)) {
            AppleSeedConstants.LOG.info("[ConfigMigration] Config version mismatch: file='{}', mod='{}'. "
                    + "Performing migration.", configVersion, AppleSeedConstants.MOD_VERSION);
            saveConfigFile();
            AppleSeedConstants.LOG.info("[ConfigMigration] Config file has been migrated from version {} to {}",
                    configVersion, AppleSeedConstants.MOD_VERSION);
        }
    }

    /**
     * 从原始TOML配置文件中读取version字段。
     * 返回null表示文件中没有version字段。
     */
    private static String readVersionFromConfigFile() {
        if (commonConfig == null) {
            return null;
        }
        try {
            Path configPath = commonConfig.getFullPath();
            if (!Files.exists(configPath)) {
                return null;
            }
            String content = Files.readString(configPath);
            // 匹配 version = "xxx" 或 version = 'xxx'
            Pattern pattern = Pattern.compile("^\\s*version\\s*=\\s*\"([^\"]*)\"", Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
        } catch (Exception e) {
            AppleSeedConstants.LOG.warn("[ConfigMigration] Failed to read config file version: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将当前配置值写入配置文件（通过 ILoadedConfig.save()）。
     */
    private void saveConfigFile() {
        if (commonConfig == null) {
            AppleSeedConstants.LOG.warn("[ConfigMigration] Cannot save config: commonConfig is null");
            return;
        }
        var loadedConfig = commonConfig.getLoadedConfig();
        if (loadedConfig == null) {
            AppleSeedConstants.LOG.warn("[ConfigMigration] Cannot save config: loadedConfig is null");
            return;
        }
        loadedConfig.save();
    }

    private void onItemUseFinish(final LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        if (entity instanceof Player player) {
            ItemStack stack = event.getItem();
            if (stack.getFoodProperties(player) != null) {
                Map<String, Float> gains = new java.util.LinkedHashMap<>();

                if (DietHookRegistry.shouldInterceptItemFood(player, stack)) {
                    gains = DietHookRegistry.modifyItemFoodGains(player, stack, gains);
                } else if (SandwichCompat.isSandwich(stack)) {
                    gains = SandwichCompat.calculateNutrition(stack, player.level());
                    gains = DietHookRegistry.modifyItemFoodGains(player, stack, gains);
                } else {
                    for (IDietGroup group : DietGroups.getGroups(player.level())) {
                        float gain = FoodNutritionManager.INSTANCE.getNutritionValue(stack.getItem(), group.getName());
                        if (gain > 0) {
                            gains.put(group.getName(), gain);
                        }
                    }
                    gains = DietHookRegistry.modifyItemFoodGains(player, stack, gains);
                }

                for (Map.Entry<String, Float> entry : gains.entrySet()) {
                    if (entry.getValue() > 0) {
                        DietData.addValue(player, entry.getKey(), entry.getValue());
                    }
                }
                DietData.syncToClient(player);
                DietHookRegistry.onAfterItemFoodEat(player, stack);
            }
        }
    }
}
