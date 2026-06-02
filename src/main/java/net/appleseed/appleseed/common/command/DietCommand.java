package net.appleseed.appleseed.common.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.appleseed.appleseed.AppleSeedConstants;
import net.appleseed.appleseed.api.type.IDietGroup;
import net.appleseed.appleseed.common.capability.DietData;
import net.appleseed.appleseed.common.config.DietConfig;
import net.appleseed.appleseed.common.data.food.FoodNutritionManager;
import net.appleseed.appleseed.common.data.group.DietGroups;
import net.appleseed.appleseed.network.OpenDietScreenPacket;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = AppleSeedConstants.MOD_ID)
public class DietCommand {

    private static final Path CACHE_DIR = FMLPaths.CONFIGDIR.get().resolve("apple_seed_foods");
    private static final Path APPLE_SEED_DATA_PATH = FMLPaths.CONFIGDIR.get().resolve("appleseed_data.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        CommandBuildContext buildContext = event.getBuildContext();

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
                                    .executes(DietCommand::setNutrition)))))
                    .then(Commands.literal("add")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                            .then(Commands.argument("nutrition", StringArgumentType.word())
                                .suggests(DietCommand::suggestNutritionIds)
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.0, 1.0))
                                    .executes(DietCommand::addNutrition)))))
                    .then(Commands.literal("remove")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                            .then(Commands.argument("nutrition", StringArgumentType.word())
                                .suggests(DietCommand::suggestNutritionIds)
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.0, 1.0))
                                    .executes(DietCommand::removeNutrition))))))
                .then(Commands.literal("config")
                    .then(Commands.literal("set")
                        .then(Commands.literal("ignoreHunger")
                            .requires(source -> source.hasPermission(2))
                            .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(DietCommand::setIgnoreHunger)
                            )
                        )
                        .then(Commands.literal("entranceVisibility")
                            .requires(source -> source.hasPermission(2))
                            .then(Commands.argument("value", StringArgumentType.word())
                                .suggests(DietCommand::suggestEntranceVisibility)
                                .executes(DietCommand::setEntranceVisibility)
                            )
                        )
                        .then(Commands.literal("craftChainSearchDepth")
                            .requires(source -> source.hasPermission(2))
                            .then(Commands.argument("value", IntegerArgumentType.integer(0, 100))
                                .executes(DietCommand::setCraftChainSearchDepth)
                            )
                        )
                    )
                )
                .then(Commands.literal("cache")
                    .then(Commands.literal("clear")
                        .requires(source -> source.hasPermission(4))
                        .executes(DietCommand::clearCache)
                    )
                    .then(Commands.literal("regenerate")
                        .requires(source -> source.hasPermission(4))
                        .executes(DietCommand::regenerateCache)
                    )
                    .then(Commands.literal("reload")
                        .requires(source -> source.hasPermission(2))
                        .executes(DietCommand::reloadCache)
                    )
                )
                .then(Commands.literal("screen")
                    .executes(DietCommand::openScreen)
                )
                .then(Commands.literal("item")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("nutrition", StringArgumentType.word())
                        .suggests(DietCommand::suggestNutritionIds)
                        .then(Commands.argument("count", DoubleArgumentType.doubleArg(0.0, 1.0))
                            .executes(DietCommand::setItemNutritionFromHand)
                            .then(Commands.argument("item", ItemArgument.item(buildContext))
                                .executes(DietCommand::setItemNutritionFromArg)
                            )
                        )
                    )
                )
                .then(Commands.literal("block")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("nutrition", StringArgumentType.word())
                        .suggests(DietCommand::suggestNutritionIds)
                        .then(Commands.argument("count", DoubleArgumentType.doubleArg(0.0, 1.0))
                            .then(Commands.argument("block", StringArgumentType.word())
                                .suggests(DietCommand::suggestBlockIds)
                                .then(Commands.argument("bites", IntegerArgumentType.integer(1))
                                    .executes(DietCommand::setBlockNutritionWithBites)
                                )
                                .executes(DietCommand::setBlockNutrition)
                            )
                        )
                    )
                )
        );
    }

    private static int queryNutrition(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
            CommandSourceStack source = ctx.getSource();

            MutableComponent header = Component.translatable("command.appleseed.query.header", target.getName());
            source.sendSuccess(() -> header, false);

            boolean hasAny = false;
            for (IDietGroup group : DietGroups.getGroups(target.level())) {
                float value = DietData.getValue(target, group.getName());
                int color = group.getColor().toInt();
                hasAny = true;

                MutableComponent line = Component.translatable(group.getTranslationKey())
                        .append(Component.literal(String.format(": %.1f%%", value * 100)))
                        .withStyle(style -> style.withColor(color));

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

            IDietGroup group = groupOpt.get();
            DietData.setValue(target, nutritionId, value);
            DietData.syncToClient(target);

            ctx.getSource().sendSuccess(() ->
                Component.translatable("command.appleseed.set.success",
                    target.getName(),
                    Component.translatable(group.getTranslationKey()),
                    Component.literal(String.format("%.1f%%", value * 100))), true);

            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int addNutrition(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
            String nutritionId = StringArgumentType.getString(ctx, "nutrition");
            float amount = (float) DoubleArgumentType.getDouble(ctx, "amount");

            var groupOpt = DietGroups.getGroup(target.level(), nutritionId);
            if (groupOpt.isEmpty()) {
                ctx.getSource().sendFailure(
                    Component.translatable("command.appleseed.set.invalid_group", nutritionId));
                return 0;
            }

            IDietGroup group = groupOpt.get();
            float currentValue = DietData.getValue(target, nutritionId);
            float newValue = Math.min(currentValue + amount, 1.0f);
            DietData.setValue(target, nutritionId, newValue);
            DietData.syncToClient(target);

            ctx.getSource().sendSuccess(() ->
                Component.translatable("command.appleseed.add.success",
                    Component.literal(String.format("%.1f%%", amount * 100)),
                    target.getName(),
                    Component.translatable(group.getTranslationKey()),
                    Component.literal(String.format("%.1f%%", newValue * 100))), true);

            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int removeNutrition(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
            String nutritionId = StringArgumentType.getString(ctx, "nutrition");
            float amount = (float) DoubleArgumentType.getDouble(ctx, "amount");

            var groupOpt = DietGroups.getGroup(target.level(), nutritionId);
            if (groupOpt.isEmpty()) {
                ctx.getSource().sendFailure(
                    Component.translatable("command.appleseed.set.invalid_group", nutritionId));
                return 0;
            }

            IDietGroup group = groupOpt.get();
            float currentValue = DietData.getValue(target, nutritionId);
            float newValue = Math.max(currentValue - amount, 0.0f);
            DietData.setValue(target, nutritionId, newValue);
            DietData.syncToClient(target);

            ctx.getSource().sendSuccess(() ->
                Component.translatable("command.appleseed.remove.success",
                    Component.literal(String.format("%.1f%%", amount * 100)),
                    target.getName(),
                    Component.translatable(group.getTranslationKey()),
                    Component.literal(String.format("%.1f%%", newValue * 100))), true);

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

    private static CompletableFuture<Suggestions> suggestEntranceVisibility(
            CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        builder.suggest("invisible");
        builder.suggest("default");
        builder.suggest("ftb_compact");
        return builder.buildFuture();
    }

    private static int openScreen(CommandContext<CommandSourceStack> ctx) {
        try {
            if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, new OpenDietScreenPacket());
                ctx.getSource().sendSuccess(() ->
                        Component.translatable("command.appleseed.screen.opened"), true);
                return 1;
            }
            ctx.getSource().sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int setEntranceVisibility(CommandContext<CommandSourceStack> ctx) {
        try {
            String value = StringArgumentType.getString(ctx, "value");
            DietConfig.EntranceVisibility oldMode = DietConfig.EntranceVisibility.fromString(
                    DietConfig.INSTANCE.entranceVisibility.get());
            DietConfig.EntranceVisibility newMode = DietConfig.EntranceVisibility.fromString(value);
            DietConfig.INSTANCE.entranceVisibility.set(newMode.name().toLowerCase(java.util.Locale.ROOT));
            saveDietConfig();

            if (oldMode == DietConfig.EntranceVisibility.FTB_COMPACT
                    || newMode == DietConfig.EntranceVisibility.FTB_COMPACT) {
                var server = ctx.getSource().getServer();
                server.getCommands().getDispatcher().execute("reload", server.createCommandSourceStack());
            }

            ctx.getSource().sendSuccess(() ->
                    Component.translatable("command.appleseed.config.entrance_visibility.set",
                            newMode.name().toLowerCase(java.util.Locale.ROOT)), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
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

    private static int setCraftChainSearchDepth(CommandContext<CommandSourceStack> ctx) {
        try {
            int value = IntegerArgumentType.getInteger(ctx, "value");
            DietConfig.INSTANCE.craftChainSearchDepth.set(value);
            saveDietConfig();
            ctx.getSource().sendSuccess(() ->
                Component.translatable("command.appleseed.config.craft_chain_search_depth.set", value), true);
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
            server.getCommands().getDispatcher().execute("reload", server.createCommandSourceStack());

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
            var server = ctx.getSource().getServer();
            server.getCommands().getDispatcher().execute("reload", server.createCommandSourceStack());

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

    private static int setItemNutritionFromHand(CommandContext<CommandSourceStack> ctx) {
        try {
            if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                ctx.getSource().sendFailure(Component.translatable("command.appleseed.item.cmd_block_requires_item"));
                return 0;
            }
            ItemStack held = player.getMainHandItem();
            if (held.isEmpty()) {
                ctx.getSource().sendFailure(Component.translatable("command.appleseed.item.no_item_held"));
                return 0;
            }
            String nutritionId = StringArgumentType.getString(ctx, "nutrition");
            float count = (float) DoubleArgumentType.getDouble(ctx, "count");
            return executeSetItemNutrition(ctx, nutritionId, count, held.getItem());
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int setItemNutritionFromArg(CommandContext<CommandSourceStack> ctx) {
        try {
            String nutritionId = StringArgumentType.getString(ctx, "nutrition");
            float count = (float) DoubleArgumentType.getDouble(ctx, "count");
            Item item = ItemArgument.getItem(ctx, "item").getItem();
            return executeSetItemNutrition(ctx, nutritionId, count, item);
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int executeSetItemNutrition(CommandContext<CommandSourceStack> ctx, String nutritionId, float count, Item item) {
        var groupOpt = DietGroups.getGroup(ctx.getSource().getLevel(), nutritionId);
        if (groupOpt.isEmpty()) {
            ctx.getSource().sendFailure(
                Component.translatable("command.appleseed.set.invalid_group", nutritionId));
            return 0;
        }

        IDietGroup group = groupOpt.get();
        String itemId = BuiltInRegistries.ITEM.getKey(item).toString();

        FoodNutritionManager.INSTANCE.putNutritionData(item, nutritionId, count);
        FoodNutritionManager.CLIENT.putNutritionData(item, nutritionId, count);

        saveAppleSeedData(itemId, nutritionId, count);

        ctx.getSource().sendSuccess(() ->
            Component.translatable("command.appleseed.item.success",
                Component.literal(itemId),
                Component.translatable(group.getTranslationKey()),
                Component.literal(String.format("%.1f%%", count * 100))), true);

        return 1;
    }

    private static int setBlockNutrition(CommandContext<CommandSourceStack> ctx) {
        return executeSetBlockNutrition(ctx, 1);
    }

    private static int setBlockNutritionWithBites(CommandContext<CommandSourceStack> ctx) {
        int bites = IntegerArgumentType.getInteger(ctx, "bites");
        return executeSetBlockNutrition(ctx, bites);
    }

    private static int executeSetBlockNutrition(CommandContext<CommandSourceStack> ctx, int bites) {
        try {
            String nutritionId = StringArgumentType.getString(ctx, "nutrition");
            float count = (float) DoubleArgumentType.getDouble(ctx, "count");
            String blockIdStr = StringArgumentType.getString(ctx, "block");

            var groupOpt = DietGroups.getGroup(ctx.getSource().getLevel(), nutritionId);
            if (groupOpt.isEmpty()) {
                ctx.getSource().sendFailure(
                    Component.translatable("command.appleseed.set.invalid_group", nutritionId));
                return 0;
            }

            IDietGroup group = groupOpt.get();

            ResourceLocation blockLoc = ResourceLocation.tryParse(blockIdStr);
            if (blockLoc == null) {
                ctx.getSource().sendFailure(
                    Component.translatable("command.appleseed.block.invalid_block", blockIdStr));
                return 0;
            }

            Block block = BuiltInRegistries.BLOCK.get(blockLoc);
            if (block == null || block == net.minecraft.world.level.block.Blocks.AIR) {
                ctx.getSource().sendFailure(
                    Component.translatable("command.appleseed.block.invalid_block", blockIdStr));
                return 0;
            }

            FoodNutritionManager.INSTANCE.putBlockNutritionData(block, nutritionId, count);
            FoodNutritionManager.CLIENT.putBlockNutritionData(block, nutritionId, count);
            FoodNutritionManager.INSTANCE.putBlockBites(block, bites);
            FoodNutritionManager.CLIENT.putBlockBites(block, bites);

            saveAppleSeedBlockData(blockIdStr, nutritionId, count, bites);

            ctx.getSource().sendSuccess(() ->
                Component.translatable("command.appleseed.block.success",
                    Component.literal(blockIdStr),
                    Component.translatable(group.getTranslationKey()),
                    Component.literal(String.format("%.1f%%", count * 100)),
                    Component.literal(String.valueOf(bites))), true);

            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static CompletableFuture<Suggestions> suggestBlockIds(
            CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        for (var entry : BuiltInRegistries.BLOCK.entrySet()) {
            String key = entry.getKey().location().toString();
            if (key.startsWith("minecraft:")) {
                builder.suggest(key);
            }
        }
        return builder.buildFuture();
    }

    private static void saveAppleSeedData(String itemId, String nutritionId, float value) {
        try {
            JsonObject root;
            File file = APPLE_SEED_DATA_PATH.toFile();

            if (file.exists()) {
                try (FileReader reader = new FileReader(file)) {
                    JsonElement element = GSON.fromJson(reader, JsonElement.class);
                    if (element != null && element.isJsonObject()) {
                        root = element.getAsJsonObject();
                    } else {
                        root = migrateLegacyData(element);
                    }
                } catch (Exception e) {
                    AppleSeedConstants.LOG.warn("Failed to read existing appleseed_data.json, creating new file", e);
                    root = new JsonObject();
                }
            } else {
                root = new JsonObject();
            }

            JsonArray items = root.has("items") ? root.getAsJsonArray("items") : new JsonArray();
            if (!root.has("items")) {
                root.add("items", items);
            }

            boolean found = false;
            for (JsonElement element : items) {
                if (element.isJsonObject()) {
                    JsonObject obj = element.getAsJsonObject();
                    if (obj.has("source_item") && obj.get("source_item").getAsString().equals(itemId)) {
                        JsonObject nutritions;
                        if (obj.has("nutritions") && obj.get("nutritions").isJsonObject()) {
                            nutritions = obj.getAsJsonObject("nutritions");
                        } else {
                            nutritions = new JsonObject();
                            obj.add("nutritions", nutritions);
                        }
                        nutritions.addProperty(nutritionId, value);
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                JsonObject newEntry = new JsonObject();
                newEntry.addProperty("source_item", itemId);
                JsonObject nutritions = new JsonObject();
                nutritions.addProperty(nutritionId, value);
                newEntry.add("nutritions", nutritions);
                items.add(newEntry);
            }

            Files.createDirectories(APPLE_SEED_DATA_PATH.getParent());
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(root, writer);
            }
            AppleSeedConstants.LOG.info("Saved nutrition data for {} ({}: {}) to appleseed_data.json", itemId, nutritionId, value);
        } catch (Exception e) {
            AppleSeedConstants.LOG.error("Failed to save appleseed_data.json", e);
        }
    }

    private static void saveAppleSeedBlockData(String blockId, String nutritionId, float value, int bites) {
        try {
            JsonObject root;
            File file = APPLE_SEED_DATA_PATH.toFile();

            if (file.exists()) {
                try (FileReader reader = new FileReader(file)) {
                    JsonElement element = GSON.fromJson(reader, JsonElement.class);
                    if (element != null && element.isJsonObject()) {
                        root = element.getAsJsonObject();
                    } else {
                        root = migrateLegacyData(element);
                    }
                } catch (Exception e) {
                    AppleSeedConstants.LOG.warn("Failed to read existing appleseed_data.json, creating new file", e);
                    root = new JsonObject();
                }
            } else {
                root = new JsonObject();
            }

            JsonArray blocks = root.has("blocks") ? root.getAsJsonArray("blocks") : new JsonArray();
            if (!root.has("blocks")) {
                root.add("blocks", blocks);
            }

            boolean found = false;
            for (JsonElement element : blocks) {
                if (element.isJsonObject()) {
                    JsonObject obj = element.getAsJsonObject();
                    if (obj.has("source_block") && obj.get("source_block").getAsString().equals(blockId)) {
                        JsonObject nutritions;
                        if (obj.has("nutritions") && obj.get("nutritions").isJsonObject()) {
                            nutritions = obj.getAsJsonObject("nutritions");
                        } else {
                            nutritions = new JsonObject();
                            obj.add("nutritions", nutritions);
                        }
                        nutritions.addProperty(nutritionId, value);
                        obj.addProperty("bites", bites);
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                JsonObject newEntry = new JsonObject();
                newEntry.addProperty("source_block", blockId);
                newEntry.addProperty("bites", bites);
                JsonObject nutritions = new JsonObject();
                nutritions.addProperty(nutritionId, value);
                newEntry.add("nutritions", nutritions);
                blocks.add(newEntry);
            }

            Files.createDirectories(APPLE_SEED_DATA_PATH.getParent());
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(root, writer);
            }
            AppleSeedConstants.LOG.info("Saved block nutrition data for {} ({}: {}, bites: {}) to appleseed_data.json", blockId, nutritionId, value, bites);
        } catch (Exception e) {
            AppleSeedConstants.LOG.error("Failed to save appleseed_data.json", e);
        }
    }

    private static JsonObject migrateLegacyData(JsonElement element) {
        JsonObject root = new JsonObject();
        if (element != null && element.isJsonArray()) {
            JsonArray items = new JsonArray();
            JsonArray oldArray = element.getAsJsonArray();
            for (JsonElement e : oldArray) {
                if (e.isJsonObject()) {
                    JsonObject obj = e.getAsJsonObject();
                    if (obj.has("source_item")) {
                        items.add(obj);
                    } else if (obj.has("source_block")) {
                        if (!root.has("blocks")) {
                            root.add("blocks", new JsonArray());
                        }
                        root.getAsJsonArray("blocks").add(obj);
                    }
                }
            }
            root.add("items", items);
        }
        return root;
    }

    private static void saveDietConfig() {
        try {
            java.nio.file.Path configPath = FMLPaths.CONFIGDIR.get().resolve("appleseed-common.toml");
            if (java.nio.file.Files.exists(configPath)) {
                java.util.List<String> lines = java.nio.file.Files.readAllLines(configPath);
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    if (line.matches("\\s*\"?ignore_hunger\"?\\s*=.*")) {
                        lines.set(i, "\tignore_hunger = " + DietConfig.INSTANCE.ignoreHunger.get());
                    } else if (line.matches("\\s*\"?entrance_visibility\"?\\s*=.*")) {
                        lines.set(i, "\tentrance_visibility = \"" + DietConfig.INSTANCE.entranceVisibility.get() + "\"");
                    }
                }
                java.nio.file.Files.write(configPath, lines);
            }
        } catch (Exception e) {
            AppleSeedConstants.LOG.error("Failed to save diet config", e);
        }
    }
}
