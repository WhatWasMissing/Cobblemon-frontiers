package com.whatwasmissing.cobblemongacha.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.whatwasmissing.cobblemongacha.core.GachaService;
import com.whatwasmissing.cobblemongacha.core.GachaUpgradeService;
import com.whatwasmissing.cobblemongacha.core.ItemValueService;
import com.whatwasmissing.cobblemongacha.core.PokemonRewardAdapter;
import com.whatwasmissing.cobblemongacha.core.UpgradeCatalog;
import com.whatwasmissing.cobblemongacha.core.UpgradeTarget;
import com.whatwasmissing.cobblemongacha.gui.GachaMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

/** Player command fallback plus operator-only diagnostics for server-only installs. */
public final class GachaCommands {
    private GachaCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cobblemon_gacha")
                .then(Commands.literal("draw").executes(context -> draw(context.getSource(), 1))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 10))
                                .executes(context -> draw(context.getSource(),
                                        IntegerArgumentType.getInteger(context, "count")))))
                .then(Commands.literal("banners").executes(context -> banners(context.getSource())))
                .then(Commands.literal("balance").executes(context -> balance(context.getSource())))
                .then(Commands.literal("redeem_voucher").executes(context -> redeemVoucher(context.getSource())))
                .then(Commands.literal("targets").executes(context -> targets(context.getSource(), "all"))
                        .then(Commands.argument("category", StringArgumentType.word())
                                .executes(context -> targets(context.getSource(),
                                        StringArgumentType.getString(context, "category")))))
                .then(Commands.literal("odds").then(Commands.argument("target_id", StringArgumentType.word())
                        .executes(context -> preview(context.getSource(),
                                StringArgumentType.getString(context, "target_id"), 1))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 8))
                                .executes(context -> preview(context.getSource(),
                                        StringArgumentType.getString(context, "target_id"),
                                        IntegerArgumentType.getInteger(context, "amount"))))))
                .then(Commands.literal("wager").then(Commands.argument("target_id", StringArgumentType.word())
                        .executes(context -> wager(context.getSource(),
                                StringArgumentType.getString(context, "target_id"), 1))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 8))
                                .executes(context -> wager(context.getSource(),
                                        StringArgumentType.getString(context, "target_id"),
                                        IntegerArgumentType.getInteger(context, "amount"))))))
                .then(Commands.literal("test_pulls").requires(source -> source.hasPermission(2))
                        .executes(context -> grant(context.getSource(), 900))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 9_000))
                                .executes(context -> grant(context.getSource(),
                                        IntegerArgumentType.getInteger(context, "amount"))))));
    }

    private static int draw(CommandSourceStack source, int count) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return playerRequired(source);
        if (count != 1 && count != 10) {
            source.sendFailure(Component.literal("Choose 1 or 10 pulls."));
            return 0;
        }
        GachaService.draw(player, count, GachaService.activeBannerIndex());
        if (player.containerMenu instanceof GachaMenu menu) menu.refreshForServer(player);
        return 1;
    }

    private static int banners(CommandSourceStack source) {
        int activeIndex = GachaService.activeBannerIndex();
        source.sendSuccess(() -> Component.literal("── Regional banners ──").withStyle(ChatFormatting.AQUA), false);
        for (int index = 0; index < GachaService.bannerCount(); index++) {
            int current = index;
            String label = (current == activeIndex ? "▶ " : "  ") + GachaService.banner(current).title;
            source.sendSuccess(() -> Component.literal(label)
                    .withStyle(current == activeIndex ? ChatFormatting.GOLD : ChatFormatting.WHITE), false);
        }
        source.sendSuccess(() -> Component.literal("Active banner rotates in "
                + GachaService.formatCooldown(GachaService.secondsUntilBannerRotation() * 1_000_000_000L) + ".")
                .withStyle(ChatFormatting.GRAY), false);
        return 1;
    }

    private static int balance(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return playerRequired(source);
        var config = GachaService.config();
        if (config == null) {
            source.sendFailure(Component.literal("Cobblemon Gacha is not ready."));
            return 0;
        }
        int progress = GachaService.captureProgress(player.getUUID());
        int left = Math.max(1, config.capturesPerTicket - Math.floorMod(progress, config.capturesPerTicket));
        player.sendSystemMessage(Component.literal("Gacha tickets: " + GachaService.tickets(player.getUUID())
                        + " · next ticket in " + left + " captures · pulls: " + GachaService.totalDraws(player.getUUID()))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.literal("Pity · Rare+: " + GachaService.pity(player.getUUID())
                        + " · Legendary: " + GachaService.legendaryPity(player.getUUID()))
                .withStyle(ChatFormatting.GRAY));
        return 1;
    }

    private static int targets(CommandSourceStack source, String category) {
        String filter = category.toLowerCase(Locale.ROOT);
        if (!filter.equals("all") && !filter.equals("items") && !filter.equals("pokemon")) {
            source.sendFailure(Component.literal("Categories: all, items, or pokemon."));
            return 0;
        }
        List<UpgradeTarget> available = UpgradeCatalog.targets().stream()
                .filter(target -> filter.equals("all")
                        || filter.equals("pokemon") == target.pokemon())
                .toList();
        source.sendSuccess(() -> Component.literal("Wager targets · use the id with odds or wager:")
                .withStyle(ChatFormatting.AQUA), false);
        available.forEach(target -> source.sendSuccess(() -> Component.literal(target.id() + " · "
                + target.displayName() + (target.pokemon() ? " · " + target.rarity().displayName() : "")), false));
        return 1;
    }

    private static int preview(CommandSourceStack source, String targetId, int amount) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return playerRequired(source);
        UpgradeTarget target = findTarget(targetId);
        if (target == null) {
            source.sendFailure(Component.literal("Unknown target. Use /cobblemon_gacha targets to list ids."));
            return 0;
        }
        if (amount != 1 && amount != 2 && amount != 4 && amount != 8) {
            source.sendFailure(Component.literal("Target amount must be 1, 2, 4, or 8."));
            return 0;
        }
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            source.sendFailure(Component.literal("Hold your source item in your main hand first."));
            return 0;
        }
        double sourceValue = ItemValueService.value(held);
        int safeAmount = target.pokemon() ? 1 : amount;
        double targetValue = target.pokemon()
                ? ItemValueService.pokemonTargetValue(target)
                : ItemValueService.targetValue(target.displayStack(1), safeAmount);
        double chance = GachaUpgradeService.chance(held, target, safeAmount);
        String chanceText = String.format(Locale.ROOT, chance < 0.001 ? "%.2f%%" : "%.1f%%", chance * 100.0);
        player.sendSystemMessage(Component.literal("Wager preview · source value " + valueText(sourceValue)
                        + " · target value " + valueText(targetValue) + " · chance " + chanceText)
                .withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("A failed wager consumes one held item. Target: "
                        + target.displayName() + (target.pokemon() ? " · Pokémon contracts award one" : " ×" + safeAmount))
                .withStyle(ChatFormatting.GRAY));
        return 1;
    }

    private static int wager(CommandSourceStack source, String targetId, int amount) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return playerRequired(source);
        UpgradeTarget target = findTarget(targetId);
        if (target == null) {
            source.sendFailure(Component.literal("Unknown target. Use /cobblemon_gacha targets to list ids."));
            return 0;
        }
        if (!GachaUpgradeService.attempt(player, target, amount)) return 0;
        return 1;
    }

    private static UpgradeTarget findTarget(String id) {
        return UpgradeCatalog.targets().stream()
                .filter(target -> target.id().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    private static String valueText(double value) {
        return value >= 1000.0 ? String.format(Locale.ROOT, "%.1fk", value / 1000.0)
                : String.format(Locale.ROOT, "%.1f", value);
    }

    private static int grant(CommandSourceStack source, int amount) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return playerRequired(source);
        return GachaService.grantTestPulls(player, amount);
    }

    private static int redeemVoucher(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return playerRequired(source);
        return PokemonRewardAdapter.redeemVoucher(player) ? 1 : 0;
    }

    private static int playerRequired(CommandSourceStack source) {
        source.sendFailure(Component.literal("Run this command as a player."));
        return 0;
    }
}
