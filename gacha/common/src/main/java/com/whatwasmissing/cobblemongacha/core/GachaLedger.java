package com.whatwasmissing.cobblemongacha.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Batched, server-side persistence for tickets, pity, and recent pulls. */
public final class GachaLedger {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAX_HISTORY = 12;
    private static final long SAVE_INTERVAL_SECONDS = 5L;

    private final Path path;
    private final LedgerData data;
    private final Logger logger;
    private final ScheduledExecutorService saver;
    private boolean dirty;

    private GachaLedger(Path path, LedgerData data, Logger logger) {
        this.path = path;
        this.data = data;
        this.logger = logger;
        this.saver = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "cobblemon-gacha-ledger");
            thread.setDaemon(true);
            return thread;
        });
        this.saver.scheduleWithFixedDelay(this::flushIfDirty, SAVE_INTERVAL_SECONDS,
                SAVE_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    public static GachaLedger load(Path path, Logger logger) {
        LedgerData data = new LedgerData();
        boolean fileExisted = Files.exists(path);
        boolean loadedSuccessfully = !fileExisted;
        try {
            Files.createDirectories(path.getParent());
            if (fileExisted) {
                LedgerData loaded = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), LedgerData.class);
                if (loaded != null) {
                    data = loaded;
                    loadedSuccessfully = true;
                } else {
                    logger.error("Cobblemon Gacha ledger {} contained no data. Keeping the file untouched and starting empty.", path);
                    preserveUnreadableLedger(path, logger);
                }
            }
        } catch (IOException | JsonParseException | IllegalStateException exception) {
            logger.error("Could not read Cobblemon Gacha ledger {}. Starting empty.", path, exception);
            preserveUnreadableLedger(path, logger);
        }
        data.normalise();
        GachaLedger ledger = new GachaLedger(path, data, logger);
        // Never rewrite an existing unreadable ledger during load. A transient
        // read failure must not destroy the only copy of a player's tickets.
        if (!fileExisted || loadedSuccessfully && !Files.exists(path)) ledger.saveNow();
        return ledger;
    }

    private static void preserveUnreadableLedger(Path path, Logger logger) {
        if (!Files.exists(path)) return;
        Path recovery = path.resolveSibling(path.getFileName() + ".corrupt");
        if (Files.exists(recovery)) return;
        try {
            Files.copy(path, recovery);
            logger.error("Preserved the unreadable Cobblemon Gacha ledger at {}.", recovery);
        } catch (IOException copyException) {
            logger.error("Could not preserve the unreadable Cobblemon Gacha ledger {}.", path, copyException);
        }
    }

    public synchronized PlayerProfile profile(UUID uuid, int startingTickets) {
        PlayerProfile profile = data.players.computeIfAbsent(uuid.toString(), ignored -> new PlayerProfile());
        if (!profile.initialised) {
            profile.initialised = true;
            profile.tickets = Math.max(0, startingTickets);
            markDirty();
        }
        profile.normalise();
        return profile;
    }

    public synchronized int tickets(UUID uuid, int startingTickets) {
        return profile(uuid, startingTickets).tickets;
    }

    public synchronized boolean spendTickets(UUID uuid, int amount, int startingTickets) {
        PlayerProfile profile = profile(uuid, startingTickets);
        if (amount <= 0 || profile.tickets < amount) return false;
        profile.tickets -= amount;
        markDirty();
        return true;
    }

    public synchronized void refundTickets(UUID uuid, int amount, int startingTickets) {
        if (amount <= 0) return;
        PlayerProfile profile = profile(uuid, startingTickets);
        profile.tickets += amount;
        markDirty();
    }

    /** Adds server-authorised test tickets without bypassing ledger persistence. */
    public synchronized void grantTickets(UUID uuid, int amount, int startingTickets) {
        if (amount <= 0) return;
        PlayerProfile profile = profile(uuid, startingTickets);
        profile.tickets += amount;
        markDirty();
    }

    public synchronized CaptureProgress recordCapture(UUID uuid, int capturesPerTicket, int startingTickets) {
        PlayerProfile profile = profile(uuid, startingTickets);
        profile.totalCaptures++;
        profile.captureProgress++;
        int earned = profile.captureProgress / Math.max(1, capturesPerTicket);
        profile.captureProgress %= Math.max(1, capturesPerTicket);
        profile.tickets += earned;
        markDirty();
        return new CaptureProgress(earned, profile.tickets, profile.captureProgress, capturesPerTicket);
    }

    public synchronized int captureProgress(UUID uuid, int startingTickets) {
        return profile(uuid, startingTickets).captureProgress;
    }

    public synchronized int pity(UUID uuid, int startingTickets) {
        return profile(uuid, startingTickets).pity;
    }

    public synchronized int legendaryPity(UUID uuid, int startingTickets) {
        return profile(uuid, startingTickets).legendaryPity;
    }

    public synchronized void recordResult(UUID uuid, GachaResult result, int startingTickets) {
        PlayerProfile profile = profile(uuid, startingTickets);
        profile.totalDraws++;
        if (result.rarity().atLeast(GachaRarity.RARE)) profile.pity = 0;
        else profile.pity++;
        if (result.rarity().atLeast(GachaRarity.LEGENDARY)) profile.legendaryPity = 0;
        else profile.legendaryPity++;
        profile.history.add(0, new HistoryEntry(result.label(), result.rarity().name(), System.currentTimeMillis()));
        while (profile.history.size() > MAX_HISTORY) profile.history.remove(profile.history.size() - 1);
        markDirty();
    }

    public synchronized List<HistoryEntry> history(UUID uuid, int startingTickets) {
        return new ArrayList<>(profile(uuid, startingTickets).history);
    }

    public synchronized long totalDraws(UUID uuid, int startingTickets) {
        return profile(uuid, startingTickets).totalDraws;
    }

    public synchronized void close() {
        saver.shutdownNow();
        saveNow();
    }

    /** Flushes recent ticket and pity mutations without stopping the saver. */
    public synchronized void flush() {
        if (dirty) saveNow();
    }

    private void markDirty() { dirty = true; }

    private void flushIfDirty() {
        synchronized (this) {
            if (dirty) saveNow();
        }
    }

    private void saveNow() {
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(temporary, GSON.toJson(data), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
            dirty = false;
        } catch (IOException exception) {
            logger.error("Could not save Cobblemon Gacha ledger {}.", path, exception);
        }
    }

    public record CaptureProgress(int ticketsEarned, int tickets, int captureProgress, int capturesPerTicket) {}

    public static final class HistoryEntry {
        public String label;
        public String rarity;
        public long createdAt;

        public HistoryEntry() {}

        private HistoryEntry(String label, String rarity, long createdAt) {
            this.label = label;
            this.rarity = rarity;
            this.createdAt = createdAt;
        }
    }

    public static final class PlayerProfile {
        public boolean initialised;
        public int tickets;
        public int captureProgress;
        public long totalCaptures;
        public long totalDraws;
        public int pity;
        public int legendaryPity;
        public List<HistoryEntry> history = new ArrayList<>();

        private void normalise() {
            tickets = Math.max(0, tickets);
            captureProgress = Math.max(0, captureProgress);
            totalCaptures = Math.max(0L, totalCaptures);
            totalDraws = Math.max(0L, totalDraws);
            pity = Math.max(0, pity);
            legendaryPity = Math.max(0, legendaryPity);
            if (history == null) history = new ArrayList<>();
            else history = new ArrayList<>(history);
            history.removeIf(Objects::isNull);
            for (HistoryEntry entry : history) {
                if (entry.label == null || entry.label.isBlank()) entry.label = "Unknown result";
                try { GachaRarity.valueOf(entry.rarity); }
                catch (IllegalArgumentException | NullPointerException ignored) { entry.rarity = GachaRarity.COMMON.name(); }
                entry.createdAt = Math.max(0L, entry.createdAt);
            }
            while (history.size() > MAX_HISTORY) history.remove(history.size() - 1);
        }
    }

    private static final class LedgerData {
        public Map<String, PlayerProfile> players = new HashMap<>();

        private void normalise() {
            if (players == null) players = new HashMap<>();
            players.entrySet().removeIf(entry -> entry.getKey() == null || entry.getValue() == null);
            players.values().forEach(PlayerProfile::normalise);
        }
    }
}
