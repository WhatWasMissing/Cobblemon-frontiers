package com.whatwasmissing.spawnannouncements.core;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.whatwasmissing.spawnannouncements.config.AnnouncementConfig;
import com.whatwasmissing.spawnannouncements.gui.FrontierShopMenu;
import com.whatwasmissing.spawnannouncements.util.BiomeRegionUtil;
import com.whatwasmissing.spawnannouncements.util.RarityDetector;
import kotlin.Unit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Main server-side feature coordinator. */
public final class AnnouncementService {
    public static final String MOD_ID = "cobblemon_frontiers";
    public static final String MOD_VERSION = "1.0.0";
    public static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MOD_ID);

    private static AnnouncementConfig config;
    private static FrontierLedger ledger;
    private static Path configDirectory;
    private static long lastGlobalAnnouncement;
    private static final Map<AnnouncementKind, Long> lastByKind = new EnumMap<>(AnnouncementKind.class);
    private static boolean initialised;

    private AnnouncementService() {}

    public static synchronized void init(Path configDirectory) {
        if (initialised) return;
        initialised = true;
        AnnouncementService.configDirectory = configDirectory.toAbsolutePath();
        Path directory = configDirectory.resolve(MOD_ID);
        config = AnnouncementConfig.load(directory.resolve("config.json"), LOGGER);
        ledger = FrontierLedger.load(directory.resolve("frontier_ledger.json"), LOGGER);

        CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe(Priority.NORMAL, event -> {
            if (event.getEntity() instanceof PokemonEntity pokemonEntity
                    && pokemonEntity.level() instanceof ServerLevel serverLevel) {
                onPokemonSpawned(serverLevel, pokemonEntity, event.getSpawnablePosition());
            }
            return Unit.INSTANCE;
        });

        CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.NORMAL, event -> {
            if (event.getPlayer().level() instanceof ServerLevel serverLevel) {
                onPokemonCaptured(serverLevel, event.getPlayer(), event.getPokemon());
            }
            return Unit.INSTANCE;
        });

        LOGGER.info("Cobblemon Frontiers initialised: anonymous regional spawn signals and field research enabled.");
    }

    public static AnnouncementConfig config() {
        return config;
    }

    public static FrontierLedger ledger() {
        return ledger;
    }

    public static synchronized void reload() {
        if (configDirectory != null) reload(configDirectory);
    }

    public static synchronized void reload(Path configDirectory) {
        Path directory = configDirectory.resolve(MOD_ID);
        config = AnnouncementConfig.load(directory.resolve("config.json"), LOGGER);
    }

    private static void onPokemonSpawned(ServerLevel level, PokemonEntity entity, SpawnablePosition position) {
        if (!config.enabled || !dimensionAllowed(level)) return;
        AnnouncementKind kind = RarityDetector.bestKind(entity, position);
        if (kind == null) return;
        // Rare spawn buckets are deliberately silent. Their UUID is retained
        // privately for a short time so capturing one pays a research bounty,
        // without creating a public signal or revealing a location. Ultra-rare
        // buckets use the normal anonymous announcement path below.
        if (kind == AnnouncementKind.RARE) {
            ledger.rememberRareSpawn(entity.getPokemon().getUuid(), kind, LOGGER);
            return;
        }
        if (!config.isEnabled(kind)) return;

        long now = System.currentTimeMillis();
        long globalCooldown = config.globalCooldownSeconds * 1000L;
        long kindCooldown = config.sameKindCooldownSeconds * 1000L;
        if (now - lastGlobalAnnouncement < globalCooldown) return;
        if (now - lastByKind.getOrDefault(kind, 0L) < kindCooldown) return;

        String region = BiomeRegionUtil.describe(level, entity.blockPosition(), config);
        MutableComponent message = format(kind, config.messageFor(kind, region, nearbyPlayerName(level, entity),
                dimensionLabel(level), timeLabel(level)));
        broadcast(level, entity.blockPosition(), message);
        lastGlobalAnnouncement = now;
        lastByKind.put(kind, now);
        ledger.addSignal(kind, region, level.dimension().location().toString(), entity.getPokemon().getUuid(), LOGGER);
    }

    private static void onPokemonCaptured(ServerLevel level, ServerPlayer player, Pokemon pokemon) {
        AnnouncementKind rareKind = ledger.consumeRareSpawn(pokemon.getUuid(), LOGGER);
        FrontierLedger.SignalRecord signal = ledger.resolveSignal(pokemon.getUuid(), "secured", LOGGER);
        FrontierLedger.CaptureUpdate update = ledger.recordCapture(player, pokemon, rareKind, signal, signal != null, config, LOGGER);
        if (signal != null) {
            broadcast(level, player.blockPosition(), Component.literal("A field signal in " + signal.region + " has gone quiet.")
                    .withStyle(ChatFormatting.DARK_AQUA));
        }

        if (update.signalBonus() > 0) {
            personal(player, Component.literal("Signal-linked capture: +" + update.signalBonus() + " research bonus.")
                    .withStyle(ChatFormatting.AQUA));
        }
        if (update.aftermathBonus() > 0) {
            personal(player, Component.literal("Signal aftermath: +" + update.aftermathBonus() + " research bonus.")
                    .withStyle(ChatFormatting.DARK_AQUA));
        }
        if (update.streakBonus() > 0) {
            personal(player, Component.literal("Regional survey streak " + update.regionalStreak() + ": +" + update.streakBonus() + " research bonus.")
                    .withStyle(ChatFormatting.GREEN));
        }
        if (update.rareBonus() > 0) {
            personal(player, Component.literal("Rare capture bounty: +" + update.rareBonus() + " research bonus.")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        if (update.dailyChallengeBonus() > 0) {
            personal(player, Component.literal("Daily research goal complete: +"
                            + update.dailyChallengeBonus() + " research points.")
                    .withStyle(ChatFormatting.GOLD));
        }
        if (update.contractBonus() > 0) {
            personal(player, Component.literal("Expedition contract complete: "
                            + String.join(", ", update.completedContracts()) + " · +"
                            + update.contractBonus() + " research.")
                    .withStyle(ChatFormatting.GOLD));
        }
        if (update.communityBonus() > 0) {
            personal(player, Component.literal("Community research complete: +" + update.communityBonus()
                            + " research for contributing to " + update.communityEvent().title() + ".")
                    .withStyle(ChatFormatting.GREEN));
        }
        if (update.newFieldGuideEntry()) {
            personal(player, Component.literal("Field Guide updated: " + update.fieldGuideSize()
                            + " species recorded.")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        personal(player, Component.literal("Field research: +" + update.pointsEarned()
                        + " · spendable balance: " + update.totalPoints()
                        + " · open Frontier Intelligence with K to exchange it.")
                .withStyle(ChatFormatting.AQUA));
        if (player.containerMenu instanceof FrontierShopMenu menu) {
            menu.refreshForServer(player);
        }
        if (update.newMilestones().isEmpty()) return;

        for (int milestone : update.newMilestones()) {
            grantMilestoneReward(player, milestone);
            personal(player, Component.literal("Field research milestone reached: " + milestone + " points. A reward has been issued.")
                    .withStyle(ChatFormatting.GOLD));
        }
    }

    private static void grantMilestoneReward(ServerPlayer player, int milestone) {
        ItemStack reward;
        int experience;
        switch (milestone) {
            case 25 -> { reward = new ItemStack(Items.GOLD_INGOT, 4); experience = 25; }
            case 100 -> { reward = new ItemStack(Items.AMETHYST_SHARD, 8); experience = 100; }
            case 250 -> { reward = new ItemStack(Items.ENDER_PEARL, 2); experience = 250; }
            case 500 -> { reward = new ItemStack(Items.DIAMOND, 1); experience = 500; }
            default -> { return; }
        }
        if (!player.getInventory().add(reward)) player.drop(reward, false);
        player.giveExperiencePoints(experience);
    }

    public static MutableComponent format(AnnouncementKind kind, String body) {
        ChatFormatting colour = switch (kind) {
            case SHINY_LEGENDARY, SHINY_MYTHICAL, SHINY_ULTRA_BEAST, SHINY_ALPHA, SHINY_PARADOX, SHINY -> ChatFormatting.GOLD;
            case LEGENDARY, MYTHICAL -> ChatFormatting.LIGHT_PURPLE;
            case ULTRA_BEAST, ULTRA_RARE -> ChatFormatting.DARK_PURPLE;
            case PARADOX -> ChatFormatting.AQUA;
            case ALPHA -> ChatFormatting.RED;
            case RARE -> ChatFormatting.GREEN;
        };
        return Component.literal("[Field Intelligence] ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(body).withStyle(colour));
    }

    private static void personal(ServerPlayer player, Component message) {
        String mode = ledger == null ? "chat" : ledger.feedbackMode(player.getUUID());
        if (mode == null || mode.isBlank()) mode = config == null ? "chat" : config.captureFeedbackMode;
        if ("silent".equals(mode)) return;
        if ("actionbar".equals(mode)) player.displayClientMessage(message, true);
        else player.sendSystemMessage(message);
    }

    private static void broadcast(ServerLevel sourceLevel, BlockPos sourcePosition, Component message) {
        MinecraftServer server = sourceLevel.getServer();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (ledger != null && !ledger.alertsEnabled(player.getUUID())) continue;
            boolean sameDimension = player.level().dimension().equals(sourceLevel.dimension());
            boolean deliver = switch (config.announcementScope) {
                case "same_dimension" -> sameDimension;
                case "nearby" -> sameDimension && player.blockPosition().distSqr(sourcePosition) <=
                        (double) config.announcementRadiusBlocks * config.announcementRadiusBlocks;
                default -> true;
            };
            if (deliver) player.sendSystemMessage(message);
        }
    }

    private static boolean dimensionAllowed(ServerLevel level) {
        if (config.allowedDimensions == null || config.allowedDimensions.isEmpty()) return true;
        return config.allowedDimensions.contains(level.dimension().location().toString().toLowerCase(java.util.Locale.ROOT));
    }

    private static String nearbyPlayerName(ServerLevel level, com.cobblemon.mod.common.entity.pokemon.PokemonEntity entity) {
        if (!config.announceNearbyPlayerName) return "";
        double radius = config.nearbyPlayerRadiusBlocks;
        double maxDistanceSquared = radius * radius;
        return level.players().stream()
                .filter(player -> player.distanceToSqr(entity) <= maxDistanceSquared)
                .min(Comparator.comparingDouble(entity::distanceToSqr))
                .map(player -> player.getGameProfile().getName())
                .orElse("");
    }

    private static String dimensionLabel(ServerLevel level) {
        if (level.dimension() == Level.NETHER) return "the Nether";
        if (level.dimension() == Level.END) return "the End";
        return "the overworld";
    }

    private static String timeLabel(ServerLevel level) {
        long time = Math.floorMod(level.getDayTime(), 24_000L);
        if (time < 1_000L) return "dawn";
        if (time < 11_000L) return "daylight";
        if (time < 13_000L) return "dusk";
        if (time < 23_000L) return "night";
        return "dawn";
    }

    public static List<FrontierLedger.SignalRecord> recentSignals() {
        return ledger == null ? Collections.emptyList() : ledger.recentSignals();
    }

    public static String statusLine() {
        if (config == null) return "not initialised";
        return "enabled=" + config.enabled
                + ", global cooldown=" + config.globalCooldownSeconds + "s"
                + ", same-kind cooldown=" + config.sameKindCooldownSeconds + "s"
                + ", announcement scope=" + config.announcementScope
                + ", rare bucket=silent capture bounty, ultra-rare=announced";
    }

    public static int points(UUID playerId) {
        return ledger == null ? 0 : ledger.pointsFor(playerId);
    }

    public static int totalEarned(UUID playerId) {
        return ledger == null ? 0 : ledger.totalEarnedFor(playerId);
    }

    public static boolean spendPoints(UUID playerId, int cost) {
        return ledger != null && ledger.spendPoints(playerId, cost, LOGGER);
    }

    public static int nextMilestone(UUID playerId) {
        return ledger == null ? -1 : ledger.nextMilestone(playerId);
    }

    public static Map<String, Integer> regionCaptureCounts(UUID playerId) {
        return ledger == null ? Map.of() : ledger.regionCaptureCounts(playerId);
    }

    public static Map<String, Integer> regionResearchPoints(UUID playerId) {
        return ledger == null ? Map.of() : ledger.regionResearchPoints(playerId);
    }

    public static int currentStreak(UUID playerId) {
        return ledger == null ? 0 : ledger.currentStreak(playerId);
    }

    public static int bestStreak(UUID playerId) {
        return ledger == null ? 0 : ledger.bestStreak(playerId);
    }

    public static FrontierLedger.DailyChallengeProgress dailyChallenge(UUID playerId) {
        return ledger == null
                ? new FrontierLedger.DailyChallengeProgress(0, 5, false, 10)
                : ledger.dailyChallenge(playerId);
    }

    public static List<FrontierLedger.ExpeditionProgress> expeditions(UUID playerId) {
        return ledger == null ? Collections.emptyList() : ledger.expeditions(playerId);
    }

    public static FrontierLedger.CommunityEventProgress communityEvent() {
        return ledger == null ? new FrontierLedger.CommunityEventProgress("Community Census", "No event is active.",
                "the wilds", 1, 0, 0, false, 0) : ledger.communityEvent();
    }

    public static List<String> fieldGuide(UUID playerId) {
        return ledger == null ? Collections.emptyList() : ledger.fieldGuide(playerId);
    }

    public static boolean alertsEnabled(UUID playerId) {
        return ledger == null || ledger.alertsEnabled(playerId);
    }

    public static void setAlertsEnabled(UUID playerId, boolean enabled) {
        if (ledger != null) ledger.setAlertsEnabled(playerId, enabled, LOGGER);
    }

    public static String feedbackMode(UUID playerId) {
        if (ledger == null) return config == null ? "chat" : config.captureFeedbackMode;
        String mode = ledger.feedbackMode(playerId);
        return mode.isBlank() ? config.captureFeedbackMode : mode;
    }

    public static void setFeedbackMode(UUID playerId, String mode) {
        if (ledger != null) ledger.setFeedbackMode(playerId, mode, LOGGER);
    }

    public static void flushLedger() {
        if (ledger != null) ledger.flush();
    }

    public static List<FrontierLedger.LeaderboardEntry> leaderboard() {
        return ledger == null ? new ArrayList<>() : ledger.leaderboard();
    }

    public static AnnouncementKind parseKind(String text) {
        return AnnouncementKind.fromId(text);
    }

    public static void openShop(ServerPlayer player) {
        if (player.isRemoved()) return;
        // A keybind can be pressed while another container is open. Closing it
        // first prevents the server from rejecting the new menu transition.
        player.closeContainer();
        java.util.OptionalInt containerId = player.openMenu(new SimpleMenuProvider(
                (menuContainerId, inventory, ignored) -> new FrontierShopMenu(menuContainerId, inventory),
                Component.literal("Frontier Intelligence")
        ));
        if (containerId.isEmpty()) {
            LOGGER.warn("Could not open Frontier Intelligence for {}", player.getGameProfile().getName());
            player.sendSystemMessage(Component.literal("Frontier Intelligence could not open. Try again in a moment.")
                    .withStyle(ChatFormatting.RED));
        } else if (player.containerMenu instanceof FrontierShopMenu menu) {
            menu.syncToClient(player);
        }
    }
}
