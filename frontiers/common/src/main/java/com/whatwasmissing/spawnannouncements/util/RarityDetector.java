package com.whatwasmissing.spawnannouncements.util;

import com.cobblemon.mod.common.api.spawning.CobblemonSpawnPools;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.whatwasmissing.spawnannouncements.core.AnnouncementKind;
import com.whatwasmissing.spawnannouncements.api.FrontierCompatibilityHooks;

import java.util.List;
import java.util.Locale;

/** Reads Cobblemon's actual traits and spawn bucket instead of maintaining a species list. */
public final class RarityDetector {
    private RarityDetector() {}

    public static AnnouncementKind bestKind(PokemonEntity entity, SpawnablePosition position) {
        Pokemon pokemon = entity.getPokemon();
        boolean shiny = pokemon.getShiny();
        boolean legendary = pokemon.isLegendary();
        boolean mythical = pokemon.isMythical();
        boolean ultraBeast = pokemon.isUltraBeast();
        boolean paradox = pokemon.hasLabels(com.cobblemon.mod.common.api.pokemon.labels.CobblemonPokemonLabels.PARADOX);
        boolean alpha = pokemon.isAlpha();
        String bucket = spawnBucket(entity, position);

        AnnouncementKind custom = FrontierCompatibilityHooks.customRarity(entity, position);
        if (custom != null) return custom;

        if (shiny && legendary) return AnnouncementKind.SHINY_LEGENDARY;
        if (shiny && mythical) return AnnouncementKind.SHINY_MYTHICAL;
        if (shiny && ultraBeast) return AnnouncementKind.SHINY_ULTRA_BEAST;
        if (shiny && alpha) return AnnouncementKind.SHINY_ALPHA;
        if (shiny && paradox) return AnnouncementKind.SHINY_PARADOX;
        if (shiny) return AnnouncementKind.SHINY;
        if (legendary) return AnnouncementKind.LEGENDARY;
        if (mythical) return AnnouncementKind.MYTHICAL;
        if (ultraBeast) return AnnouncementKind.ULTRA_BEAST;
        if (paradox) return AnnouncementKind.PARADOX;
        if (alpha) return AnnouncementKind.ALPHA;
        if ("ultra-rare".equals(bucket)) return AnnouncementKind.ULTRA_RARE;
        if ("rare".equals(bucket)) return AnnouncementKind.RARE;
        return null;
    }

    private static String spawnBucket(PokemonEntity entity, SpawnablePosition position) {
        String speciesName = entity.getPokemon().getSpecies().getName().toLowerCase(Locale.ROOT);
        List<SpawnDetail> matching = CobblemonSpawnPools.WORLD_SPAWN_POOL.getDetails().stream()
                .filter(detail -> detail instanceof PokemonSpawnDetail pokemonSpawnDetail
                        && pokemonSpawnDetail.getPokemon().getSpecies() instanceof String species
                        && species.equals(speciesName))
                .filter(detail -> detail.isSatisfiedBy(position))
                .toList();
        String best = "none";
        int bestRank = 0;
        for (SpawnDetail detail : matching) {
            String bucket = detail.getBucket();
            if (bucket == null) continue;
            String normalised = bucket.toLowerCase(Locale.ROOT).replace('_', '-');
            int rank = switch (normalised) {
                case "ultra-rare" -> 2;
                case "rare" -> 1;
                default -> 0;
            };
            if (rank > bestRank) {
                best = normalised;
                bestRank = rank;
            }
        }
        return best;
    }
}
