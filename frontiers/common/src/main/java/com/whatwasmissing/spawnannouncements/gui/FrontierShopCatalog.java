package com.whatwasmissing.spawnannouncements.gui;

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
import java.util.Comparator;

/**
 * The exchange catalogue. Vanilla products are always present; Cobblemon products
 * are resolved from the registry so the catalogue uses Cobblemon's real items and
 * remains resilient if a server updates its item set. Optional integration is
 * registry-driven: Mega Showdown products appear when that mod is installed, but
 * it is never a required dependency.
 */
public final class FrontierShopCatalog {
    public static final int PRODUCTS_PER_PAGE = 12;
    private static final Set<String> OPTIONAL_NAMESPACES = Set.of("mega_showdown", "megashowdown", "msd");
    private static final List<FrontierShopProduct> PRODUCTS = buildProducts();

    private FrontierShopCatalog() {}

    public static List<FrontierShopProduct> products() {
        return PRODUCTS;
    }

    public static int pageCount() {
        return Math.max(1, (PRODUCTS.size() + PRODUCTS_PER_PAGE - 1) / PRODUCTS_PER_PAGE);
    }

    public static List<FrontierShopProduct> filteredProducts(String query, SortMode sortMode) {
        return filteredProducts(query, Category.ALL, sortMode);
    }

    public static List<FrontierShopProduct> filteredProducts(String query, Category category, SortMode sortMode) {
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        Category selectedCategory = category == null ? Category.ALL : category;
        java.util.stream.Stream<FrontierShopProduct> stream = PRODUCTS.stream()
                .filter(product -> selectedCategory == Category.ALL || product.category() == selectedCategory)
                .filter(product -> needle.isEmpty()
                        || product.displayName().toLowerCase(Locale.ROOT).contains(needle)
                        || product.id().toLowerCase(Locale.ROOT).contains(needle));
        if (sortMode == SortMode.NAME) {
            stream = stream.sorted(Comparator.comparing(FrontierShopProduct::displayName, String.CASE_INSENSITIVE_ORDER));
        } else if (sortMode == SortMode.COST) {
            stream = stream.sorted(Comparator.comparingInt(FrontierShopProduct::cost)
                    .thenComparing(FrontierShopProduct::displayName, String.CASE_INSENSITIVE_ORDER));
        }
        return stream.toList();
    }

    public enum Category {
        ALL("all", "All"),
        SUPPLIES("supplies", "Supplies"),
        CAPTURE("capture", "Capture"),
        HEALING("healing", "Healing"),
        EVOLUTION("evolution", "Evolution"),
        RARE("rare", "Rare"),
        MEGA_SHOWDOWN("mega_showdown", "Mega Showdown");

        private final String id;
        private final String label;

        Category(String id, String label) {
            this.id = id;
            this.label = label;
        }

        public String id() {
            return id;
        }

        public String label() {
            return label;
        }

        public static Category parse(String id) {
            if (id == null) return null;
            for (Category category : values()) {
                if (category.id.equalsIgnoreCase(id)) return category;
            }
            return null;
        }
    }

    public enum SortMode {
        FEATURED("featured"), NAME("name"), COST("cost");

        private final String id;
        SortMode(String id) { this.id = id; }
        public String id() { return id; }
        public String label() {
            return switch (this) {
                case FEATURED -> "Featured";
                case NAME -> "Name";
                case COST -> "Cost";
            };
        }
        public SortMode next() { return values()[(ordinal() + 1) % values().length]; }
        public static SortMode parse(String id) {
            if (id == null) return null;
            for (SortMode mode : values()) if (mode.id.equalsIgnoreCase(id)) return mode;
            return null;
        }
    }

    private static List<FrontierShopProduct> buildProducts() {
        List<FrontierShopProduct> products = new ArrayList<>();
        Set<String> ids = new HashSet<>();

        // General frontier supplies.
        vanilla(products, ids, "experience", "Field Experience", Items.EXPERIENCE_BOTTLE, 8, 10, ChatFormatting.GREEN);
        vanilla(products, ids, "amethyst", "Amethyst Samples", Items.AMETHYST_SHARD, 16, 15, ChatFormatting.LIGHT_PURPLE);
        vanilla(products, ids, "gold", "Surveyor's Gold", Items.GOLD_INGOT, 4, 25, ChatFormatting.GOLD);
        vanilla(products, ids, "berries", "Trail Rations", Items.GLOW_BERRIES, 16, 20, ChatFormatting.YELLOW);
        vanilla(products, ids, "pearls", "Ender Pearls", Items.ENDER_PEARL, 2, 40, ChatFormatting.DARK_PURPLE);
        vanilla(products, ids, "compass", "Field Compass", Items.COMPASS, 1, 35, ChatFormatting.AQUA);
        vanilla(products, ids, "golden_apple", "Emergency Rations", Items.GOLDEN_APPLE, 1, 75, ChatFormatting.GOLD);
        vanilla(products, ids, "diamond", "Frontier Diamond", Items.DIAMOND, 1, 100, ChatFormatting.AQUA);
        vanilla(products, ids, "bread", "Trail Bread", Items.BREAD, 16, 12, ChatFormatting.YELLOW);
        vanilla(products, ids, "cooked_beef", "Smoked Trail Steak", Items.COOKED_BEEF, 8, 18, ChatFormatting.GOLD);
        vanilla(products, ids, "golden_carrot", "Focus Rations", Items.GOLDEN_CARROT, 8, 36, ChatFormatting.GOLD);
        vanilla(products, ids, "honey_bottle", "Restorative Honey", Items.HONEY_BOTTLE, 2, 28, ChatFormatting.YELLOW);
        vanilla(products, ids, "torch", "Trail Torches", Items.TORCH, 16, 8, ChatFormatting.YELLOW);
        vanilla(products, ids, "lantern", "Survey Lantern", Items.LANTERN, 4, 16, ChatFormatting.AQUA);
        vanilla(products, ids, "campfire", "Field Campfire", Items.CAMPFIRE, 1, 22, ChatFormatting.RED);
        vanilla(products, ids, "obsidian", "Stabilised Obsidian", Items.OBSIDIAN, 8, 60, ChatFormatting.DARK_PURPLE);
        vanilla(products, ids, "blaze_rod", "Blaze Research Sample", Items.BLAZE_ROD, 2, 55, ChatFormatting.GOLD);
        vanilla(products, ids, "ender_eye", "Rift Finder", Items.ENDER_EYE, 1, 80, ChatFormatting.GREEN);
        vanilla(products, ids, "phantom_membrane", "Aerial Field Sample", Items.PHANTOM_MEMBRANE, 2, 50, ChatFormatting.LIGHT_PURPLE);
        vanilla(products, ids, "prismarine_crystals", "Tidal Crystals", Items.PRISMARINE_CRYSTALS, 8, 35, ChatFormatting.AQUA);
        vanilla(products, ids, "saddle", "Expedition Saddle", Items.SADDLE, 1, 70, ChatFormatting.GOLD);
        vanilla(products, ids, "name_tag", "Research Name Tag", Items.NAME_TAG, 1, 65, ChatFormatting.YELLOW);
        vanilla(products, ids, "spyglass", "Long-range Spyglass", Items.SPYGLASS, 1, 60, ChatFormatting.AQUA);
        vanilla(products, ids, "firework_rocket", "Signal Flares", Items.FIREWORK_ROCKET, 8, 30, ChatFormatting.RED);
        vanilla(products, ids, "shulker_shell", "Portable Field Shell", Items.SHULKER_SHELL, 2, 120, ChatFormatting.LIGHT_PURPLE);
        vanilla(products, ids, "netherite_scrap", "Ancient Alloy Scrap", Items.NETHERITE_SCRAP, 1, 180, ChatFormatting.DARK_PURPLE);
        vanilla(products, ids, "nether_star", "Frontier Star", Items.NETHER_STAR, 1, 500, ChatFormatting.AQUA);
        vanilla(products, ids, "totem", "Emergency Totem", Items.TOTEM_OF_UNDYING, 1, 650, ChatFormatting.GOLD);

        // Cobblemon capture supplies.
        cobblemon(products, ids, "poke_ball", "Poké Ball", 8, 12, ChatFormatting.RED);
        cobblemon(products, ids, "great_ball", "Great Ball", 6, 24, ChatFormatting.BLUE);
        cobblemon(products, ids, "ultra_ball", "Ultra Ball", 4, 48, ChatFormatting.YELLOW);
        cobblemon(products, ids, "premier_ball", "Premier Ball", 4, 42, ChatFormatting.WHITE);
        cobblemon(products, ids, "dusk_ball", "Dusk Ball", 4, 42, ChatFormatting.DARK_GREEN);
        cobblemon(products, ids, "quick_ball", "Quick Ball", 4, 42, ChatFormatting.AQUA);
        cobblemon(products, ids, "repeat_ball", "Repeat Ball", 4, 42, ChatFormatting.GOLD);
        cobblemon(products, ids, "timer_ball", "Timer Ball", 4, 42, ChatFormatting.DARK_GRAY);
        cobblemon(products, ids, "net_ball", "Net Ball", 4, 42, ChatFormatting.DARK_AQUA);
        cobblemon(products, ids, "dive_ball", "Dive Ball", 4, 42, ChatFormatting.BLUE);
        cobblemon(products, ids, "luxury_ball", "Luxury Ball", 2, 70, ChatFormatting.GOLD);
        cobblemon(products, ids, "heal_ball", "Heal Ball", 4, 42, ChatFormatting.LIGHT_PURPLE);
        cobblemon(products, ids, "beast_ball", "Beast Ball", 1, 95, ChatFormatting.DARK_PURPLE);
        cobblemon(products, ids, "master_ball", "Master Ball", 1, 300, ChatFormatting.LIGHT_PURPLE);
        cobblemon(products, ids, "safari_ball", "Safari Ball", 2, 85, ChatFormatting.GREEN);
        cobblemon(products, ids, "fast_ball", "Fast Ball", 2, 70, ChatFormatting.YELLOW);
        cobblemon(products, ids, "level_ball", "Level Ball", 2, 70, ChatFormatting.GOLD);
        cobblemon(products, ids, "lure_ball", "Lure Ball", 2, 70, ChatFormatting.BLUE);
        cobblemon(products, ids, "heavy_ball", "Heavy Ball", 2, 70, ChatFormatting.GRAY);
        cobblemon(products, ids, "love_ball", "Love Ball", 2, 70, ChatFormatting.LIGHT_PURPLE);
        cobblemon(products, ids, "friend_ball", "Friend Ball", 2, 70, ChatFormatting.GREEN);
        cobblemon(products, ids, "moon_ball", "Moon Ball", 2, 70, ChatFormatting.DARK_PURPLE);
        cobblemon(products, ids, "sport_ball", "Sport Ball", 2, 70, ChatFormatting.RED);
        cobblemon(products, ids, "dream_ball", "Dream Ball", 2, 95, ChatFormatting.LIGHT_PURPLE);
        cobblemon(products, ids, "cherish_ball", "Cherish Ball", 1, 120, ChatFormatting.RED);

        // Healing, training, and progression supplies.
        cobblemon(products, ids, "potion", "Potion", 4, 14, ChatFormatting.RED);
        cobblemon(products, ids, "super_potion", "Super Potion", 3, 24, ChatFormatting.RED);
        cobblemon(products, ids, "hyper_potion", "Hyper Potion", 2, 38, ChatFormatting.RED);
        cobblemon(products, ids, "max_potion", "Max Potion", 1, 50, ChatFormatting.RED);
        cobblemon(products, ids, "full_restore", "Full Restore", 1, 65, ChatFormatting.LIGHT_PURPLE);
        cobblemon(products, ids, "revive", "Revive", 1, 55, ChatFormatting.YELLOW);
        cobblemon(products, ids, "max_revive", "Max Revive", 1, 110, ChatFormatting.YELLOW);
        cobblemon(products, ids, "revival_herb", "Revival Herb", 1, 80, ChatFormatting.GREEN);
        cobblemon(products, ids, "rare_candy", "Rare Candy", 1, 90, ChatFormatting.LIGHT_PURPLE);
        cobblemon(products, ids, "exp_candy_s", "EXP Candy S", 8, 24, ChatFormatting.GREEN);
        cobblemon(products, ids, "exp_candy_m", "EXP Candy M", 6, 34, ChatFormatting.GREEN);
        cobblemon(products, ids, "exp_candy_l", "EXP Candy L", 4, 45, ChatFormatting.GREEN);
        cobblemon(products, ids, "exp_candy_xl", "EXP Candy XL", 2, 80, ChatFormatting.GREEN);
        cobblemon(products, ids, "ability_capsule", "Ability Capsule", 1, 150, ChatFormatting.AQUA);
        cobblemon(products, ids, "ability_patch", "Ability Patch", 1, 275, ChatFormatting.LIGHT_PURPLE);
        cobblemon(products, ids, "everstone", "Everstone", 1, 60, ChatFormatting.GRAY);
        cobblemon(products, ids, "fire_stone", "Fire Stone", 1, 55, ChatFormatting.RED);
        cobblemon(products, ids, "water_stone", "Water Stone", 1, 55, ChatFormatting.BLUE);
        cobblemon(products, ids, "thunder_stone", "Thunder Stone", 1, 55, ChatFormatting.YELLOW);
        cobblemon(products, ids, "leaf_stone", "Leaf Stone", 1, 55, ChatFormatting.GREEN);
        cobblemon(products, ids, "ice_stone", "Ice Stone", 1, 55, ChatFormatting.AQUA);
        cobblemon(products, ids, "sun_stone", "Sun Stone", 1, 55, ChatFormatting.YELLOW);
        cobblemon(products, ids, "moon_stone", "Moon Stone", 1, 55, ChatFormatting.DARK_PURPLE);
        cobblemon(products, ids, "dawn_stone", "Dawn Stone", 1, 65, ChatFormatting.LIGHT_PURPLE);
        cobblemon(products, ids, "dusk_stone", "Dusk Stone", 1, 65, ChatFormatting.DARK_PURPLE);
        cobblemon(products, ids, "shiny_stone", "Shiny Stone", 1, 75, ChatFormatting.AQUA);
        cobblemon(products, ids, "metal_alloy", "Metal Alloy", 1, 125, ChatFormatting.GRAY);

        discoverOptionalModItems(products, ids);

        return List.copyOf(products);
    }

    private static void vanilla(List<FrontierShopProduct> products, Set<String> ids, String id, String name, Item item,
                                int amount, int cost, ChatFormatting colour) {
        add(products, ids, id, name, item, amount, cost, colour, vanillaCategory(id));
    }

    private static void cobblemon(List<FrontierShopProduct> products, Set<String> ids, String id, String name, int amount,
                                  int cost, ChatFormatting colour) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("cobblemon", id));
        // A missing optional registry entry should hide one product, not break the entire mod.
        add(products, ids, "cobblemon:" + id, name, item, amount, cost, colour, cobblemonCategory(id));
    }

    private static void discoverOptionalModItems(List<FrontierShopProduct> products, Set<String> ids) {
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            if (itemId == null || !OPTIONAL_NAMESPACES.contains(itemId.getNamespace()) || item == Items.AIR) continue;
            if (!isShopItem(itemId.getPath())) continue;

            String id = itemId.toString();
            int cost = optionalCost(itemId.getPath());
            add(products, ids, id, "Mega Showdown · " + titleCase(itemId.getPath()), item,
                    Math.min(4, Math.max(1, item.getDefaultMaxStackSize())), cost, ChatFormatting.LIGHT_PURPLE,
                    Category.MEGA_SHOWDOWN);
        }
    }

    private static Category vanillaCategory(String id) {
        return switch (id) {
            case "golden_apple", "diamond", "netherite_scrap", "nether_star", "totem" -> Category.RARE;
            default -> Category.SUPPLIES;
        };
    }

    private static Category cobblemonCategory(String id) {
        String lower = id.toLowerCase(Locale.ROOT);
        if (lower.contains("ball")) return Category.CAPTURE;
        if (lower.contains("potion") || lower.contains("restore") || lower.contains("revive")
                || lower.contains("herb")) return Category.HEALING;
        if (lower.contains("candy") || lower.contains("stone") || lower.contains("ability")
                || lower.contains("everstone") || lower.contains("alloy")) return Category.EVOLUTION;
        return Category.SUPPLIES;
    }

    private static boolean isShopItem(String path) {
        String lower = path.toLowerCase(Locale.ROOT);
        return !lower.contains("debug")
                && !lower.contains("test")
                && !lower.contains("spawn_egg")
                && !lower.contains("placeholder")
                && !lower.contains("creative")
                && !lower.contains("gui")
                && !lower.contains("menu")
                && !lower.contains("recipe");
    }

    private static int optionalCost(String path) {
        String lower = path.toLowerCase(Locale.ROOT);
        if (lower.contains("master") || lower.contains("mega") || lower.contains("ultra") || lower.contains("dyna")
                || lower.contains("giga") || lower.contains("fusion")) return 240;
        if (lower.contains("key") || lower.contains("bracelet") || lower.contains("band") || lower.contains("ring")) return 180;
        if (lower.contains("stone") || lower.contains("crystal") || lower.contains("orb") || lower.contains("z_")) return 120;
        return 80;
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

    private static void add(List<FrontierShopProduct> products, Set<String> ids, String id, String name, Item item,
                            int amount, int cost, ChatFormatting colour) {
        add(products, ids, id, name, item, amount, cost, colour, Category.SUPPLIES);
    }

    private static void add(List<FrontierShopProduct> products, Set<String> ids, String id, String name, Item item,
                            int amount, int cost, ChatFormatting colour, Category category) {
        if (item != Items.AIR && ids.add(id)) {
            products.add(new FrontierShopProduct(id, name, item, amount, cost, colour, category));
        }
    }
}
