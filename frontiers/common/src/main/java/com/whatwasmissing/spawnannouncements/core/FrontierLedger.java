package com.whatwasmissing.spawnannouncements.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.whatwasmissing.spawnannouncements.util.BiomeRegionUtil;
import com.whatwasmissing.spawnannouncements.config.AnnouncementConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/** Persistent server ledger for signals, field research, and expedition state. */
public final class FrontierLedger implements AutoCloseable {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAX_SIGNALS = 24;
    private static final long SIGNAL_LIFETIME_MILLIS = 15 * 60 * 1000L;
    private static final long RARE_SPAWN_LIFETIME_MILLIS = 30 * 60 * 1000L;
    private static final long SIGNAL_AFTERMATH_MILLIS = 5 * 60 * 1000L;
    private static final int DAILY_CAPTURE_GOAL = 5;
    private static final int DAILY_CAPTURE_REWARD = 10;
    private static final int COMMUNITY_EVENT_GOAL = 100;
    private static final int COMMUNITY_EVENT_REWARD = 10;

    private static final List<ContractDefinition> CONTRACT_POOL = List.of(
            new ContractDefinition("wetland_survey", "Wetland Survey", "Capture 4 Pokémon in the wetlands.", 4, 5),
            new ContractDefinition("frozen_survey", "Frozen Frontier", "Capture 4 Pokémon in the frozen highlands.", 4, 5),
            new ContractDefinition("night_watch", "Night Watch", "Capture 3 Pokémon during the night.", 3, 6),
            new ContractDefinition("nether_expedition", "Nether Expedition", "Capture 3 Pokémon in the Nether.", 3, 8),
            new ContractDefinition("signal_response", "Signal Response", "Secure 1 announced field signal.", 1, 8),
            new ContractDefinition("exceptional_record", "Exceptional Record", "Capture 1 shiny or exceptional Pokémon.", 1, 10),
            new ContractDefinition("waterside_survey", "Waterside Survey", "Capture 4 Pokémon near waterside biomes.", 4, 5),
            new ContractDefinition("underground_survey", "Underground Survey", "Capture 4 Pokémon below the surface.", 4, 5)
    );

    private static final List<CommunityDefinition> COMMUNITY_POOL = List.of(
            new CommunityDefinition("the wetlands", "Wetlands Census", "Contribute 100 captures to the wetlands census."),
            new CommunityDefinition("the forest", "Forest Census", "Contribute 100 captures to the forest census."),
            new CommunityDefinition("the waterside", "Waterside Census", "Contribute 100 captures to the waterside census."),
            new CommunityDefinition("the underground", "Underground Census", "Contribute 100 captures to the underground census."),
            new CommunityDefinition("the Nether", "Nether Census", "Contribute 100 captures to the Nether census."),
            new CommunityDefinition("the frozen highlands", "Frozen Census", "Contribute 100 captures to the frozen-highlands census.")
    );

    private final Path path;
    private final LedgerData data;
    private final org.slf4j.Logger logger;
    private final ScheduledExecutorService saver;
    private boolean dirty;

    private FrontierLedger(Path path, LedgerData data, org.slf4j.Logger logger) {
        this.path = path;
        this.data = data;
        this.logger = logger;
        this.saver = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "cobblemon-frontiers-ledger-save");
            thread.setDaemon(true);
            return thread;
        });
        this.saver.scheduleAtFixedRate(this::flushIfDirty, 5, 5, TimeUnit.SECONDS);
    }

    public static FrontierLedger load(Path path, org.slf4j.Logger logger) {
        LedgerData loadedData = new LedgerData();
        boolean unreadable = false;
        try {
            if (Files.exists(path)) {
                LedgerData loaded = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), LedgerData.class);
                if (loaded != null) {
                    loadedData = loaded;
                } else {
                    unreadable = true;
                    logger.error("Frontier ledger {} contained no data. Keeping a corrupt backup and starting empty.", path);
                    preserveUnreadableLedger(path, logger);
                }
            }
        } catch (IOException | JsonParseException | IllegalStateException exception) {
            unreadable = true;
            logger.error("Could not read frontier ledger {}. Keeping a corrupt backup and starting empty.", path, exception);
            preserveUnreadableLedger(path, logger);
        }
        loadedData.normalise();
        FrontierLedger ledger = new FrontierLedger(path, loadedData, logger);
        ledger.prune(System.currentTimeMillis());
        ledger.ensureCommunityEvent();
        if (unreadable) ledger.markDirty();
        return ledger;
    }

    private static void preserveUnreadableLedger(Path path, org.slf4j.Logger logger) {
        if (!Files.exists(path)) return;
        try {
            Path backup = path.resolveSibling(path.getFileName() + ".corrupt-" + System.currentTimeMillis());
            Files.move(path, backup, StandardCopyOption.REPLACE_EXISTING);
            logger.error("Moved unreadable frontier ledger to {}.", backup);
        } catch (IOException backupException) {
            logger.error("Could not preserve unreadable frontier ledger {}.", path, backupException);
        }
    }

    /** Marks the ledger for a batched atomic save. */
    public synchronized void save(org.slf4j.Logger ignored) {
        dirty = true;
    }

    public synchronized void flush() {
        saveNow();
    }

    private synchronized void flushIfDirty() {
        if (dirty) saveNow();
    }

    private synchronized void saveNow() {
        if (!dirty) return;
        try {
            Files.createDirectories(path.getParent());
            Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
            Files.writeString(temporary, GSON.toJson(data), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
            dirty = false;
        } catch (IOException exception) {
            logger.error("Could not save frontier ledger {}.", path, exception);
        }
    }

    private synchronized void markDirty() {
        dirty = true;
    }

    @Override
    public synchronized void close() {
        saver.shutdownNow();
        saveNow();
    }

    public synchronized void addSignal(AnnouncementKind kind, String region, String dimension, UUID pokemonUuid,
                                        org.slf4j.Logger ignored) {
        prune(System.currentTimeMillis());
        SignalRecord record = new SignalRecord();
        record.id = UUID.randomUUID().toString();
        record.pokemonUuid = pokemonUuid.toString();
        record.kind = kind.id();
        record.region = region;
        record.dimension = dimension;
        record.createdAt = System.currentTimeMillis();
        record.status = "active";
        data.signals.add(0, record);
        while (data.signals.size() > MAX_SIGNALS) data.signals.remove(data.signals.size() - 1);
        markDirty();
    }

    public synchronized void rememberRareSpawn(UUID pokemonUuid, AnnouncementKind kind, org.slf4j.Logger ignored) {
        if (kind != AnnouncementKind.RARE) return;
        prune(System.currentTimeMillis());
        RareSpawnRecord record = new RareSpawnRecord();
        record.kind = kind.id();
        record.createdAt = System.currentTimeMillis();
        data.rareSpawns.put(pokemonUuid.toString(), record);
        markDirty();
    }

    public synchronized AnnouncementKind consumeRareSpawn(UUID pokemonUuid, org.slf4j.Logger ignored) {
        prune(System.currentTimeMillis());
        RareSpawnRecord record = data.rareSpawns.remove(pokemonUuid.toString());
        if (record == null) return null;
        markDirty();
        AnnouncementKind kind = AnnouncementKind.fromId(record.kind);
        return kind == AnnouncementKind.RARE ? kind : null;
    }

    public synchronized SignalRecord resolveSignal(UUID pokemonUuid, String status, org.slf4j.Logger ignored) {
        prune(System.currentTimeMillis());
        String target = pokemonUuid.toString();
        for (SignalRecord signal : data.signals) {
            if (target.equals(signal.pokemonUuid) && "active".equals(signal.status)) {
                signal.status = status;
                signal.resolvedAt = System.currentTimeMillis();
                if ("secured".equals(status)) {
                    SignalAftermath aftermath = new SignalAftermath();
                    aftermath.region = signal.region;
                    aftermath.dimension = signal.dimension;
                    aftermath.createdAt = signal.resolvedAt;
                    aftermath.expiresAt = signal.resolvedAt + SIGNAL_AFTERMATH_MILLIS;
                    data.aftermath = aftermath;
                }
                markDirty();
                return signal;
            }
        }
        return null;
    }

    public synchronized List<SignalRecord> recentSignals() {
        prune(System.currentTimeMillis());
        return new ArrayList<>(data.signals);
    }

    public synchronized CaptureUpdate recordCapture(ServerPlayer player, Pokemon pokemon, AnnouncementKind rareKind,
                                                    org.slf4j.Logger ignored) {
        return recordCapture(player, pokemon, rareKind, null, null, ignored);
    }

    public synchronized CaptureUpdate recordCapture(ServerPlayer player, Pokemon pokemon, AnnouncementKind rareKind,
                                                    SignalRecord linkedSignal,
                                                    AnnouncementConfig config, org.slf4j.Logger ignored) {
        prune(System.currentTimeMillis());
        ensureCommunityEvent();
        String playerId = player.getUUID().toString();
        ResearchProfile profile = data.players.computeIfAbsent(playerId, ignoredPlayer -> new ResearchProfile());
        String today = todayUtc();
        resetDailyStateIfNeeded(profile, today);
        ServerLevel serverLevel = (ServerLevel) player.level();
        String region = BiomeRegionUtil.describe(serverLevel, player.blockPosition(), config);
        String dimension = serverLevel.dimension().location().toString();
        boolean newFieldGuideEntry = profile.fieldGuide.add(pokemon.getSpecies().getName().toLowerCase(Locale.ROOT));
        int signalBonus = linkedSignal == null ? 0 : 3;
        int aftermathBonus = linkedSignal == null && signalAftermathMatches(region, dimension) ? 1 : 0;
        if (region.equals(profile.lastRegion)) profile.regionalStreak++;
        else profile.regionalStreak = 1;
        profile.lastRegion = region;
        profile.bestRegionalStreak = Math.max(profile.bestRegionalStreak, profile.regionalStreak);
        int streakBonus = profile.regionalStreak % 5 == 0 ? 2 : 0;
        int rareBonus = rareKind == AnnouncementKind.RARE ? 6 : 0;
        int challengeBonus = 0;
        if (!profile.dailyChallengeCompleted) {
            profile.dailyCaptures = Math.min(DAILY_CAPTURE_GOAL, profile.dailyCaptures + 1);
            if (profile.dailyCaptures >= DAILY_CAPTURE_GOAL) {
                profile.dailyChallengeCompleted = true;
                challengeBonus = DAILY_CAPTURE_REWARD;
            }
        }

        List<ExpeditionContract> contracts = activeExpeditions(today);
        int contractBonus = 0;
        List<String> completedContracts = new ArrayList<>();
        for (ExpeditionContract contract : contracts) {
            if (profile.completedContracts.contains(contract.id())) continue;
            if (!matchesContract(contract.id(), player, pokemon, region, dimension, linkedSignal)) continue;
            int progress = Math.min(contract.goal(), profile.contractProgress.getOrDefault(contract.id(), 0) + 1);
            profile.contractProgress.put(contract.id(), progress);
            if (progress >= contract.goal()) {
                profile.completedContracts.add(contract.id());
                contractBonus += contract.reward();
                completedContracts.add(contract.title());
            }
        }

        int basePoints = pointsFor(pokemon) + signalBonus + aftermathBonus + streakBonus
                + rareBonus + challengeBonus + contractBonus;
        profile.points += basePoints;
        profile.totalEarned += basePoints;
        profile.regionCaptures.merge(region, 1, Integer::sum);
        profile.regionPoints.merge(region, basePoints, Integer::sum);

        int communityBonus = updateCommunityEvent(profile, playerId, region);
        List<Integer> newlyClaimed = new ArrayList<>();
        for (int milestone : MILESTONES) {
            if (profile.totalEarned >= milestone && profile.claimedMilestones.add(milestone)) newlyClaimed.add(milestone);
        }
        markDirty();
        return new CaptureUpdate(basePoints + communityBonus, profile.points, newlyClaimed, region,
                signalBonus, aftermathBonus, streakBonus, rareBonus, profile.regionalStreak,
                challengeBonus, profile.dailyCaptures, profile.dailyChallengeCompleted, contractBonus,
                completedContracts, communityBonus, communityEvent(), newFieldGuideEntry, profile.fieldGuide.size());
    }

    private int updateCommunityEvent(ResearchProfile currentProfile, String playerId, String region) {
        CommunityEventState event = data.communityEvent;
        if (event.completed || !event.targetRegion.equals(region)) return 0;
        event.progress = Math.min(event.goal, event.progress + 1);
        event.contributors.add(playerId);
        if (event.progress < event.goal) return 0;
        event.completed = true;
        int currentReward = 0;
        for (String contributorId : event.contributors) {
            if (!event.rewarded.add(contributorId)) continue;
            ResearchProfile contributor = data.players.computeIfAbsent(contributorId, ignored -> new ResearchProfile());
            contributor.points += event.reward;
            contributor.totalEarned += event.reward;
            if (contributorId.equals(playerId)) {
                currentProfile.regionPoints.merge(region, event.reward, Integer::sum);
                currentReward = event.reward;
            }
        }
        return currentReward;
    }

    private boolean signalAftermathMatches(String region, String dimension) {
        SignalAftermath aftermath = data.aftermath;
        return aftermath != null && aftermath.expiresAt > System.currentTimeMillis()
                && region.equals(aftermath.region) && dimension.equals(aftermath.dimension);
    }

    private static boolean matchesContract(String id, ServerPlayer player, Pokemon pokemon, String region,
                                           String dimension, SignalRecord linkedSignal) {
        return switch (id) {
            case "wetland_survey" -> region.equals("the wetlands");
            case "frozen_survey" -> region.equals("the frozen highlands");
            case "night_watch" -> {
                long time = Math.floorMod(player.level().getDayTime(), 24_000L);
                yield time >= 13_000L && time < 23_000L;
            }
            case "nether_expedition" -> dimension.equals("minecraft:the_nether");
            case "signal_response" -> linkedSignal != null;
            case "exceptional_record" -> pokemon.getShiny() || pokemon.isLegendary() || pokemon.isMythical()
                    || pokemon.isUltraBeast() || pokemon.isAlpha();
            case "waterside_survey" -> region.equals("the waterside");
            case "underground_survey" -> region.equals("the underground");
            default -> false;
        };
    }

    public synchronized int pointsFor(Pokemon pokemon) {
        int points = 1;
        if (pokemon.getShiny()) points += 9;
        if (pokemon.isLegendary()) points += 14;
        if (pokemon.isMythical()) points += 14;
        if (pokemon.isUltraBeast()) points += 12;
        if (pokemon.isAlpha()) points += 5;
        return points;
    }

    public synchronized int pointsFor(UUID playerId) {
        ResearchProfile profile = data.players.get(playerId.toString());
        return profile == null ? 0 : profile.points;
    }

    public synchronized int totalEarnedFor(UUID playerId) {
        ResearchProfile profile = data.players.get(playerId.toString());
        return profile == null ? 0 : profile.totalEarned;
    }

    public synchronized boolean spendPoints(UUID playerId, int cost, org.slf4j.Logger ignored) {
        if (cost < 0) return false;
        ResearchProfile profile = data.players.get(playerId.toString());
        if (profile == null || profile.points < cost) return false;
        int regionalTotal = profile.regionPoints.values().stream().filter(java.util.Objects::nonNull)
                .mapToInt(value -> Math.max(0, value)).sum();
        int legacyBalance = Math.max(0, profile.points - regionalTotal);
        int remaining = Math.max(0, cost - Math.min(cost, legacyBalance));
        if (remaining > 0) {
            int regionalAvailable = Math.min(remaining, regionalTotal);
            if (regionalAvailable < remaining) return false;
            for (Map.Entry<String, Integer> entry : profile.regionPoints.entrySet()) {
                if (remaining <= 0) break;
                int available = Math.max(0, entry.getValue());
                int used = Math.min(available, remaining);
                if (used > 0) {
                    entry.setValue(available - used);
                    remaining -= used;
                }
            }
        }
        profile.points -= cost;
        markDirty();
        return true;
    }

    public synchronized List<LeaderboardEntry> leaderboard() {
        return data.players.entrySet().stream().map(entry -> {
                    try {
                        return new LeaderboardEntry(UUID.fromString(entry.getKey()), entry.getValue().totalEarned);
                    } catch (IllegalArgumentException exception) {
                        return null;
                    }
                }).filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparingInt(LeaderboardEntry::points).reversed())
                .limit(5).collect(Collectors.toList());
    }

    public synchronized int nextMilestone(UUID playerId) {
        ResearchProfile profile = data.players.get(playerId.toString());
        int points = profile == null ? 0 : profile.totalEarned;
        for (int milestone : MILESTONES) if (points < milestone) return milestone;
        return -1;
    }

    public synchronized Map<String, Integer> regionCaptureCounts(UUID playerId) {
        ResearchProfile profile = data.players.get(playerId.toString());
        return profile == null ? Map.of() : new LinkedHashMap<>(profile.regionCaptures);
    }

    public synchronized Map<String, Integer> regionResearchPoints(UUID playerId) {
        ResearchProfile profile = data.players.get(playerId.toString());
        return profile == null ? Map.of() : new LinkedHashMap<>(profile.regionPoints);
    }

    public synchronized int currentStreak(UUID playerId) {
        ResearchProfile profile = data.players.get(playerId.toString());
        return profile == null ? 0 : profile.regionalStreak;
    }

    public synchronized int bestStreak(UUID playerId) {
        ResearchProfile profile = data.players.get(playerId.toString());
        return profile == null ? 0 : profile.bestRegionalStreak;
    }

    public synchronized DailyChallengeProgress dailyChallenge(UUID playerId) {
        ResearchProfile profile = data.players.get(playerId.toString());
        if (profile == null || !todayUtc().equals(profile.dailyChallengeDate)) {
            return new DailyChallengeProgress(0, DAILY_CAPTURE_GOAL, false, DAILY_CAPTURE_REWARD);
        }
        return new DailyChallengeProgress(profile.dailyCaptures, DAILY_CAPTURE_GOAL,
                profile.dailyChallengeCompleted, DAILY_CAPTURE_REWARD);
    }

    public synchronized List<ExpeditionProgress> expeditions(UUID playerId) {
        ResearchProfile profile = data.players.get(playerId.toString());
        String today = todayUtc();
        if (profile == null || !today.equals(profile.contractDate)) {
            return activeExpeditions(today).stream().map(contract ->
                    new ExpeditionProgress(contract, 0, false)).toList();
        }
        return activeExpeditions(today).stream().map(contract -> new ExpeditionProgress(contract,
                Math.min(contract.goal(), profile.contractProgress.getOrDefault(contract.id(), 0)),
                profile.completedContracts.contains(contract.id()))).toList();
    }

    public synchronized CommunityEventProgress communityEvent() {
        ensureCommunityEvent();
        return new CommunityEventProgress(data.communityEvent.title, data.communityEvent.description,
                data.communityEvent.targetRegion, data.communityEvent.goal, data.communityEvent.progress,
                data.communityEvent.reward, data.communityEvent.completed, data.communityEvent.contributors.size());
    }

    public synchronized List<String> fieldGuide(UUID playerId) {
        ResearchProfile profile = data.players.get(playerId.toString());
        return profile == null ? List.of() : profile.fieldGuide.stream().sorted().toList();
    }

    public synchronized boolean alertsEnabled(UUID playerId) {
        ResearchProfile profile = data.players.get(playerId.toString());
        return profile == null || profile.alertsEnabled;
    }

    public synchronized void setAlertsEnabled(UUID playerId, boolean enabled, org.slf4j.Logger ignored) {
        ResearchProfile profile = data.players.computeIfAbsent(playerId.toString(), ignoredPlayer -> new ResearchProfile());
        profile.alertsEnabled = enabled;
        markDirty();
    }

    public synchronized String feedbackMode(UUID playerId) {
        ResearchProfile profile = data.players.get(playerId.toString());
        return profile == null ? "" : profile.feedbackMode;
    }

    public synchronized void setFeedbackMode(UUID playerId, String mode, org.slf4j.Logger ignored) {
        ResearchProfile profile = data.players.computeIfAbsent(playerId.toString(), ignoredPlayer -> new ResearchProfile());
        String normalised = mode == null ? "" : mode.toLowerCase(Locale.ROOT).trim();
        profile.feedbackMode = normalised.equals("chat") || normalised.equals("actionbar") || normalised.equals("silent")
                ? normalised : "";
        markDirty();
    }

    public static int[] milestones() { return MILESTONES.clone(); }

    private void prune(long now) {
        boolean changed = data.signals.removeIf(signal -> signal == null || signal.pokemonUuid == null
                || signal.createdAt <= 0 || now - signal.createdAt > SIGNAL_LIFETIME_MILLIS && !"active".equals(signal.status));
        changed |= data.rareSpawns.entrySet().removeIf(entry -> entry.getKey() == null || entry.getValue() == null
                || entry.getValue().createdAt <= 0 || now - entry.getValue().createdAt > RARE_SPAWN_LIFETIME_MILLIS);
        for (SignalRecord signal : data.signals) {
            if ("active".equals(signal.status) && now - signal.createdAt > SIGNAL_LIFETIME_MILLIS) {
                signal.status = "faded";
                signal.resolvedAt = now;
                changed = true;
            }
        }
        if (data.aftermath != null && data.aftermath.expiresAt <= now) {
            data.aftermath = null;
            changed = true;
        }
        while (data.signals.size() > MAX_SIGNALS) {
            data.signals.remove(data.signals.size() - 1);
            changed = true;
        }
        if (changed) markDirty();
    }

    private void resetDailyStateIfNeeded(ResearchProfile profile, String today) {
        if (!today.equals(profile.dailyChallengeDate)) {
            profile.dailyChallengeDate = today;
            profile.dailyCaptures = 0;
            profile.dailyChallengeCompleted = false;
        }
        if (!today.equals(profile.contractDate)) {
            profile.contractDate = today;
            profile.contractProgress.clear();
            profile.completedContracts.clear();
        }
    }

    private void ensureCommunityEvent() {
        String week = communityWeekKey();
        if (data.communityEvent == null || !week.equals(data.communityEvent.weekKey)) {
            CommunityDefinition definition = COMMUNITY_POOL.get(Math.floorMod((int) LocalDate.parse(week).toEpochDay(), COMMUNITY_POOL.size()));
            CommunityEventState event = new CommunityEventState();
            event.weekKey = week;
            event.title = definition.title;
            event.description = definition.description;
            event.targetRegion = definition.targetRegion;
            event.goal = COMMUNITY_EVENT_GOAL;
            event.reward = COMMUNITY_EVENT_REWARD;
            data.communityEvent = event;
            markDirty();
        }
    }

    private static String communityWeekKey() {
        return LocalDate.now(ZoneOffset.UTC).with(DayOfWeek.MONDAY).toString();
    }

    private static List<ExpeditionContract> activeExpeditions(String today) {
        long day = LocalDate.parse(today).toEpochDay();
        List<ExpeditionContract> contracts = new ArrayList<>();
        int start = Math.floorMod((int) day, CONTRACT_POOL.size());
        for (int offset = 0; offset < 3; offset++) {
            ContractDefinition definition = CONTRACT_POOL.get((start + offset) % CONTRACT_POOL.size());
            contracts.add(new ExpeditionContract(definition.id, definition.title, definition.description,
                    definition.goal, definition.reward));
        }
        return contracts;
    }

    private static String todayUtc() { return LocalDate.now(ZoneOffset.UTC).toString(); }

    private static final int[] MILESTONES = {25, 100, 250, 500};

    public static final class LedgerData {
        public List<SignalRecord> signals = new ArrayList<>();
        public Map<String, RareSpawnRecord> rareSpawns = new LinkedHashMap<>();
        public Map<String, ResearchProfile> players = new LinkedHashMap<>();
        public SignalAftermath aftermath;
        public CommunityEventState communityEvent;

        void normalise() {
            if (signals == null) signals = new ArrayList<>();
            if (rareSpawns == null) rareSpawns = new LinkedHashMap<>();
            if (players == null) players = new LinkedHashMap<>();
            signals.removeIf(signal -> signal == null || signal.pokemonUuid == null);
            signals.forEach(signal -> {
                if (signal.id == null) signal.id = "";
                if (signal.kind == null) signal.kind = "rare";
                if (signal.region == null) signal.region = "the wilds";
                if (signal.dimension == null) signal.dimension = "minecraft:overworld";
                if (signal.status == null) signal.status = "active";
            });
            rareSpawns.entrySet().removeIf(entry -> entry.getKey() == null || entry.getValue() == null);
            players.entrySet().removeIf(entry -> entry.getKey() == null || entry.getValue() == null);
            players.values().forEach(ResearchProfile::normalise);
            if (aftermath != null && aftermath.region == null) aftermath = null;
            if (communityEvent != null) communityEvent.normalise();
        }
    }

    public static final class RareSpawnRecord {
        public String kind = "rare";
        public long createdAt;
    }

    public static final class SignalAftermath {
        public String region = "the wilds";
        public String dimension = "minecraft:overworld";
        public long createdAt;
        public long expiresAt;
    }

    public static final class SignalRecord {
        public String id = "";
        public String pokemonUuid = "";
        public String kind = "rare";
        public String region = "the wilds";
        public String dimension = "minecraft:overworld";
        public long createdAt;
        public long resolvedAt;
        public String status = "active";

        public AnnouncementKind kindEnum() {
            AnnouncementKind parsed = AnnouncementKind.fromId(kind);
            return parsed == null ? AnnouncementKind.RARE : parsed;
        }
    }

    public static final class CommunityEventState {
        public String weekKey = "";
        public String title = "";
        public String description = "";
        public String targetRegion = "the wilds";
        public int goal = COMMUNITY_EVENT_GOAL;
        public int progress;
        public int reward = COMMUNITY_EVENT_REWARD;
        public boolean completed;
        public Set<String> contributors = new LinkedHashSet<>();
        public Set<String> rewarded = new LinkedHashSet<>();

        void normalise() {
            if (contributors == null) contributors = new LinkedHashSet<>();
            if (rewarded == null) rewarded = new LinkedHashSet<>();
            if (weekKey == null) weekKey = "";
            if (title == null) title = "";
            if (description == null) description = "";
            if (targetRegion == null || targetRegion.isBlank()) targetRegion = "the wilds";
            goal = Math.max(1, goal);
            reward = Math.max(0, reward);
            progress = Math.max(0, Math.min(goal, progress));
        }
    }

    public static final class ResearchProfile {
        public int points;
        public int totalEarned;
        public Map<String, Integer> regionCaptures = new LinkedHashMap<>();
        public Map<String, Integer> regionPoints = new LinkedHashMap<>();
        public Set<Integer> claimedMilestones = new LinkedHashSet<>();
        public String lastRegion = "";
        public int regionalStreak;
        public int bestRegionalStreak;
        public String dailyChallengeDate = "";
        public int dailyCaptures;
        public boolean dailyChallengeCompleted;
        public String contractDate = "";
        public Map<String, Integer> contractProgress = new LinkedHashMap<>();
        public Set<String> completedContracts = new LinkedHashSet<>();
        /** Private species collection; never included in public signal records or alerts. */
        public Set<String> fieldGuide = new LinkedHashSet<>();
        public boolean alertsEnabled = true;
        public String feedbackMode = "";

        void normalise() {
            if (regionCaptures == null) regionCaptures = new LinkedHashMap<>();
            if (regionPoints == null) regionPoints = new LinkedHashMap<>();
            if (claimedMilestones == null) claimedMilestones = new LinkedHashSet<>();
            if (contractProgress == null) contractProgress = new LinkedHashMap<>();
            if (completedContracts == null) completedContracts = new LinkedHashSet<>();
            if (fieldGuide == null) fieldGuide = new LinkedHashSet<>();
            if (lastRegion == null) lastRegion = "";
            if (dailyChallengeDate == null) dailyChallengeDate = "";
            if (contractDate == null) contractDate = "";
            if (feedbackMode == null) feedbackMode = "";
            points = Math.max(0, points);
            totalEarned = Math.max(points, totalEarned);
            regionCaptures.entrySet().removeIf(entry -> entry.getKey() == null || entry.getValue() == null || entry.getValue() < 0);
            regionPoints.entrySet().removeIf(entry -> entry.getKey() == null || entry.getValue() == null || entry.getValue() < 0);
            contractProgress.entrySet().removeIf(entry -> entry.getKey() == null || entry.getValue() == null || entry.getValue() < 0);
            fieldGuide.removeIf(entry -> entry == null || entry.isBlank());
            regionalStreak = Math.max(0, regionalStreak);
            bestRegionalStreak = Math.max(regionalStreak, Math.max(0, bestRegionalStreak));
            dailyCaptures = Math.max(0, Math.min(DAILY_CAPTURE_GOAL, dailyCaptures));
            if (dailyCaptures >= DAILY_CAPTURE_GOAL) dailyChallengeCompleted = true;

            long regionalTotal = regionPoints.values().stream()
                    .filter(java.util.Objects::nonNull)
                    .mapToLong(value -> Math.max(0L, value))
                    .sum();
            long excess = Math.max(0L, regionalTotal - points);
            if (excess > 0L) {
                for (Map.Entry<String, Integer> entry : regionPoints.entrySet()) {
                    if (excess <= 0L) break;
                    int available = Math.max(0, entry.getValue());
                    int reduction = (int) Math.min((long) available, excess);
                    entry.setValue(available - reduction);
                    excess -= reduction;
                }
            }
        }
    }

    private record ContractDefinition(String id, String title, String description, int goal, int reward) {}
    private record CommunityDefinition(String targetRegion, String title, String description) {}

    public record CaptureUpdate(int pointsEarned, int totalPoints, List<Integer> newMilestones, String region,
                                int signalBonus, int aftermathBonus, int streakBonus, int rareBonus,
                                int regionalStreak, int dailyChallengeBonus, int dailyCaptures,
                                boolean dailyChallengeCompleted, int contractBonus,
                                List<String> completedContracts, int communityBonus,
                                CommunityEventProgress communityEvent, boolean newFieldGuideEntry,
                                int fieldGuideSize) {}
    public record DailyChallengeProgress(int captures, int goal, boolean completed, int reward) {}
    public record ExpeditionContract(String id, String title, String description, int goal, int reward) {}
    public record ExpeditionProgress(ExpeditionContract contract, int progress, boolean completed) {}
    public record CommunityEventProgress(String title, String description, String targetRegion, int goal,
                                         int progress, int reward, boolean completed, int contributors) {}
    public record LeaderboardEntry(UUID playerId, int points) {}
}
