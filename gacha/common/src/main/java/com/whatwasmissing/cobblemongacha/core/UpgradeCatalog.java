package com.whatwasmissing.cobblemongacha.core;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared target catalogue. Registry-driven optional entries make Cobblemon and
 * Mega Showdown items appear when those mods are installed without hard dependencies.
 */
public final class UpgradeCatalog {
    public enum TargetCategory {
        ITEMS("ITEMS"),
        POKEMON("POKÉMON");

        private final String displayName;

        TargetCategory(String displayName) { this.displayName = displayName; }
        public String displayName() { return displayName; }
    }

    private static final Set<String> OPTIONAL_NAMESPACES = ConcurrentHashMap.newKeySet();

    static {
        registerOptionalNamespace("mega_showdown");
        registerOptionalNamespace("megashowdown");
        registerOptionalNamespace("msd");
    }

    private static volatile List<UpgradeTarget> targets;
    public static final int TARGETS_PER_PAGE = 6;

    static {
        targets = buildTargets();
    }

    private UpgradeCatalog() {}

    /** Registers a namespace whose compatible items should appear in the upgrader. */
    public static void registerOptionalNamespace(String namespace) {
        if (namespace != null && namespace.matches("[a-z0-9_.-]+")) {
            OPTIONAL_NAMESPACES.add(namespace.toLowerCase(Locale.ROOT));
            if (targets != null) targets = buildTargets();
        }
    }

    public static List<UpgradeTarget> targets() { return availableTargets(); }
    public static int pageCount() {
        List<UpgradeTarget> targets = availableTargets();
        return Math.max(1, (targets.size() + TARGETS_PER_PAGE - 1) / TARGETS_PER_PAGE);
    }
    public static List<UpgradeTarget> targets(TargetCategory category) {
        return availableTargets().stream()
                .filter(target -> category == TargetCategory.POKEMON ? target.pokemon() : !target.pokemon())
                .toList();
    }
    public static int pageCount(TargetCategory category) {
        List<UpgradeTarget> targets = targets(category);
        return Math.max(1, (targets.size() + TARGETS_PER_PAGE - 1) / TARGETS_PER_PAGE);
    }

    private static List<UpgradeTarget> availableTargets() {
        return targets.stream()
                .filter(target -> !(target.pokemon() && GachaService.isLegendaryMonumentSpecies(target.id())))
                .toList();
    }

    private static List<UpgradeTarget> buildTargets() {
        List<UpgradeTarget> targets = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        addVanilla(targets, ids, "iron_ingot", "Iron Ingot", Items.IRON_INGOT, ChatFormatting.GRAY);
        addVanilla(targets, ids, "gold_ingot", "Gold Ingot", Items.GOLD_INGOT, ChatFormatting.GOLD);
        addVanilla(targets, ids, "diamond", "Diamond", Items.DIAMOND, ChatFormatting.AQUA);
        addVanilla(targets, ids, "emerald", "Emerald", Items.EMERALD, ChatFormatting.GREEN);
        addVanilla(targets, ids, "golden_apple", "Golden Apple", Items.GOLDEN_APPLE, ChatFormatting.GOLD);
        addVanilla(targets, ids, "diamond_block", "Diamond Block", Items.DIAMOND_BLOCK, ChatFormatting.AQUA);
        addVanilla(targets, ids, "netherite_scrap", "Netherite Scrap", Items.NETHERITE_SCRAP, ChatFormatting.DARK_PURPLE);
        addVanilla(targets, ids, "netherite_ingot", "Netherite Ingot", Items.NETHERITE_INGOT, ChatFormatting.DARK_PURPLE);
        addVanilla(targets, ids, "nether_star", "Nether Star", Items.NETHER_STAR, ChatFormatting.LIGHT_PURPLE);
        addVanilla(targets, ids, "totem_of_undying", "Totem of Undying", Items.TOTEM_OF_UNDYING, ChatFormatting.GOLD);

        addCobblemon(targets, ids, "poke_ball", "Poké Ball", ChatFormatting.RED);
        addCobblemon(targets, ids, "great_ball", "Great Ball", ChatFormatting.BLUE);
        addCobblemon(targets, ids, "ultra_ball", "Ultra Ball", ChatFormatting.YELLOW);
        addCobblemon(targets, ids, "premier_ball", "Premier Ball", ChatFormatting.WHITE);
        addCobblemon(targets, ids, "quick_ball", "Quick Ball", ChatFormatting.AQUA);
        addCobblemon(targets, ids, "dusk_ball", "Dusk Ball", ChatFormatting.DARK_GREEN);
        addCobblemon(targets, ids, "luxury_ball", "Luxury Ball", ChatFormatting.GOLD);
        addCobblemon(targets, ids, "beast_ball", "Beast Ball", ChatFormatting.DARK_PURPLE);
        addCobblemon(targets, ids, "master_ball", "Master Ball", ChatFormatting.LIGHT_PURPLE);
        addCobblemon(targets, ids, "potion", "Potion", ChatFormatting.RED);
        addCobblemon(targets, ids, "super_potion", "Super Potion", ChatFormatting.RED);
        addCobblemon(targets, ids, "hyper_potion", "Hyper Potion", ChatFormatting.RED);
        addCobblemon(targets, ids, "rare_candy", "Rare Candy", ChatFormatting.LIGHT_PURPLE);
        addCobblemon(targets, ids, "ability_capsule", "Ability Capsule", ChatFormatting.AQUA);
        addCobblemon(targets, ids, "ability_patch", "Ability Patch", ChatFormatting.LIGHT_PURPLE);
        addCobblemon(targets, ids, "metal_alloy", "Metal Alloy", ChatFormatting.GRAY);

        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            if (itemId == null || item == Items.AIR || !OPTIONAL_NAMESPACES.contains(itemId.getNamespace())) continue;
            String path = itemId.getPath().toLowerCase(Locale.ROOT);
            if (path.contains("debug") || path.contains("test") || path.contains("spawn_egg")
                    || path.contains("placeholder") || path.contains("gui") || path.contains("menu")) continue;
            add(targets, ids, itemId.toString(), titleCase(path), item, ChatFormatting.LIGHT_PURPLE);
        }

        // Pokémon contracts are represented by paper in the catalogue so the
        // UI remains registry-safe; the server resolves the actual species via
        // PokemonRewardAdapter after a successful wager.
        addPokemon(targets, ids, "rattata", "Rattata", GachaRarity.COMMON, ChatFormatting.GRAY);
        addPokemon(targets, ids, "pidgey", "Pidgey", GachaRarity.COMMON, ChatFormatting.GRAY);
        addPokemon(targets, ids, "zubat", "Zubat", GachaRarity.COMMON, ChatFormatting.GRAY);
        addPokemon(targets, ids, "geodude", "Geodude", GachaRarity.COMMON, ChatFormatting.GRAY);
        addPokemon(targets, ids, "magikarp", "Magikarp", GachaRarity.COMMON, ChatFormatting.GRAY);
        addPokemon(targets, ids, "bellsprout", "Bellsprout", GachaRarity.COMMON, ChatFormatting.GRAY);
        addPokemon(targets, ids, "sandshrew", "Sandshrew", GachaRarity.COMMON, ChatFormatting.GRAY);
        addPokemon(targets, ids, "growlithe", "Growlithe", GachaRarity.COMMON, ChatFormatting.GRAY);

        addPokemon(targets, ids, "eevee", "Eevee", GachaRarity.RARE, ChatFormatting.BLUE);
        addPokemon(targets, ids, "dratini", "Dratini", GachaRarity.RARE, ChatFormatting.BLUE);
        addPokemon(targets, ids, "riolu", "Riolu", GachaRarity.RARE, ChatFormatting.BLUE);
        addPokemon(targets, ids, "gible", "Gible", GachaRarity.RARE, ChatFormatting.BLUE);
        addPokemon(targets, ids, "zorua", "Zorua", GachaRarity.RARE, ChatFormatting.BLUE);
        addPokemon(targets, ids, "rotom", "Rotom", GachaRarity.RARE, ChatFormatting.BLUE);

        addPokemon(targets, ids, "lapras", "Lapras", GachaRarity.EPIC, ChatFormatting.DARK_PURPLE);
        addPokemon(targets, ids, "snorlax", "Snorlax", GachaRarity.EPIC, ChatFormatting.DARK_PURPLE);
        addPokemon(targets, ids, "lucario", "Lucario", GachaRarity.EPIC, ChatFormatting.DARK_PURPLE);
        addPokemon(targets, ids, "metagross", "Metagross", GachaRarity.EPIC, ChatFormatting.DARK_PURPLE);
        addPokemon(targets, ids, "tyranitar", "Tyranitar", GachaRarity.EPIC, ChatFormatting.DARK_PURPLE);
        addPokemon(targets, ids, "volcarona", "Volcarona", GachaRarity.EPIC, ChatFormatting.DARK_PURPLE);

        addPokemon(targets, ids, "mew", "Mew", GachaRarity.MYTHIC, ChatFormatting.LIGHT_PURPLE);
        addPokemon(targets, ids, "celebi", "Celebi", GachaRarity.MYTHIC, ChatFormatting.LIGHT_PURPLE);
        addPokemon(targets, ids, "jirachi", "Jirachi", GachaRarity.MYTHIC, ChatFormatting.LIGHT_PURPLE);
        addPokemon(targets, ids, "darkrai", "Darkrai", GachaRarity.MYTHIC, ChatFormatting.LIGHT_PURPLE);

        // Legendary contracts deliberately do not use the ordinary value
        // curve. They are resolved by the very small, configurable contract
        // chance in GachaUpgradeService.
        addPokemon(targets, ids, "kyogre", "Kyogre", GachaRarity.LEGENDARY, ChatFormatting.GOLD);
        addPokemon(targets, ids, "groudon", "Groudon", GachaRarity.LEGENDARY, ChatFormatting.GOLD);
        addPokemon(targets, ids, "rayquaza", "Rayquaza", GachaRarity.LEGENDARY, ChatFormatting.GOLD);
        addPokemon(targets, ids, "mewtwo", "Mewtwo", GachaRarity.LEGENDARY, ChatFormatting.GOLD);
        addPokemon(targets, ids, "zygarde", "Zygarde", GachaRarity.LEGENDARY, ChatFormatting.GOLD);
        addPokemon(targets, ids, "thundurus", "Thundurus", GachaRarity.LEGENDARY, ChatFormatting.GOLD);
        addPokemon(targets, ids, "necrozma", "Necrozma", GachaRarity.LEGENDARY, ChatFormatting.GOLD);
        return List.copyOf(targets);
    }

    private static void addVanilla(List<UpgradeTarget> targets, Set<String> ids, String id, String name,
                                   Item item, ChatFormatting formatting) {
        add(targets, ids, "minecraft:" + id, name, item, formatting);
    }

    private static void addCobblemon(List<UpgradeTarget> targets, Set<String> ids, String id, String name,
                                     ChatFormatting formatting) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("cobblemon", id));
        add(targets, ids, "cobblemon:" + id, name, item, formatting);
    }

    private static void add(List<UpgradeTarget> targets, Set<String> ids, String id, String name, Item item,
                            ChatFormatting formatting) {
        if (item != null && item != Items.AIR && ids.add(id)) targets.add(new UpgradeTarget(id, name, item, formatting));
    }

    private static void addPokemon(List<UpgradeTarget> targets, Set<String> ids, String species, String name,
                                   GachaRarity rarity, ChatFormatting formatting) {
        UpgradeTarget target = UpgradeTarget.pokemon(species, name, rarity, formatting);
        if (ids.add(target.id())) targets.add(target);
    }

    private static String titleCase(String path) {
        StringBuilder result = new StringBuilder();
        for (String word : path.replace('-', '_').split("_")) {
            if (word.isBlank()) continue;
            if (result.length() > 0) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }
}
