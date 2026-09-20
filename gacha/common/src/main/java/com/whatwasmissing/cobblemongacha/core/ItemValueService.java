package com.whatwasmissing.cobblemongacha.core;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Deterministic item pricing used by the Upgrader-style success calculation. */
public final class ItemValueService {
    private static final double MIN_SUCCESS_CHANCE = 0.001;
    private static final double MAX_SUCCESS_CHANCE = 0.90;
    private static final Set<String> OPTIONAL_HIGH_TIER_NAMESPACES = ConcurrentHashMap.newKeySet();
    private static final Map<String, Double> EXACT_VALUES = Map.ofEntries(
            Map.entry("minecraft:stick", 0.1),
            Map.entry("minecraft:cobblestone", 0.2),
            Map.entry("minecraft:iron_ingot", 8.0),
            Map.entry("minecraft:gold_ingot", 15.0),
            Map.entry("minecraft:diamond", 50.0),
            Map.entry("minecraft:emerald", 25.0),
            Map.entry("minecraft:golden_apple", 120.0),
            Map.entry("minecraft:diamond_block", 450.0),
            Map.entry("minecraft:netherite_scrap", 225.0),
            Map.entry("minecraft:netherite_ingot", 900.0),
            Map.entry("minecraft:nether_star", 2500.0),
            Map.entry("minecraft:totem_of_undying", 1800.0),
            Map.entry("cobblemon:poke_ball", 25.0),
            Map.entry("cobblemon:great_ball", 60.0),
            Map.entry("cobblemon:ultra_ball", 120.0),
            Map.entry("cobblemon:master_ball", 2500.0),
            Map.entry("cobblemon:potion", 30.0),
            Map.entry("cobblemon:super_potion", 60.0),
            Map.entry("cobblemon:hyper_potion", 100.0),
            Map.entry("cobblemon:max_potion", 180.0),
            Map.entry("cobblemon:full_restore", 300.0),
            Map.entry("cobblemon:revive", 180.0),
            Map.entry("cobblemon:max_revive", 400.0),
            Map.entry("cobblemon:revival_herb", 300.0),
            Map.entry("cobblemon:rare_candy", 400.0),
            Map.entry("cobblemon:exp_candy_s", 50.0),
            Map.entry("cobblemon:exp_candy_m", 350.0),
            Map.entry("cobblemon:exp_candy_l", 600.0),
            Map.entry("cobblemon:exp_candy_xl", 1000.0),
            Map.entry("cobblemon:ability_capsule", 700.0),
            Map.entry("cobblemon:ability_patch", 1200.0),
            Map.entry("cobblemon:metal_alloy", 300.0)
    );

    static {
        OPTIONAL_HIGH_TIER_NAMESPACES.add("mega_showdown");
        OPTIONAL_HIGH_TIER_NAMESPACES.add("megashowdown");
        OPTIONAL_HIGH_TIER_NAMESPACES.add("msd");
    }

    private ItemValueService() {}

    /** Lets optional integrations opt into the guarded high-tier item curve. */
    public static void registerOptionalNamespace(String namespace) {
        if (namespace != null && namespace.matches("[a-z0-9_.-]+")) {
            OPTIONAL_HIGH_TIER_NAMESPACES.add(namespace.toLowerCase(java.util.Locale.ROOT));
        }
    }

    public static double value(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0.0;
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        double base = key == null ? 10.0 : EXACT_VALUES.getOrDefault(key.toString(), heuristicValue(key));
        if (stack.isDamageableItem() && stack.getMaxDamage() > 0) {
            base *= Math.max(0.05, (double) (stack.getMaxDamage() - stack.getDamageValue()) / stack.getMaxDamage());
        }
        return Math.max(0.1, base);
    }

    public static double targetValue(ItemStack stack, int amount) {
        return value(stack) * Math.max(1, amount);
    }

    /** Contract values keep progressively rarer non-legendary Pokémon harder to win. */
    public static double pokemonTargetValue(UpgradeTarget target) {
        return switch (target.rarity()) {
            case COMMON -> 500.0;
            case RARE -> 1500.0;
            case EPIC -> 4500.0;
            case MYTHIC -> 12500.0;
            case LEGENDARY -> 25000.0;
        };
    }

    public static double pokemonTargetValue(UpgradeTarget target, int amount) {
        return pokemonTargetValue(target) * Math.max(1, amount);
    }

    /**
     * Value-share chance: the source contributes its share of the combined
     * source/target value, with a 90% ceiling and a 0.1% floor. This lets a
     * vastly better source strongly favour a cheap target without making a
     * merely better source an automatic win.
     */
    public static double successChance(double sourceValue, double targetValue) {
        if (!Double.isFinite(sourceValue) || !Double.isFinite(targetValue)
                || sourceValue <= 0.0 || targetValue <= 0.0) return MIN_SUCCESS_CHANCE;
        double combinedValue = sourceValue + targetValue;
        if (!Double.isFinite(combinedValue) || combinedValue <= 0.0) return MIN_SUCCESS_CHANCE;
        return Math.max(MIN_SUCCESS_CHANCE,
                Math.min(MAX_SUCCESS_CHANCE, 0.90 * (sourceValue / combinedValue)));
    }

    private static double heuristicValue(ResourceLocation key) {
        String path = key.getPath().toLowerCase();
        String namespace = key.getNamespace().toLowerCase();
        if (namespace.equals("cobblemon")) {
            if (path.contains("master")) return 2500.0;
            if (path.contains("ultra") || path.contains("ability_patch")) return 1200.0;
            if (path.contains("great") || path.contains("rare_candy")) return 400.0;
            if (path.contains("ball")) return 90.0;
            if (path.contains("potion")) return 30.0;
            if (path.contains("stone") || path.contains("candy")) return 300.0;
            if (path.contains("berry") || path.contains("seed") || path.contains("feather")
                    || path.contains("bone") || path.contains("string")) return 5.0;
            return 25.0;
        }
        if (OPTIONAL_HIGH_TIER_NAMESPACES.contains(namespace)) {
            return optionalIntegrationValue(path);
        }
        if (path.contains("netherite") || path.contains("mega") || path.contains("master")) return 900.0;
        if (path.contains("diamond") || path.contains("legendary")) return 50.0;
        if (path.contains("gold")) return 15.0;
        if (path.contains("emerald")) return 25.0;
        if (path.contains("iron") || path.contains("copper")) return 8.0;
        if (path.contains("coal") || path.contains("flint") || path.contains("redstone")) return 2.0;
        if (path.contains("log") || path.contains("plank") || path.contains("wood")) return 0.5;
        if (path.contains("dirt") || path.contains("sand") || path.contains("gravel")
                || path.contains("stone") || path.contains("netherrack")) return 0.2;
        return 1.0;
    }

    /**
     * Optional battle-mechanics items are progression targets, not generic
     * modded loot. Give them an explicit floor so a stale/default heuristic
     * cannot turn a valuable target into a near-guaranteed wager.
     */
    private static double optionalIntegrationValue(String path) {
        if (path.contains("master")) return 2500.0;
        if (path.contains("ultra") || path.contains("burst") || path.contains("dynamax")
                || path.contains("gigantamax") || path.contains("tera") || path.contains("terastal")
                || path.contains("z_crystal") || path.contains("z-crystal") || path.contains("zmove")) {
            return 1200.0;
        }
        if (path.contains("mega") || path.contains("key_stone") || path.contains("key-stone")
                || path.contains("bracelet") || path.contains("band") || path.contains("ring")
                || path.contains("orb") || path.contains("stone") || path.contains("crystal")) {
            return 600.0;
        }
        return 900.0;
    }
}
