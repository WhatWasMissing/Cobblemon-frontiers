package com.whatwasmissing.spawnannouncements.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.whatwasmissing.spawnannouncements.core.AnnouncementKind;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** Server-owned configuration. A fresh file is deliberately conservative. */
public final class AnnouncementConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean enabled = true;
    public boolean announceShiny = true;
    public boolean announceLegendary = true;
    public boolean announceMythical = true;
    public boolean announceUltraBeast = true;
    public boolean announceParadox = true;
    public boolean announceAlpha = true;
    // Rare spawn buckets are capture bounties, not public announcements.
    public boolean announceUltraRareSpawns = true;
    public boolean announceRareSpawns = false;
    public boolean includeRegion = true;
    /** Adds the nearest eligible player's name to public alerts, never their location. */
    public boolean announceNearbyPlayerName = true;
    public int nearbyPlayerRadiusBlocks = 96;
    public int globalCooldownSeconds = 45;
    public int sameKindCooldownSeconds = 90;

    /** Personal capture feedback: actionbar, chat, or silent. */
    public String captureFeedbackMode = "actionbar";
    /** Public alert delivery: global, same_dimension, or nearby. */
    public String announcementScope = "global";
    public int announcementRadiusBlocks = 128;

    /** Empty means every dimension. Values use identifiers such as minecraft:overworld. */
    public java.util.List<String> allowedDimensions = new java.util.ArrayList<>();

    /** Optional biome-path overrides for modded biome packs. */
    public Map<String, String> regionOverrides = new LinkedHashMap<>();

    /**
     * Supported placeholders: {region}, {player}, {player_clause}, {dimension},
     * and {time}; coordinate/species placeholders are scrubbed. Separate
     * variants with " || " to keep alerts varied without changing the schema.
     */
    public Map<String, String> messages = defaultMessages();

    public boolean isEnabled(AnnouncementKind kind) {
        return switch (kind) {
            case SHINY_LEGENDARY, SHINY_MYTHICAL, SHINY_ULTRA_BEAST, SHINY_ALPHA, SHINY_PARADOX, SHINY -> announceShiny;
            case LEGENDARY -> announceLegendary;
            case MYTHICAL -> announceMythical;
            case ULTRA_BEAST -> announceUltraBeast;
            case PARADOX -> announceParadox;
            case ALPHA -> announceAlpha;
            case ULTRA_RARE -> announceUltraRareSpawns;
            case RARE -> announceRareSpawns;
        };
    }

    public String messageFor(AnnouncementKind kind, String region) {
        return messageFor(kind, region, "");
    }

    public String messageFor(AnnouncementKind kind, String region, String nearbyPlayerName) {
        return messageFor(kind, region, nearbyPlayerName, "the overworld", "day");
    }

    public String messageFor(AnnouncementKind kind, String region, String nearbyPlayerName,
                             String dimension, String time) {
        String template = messages.getOrDefault(kind.id(), defaultMessages().get(kind.id()));
        if (template == null || template.isBlank()) {
            template = "An unusual Pokémon has appeared somewhere in {region}.";
        }
        String[] variants = java.util.Arrays.stream(template.split("\\s*\\|\\|\\s*"))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toArray(String[]::new);
        template = variants.length == 0 ? "An unusual Pokémon has appeared somewhere in {region}."
                : variants[ThreadLocalRandom.current().nextInt(variants.length)];
        String safeRegion = includeRegion ? region : "the wilds";
        String safePlayer = safePlayerName(nearbyPlayerName);
        if (!announceNearbyPlayerName) safePlayer = "";
        String clause = safePlayer.isBlank() ? "" : " near " + safePlayer;
        String message = template.replace("{region}", safeRegion)
                .replace("{dimension}", dimension == null || dimension.isBlank() ? "the overworld" : dimension)
                .replace("{time}", time == null || time.isBlank() ? "day" : time)
                .replace("{player_clause}", clause)
                .replace("{player}", safePlayer)
                .replace("{player_name}", safePlayer)
                // Keep the privacy guarantee even if a copied config contains old alert placeholders.
                .replace("{coordinates}", "the wilds")
                .replace("{coords}", "the wilds")
                .replace("{species}", "an unusual Pokémon")
                .replace("{pokemon}", "an unusual Pokémon");
        // Older generated configs won't have the new clause token. Preserve their
        // wording while still honouring the named-nearby-player setting.
        if (!safePlayer.isBlank() && !template.contains("{player_clause}")
                && !template.contains("{player}") && !template.contains("{player_name}")) {
            if (message.endsWith(".")) return message.substring(0, message.length() - 1) + clause + ".";
            return message + clause;
        }
        return message;
    }

    private static String safePlayerName(String value) {
        if (value == null || !value.matches("[A-Za-z0-9_]{1,16}")) return "";
        return value;
    }

    public static AnnouncementConfig load(Path path, Logger logger) {
        AnnouncementConfig config = null;
        boolean unreadable = false;
        try {
            if (Files.exists(path)) {
                String json = Files.readString(path, StandardCharsets.UTF_8);
                config = GSON.fromJson(json, AnnouncementConfig.class);
                if (config == null) unreadable = true;
            }
        } catch (IOException | JsonParseException | IllegalStateException exception) {
            unreadable = true;
            logger.error("Could not read {}. Recreating the default configuration.", path, exception);
        }

        if (unreadable) preserveUnreadableConfig(path, logger);
        if (config == null) {
            config = new AnnouncementConfig();
        }
        config.applyDefaultsAndLimits();
        save(path, config, logger);
        return config;
    }

    public static void save(Path path, AnnouncementConfig config, Logger logger) {
        try {
            Files.createDirectories(path.getParent());
            Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
            Files.writeString(temporary, GSON.toJson(config), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            logger.error("Could not save {}.", path, exception);
        }
    }

    private static void preserveUnreadableConfig(Path path, Logger logger) {
        if (!Files.exists(path)) return;
        try {
            Path backup = path.resolveSibling(path.getFileName() + ".corrupt-" + System.currentTimeMillis());
            Files.move(path, backup, StandardCopyOption.REPLACE_EXISTING);
            logger.error("Moved unreadable configuration to {}.", backup);
        } catch (IOException backupException) {
            logger.error("Could not preserve unreadable configuration {}.", path, backupException);
        }
    }

    private void applyDefaultsAndLimits() {
        // Keep the legacy rare field for config compatibility, but enforce the
        // design: rare buckets are always silent and pay out on capture.
        announceRareSpawns = false;
        globalCooldownSeconds = clamp(globalCooldownSeconds, 0, 86_400);
        sameKindCooldownSeconds = clamp(sameKindCooldownSeconds, 0, 86_400);
        nearbyPlayerRadiusBlocks = clamp(nearbyPlayerRadiusBlocks, 1, 512);
        captureFeedbackMode = normaliseMode(captureFeedbackMode, "actionbar");
        announcementScope = normaliseScope(announcementScope);
        announcementRadiusBlocks = clamp(announcementRadiusBlocks, 16, 512);
        if (allowedDimensions == null) {
            allowedDimensions = new java.util.ArrayList<>();
        }
        if (messages == null) {
            messages = new LinkedHashMap<>();
        }
        if (regionOverrides == null) {
            regionOverrides = new LinkedHashMap<>();
        }
        Map<String, String> defaults = defaultMessages();
        Map<String, String> legacy = legacyMessages();
        defaults.forEach((key, value) -> {
            String existing = messages.get(key);
            if (existing == null || existing.equals(legacy.get(key))) messages.put(key, value);
        });
        allowedDimensions.replaceAll(value -> value == null ? "" : value.toLowerCase(Locale.ROOT));
        allowedDimensions.removeIf(String::isBlank);
        regionOverrides.replaceAll((key, value) -> value == null ? "" : value.trim());
        regionOverrides.entrySet().removeIf(entry -> entry.getKey() == null || entry.getKey().isBlank()
                || entry.getValue() == null || entry.getValue().isBlank());
        Map<String, String> normalizedRegions = new LinkedHashMap<>();
        regionOverrides.forEach((key, value) -> normalizedRegions.put(key.toLowerCase(Locale.ROOT), value));
        regionOverrides = normalizedRegions;
    }

    private static String normaliseMode(String value, String fallback) {
        String normalized = value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
        return normalized.equals("chat") || normalized.equals("actionbar") || normalized.equals("silent")
                ? normalized : fallback;
    }

    private static String normaliseScope(String value) {
        String normalized = value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
        return normalized.equals("same_dimension") || normalized.equals("nearby") || normalized.equals("global")
                ? normalized : "global";
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static Map<String, String> defaultMessages() {
        Map<String, String> defaults = new LinkedHashMap<>();
        defaults.put("shiny_legendary", "A legendary presence with an impossible gleam has been detected somewhere in {region}{player_clause}. || Field sensors report a legendary signature wrapped in strange light in {region}{player_clause}. || The frontier has caught a legendary shimmer in {region}{player_clause} during the {time}.");
        defaults.put("shiny_mythical", "A mythical presence is shining through the wilds of {region}{player_clause}. || A once-in-an-age gleam has been detected around {region}{player_clause}. || The frontier has recorded a mythical signature carrying an otherworldly sparkle in {region}{player_clause}.");
        defaults.put("shiny_ultra_beast", "An Ultra Beast is distorting the frontier with a strange gleam in {region}{player_clause}. || Reality has flashed around a shining Ultra Beast somewhere in {region}{player_clause}. || Field sensors have caught an Ultra Beast signature glowing in {region}{player_clause}.");
        defaults.put("shiny_alpha", "A massive silhouette with a rare gleam has stirred in {region}{player_clause}. || An alpha presence is shining through the terrain of {region}{player_clause}. || The frontier has picked up a huge, sparkling signature in {region}{player_clause}.");
        defaults.put("shiny_paradox", "A Paradox anomaly is giving off an impossible gleam in {region}{player_clause}. || Temporal readings are sparkling around a Paradox presence in {region}{player_clause}. || A shining Paradox signature has slipped through the frontier in {region}{player_clause}.");
        defaults.put("shiny", "A strange sparkle has been spotted somewhere in {region}{player_clause}. || Field observers report an unmistakable gleam moving through {region}{player_clause}. || Something in {region}{player_clause} is reflecting light that should not be there.");
        defaults.put("legendary", "A legendary presence has been detected somewhere in {region}{player_clause}. || Field sensors are struggling to classify a legendary signature in {region}{player_clause}. || The frontier has gone quiet around a legendary reading in {region}{player_clause} during the {time}.");
        defaults.put("mythical", "A mythical presence has been detected somewhere in {region}{player_clause}. || An almost-forgotten mythical signature has surfaced in {region}{player_clause}. || Something extraordinarily old is moving through {region}{player_clause}.");
        defaults.put("ultra_beast", "An Ultra Beast has breached reality somewhere in {region}{player_clause}. || Field sensors report an Ultra Beast crossing into {region}{player_clause}. || A hostile off-world signature has opened in {region}{player_clause}.");
        defaults.put("paradox", "A temporal anomaly has been detected somewhere in {region}{player_clause}. || The timeline is behaving strangely around {region}{player_clause}. || Field instruments have recorded a Paradox reading in {region}{player_clause} during the {time}.");
        defaults.put("alpha", "A massive presence has been disturbed somewhere in {region}{player_clause}. || Heavy footsteps have been recorded across {region}{player_clause}. || The frontier has picked up an alpha-scale signature in {region}{player_clause}.");
        defaults.put("ultra_rare", "An exceptionally rare presence has surfaced somewhere in {region}{player_clause}. || A near-mythic field reading has appeared in {region}{player_clause}. || The frontier has recorded an ultra-rare signature in {region}{player_clause}.");
        defaults.put("rare", "An unusual Pokémon has appeared somewhere in {region}{player_clause}. || Field observers have noticed an uncommon signature in {region}{player_clause}. || Something worth documenting has surfaced in {region}{player_clause}.");
        return defaults;
    }

    private static Map<String, String> legacyMessages() {
        Map<String, String> legacy = new LinkedHashMap<>();
        legacy.put("shiny_legendary", "A legendary Pokémon with an otherworldly gleam has appeared somewhere in {region}{player_clause}.");
        legacy.put("shiny_mythical", "A mythical Pokémon with an otherworldly gleam has appeared somewhere in {region}{player_clause}.");
        legacy.put("shiny_ultra_beast", "An Ultra Beast with an otherworldly gleam has appeared somewhere in {region}{player_clause}.");
        legacy.put("shiny_alpha", "A massive Pokémon with an otherworldly gleam has appeared somewhere in {region}{player_clause}.");
        legacy.put("shiny_paradox", "A strange gleam has been seen around a Paradox Pokémon somewhere in {region}{player_clause}.");
        legacy.put("shiny", "A strange sparkle has been spotted somewhere in {region}{player_clause}.");
        legacy.put("legendary", "A legendary presence has been detected somewhere in {region}{player_clause}.");
        legacy.put("mythical", "A mythical presence has been detected somewhere in {region}{player_clause}.");
        legacy.put("ultra_beast", "An Ultra Beast has breached reality somewhere in {region}{player_clause}.");
        legacy.put("paradox", "A temporal anomaly has been detected somewhere in {region}{player_clause}.");
        legacy.put("alpha", "A massive Pokémon has been disturbed somewhere in {region}{player_clause}.");
        legacy.put("ultra_rare", "An exceptionally rare Pokémon has appeared somewhere in {region}{player_clause}.");
        legacy.put("rare", "An unusual Pokémon has appeared somewhere in {region}{player_clause}.");
        return legacy;
    }
}
