package com.whatwasmissing.spawnannouncements.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.server.level.ServerLevel;
import com.whatwasmissing.spawnannouncements.api.FrontierCompatibilityHooks;
import com.whatwasmissing.spawnannouncements.config.AnnouncementConfig;

import java.util.Locale;

/** Converts a precise biome identifier into a broad, non-locating region label. */
public final class BiomeRegionUtil {
    private BiomeRegionUtil() {}

    public static String describe(ServerLevel level, BlockPos position) {
        return describe(level, position, null);
    }

    public static String describe(ServerLevel level, BlockPos position, AnnouncementConfig config) {
        ResourceKey<Level> dimension = level.dimension();
        String biomeId = level.getBiome(position).unwrapKey()
                .map(key -> key.location().toString())
                .orElse("minecraft:plains")
                .toLowerCase(Locale.ROOT);
        String path = biomeId.substring(biomeId.indexOf(':') + 1);

        if (config != null && config.regionOverrides != null) {
            String configured = config.regionOverrides.get(biomeId);
            if (configured == null) configured = config.regionOverrides.get(path);
            if (configured != null && !configured.isBlank()) return configured;
        }
        String registered = FrontierCompatibilityHooks.regionAlias(biomeId);
        if (registered != null) return registered;

        if (dimension == Level.NETHER) {
            if (contains(path, "crimson")) return "the crimson wastes";
            if (contains(path, "warped")) return "the warped wastes";
            if (contains(path, "soul")) return "the soul valleys";
            if (contains(path, "basalt")) return "the basalt deltas";
            return "the Nether";
        }
        if (dimension == Level.END) {
            return contains(path, "void") ? "the End void" : "the End";
        }

        // Many cave Pokémon inherit the surface biome identifier. Sky access
        // and depth are therefore more reliable than a biome name alone.
        if (!level.canSeeSky(position) && position.getY() < level.getSeaLevel()) {
            return "the underground";
        }

        if (contains(path, "deep_dark", "dripstone", "lush_cave", "cave")) return "the underground";
        if (contains(path, "snow", "ice", "frozen", "grove", "peaks", "mountain", "jagged")) return "the frozen highlands";
        if (contains(path, "ocean", "river", "beach", "shore", "coast")) return "the waterside";
        if (contains(path, "swamp", "mangrove")) return "the wetlands";
        if (contains(path, "jungle", "bamboo")) return "the jungle";
        if (contains(path, "savanna")) return "the savanna";
        if (contains(path, "desert", "badlands", "mesa")) return "the drylands";
        if (contains(path, "taiga")) return "the taiga";
        if (contains(path, "forest", "wooded")) return "the forest";
        if (contains(path, "plains", "meadow", "cherry", "flower")) return "the grasslands";
        if (contains(path, "mushroom")) return "the mushroom islands";
        if (contains(path, "nether")) return "the Nether";
        if (contains(path, "end")) return "the End";
        return "the wilds";
    }

    private static boolean contains(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) return true;
        }
        return false;
    }
}
