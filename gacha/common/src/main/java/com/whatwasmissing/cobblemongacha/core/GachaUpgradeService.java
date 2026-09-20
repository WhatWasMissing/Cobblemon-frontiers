package com.whatwasmissing.cobblemongacha.core;

import com.whatwasmissing.cobblemongacha.config.GachaConfig;
import com.whatwasmissing.cobblemongacha.gui.UpgradeMenu;
import com.whatwasmissing.cobblemongacha.network.UpgradeResultPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;

/** Server-only resolution of the source-to-target gamble. */
public final class GachaUpgradeService {
    private GachaUpgradeService() {}

    public static void attempt(ServerPlayer player, UpgradeMenu menu) {
        resolve(player, menu.sourceStack(), menu.selectedTarget(), menu.targetAmount(), menu::consumeSourceOne, menu);
    }

    /** Command-based wager path for players using a server without the custom client screen. */
    public static boolean attempt(ServerPlayer player, UpgradeTarget target, int amount) {
        ItemStack source = player.getMainHandItem();
        if (source.isEmpty()) {
            player.sendSystemMessage(Component.literal("Hold the source item in your main hand first.")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        if (amount != 1 && amount != 2 && amount != 4 && amount != 8) {
            player.sendSystemMessage(Component.literal("Target amount must be 1, 2, 4, or 8.")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        return resolve(player, source, target, amount, () -> {
            if (source.isEmpty()) return false;
            source.shrink(1);
            player.getInventory().setChanged();
            return true;
        }, player.containerMenu instanceof UpgradeMenu menu ? menu : null);
    }

    private static boolean resolve(ServerPlayer player, ItemStack source, UpgradeTarget target, int amount,
                                   BooleanSupplier consumeSource, UpgradeMenu menu) {
        GachaConfig config = GachaService.config();
        if (config == null || GachaService.ledger() == null || !config.enabled) {
            player.sendSystemMessage(Component.literal("Cobblemon Gacha is currently disabled.")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        if (source.isEmpty()) {
            player.sendSystemMessage(Component.literal("Hold or select a source item first.")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        if (target == null) {
            player.sendSystemMessage(Component.literal("Choose a target first.").withStyle(ChatFormatting.RED));
            return false;
        }

        double sourceValue = ItemValueService.value(source);
        if (target.pokemon() && target.rarity() == GachaRarity.LEGENDARY
                && sourceValue < config.legendaryPokemonMinimumSourceValue) {
            player.sendSystemMessage(Component.literal("Legendary contracts require a source worth at least "
                            + formatValue(config.legendaryPokemonMinimumSourceValue) + ".")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        long remainingNanos = GachaService.gamblingCooldownRemainingNanos(player, target.pokemon());
        if (remainingNanos > 0L) {
            player.sendSystemMessage(Component.literal("Gambling is on cooldown. Try again in "
                            + GachaService.formatCooldown(remainingNanos) + ".")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        int safeAmount = target.pokemon() ? 1 : amount;
        double chance = chance(source, target, safeAmount,
                config.legendaryPokemonChance, config.legendaryPokemonMinimumSourceValue);
        if (!consumeSource.getAsBoolean()) {
            player.sendSystemMessage(Component.literal("The source item changed before the gamble resolved.")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        // Start the cooldown only after a valid wager has actually consumed its
        // source. Failed attempts therefore cannot be spammed, while inventory
        // races or invalid submissions do not lock the player out.
        GachaService.startGamblingCooldown(player, target.pokemon());

        boolean success = ThreadLocalRandom.current().nextDouble() < chance;
        if (success) {
            if (target.pokemon()) {
                GachaResult result = new GachaResult(target.id(), target.displayName(), target.rarity(), false);
                boolean delivered = PokemonRewardAdapter.deliver(player, result);
                if (!delivered) PokemonRewardAdapter.giveVoucher(player, result);
                GachaService.announceExceptionalDrop(player, result, "a Pokémon contract");
                player.sendSystemMessage(Component.literal("Contract succeeded! " + source.getHoverName().getString()
                                + " awarded " + target.displayName() + (delivered ? "" : " Voucher")
                                + " (" + formatPercent(chance) + ")")
                        .withStyle(ChatFormatting.GREEN));
                if (menu != null) sendResult(player, true, "SUCCESS", target.displayName() + " delivered", chance);
            } else {
                ItemStack reward = target.rewardStack(safeAmount);
                if (!player.getInventory().add(reward)) player.drop(reward, false);
                player.sendSystemMessage(Component.literal("Upgrade succeeded! " + source.getHoverName().getString()
                                + " became " + safeAmount + "× " + target.displayName()
                                + " (" + formatPercent(chance) + ")")
                        .withStyle(ChatFormatting.GREEN));
                if (menu != null) sendResult(player, true, "SUCCESS", target.displayName() + " ×" + safeAmount + " delivered", chance);
            }
        } else {
            player.sendSystemMessage(Component.literal("Upgrade failed. The source item was lost. Chance was "
                            + formatPercent(chance) + ".")
                    .withStyle(ChatFormatting.RED));
            if (menu != null) sendResult(player, false, "FAILED", "Source item lost", chance);
        }
        if (menu != null) {
            menu.refresh(player);
            menu.broadcastChanges();
            menu.syncToClient(player);
        }
        return true;
    }

    private static void sendResult(ServerPlayer player, boolean success, String title, String detail, double chance) {
        player.connection.send(new ClientboundCustomPayloadPacket(
                new UpgradeResultPayload(success, title, detail, chance)));
    }

    public static double chance(UpgradeMenu menu) {
        UpgradeTarget target = menu.selectedTarget();
        if (menu.sourceStack().isEmpty() || target == null) return 0.0;
        return chance(menu.sourceStack(), target, menu.targetAmount(), menu.legendaryPokemonChance(),
                menu.legendaryPokemonMinimumSourceValue());
    }

    public static double chance(ItemStack source, UpgradeTarget target, int amount) {
        GachaConfig config = GachaService.config();
        if (config == null || target == null || source == null || source.isEmpty()) return 0.0;
        return chance(source, target, amount, config.legendaryPokemonChance,
                config.legendaryPokemonMinimumSourceValue);
    }

    private static double chance(ItemStack source, UpgradeTarget target, int amount,
                                 double legendaryChance, double legendaryMinimumSourceValue) {
        double sourceValue = ItemValueService.value(source);
        if (target.pokemon()) {
            if (target.rarity() == GachaRarity.LEGENDARY) {
                if (sourceValue < legendaryMinimumSourceValue) return 0.0;
                return legendaryChance;
            }
            return ItemValueService.successChance(sourceValue, ItemValueService.pokemonTargetValue(target));
        }
        return ItemValueService.successChance(sourceValue,
                ItemValueService.targetValue(target.displayStack(1), amount));
    }

    private static String formatPercent(double chance) {
        double percent = chance * 100.0;
        return String.format(java.util.Locale.ROOT, percent < 0.1 ? "%.2f%%" : "%.1f%%", percent);
    }
    private static String formatValue(double value) { return value >= 1000.0
            ? String.format(java.util.Locale.ROOT, "%.1fk", value / 1000.0)
            : String.format(java.util.Locale.ROOT, "%.0f", value); }

}
