package com.whatwasmissing.spawnannouncements.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.whatwasmissing.spawnannouncements.core.AnnouncementKind;
import com.whatwasmissing.spawnannouncements.core.AnnouncementService;
import com.whatwasmissing.spawnannouncements.core.FrontierLedger;
import com.whatwasmissing.spawnannouncements.gui.FrontierShopCatalog;
import com.whatwasmissing.spawnannouncements.gui.FrontierShopMenu;
import com.whatwasmissing.spawnannouncements.gui.FrontierShopProduct;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;

/** Operator tools and player-facing field-intelligence reports. */
public final class AnnouncementCommands {
    private AnnouncementCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("spawnannounce")
                .then(Commands.literal("signals").executes(AnnouncementCommands::showSignals))
                .then(Commands.literal("research").executes(AnnouncementCommands::showResearch)
                        .then(Commands.literal("regions").executes(AnnouncementCommands::showRegions))
                        .then(Commands.literal("top").executes(AnnouncementCommands::showLeaderboard)))
                .then(Commands.literal("challenges").executes(AnnouncementCommands::showChallenge))
                .then(Commands.literal("expeditions").executes(AnnouncementCommands::showExpeditions))
                .then(Commands.literal("community").executes(AnnouncementCommands::showCommunity))
                .then(Commands.literal("guide").executes(AnnouncementCommands::showGuide))
                .then(Commands.literal("notifications").executes(AnnouncementCommands::showNotifications)
                        .then(Commands.argument("mode", StringArgumentType.word())
                                .executes(context -> setNotifications(context,
                                        StringArgumentType.getString(context, "mode")))))
                .then(Commands.literal("exchange")
                        .then(Commands.literal("list").executes(context -> showExchange(context, 1,
                                        FrontierShopCatalog.SortMode.FEATURED, ""))
                                .then(Commands.argument("page", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1))
                                        .executes(context -> showExchange(context,
                                                com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "page"),
                                                FrontierShopCatalog.SortMode.FEATURED, ""))))
                        .then(Commands.literal("search").then(Commands.argument("query", StringArgumentType.greedyString())
                                .executes(context -> searchExchange(context, StringArgumentType.getString(context, "query")))))
                        .then(Commands.literal("sort").then(Commands.argument("mode", StringArgumentType.word())
                                .executes(context -> sortExchange(context, StringArgumentType.getString(context, "mode")))))
                        .then(Commands.literal("clear").executes(AnnouncementCommands::clearExchangeSearch))
                        .then(Commands.literal("buy").then(Commands.argument("product_id", StringArgumentType.word())
                                .executes(context -> buyExchange(context, StringArgumentType.getString(context, "product_id"))))))
                .then(Commands.literal("reload")
                        .requires(source -> source.hasPermission(2))
                        .executes(AnnouncementCommands::reload))
                .then(Commands.literal("status")
                        .requires(source -> source.hasPermission(2))
                        .executes(AnnouncementCommands::status))
                .then(Commands.literal("test")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("kind", StringArgumentType.word())
                                .executes(AnnouncementCommands::test)));
        dispatcher.register(root);
    }

    private static int showSignals(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        List<FrontierLedger.SignalRecord> signals = AnnouncementService.recentSignals();
        if (signals.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No recent field signals are on record.").withStyle(ChatFormatting.GRAY), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("── Active field signals ──").withStyle(ChatFormatting.AQUA), false);
        long now = System.currentTimeMillis();
        signals.stream().limit(8).forEach(signal -> {
            long ageSeconds = Math.max(0L, (now - signal.createdAt) / 1000L);
            String line = "• " + signal.kindEnum().displayName() + " · " + signal.region
                    + " · " + signal.status + " · " + ageSeconds + "s ago";
            source.sendSuccess(() -> Component.literal(line).withStyle(signal.status.equals("active") ? ChatFormatting.GREEN : ChatFormatting.GRAY), false);
        });
        return 1;
    }

    private static int showExpeditions(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("This report must be viewed by a player."));
            return 0;
        }
        player.sendSystemMessage(Component.literal("── Rotating expedition contracts ──").withStyle(ChatFormatting.GOLD));
        for (FrontierLedger.ExpeditionProgress progress : AnnouncementService.expeditions(player.getUUID())) {
            FrontierLedger.ExpeditionContract contract = progress.contract();
            player.sendSystemMessage(Component.literal((progress.completed() ? "✓ " : "• ")
                            + contract.title() + " · " + progress.progress() + "/" + contract.goal()
                            + " · reward +" + contract.reward() + " research\n  " + contract.description())
                    .withStyle(progress.completed() ? ChatFormatting.GREEN : ChatFormatting.WHITE));
        }
        return 1;
    }

    private static int showCommunity(CommandContext<CommandSourceStack> context) {
        FrontierLedger.CommunityEventProgress event = AnnouncementService.communityEvent();
        context.getSource().sendSuccess(() -> Component.literal("── Community research ──").withStyle(ChatFormatting.GREEN), false);
        context.getSource().sendSuccess(() -> Component.literal(event.title() + " · " + event.progress() + "/"
                + event.goal() + " · target: " + event.targetRegion() + " · contributors: " + event.contributors()), false);
        context.getSource().sendSuccess(() -> Component.literal(event.description()
                + (event.completed() ? " · complete" : " · reward: +" + event.reward() + " research")), false);
        return 1;
    }

    private static int showGuide(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("The Field Guide belongs to a player."));
            return 0;
        }
        List<String> entries = AnnouncementService.fieldGuide(player.getUUID());
        player.sendSystemMessage(Component.literal("── Private Field Guide ──").withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.literal(entries.size() + " species recorded. The guide is private to your profile.")
                .withStyle(ChatFormatting.WHITE));
        if (entries.isEmpty()) {
            player.sendSystemMessage(Component.literal("Capture Pokémon to begin your collection journal.")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            player.sendSystemMessage(Component.literal(String.join(", ", entries)).withStyle(ChatFormatting.GRAY));
        }
        return 1;
    }

    private static int showNotifications(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("Notification preferences belong to a player."));
            return 0;
        }
        player.sendSystemMessage(Component.literal("Frontier notifications: "
                        + (AnnouncementService.alertsEnabled(player.getUUID()) ? "on" : "off")
                        + " · capture feedback: " + AnnouncementService.feedbackMode(player.getUUID()))
                .withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("Use on, off, chat, actionbar, or silent.").withStyle(ChatFormatting.GRAY));
        return 1;
    }

    private static int setNotifications(CommandContext<CommandSourceStack> context, String requested) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("Notification preferences belong to a player."));
            return 0;
        }
        String mode = requested.toLowerCase(java.util.Locale.ROOT);
        if (mode.equals("on") || mode.equals("off")) {
            AnnouncementService.setAlertsEnabled(player.getUUID(), mode.equals("on"));
        } else if (mode.equals("chat") || mode.equals("actionbar") || mode.equals("silent")) {
            AnnouncementService.setFeedbackMode(player.getUUID(), mode);
        } else {
            context.getSource().sendFailure(Component.literal("Modes: on, off, chat, actionbar, or silent."));
            return 0;
        }
        return showNotifications(context);
    }

    private static int showResearch(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("This report must be viewed by a player."));
            return 0;
        }
        int points = AnnouncementService.points(player.getUUID());
        int totalEarned = AnnouncementService.totalEarned(player.getUUID());
        int next = AnnouncementService.nextMilestone(player.getUUID());
        String nextText = next < 0 ? "all standard milestones complete" : next + " points";
        player.sendSystemMessage(Component.literal("── Field research ──").withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("Research balance: " + points + " · lifetime earned: " + totalEarned + " · next milestone: " + nextText)
                .withStyle(ChatFormatting.WHITE));
        player.sendSystemMessage(Component.literal("Regional streak: " + AnnouncementService.currentStreak(player.getUUID())
                        + " · best: " + AnnouncementService.bestStreak(player.getUUID()))
                .withStyle(ChatFormatting.WHITE));
        FrontierLedger.DailyChallengeProgress challenge = AnnouncementService.dailyChallenge(player.getUUID());
        player.sendSystemMessage(Component.literal("Daily captures: " + challenge.captures() + "/" + challenge.goal()
                        + (challenge.completed() ? " · complete" : "") + " · reward: " + challenge.reward() + " research")
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal("Capture bonuses and regional research allocations feed the same spendable balance.")
                .withStyle(ChatFormatting.GRAY));
        return 1;
    }

    private static int showRegions(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("This report must be viewed by a player."));
            return 0;
        }
        Map<String, Integer> counts = AnnouncementService.regionCaptureCounts(player.getUUID());
        Map<String, Integer> points = AnnouncementService.regionResearchPoints(player.getUUID());
        player.sendSystemMessage(Component.literal("── Surveyed regions ──").withStyle(ChatFormatting.AQUA));
        if (counts.isEmpty()) {
            player.sendSystemMessage(Component.literal("No regional captures recorded yet.").withStyle(ChatFormatting.GRAY));
            return 1;
        }
        counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> player.sendSystemMessage(Component.literal(entry.getKey() + " · " + entry.getValue()
                                + " captures · " + points.getOrDefault(entry.getKey(), 0) + " spendable research")
                        .withStyle(ChatFormatting.WHITE)));
        return 1;
    }

    private static int showLeaderboard(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal("── Field research leaders ──").withStyle(ChatFormatting.GOLD), false);
        int rank = 1;
        for (FrontierLedger.LeaderboardEntry entry : AnnouncementService.leaderboard()) {
            String name = context.getSource().getServer().getProfileCache()
                    .get(entry.playerId()).map(profile -> profile.getName()).orElse(entry.playerId().toString());
            int currentRank = rank++;
            context.getSource().sendSuccess(() -> Component.literal("#" + currentRank + " " + name + " · " + entry.points() + " points"), false);
        }
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        AnnouncementService.reload();
        context.getSource().sendSuccess(() -> Component.literal("Cobblemon Frontiers configuration reloaded."), true);
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal("Cobblemon Frontiers: " + AnnouncementService.statusLine()), false);
        return 1;
    }

    private static int test(CommandContext<CommandSourceStack> context) {
        String requested = StringArgumentType.getString(context, "kind");
        AnnouncementKind kind = AnnouncementService.parseKind(requested);
        if (kind == null) {
            context.getSource().sendFailure(Component.literal("Unknown kind. Try rare, shiny, legendary, mythical, ultra_beast, paradox, or alpha."));
            return 0;
        }
        context.getSource().sendSuccess(() -> AnnouncementService.format(kind,
                AnnouncementService.config().messageFor(kind, "the savanna")), false);
        return 1;
    }

    private static int showChallenge(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("This report must be viewed by a player."));
            return 0;
        }
        FrontierLedger.DailyChallengeProgress challenge = AnnouncementService.dailyChallenge(player.getUUID());
        player.sendSystemMessage(Component.literal("Daily field challenge").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal("Capture " + challenge.goal() + " Pokémon: "
                        + challenge.captures() + "/" + challenge.goal() + " · reward +" + challenge.reward() + " research"
                        + (challenge.completed() ? " · complete" : ""))
                .withStyle(ChatFormatting.WHITE));
        return 1;
    }

    private static int searchExchange(CommandContext<CommandSourceStack> context, String query) {
        String safeQuery = query.trim();
        if (context.getSource().getEntity() instanceof ServerPlayer player
                && player.containerMenu instanceof FrontierShopMenu menu) {
            menu.applySearch(player, safeQuery);
        }
        return showExchange(context, 1, FrontierShopCatalog.SortMode.NAME, safeQuery);
    }

    private static int clearExchangeSearch(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player
                && player.containerMenu instanceof FrontierShopMenu menu) {
            menu.applySearch(player, "");
            return showExchange(context, 1, menu.sortMode(), "");
        }
        return showExchange(context, 1, FrontierShopCatalog.SortMode.FEATURED, "");
    }

    private static int sortExchange(CommandContext<CommandSourceStack> context, String requested) {
        FrontierShopCatalog.SortMode mode = FrontierShopCatalog.SortMode.parse(requested);
        if (mode == null) {
            context.getSource().sendFailure(Component.literal("Sort options: featured, name, or cost."));
            return 0;
        }
        String query = "";
        if (context.getSource().getEntity() instanceof ServerPlayer player
                && player.containerMenu instanceof FrontierShopMenu menu) {
            query = menu.searchQuery();
            menu.applySort(player, mode);
        }
        return showExchange(context, 1, mode, query);
    }

    private static int showExchange(CommandContext<CommandSourceStack> context, int page,
                                    FrontierShopCatalog.SortMode mode, String query) {
        List<FrontierShopProduct> products = FrontierShopCatalog.filteredProducts(query, mode);
        int pageSize = FrontierShopCatalog.PRODUCTS_PER_PAGE;
        int pageCount = Math.max(1, (products.size() + pageSize - 1) / pageSize);
        if (page > pageCount) {
            context.getSource().sendFailure(Component.literal("That page does not exist. There are " + pageCount + " page(s)."));
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.literal("── Frontier exchange · " + mode.label()
                + (query.isBlank() ? "" : " · “" + query + "”") + " · page " + page + "/" + pageCount + " ──")
                .withStyle(ChatFormatting.AQUA), false);
        if (products.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.literal("No matching items. Try another search."), false);
            return 1;
        }
        int start = (page - 1) * pageSize;
        for (int index = start; index < Math.min(products.size(), start + pageSize); index++) {
            FrontierShopProduct product = products.get(index);
            String line = product.displayName() + " ×" + product.amount() + " · " + product.cost()
                    + " research · /spawnannounce exchange buy " + product.id();
            context.getSource().sendSuccess(() -> Component.literal(line).withStyle(ChatFormatting.WHITE), false);
        }
        return 1;
    }

    private static int buyExchange(CommandContext<CommandSourceStack> context, String productId) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("Purchases must be made by a player."));
            return 0;
        }
        FrontierShopProduct product = FrontierShopCatalog.products().stream()
                .filter(candidate -> candidate.id().equalsIgnoreCase(productId)).findFirst().orElse(null);
        if (product == null) {
            player.sendSystemMessage(Component.literal("Unknown exchange item. Use /spawnannounce exchange list to browse.")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        return FrontierShopMenu.purchase(player, product) ? 1 : 0;
    }
}
