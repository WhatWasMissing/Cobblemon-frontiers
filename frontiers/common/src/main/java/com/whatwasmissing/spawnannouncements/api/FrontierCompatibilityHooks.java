package com.whatwasmissing.spawnannouncements.api;

import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.whatwasmissing.spawnannouncements.core.AnnouncementKind;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;

/**
 * Small optional-integration surface for Cobblemon add-ons.
 *
 * Add-ons can register a broad biome alias or provide an existing Frontiers
 * announcement kind without taking a hard dependency on those add-ons here.
 */
public final class FrontierCompatibilityHooks {
    private static final Map<String, String> REGION_ALIASES = new ConcurrentHashMap<>();
    private static final CopyOnWriteArrayList<BiFunction<PokemonEntity, SpawnablePosition, AnnouncementKind>>
            RARITY_PROVIDERS = new CopyOnWriteArrayList<>();

    private FrontierCompatibilityHooks() { }

    public static void registerRegionAlias(String biomePath, String broadRegion) {
        if (biomePath == null || biomePath.isBlank() || broadRegion == null || broadRegion.isBlank()) return;
        REGION_ALIASES.put(biomePath.toLowerCase(java.util.Locale.ROOT), broadRegion.trim());
    }

    public static String regionAlias(String biomePath) {
        return biomePath == null ? null : REGION_ALIASES.get(biomePath.toLowerCase(java.util.Locale.ROOT));
    }

    public static void registerRarityProvider(
            BiFunction<PokemonEntity, SpawnablePosition, AnnouncementKind> provider) {
        if (provider != null) RARITY_PROVIDERS.addIfAbsent(provider);
    }

    public static AnnouncementKind customRarity(PokemonEntity entity, SpawnablePosition position) {
        for (BiFunction<PokemonEntity, SpawnablePosition, AnnouncementKind> provider : RARITY_PROVIDERS) {
            try {
                AnnouncementKind kind = provider.apply(entity, position);
                if (kind != null) return kind;
            } catch (RuntimeException ignored) {
                // An optional integration must never stop normal spawn handling.
            }
        }
        return null;
    }
}
