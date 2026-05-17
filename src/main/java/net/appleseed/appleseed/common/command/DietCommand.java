package net.appleseed.appleseed.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.appleseed.appleseed.AppleSeedConstants;
import net.appleseed.appleseed.api.type.IDietGroup;
import net.appleseed.appleseed.common.capability.DietData;
import net.appleseed.appleseed.common.capability.DietEffects;
import net.appleseed.appleseed.common.config.DietConfig;
import net.appleseed.appleseed.common.data.food.FoodNutritionAutoCalculator;
import net.appleseed.appleseed.common.data.food.FoodNutritionManager;
import net.appleseed.appleseed.common.data.group.DietGroups;
import net.appleseed.appleseed.network.SyncDietConfigPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = AppleSeedConstants.MOD_ID)
public class DietCommand {

    private static final Path CACHE_DIR = FMLPaths.CONFIGDIR.get().resolve("apple_seed_foods");

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
            Commands.literal("diet")
                .then(Commands.literal("nutritions")
                    .then(Commands.literal("query")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                            .executes(DietCommand::queryNutrition)))
                    .then(Commands.literal("set")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                            .then(Commands.argument("nutrition", StringArgumentType.word())
                                .suggests(DietCommand::suggestNutritionIds)
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                                    .executes(DietCommand::setNutrition))))))
                .then(Commands.literal("config")
                    .then(Commands.literal("set")
                        .then(Commands.literal("ignoreHunger")
                            .requires(source -> source.hasPermission(2))
                            .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(DietCommand::setIgnoreHunger)))))
                .then(Commands.literal("cache")
                    .then(Commands.literal("clear")
                        .requires(source -> source.hasPermission(4))
                        .executes(DietCommand::clearCache))
                    .then(Commands.literal("regenerate")
                        .requires(source -> source.hasPermission(4))
                        .executes(DietCommand::regenerateCache))
                    .then(Commands.literal("reload")
                        .requires(source -> source.hasPermission(2))
                        .executes(DietCommand::reloadCache)))
        );
    }

    private static int queryNutrition(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
            CommandSourceStack source = ctx.getSource();

            MutableComponent header = Component.translatable("command.appleseed.query.header", target.getName().getString());
            source.sendSuccess(() -> header, false);

            boolean hasAny = false;
            for (IDietGroup group : DietGroups.getGroups(target.level())) {
                float value = DietData.getValue(target, group.getName());
                int color = group.getColor().toInt();
                hasAny = true;

                MutableComponent line = Component.literal(
                    String.format("  %s: %.1f%%",
                        Component.translatable(group.getTranslationKey()).getString(),
                        value * 100)
                ).withStyle(style -> style.withColor(color));

                source.sendSuccess(() -> line, false);
            }

            if (!hasAny) {
                source.sendSuccess(() -> Component.translatable("command.appleseed.query.empty"), false);
            }

            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int setNutrition(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
            String nutritionId = StringArgumentType.getString(ctx, "nutrition");
            float value = (float) DoubleArgumentType.getDouble(ctx, "value");

            var groupOpt = DietGroups.getGroup(target.level(), nutritionId);
            if (groupOpt.isEmpty()) {
                ctx.getSource().sendFailure(
                    Component.translatable("command.appleseed.set.invalid_group", nutritionId));
                return 0;
            }

            DietData.setValue(target, nutritionId, value);
            DietData.syncToClient(target);

            ctx.getSource().sendSuccess(() ->
                Component.translatable("command.appleseed.set.success",
                    target.getName().getString(), nutritionId, String.format("%.1f%%", value * 100)), true);

            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static CompletableFuture<Suggestions> suggestNutritionIds(
            CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        var level = ctx.getSource().getLevel();
        for (IDietGroup group : DietGroups.getGroups(level)) {
            builder.suggest(group.getName());
        }
        return builder.buildFuture();
    }

    private static int setIgnoreHunger(CommandContext<CommandSourceStack> ctx) {
        try {
            boolean value = BoolArgumentType.getBool(ctx, "value");
            DietConfig.INSTANCE.ignoreHunger.set(value);
            saveDietConfig();
            ctx.getSource().sendSuccess(() ->
                Component.translatable("command.appleseed.config.ignoreHunger.set", value), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int clearCache(CommandContext<CommandSourceStack> ctx) {
        try {
            File cacheDir = CACHE_DIR.toFile();
            if (cacheDir.exists() && cacheDir.isDirectory()) {
                deleteRecursively(cacheDir);
                ctx.getSource().sendSuccess(() ->
                    Component.translatable("command.appleseed.cache.cleared"), true);
            } else {
                ctx.getSource().sendSuccess(() ->
                    Component.translatable("command.appleseed.cache.no_cache"), false);
            }
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int regenerateCache(CommandContext<CommandSourceStack> ctx) {
        try {
            File cacheDir = CACHE_DIR.toFile();
            if (cacheDir.exists() && cacheDir.isDirectory()) {
                deleteRecursively(cacheDir);
            }

            var server = ctx.getSource().getServer();
            FoodNutritionAutoCalculator.calculateAllAsync(server, true);

            ctx.getSource().sendSuccess(() ->
                Component.translatable("command.appleseed.cache.regenerating"), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int reloadCache(CommandContext<CommandSourceStack> ctx) {
        try {
            FoodNutritionManager.INSTANCE.reloadConfigFiles();
            FoodNutritionManager.CLIENT.reloadConfigFiles();
            DietEffects.clearCache();

            var server = ctx.getSource().getServer();
            if (server != null) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    String iconId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(
                        net.minecraft.world.item.Items.APPLE).toString();
                    java.util.List<SyncDietConfigPacket.GroupData> groupsData = new java.util.ArrayList<>();
                    for (IDietGroup group : DietGroups.getGroups(player.level())) {
                        if (group instanceof net.appleseed.appleseed.common.data.group.DietGroup dietGroup) {
                            iconId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(dietGroup.getIcon()).toString();
                            groupsData.add(new SyncDietConfigPacket.GroupData(
                                dietGroup.getName(), iconId, dietGroup.getColor().toInt(),
                                dietGroup.getDefaultValue(), dietGroup.getOrder(),
                                dietGroup.getGainMultiplier(), dietGroup.getDecayMultiplier(),
                                dietGroup.isBeneficial(), dietGroup.getTranslationKey()));
                        }
                    }
                    java.util.Map<String, java.util.Map<String, Float>> foodData = new java.util.HashMap<>();
                    for (java.util.Map.Entry<net.minecraft.world.item.Item, java.util.Map<String, Float>> entry :
                            FoodNutritionManager.INSTANCE.getAllFoodData().entrySet()) {
                        String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(entry.getKey()).toString();
                        foodData.put(itemId, entry.getValue());
                    }
                    SyncDietConfigPacket packet = new SyncDietConfigPacket(groupsData, foodData);
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, packet);
                }
            }

            ctx.getSource().sendSuccess(() ->
                Component.translatable("command.appleseed.cache.reloaded"), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }

    private static void saveDietConfig() {
        try {
            java.nio.file.Path configPath = FMLPaths.CONFIGDIR.get().resolve("appleseed-common.toml");
            if (java.nio.file.Files.exists(configPath)) {
                java.util.List<String> lines = java.nio.file.Files.readAllLines(configPath);
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).matches("\\s*\"?ignore_hunger\"?\\s*=.*")) {
                        lines.set(i, "\tignore_hunger = " + DietConfig.INSTANCE.ignoreHunger.get());
                        break;
                    }
                }
                java.nio.file.Files.write(configPath, lines);
            }
        } catch (Exception e) {
            AppleSeedConstants.LOG.error("Failed to save diet config", e);
        }
    }
}
