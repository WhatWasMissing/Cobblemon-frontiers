package com.whatwasmissing.cobblemongacha.core;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.whatwasmissing.cobblemongacha.config.GachaConfig;
import com.whatwasmissing.cobblemongacha.gui.GachaMenu;
import com.whatwasmissing.cobblemongacha.gui.UpgradeMenu;
import com.whatwasmissing.cobblemongacha.network.GachaPullResultPayload;
import kotlin.Unit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Main server coordinator. No client packet is trusted with odds or rewards. */
public final class GachaService {
    public static final String MOD_ID = "cobblemon_gacha";
    public static final String MOD_VERSION = "1.0.0";
    public static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MOD_ID);
    private static final long NANOS_PER_SECOND = 1_000_000_000L;
    private static final Map<UUID, Long> GAMBLING_COOLDOWNS = new HashMap<>();

    private static GachaConfig config;
    private static GachaLedger ledger;
    private static boolean initialised;

    private GachaService() {}

    public static synchronized void init(Path configDirectory) {
        if (initialised) return;
        initialised = true;
        Path directory = configDirectory.toAbsolutePath().resolve(MOD_ID);
        config = GachaConfig.load(directory.resolve("config.json"), LOGGER);
        ledger = GachaLedger.load(directory.resolve("gacha_ledger.json"), LOGGER);

        CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.NORMAL, event -> {
            ServerPlayer player = event.getPlayer();
            if (player != null && config != null && ledger != null && config.enabled) onCapture(player);
            return Unit.INSTANCE;
        });
        LOGGER.info("Cobblemon Gacha initialised with {} banner(s).", config.banners.size());
    }

    public static GachaConfig config() { return config; }
    public static GachaLedger ledger() { return ledger; }
    public static void flushLedger() { if (ledger != null) ledger.flush(); }

    /** Returns the server-side cooldown remaining for a draw or wager. */
    public static long gamblingCooldownRemainingNanos(ServerPlayer player, boolean pokemonWager) {
        int cooldownSeconds = gamblingCooldownSeconds(pokemonWager);
        if (cooldownSeconds <= 0) return 0L;
        return gamblingCooldownRemainingNanos(player.getUUID());
    }

    /** Returns the shared cooldown remaining, regardless of wager type. */
    public static long gamblingCooldownRemainingNanos(UUID playerId) {
        Long expiresAt = GAMBLING_COOLDOWNS.get(playerId);
        if (expiresAt == null) return 0L;
        long remaining = expiresAt - System.nanoTime();
        if (remaining <= 0L) {
            GAMBLING_COOLDOWNS.remove(playerId);
            return 0L;
        }
        return remaining;
    }

    public static long gamblingCooldownRemainingSeconds(UUID playerId) {
        long remaining = gamblingCooldownRemainingNanos(playerId);
        return remaining <= 0L ? 0L : (remaining + NANOS_PER_SECOND - 1L) / NANOS_PER_SECOND;
    }

    /** Starts the shared cooldown only after a draw or wager has been accepted. */
    public static void startGamblingCooldown(ServerPlayer player, boolean pokemonWager) {
        int cooldownSeconds = gamblingCooldownSeconds(pokemonWager);
        if (cooldownSeconds <= 0) return;
        GAMBLING_COOLDOWNS.put(player.getUUID(), System.nanoTime() + cooldownSeconds * NANOS_PER_SECOND);
    }

    public static String formatCooldown(long remainingNanos) {
        long seconds = (remainingNanos + NANOS_PER_SECOND - 1L) / NANOS_PER_SECOND;
        return seconds >= 60L ? (seconds / 60L) + "m " + (seconds % 60L) + "s" : seconds + "s";
    }

    private static int gamblingCooldownSeconds(boolean pokemonWager) {
        if (config == null) return 0;
        return pokemonWager
                ? Math.max(config.gamblingCooldownSeconds, config.pokemonWagerCooldownSeconds)
                : config.gamblingCooldownSeconds;
    }

    public static boolean isLegendaryMonumentSpecies(String species) {
        return config != null && config.isLegendaryMonumentSpecies(species);
    }

    /**
     * Returns the deterministic real-world rotation bucket. Using UTC epoch
     * time means every server and client agrees on the hour without storing a
     * mutable timer or ticking every banner once per game tick.
     */
    private static long rotationBucket() {
        long periodSeconds = Math.max(1, config == null ? 1 : config.bannerRotationHours) * 3600L;
        return Math.floorDiv(Instant.now().getEpochSecond(), periodSeconds);
    }

    public static int activeBannerIndex() {
        int count = bannerCount();
        if (count <= 0) return 0;
        return (int) Math.floorMod(rotationBucket(), count);
    }

    public static boolean isActiveBanner(int index) {
        return bannerCount() > 0 && index == activeBannerIndex();
    }

    public static long secondsUntilBannerRotation() {
        if (config == null) return 0L;
        long periodSeconds = Math.max(1, config.bannerRotationHours) * 3600L;
        long now = Instant.now().getEpochSecond();
        long elapsed = Math.floorMod(now, periodSeconds);
        return Math.max(1L, periodSeconds - elapsed);
    }

    public static void openMenu(ServerPlayer player) {
        openUpgradeMenu(player);
    }

    public static void openUpgradeMenu(ServerPlayer player) {
        if (config == null || !config.enabled || ledger == null) {
            player.sendSystemMessage(Component.literal("Cobblemon Gacha is currently disabled.").withStyle(ChatFormatting.RED));
            return;
        }
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new UpgradeMenu(containerId, inventory),
                Component.literal("Cobblemon Item Upgrader")));
        if (player.containerMenu instanceof UpgradeMenu menu) menu.syncToClient(player);
    }

    public static void openDrawMenu(ServerPlayer player) {
        if (config == null || !config.enabled || ledger == null) {
            player.sendSystemMessage(Component.literal("Cobblemon Gacha is currently disabled.").withStyle(ChatFormatting.RED));
            return;
        }
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new GachaMenu(containerId, inventory),
                Component.literal("Cobblemon Gacha")));
        if (player.containerMenu instanceof GachaMenu menu) menu.syncToClient(player);
    }

    public static int tickets(UUID uuid) { return config == null || ledger == null ? 0 : ledger.tickets(uuid, config.startingTickets); }
    public static int captureProgress(UUID uuid) { return config == null || ledger == null ? 0 : ledger.captureProgress(uuid, config.startingTickets); }
    public static long totalDraws(UUID uuid) { return config == null || ledger == null ? 0L : ledger.totalDraws(uuid, config.startingTickets); }
    public static int pity(UUID uuid) { return config == null || ledger == null ? 0 : ledger.pity(uuid, config.startingTickets); }
    public static int legendaryPity(UUID uuid) { return config == null || ledger == null ? 0 : ledger.legendaryPity(uuid, config.startingTickets); }
    public static List<GachaLedger.HistoryEntry> history(UUID uuid) { return config == null || ledger == null ? List.of() : ledger.history(uuid, config.startingTickets); }

    /** Grants a bounded test balance to an operator through the dedicated test command. */
    public static int grantTestPulls(ServerPlayer player, int requested) {
        if (config == null || ledger == null || !config.enabled) return 0;
        int pulls = Math.max(1, Math.min(9_000, requested));
        ledger.grantTickets(player.getUUID(), pulls, config.startingTickets);
        if (player.containerMenu instanceof GachaMenu menu) menu.refreshForServer(player);
        player.sendSystemMessage(Component.literal("Granted " + pulls + " Gacha pulls for testing.")
                .withStyle(ChatFormatting.GREEN));
        return pulls;
    }

    public static GachaBanner banner(int index) {
        if (config == null || config.banners == null || config.banners.isEmpty()) {
            return new GachaBanner("empty", "No banners", "No gacha banners are configured.", new ArrayList<>());
        }
        int safe = Math.max(0, Math.min(index, config.banners.size() - 1));
        return config.banners.get(safe);
    }

    public static int bannerCount() { return config == null || config.banners == null ? 0 : config.banners.size(); }

    private static void onCapture(ServerPlayer player) {
        GachaLedger.CaptureProgress progress = ledger.recordCapture(
                player.getUUID(), config.capturesPerTicket, config.startingTickets);
        if (progress.ticketsEarned() > 0) {
            player.sendSystemMessage(Component.literal("Capture milestone: +" + progress.ticketsEarned()
                            + " Gacha Ticket" + (progress.ticketsEarned() == 1 ? "" : "s") + ".")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        if (player.containerMenu instanceof GachaMenu menu) {
            menu.refreshForServer(player);
        }
    }

    public static DrawSummary draw(ServerPlayer player, int count, int bannerIndex) {
        if (config == null || ledger == null || !config.enabled) {
            player.sendSystemMessage(Component.literal("Cobblemon Gacha is currently disabled.").withStyle(ChatFormatting.RED));
            return new DrawSummary(List.of(), false);
        }
        int pulls = count == 10 ? 10 : 1;
        int cost = pulls == 10 ? config.tenDrawCost : config.singleDrawCost;
        if (bannerCount() <= 0) {
            player.sendSystemMessage(Component.literal("No usable gacha banners are configured.")
                    .withStyle(ChatFormatting.RED));
            return new DrawSummary(List.of(), false);
        }
        if (!isActiveBanner(bannerIndex)) {
            player.sendSystemMessage(Component.literal("That banner has rotated. The active banner is now "
                            + banner(activeBannerIndex()).title + ".")
                    .withStyle(ChatFormatting.YELLOW));
            return new DrawSummary(List.of(), false);
        }
        long remainingCooldown = gamblingCooldownRemainingNanos(player, false);
        if (remainingCooldown > 0L) {
            player.sendSystemMessage(Component.literal("Gambling is on cooldown. Try again in "
                            + formatCooldown(remainingCooldown) + ".")
                    .withStyle(ChatFormatting.RED));
            return new DrawSummary(List.of(), false);
        }
        if (!ledger.spendTickets(player.getUUID(), cost, config.startingTickets)) {
            player.sendSystemMessage(Component.literal("You need " + cost + " Gacha Ticket"
                    + (cost == 1 ? "" : "s") + ".").withStyle(ChatFormatting.RED));
            return new DrawSummary(List.of(), false);
        }

        GachaBanner banner = banner(bannerIndex);
        List<GachaResult> results = new ArrayList<>();
        boolean rareSeen = false;
        for (int index = 0; index < pulls; index++) {
            boolean tenPullGuarantee = pulls == 10 && index == pulls - 1 && !rareSeen;
            int currentPity = ledger.pity(player.getUUID(), config.startingTickets);
            int currentLegendaryPity = ledger.legendaryPity(player.getUUID(), config.startingTickets);
            boolean legendaryGuarantee = currentLegendaryPity >= config.legendaryPityDraws - 1
                    && hasWeightedEntry(banner, GachaRarity.LEGENDARY);
            boolean rareGuarantee = (tenPullGuarantee || currentPity >= config.rarePityDraws - 1)
                    && hasWeightedEntry(banner, GachaRarity.RARE);
            GachaResult result = roll(banner, rareGuarantee, legendaryGuarantee);
            if (result == null) continue;
            results.add(result);
            if (result.rarity().atLeast(GachaRarity.RARE)) rareSeen = true;
            ledger.recordResult(player.getUUID(), result, config.startingTickets);
            if (!PokemonRewardAdapter.deliver(player, result)) PokemonRewardAdapter.giveVoucher(player, result);
        }

        if (results.isEmpty()) {
            ledger.refundTickets(player.getUUID(), cost, config.startingTickets);
            player.sendSystemMessage(Component.literal("This banner has no usable entries. No ticket was consumed.")
                    .withStyle(ChatFormatting.RED));
            return new DrawSummary(List.of(), false);
        }
        startGamblingCooldown(player, false);
        announceResults(player, results, banner);
        sendPullReveal(player, results);
        return new DrawSummary(results, true);
    }

    private static void sendPullReveal(ServerPlayer player, List<GachaResult> results) {
        if (results == null || results.isEmpty()) return;
        GachaResult highlight = results.stream()
                .max(java.util.Comparator.comparingInt((GachaResult result) -> result.rarity().rank())
                        .thenComparingInt(result -> result.shiny() ? 1 : 0))
                .orElse(results.get(0));
        player.connection.send(new ClientboundCustomPayloadPacket(new GachaPullResultPayload(
                highlight.species(), highlight.label(), highlight.rarity().name(), highlight.shiny(), results.size())));
    }

    private static boolean hasWeightedEntry(GachaBanner banner, GachaRarity minimum) {
        return banner.entries.stream().anyMatch(entry -> entry != null && entry.rarity != null
                && entry.rarity.atLeast(minimum) && entry.weight > 0.0 && Double.isFinite(entry.weight));
    }

    private static GachaResult roll(GachaBanner banner, boolean rareGuarantee, boolean legendaryGuarantee) {
        GachaRarity minimum = legendaryGuarantee ? GachaRarity.LEGENDARY
                : (rareGuarantee ? GachaRarity.RARE : GachaRarity.COMMON);
        List<GachaEntry> candidates = banner.entries.stream()
                .filter(entry -> entry != null && entry.rarity != null && entry.weight > 0.0
                        && Double.isFinite(entry.weight) && entry.rarity.atLeast(minimum))
                .toList();
        if (candidates.isEmpty()) return null;
        double total = candidates.stream().mapToDouble(entry -> entry.weight).sum();
        if (!Double.isFinite(total) || total <= 0.0) return null;
        double target = ThreadLocalRandom.current().nextDouble(total);
        for (GachaEntry entry : candidates) {
            target -= entry.weight;
            if (target <= 0.0) {
                boolean shiny = ThreadLocalRandom.current().nextDouble() < config.shinyChance;
                return new GachaResult(entry.species, entry.displayName, entry.rarity, shiny);
            }
        }
        GachaEntry last = candidates.get(candidates.size() - 1);
        return new GachaResult(last.species, last.displayName, last.rarity,
                ThreadLocalRandom.current().nextDouble() < config.shinyChance);
    }

    private static void announceResults(ServerPlayer player, List<GachaResult> results, GachaBanner banner) {
        if (results.size() == 1) {
            GachaResult result = results.get(0);
            player.sendSystemMessage(Component.literal("Gacha result: " + result.label() + " · "
                    + result.rarity().displayName()).withStyle(result.rarity().formatting()));
            announceExceptionalDrop(player, result, banner.title);
            return;
        }
        Map<GachaRarity, Integer> rarityCounts = new EnumMap<>(GachaRarity.class);
        Map<String, Integer> speciesCounts = new LinkedHashMap<>();
        int shinyCount = 0;
        for (GachaResult result : results) {
            rarityCounts.merge(result.rarity(), 1, Integer::sum);
            speciesCounts.merge(result.species().toLowerCase(java.util.Locale.ROOT), 1, Integer::sum);
            if (result.shiny()) shinyCount++;
        }
        StringBuilder tiers = new StringBuilder("Ten-pull complete");
        for (GachaRarity rarity : GachaRarity.values()) {
            Integer count = rarityCounts.get(rarity);
            if (count != null) tiers.append(" · ").append(rarity.displayName()).append(' ').append(count);
        }
        int duplicates = speciesCounts.values().stream().mapToInt(count -> Math.max(0, count - 1)).sum();
        player.sendSystemMessage(Component.literal(tiers.toString()).withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("Shiny: " + shinyCount + " · duplicates: " + duplicates)
                .withStyle(shinyCount > 0 ? ChatFormatting.GOLD : ChatFormatting.GRAY));
        String summary = results.stream().map(result -> result.label()).reduce((left, right) -> left + ", " + right).orElse("");
        player.sendSystemMessage(Component.literal("Results: " + summary).withStyle(ChatFormatting.WHITE));
        results.forEach(result -> announceExceptionalDrop(player, result, banner.title));
    }

    /**
     * Sends only exceptional rewards to the online server population. Normal
     * pulls remain private, while Mythic/Legendary or shiny results can create
     * a celebratory moment without revealing coordinates or player location.
     */
    public static void announceExceptionalDrop(ServerPlayer player, GachaResult result, String source) {
        if (config == null || !config.announceExceptionalDrops || result == null || result.rarity() == null) return;
        boolean rarityQualifies = result.rarity().atLeast(config.serverAnnouncementMinimumRarity);
        if (!rarityQualifies && !(config.announceShinyDrops && result.shiny())) return;

        ChatFormatting accent = result.shiny() ? ChatFormatting.AQUA : result.rarity().formatting();
        Component message = Component.literal("[Gacha] ").withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(Component.literal(player.getGameProfile().getName()).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" obtained ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(result.label()).withStyle(accent))
                .append(Component.literal(" · " + result.rarity().displayName()
                        + " from " + (source == null || source.isBlank() ? "the gacha" : source))
                        .withStyle(ChatFormatting.GRAY));
        for (ServerPlayer recipient : player.server.getPlayerList().getPlayers()) {
            recipient.sendSystemMessage(message);
        }
    }

    public record DrawSummary(List<GachaResult> results, boolean success) {}
}
