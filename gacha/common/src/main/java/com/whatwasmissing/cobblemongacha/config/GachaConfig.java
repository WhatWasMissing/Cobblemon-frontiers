package com.whatwasmissing.cobblemongacha.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.whatwasmissing.cobblemongacha.core.GachaBanner;
import com.whatwasmissing.cobblemongacha.core.GachaEntry;
import com.whatwasmissing.cobblemongacha.core.GachaRarity;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Server configuration. All odds are weights, not percentages. */
public final class GachaConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean enabled = true;
    public int startingTickets = 3;
    public int capturesPerTicket = 10;
    public int singleDrawCost = 1;
    public int tenDrawCost = 10;
    public int rarePityDraws = 50;
    public int legendaryPityDraws = 100;
    /** Each configured regional group is active for this many real-world hours. */
    public int bannerRotationHours = 1;
    public double shinyChance = 0.01;
    /** Broadcasts exceptional pulls to every online player when enabled. */
    public boolean announceExceptionalDrops = true;
    /** Legendary and Mythic pulls are announced by default; lower tiers stay private. */
    public GachaRarity serverAnnouncementMinimumRarity = GachaRarity.LEGENDARY;
    /** A shiny result is announced regardless of its normal rarity when enabled. */
    public boolean announceShinyDrops = true;
    /** Separate contract chance for legendary Pokémon wagers; 0.1% by default. */
    public double legendaryPokemonChance = 0.001;
    /** A legendary contract also requires a meaningful source item by default. */
    public double legendaryPokemonMinimumSourceValue = 5000.0;
    /** Minimum time between any ticket draw or source-to-target wager; zero disables it. */
    public int gamblingCooldownSeconds = 3;
    /** Minimum time between Pokémon contract wagers per player; zero disables it. */
    public int pokemonWagerCooldownSeconds = 30;
    /** Species already obtainable from Legendary Monuments are excluded from banners. */
    public List<String> legendaryMonumentSpecies = defaultLegendaryMonumentSpecies();
    public List<GachaBanner> banners = defaultBanners();

    public static GachaConfig load(Path path, Logger logger) {
        GachaConfig config = new GachaConfig();
        boolean writeDefaults = !Files.exists(path);
        boolean migrateLegacyBanners = false;
        try {
            Files.createDirectories(path.getParent());
            if (Files.exists(path)) {
                GachaConfig loaded = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), GachaConfig.class);
                if (loaded != null) {
                    migrateLegacyBanners = isLegacyThemedBannerSet(loaded.banners);
                    config = loaded;
                } else {
                    writeDefaults = true;
                    preserveUnreadableConfig(path, logger);
                }
            } else {
                writeDefaults = true;
            }
        } catch (IOException | JsonParseException | IllegalStateException exception) {
            logger.error("Could not load Cobblemon Gacha config at {}. Using defaults.", path, exception);
            writeDefaults = true;
            preserveUnreadableConfig(path, logger);
        }
        config.normalise();
        boolean migrateDefaultTierWeights = config.migrateDefaultTierWeights();
        if (writeDefaults || migrateLegacyBanners || migrateDefaultTierWeights) {
            try {
                Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
                Files.writeString(temporary, GSON.toJson(config), StandardCharsets.UTF_8);
                try {
                    Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException ignored) {
                    Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException exception) {
                logger.error("Could not write Cobblemon Gacha defaults at {}.", path, exception);
            }
        }
        return config;
    }

    /**
     * Raises the pre-0.1% shipped Legendary/Mythic weights. This migration is
     * intentionally exact so values outside the old shipped defaults are not
     * silently overwritten.
     */
    private boolean migrateDefaultTierWeights() {
        if (banners == null) return false;
        boolean changed = false;
        for (GachaBanner banner : banners) {
            if (banner == null || banner.entries == null) continue;
            for (GachaEntry entry : banner.entries) {
                if (entry == null || entry.rarity == null) continue;
                if (entry.rarity == GachaRarity.LEGENDARY
                        && (sameWeight(entry.weight, 0.05) || sameWeight(entry.weight, 0.25))) {
                    entry.weight = 0.88;
                    changed = true;
                } else if (entry.rarity == GachaRarity.MYTHIC && sameWeight(entry.weight, 0.05)) {
                    entry.weight = 0.88;
                    changed = true;
                }
            }
        }
        return changed;
    }

    private static boolean sameWeight(double value, double expected) {
        return Double.isFinite(value) && Math.abs(value - expected) < 0.000001;
    }

    private static void preserveUnreadableConfig(Path path, Logger logger) {
        if (!Files.exists(path)) return;
        try {
            Path backup = path.resolveSibling(path.getFileName() + ".corrupt-" + System.currentTimeMillis());
            Files.move(path, backup, StandardCopyOption.REPLACE_EXISTING);
            logger.error("Moved unreadable Cobblemon Gacha configuration to {}.", backup);
        } catch (IOException backupException) {
            logger.error("Could not preserve unreadable Cobblemon Gacha configuration {}.", path, backupException);
        }
    }

    private void normalise() {
        startingTickets = Math.max(0, startingTickets);
        capturesPerTicket = Math.max(1, capturesPerTicket);
        singleDrawCost = Math.max(1, singleDrawCost);
        tenDrawCost = Math.max(singleDrawCost, tenDrawCost);
        rarePityDraws = Math.max(1, rarePityDraws);
        legendaryPityDraws = Math.max(rarePityDraws, legendaryPityDraws);
        bannerRotationHours = Math.max(1, Math.min(168, bannerRotationHours));
        if (!Double.isFinite(shinyChance)) shinyChance = 0.01;
        shinyChance = Math.max(0.0, Math.min(1.0, shinyChance));
        if (serverAnnouncementMinimumRarity == null) serverAnnouncementMinimumRarity = GachaRarity.LEGENDARY;
        if (!Double.isFinite(legendaryPokemonChance)) legendaryPokemonChance = 0.001;
        legendaryPokemonChance = Math.max(0.0, Math.min(1.0, legendaryPokemonChance));
        if (!Double.isFinite(legendaryPokemonMinimumSourceValue)) legendaryPokemonMinimumSourceValue = 5000.0;
        legendaryPokemonMinimumSourceValue = Math.max(0.0, legendaryPokemonMinimumSourceValue);
        gamblingCooldownSeconds = Math.max(0, Math.min(86_400, gamblingCooldownSeconds));
        pokemonWagerCooldownSeconds = Math.max(0, Math.min(86_400, pokemonWagerCooldownSeconds));
        // Merge the built-in official roster on every load. This upgrades older
        // generated configs instead of allowing a stale 14-entry list to reopen
        // acquisition routes that Legendary Monuments already owns.
        List<String> protectedSpecies = new ArrayList<>(defaultLegendaryMonumentSpecies());
        if (legendaryMonumentSpecies != null) protectedSpecies.addAll(legendaryMonumentSpecies);
        legendaryMonumentSpecies = protectedSpecies.stream()
                .filter(species -> species != null && !species.isBlank())
                .map(GachaConfig::speciesKey)
                .distinct()
                .toList();
        if (banners == null || banners.isEmpty()) banners = defaultBanners();
        else banners = new ArrayList<>(banners);
        if (isLegacyThemedBannerSet(banners)) banners = defaultBanners();
        banners.removeIf(banner -> banner == null || banner.entries == null || banner.entries.isEmpty());
        for (GachaBanner banner : banners) {
            banner.entries = new ArrayList<>(banner.entries);
            if (banner.id == null || banner.id.isBlank()) banner.id = "standard";
            if (banner.title == null || banner.title.isBlank()) banner.title = "Standard Banner";
            if (banner.description == null) banner.description = "A rotating selection of Cobblemon.";
            banner.entries.removeIf(entry -> entry == null || entry.species == null || entry.species.isBlank()
                    || entry.rarity == null || entry.weight <= 0.0 || !Double.isFinite(entry.weight));
            banner.entries.removeIf(entry -> legendaryMonumentSpecies.contains(speciesKey(entry.species)));
            for (GachaEntry entry : banner.entries) {
                if (entry.displayName == null || entry.displayName.isBlank()) entry.displayName = entry.species;
                entry.weight = Math.min(1_000_000_000.0, entry.weight);
            }
        }
        banners.removeIf(banner -> banner.entries == null || banner.entries.isEmpty());
        if (banners.isEmpty()) {
            // A server may intentionally protect every entry in a custom pool.
            // Reset to the defaults once, but never recurse: a broad custom
            // protection list could otherwise make normalisation overflow the
            // stack forever.
            banners = defaultBanners();
            banners.removeIf(banner -> banner == null || banner.entries == null || banner.entries.isEmpty());
            for (GachaBanner banner : banners) {
                banner.entries.removeIf(entry -> entry == null || entry.species == null || entry.species.isBlank()
                        || entry.rarity == null || entry.weight <= 0.0 || !Double.isFinite(entry.weight)
                        || legendaryMonumentSpecies.contains(speciesKey(entry.species)));
            }
            banners.removeIf(banner -> banner.entries == null || banner.entries.isEmpty());
        }
    }

    private static String speciesKey(String species) {
        String value = species.trim().toLowerCase(Locale.ROOT).replace('_', '-');
        int separator = value.indexOf(':');
        return separator >= 0 ? value.substring(separator + 1) : value;
    }

    public boolean isLegendaryMonumentSpecies(String species) {
        return species != null && legendaryMonumentSpecies.contains(speciesKey(species));
    }

    private static boolean isLegacyThemedBannerSet(List<GachaBanner> candidates) {
        if (candidates == null || candidates.size() != 18) return false;
        List<String> legacyIds = List.of("verdant", "ember", "tidal", "voltage", "stone", "night",
                "dragon", "frost", "battle", "fossil", "eon", "sky", "canopy", "urban", "fairy",
                "shadow", "frontier", "beyond_monuments");
        return candidates.stream().allMatch(banner -> banner != null && legacyIds.contains(banner.id));
    }

    private static List<String> defaultLegendaryMonumentSpecies() {
        // Keep this list aligned with the current Legendary Monuments wiki and
        // Modrinth page. The config remains editable so a server can add
        // server-specific monument acquisitions without changing the gacha jar.
        return new ArrayList<>(List.of(
                "zacian", "zamazenta", "cobalion", "virizion", "terrakion", "keldeo",
                "moltres", "articuno", "zapdos", "raikou", "entei", "suicune", "cosmog",
                "cosmoem", "lunala", "solgaleo", "regirock", "registeel", "regice",
                "regieleki", "regidrago", "regigigas", "eternatus", "reshiram", "zekrom",
                "kyurem", "latias", "latios", "hoopa", "ho-oh", "lugia", "cresselia",
                "darkrai", "heatran", "celebi", "chien-pao", "chi-yu", "ting-lu", "wo-chien",
                "mew", "azelf", "uxie", "mesprit", "palkia", "dialga", "giratina", "arceus",
                "victini", "yveltal", "xerneas", "marshadow", "meloetta", "spectrier", "glastrier",
                "calyrex", "enamorus", "meltan", "melmetal"));
    }

    private static List<GachaBanner> defaultBanners() {
        // Each pool is a regional research exchange. One region is active at
        // a time; the active region moves forward every bannerRotationHours.
        List<GachaBanner> banners = new ArrayList<>();
        banners.add(banner("kanto", "Kanto Research", "Classic partners from the original field guide.",
                common("bulbasaur", "Bulbasaur"), common("charmander", "Charmander"), common("squirtle", "Squirtle"),
                common("pikachu", "Pikachu"), common("eevee", "Eevee"), common("pidgey", "Pidgey"),
                common("geodude", "Geodude"), common("zubat", "Zubat"), rare("abra", "Abra"),
                rare("dratini", "Dratini"), epic("lapras", "Lapras"), epic("snorlax", "Snorlax"),
                unmonumentedLegendary("mewtwo", "Mewtwo"), mythic("mew", "Mew")));
        banners.add(banner("johto", "Johto Research", "Rural routes, ancient towers, and Johto staples.",
                common("chikorita", "Chikorita"), common("cyndaquil", "Cyndaquil"), common("totodile", "Totodile"),
                common("sentret", "Sentret"), common("mareep", "Mareep"), common("hoothoot", "Hoothoot"),
                common("yanma", "Yanma"), common("sneasel", "Sneasel"), rare("togepi", "Togepi"),
                rare("heracross", "Heracross"), epic("scizor", "Scizor"), epic("tyranitar", "Tyranitar"),
                unmonumentedLegendary("lugia", "Lugia"), mythic("celebi", "Celebi")));
        banners.add(banner("hoenn", "Hoenn Research", "Coasts, caves, and high-energy Hoenn encounters.",
                common("treecko", "Treecko"), common("torchic", "Torchic"), common("mudkip", "Mudkip"),
                common("ralts", "Ralts"), common("shroomish", "Shroomish"), common("electrike", "Electrike"),
                common("aron", "Aron"), common("wailmer", "Wailmer"), rare("feebas", "Feebas"),
                rare("bagon", "Bagon"), epic("salamence", "Salamence"), epic("metagross", "Metagross"),
                unmonumentedLegendary("rayquaza", "Rayquaza"), mythic("deoxys", "Deoxys")));
        banners.add(banner("sinnoh", "Sinnoh Research", "Mountain routes, lakes, and Sinnoh evolution lines.",
                common("turtwig", "Turtwig"), common("chimchar", "Chimchar"), common("piplup", "Piplup"),
                common("starly", "Starly"), common("shinx", "Shinx"), common("bidoof", "Bidoof"),
                common("budew", "Budew"), common("hippopotas", "Hippopotas"), rare("riolu", "Riolu"),
                rare("gible", "Gible"), epic("lucario", "Lucario"), epic("garchomp", "Garchomp"),
                unmonumentedLegendary("heatran", "Heatran"), mythic("manaphy", "Manaphy")));
        banners.add(banner("unova", "Unova Research", "City routes, hidden groves, and Unova partners.",
                common("snivy", "Snivy"), common("tepig", "Tepig"), common("oshawott", "Oshawott"),
                common("pidove", "Pidove"), common("lillipup", "Lillipup"), common("sandile", "Sandile"),
                common("litwick", "Litwick"), common("scraggy", "Scraggy"), rare("zoroark", "Zoroark"),
                rare("axew", "Axew"), epic("chandelure", "Chandelure"), epic("volcarona", "Volcarona"),
                unmonumentedLegendary("landorus", "Landorus"), mythic("genesect", "Genesect")));
        banners.add(banner("kalos", "Kalos Research", "Kalos routes, fairy valleys, and polished partners.",
                common("chespin", "Chespin"), common("fennekin", "Fennekin"), common("froakie", "Froakie"),
                common("bunnelby", "Bunnelby"), common("flabebe", "Flabébé"), common("honedge", "Honedge"),
                common("goomy", "Goomy"), common("helioptile", "Helioptile"), rare("noibat", "Noibat"),
                rare("espurr", "Espurr"), epic("aegislash", "Aegislash"), epic("goodra", "Goodra"),
                unmonumentedLegendary("zygarde", "Zygarde"), mythic("diancie", "Diancie")));
        banners.add(banner("alola", "Alola Research", "Island trials, tropical habitats, and Alolan discoveries.",
                common("rowlet", "Rowlet"), common("litten", "Litten"), common("popplio", "Popplio"),
                common("pikipek", "Pikipek"), common("rockruff", "Rockruff"), common("mareanie", "Mareanie"),
                common("cutiefly", "Cutiefly"), common("stufful", "Stufful"), rare("mimikyu", "Mimikyu"),
                rare("turtonator", "Turtonator"), epic("kommo-o", "Kommo-o"), epic("salazzle", "Salazzle"),
                unmonumentedLegendary("necrozma", "Necrozma"), mythic("zeraora", "Zeraora")));
        banners.add(banner("galar", "Galar Research", "Wild Areas, stadium towns, and Galar discoveries.",
                common("grookey", "Grookey"), common("scorbunny", "Scorbunny"), common("sobble", "Sobble"),
                common("wooloo", "Wooloo"), common("rookidee", "Rookidee"), common("yamper", "Yamper"),
                common("impidimp", "Impidimp"), common("applin", "Applin"), rare("dreepy", "Dreepy"),
                rare("sizzlipede", "Sizzlipede"), epic("corviknight", "Corviknight"), epic("dragapult", "Dragapult"),
                unmonumentedLegendary("urshifu", "Urshifu"), mythic("zarude", "Zarude")));
        banners.add(banner("hisui", "Hisui Research", "Ancient routes, space-time rifts, and Hisuian forms.",
                common("rowlet", "Rowlet"), common("cyndaquil", "Cyndaquil"), common("oshawott", "Oshawott"),
                common("wyrdeer", "Wyrdeer"), common("kleavor", "Kleavor"), common("basculegion", "Basculegion"),
                common("overqwil", "Overqwil"), common("ursaluna", "Ursaluna"), rare("hisuian-zorua", "Hisuian Zorua"),
                rare("hisuian-sliggoo", "Hisuian Sliggoo"), epic("hisuian-arcanine", "Hisuian Arcanine"),
                epic("hisuian-electrode", "Hisuian Electrode"), unmonumentedLegendary("tornadus", "Tornadus"),
                mythic("shaymin", "Shaymin")));
        banners.add(banner("paldea", "Paldea Research", "Open skies, crater research, and Paldean partners.",
                common("sprigatito", "Sprigatito"), common("fuecoco", "Fuecoco"), common("quaxly", "Quaxly"),
                common("pawmi", "Pawmi"), common("fidough", "Fidough"), common("smoliv", "Smoliv"),
                common("nacli", "Nacli"), common("wiglett", "Wiglett"), rare("charcadet", "Charcadet"),
                rare("frigibax", "Frigibax"), epic("ceruledge", "Ceruledge"), epic("annihilape", "Annihilape"),
                unmonumentedLegendary("koraidon", "Koraidon"), mythic("terapagos", "Terapagos")));
        return banners;
    }

    private static List<GachaBanner> legacyThemedBanners() {
        // Retained as source compatibility for older generated configs.
        List<GachaBanner> banners = new ArrayList<>();
        banners.add(banner("verdant", "Verdant Trails", "Grass, bug, and forest-dwelling partners.",
                common("bulbasaur", "Bulbasaur"), common("oddish", "Oddish"), common("bellsprout", "Bellsprout"),
                common("chikorita", "Chikorita"), common("treecko", "Treecko"), common("turtwig", "Turtwig"),
                common("snivy", "Snivy"), common("rowlet", "Rowlet"), rare("tangela", "Tangela"),
                rare("grookey", "Grookey"), epic("venusaur", "Venusaur"), epic("sceptile", "Sceptile"),
                legendary("tapu-bulu", "Tapu Bulu"), mythic("shaymin", "Shaymin")));
        banners.add(banner("ember", "Ember & Ash", "Fire starters, volcanic survivors, and blazing aces.",
                common("charmander", "Charmander"), common("growlithe", "Growlithe"), common("vulpix", "Vulpix"),
                common("ponyta", "Ponyta"), common("cyndaquil", "Cyndaquil"), common("torchic", "Torchic"),
                common("chimchar", "Chimchar"), common("fuecoco", "Fuecoco"), rare("litten", "Litten"),
                rare("fletchinder", "Fletchinder"), epic("arcanine", "Arcanine"), epic("blaziken", "Blaziken"),
                legendary("groudon", "Groudon"), mythic("volcanion", "Volcanion")));
        banners.add(banner("tidal", "Tidal Current", "Water partners from quiet ponds to deep oceans.",
                common("squirtle", "Squirtle"), common("psyduck", "Psyduck"), common("magikarp", "Magikarp"),
                common("goldeen", "Goldeen"), common("totodile", "Totodile"), common("mudkip", "Mudkip"),
                common("piplup", "Piplup"), common("froakie", "Froakie"), rare("feebas", "Feebas"),
                rare("oshawott", "Oshawott"), epic("lapras", "Lapras"), epic("gyarados", "Gyarados"),
                legendary("kyogre", "Kyogre"), mythic("manaphy", "Manaphy")));
        banners.add(banner("voltage", "Voltage Circuit", "Electric specialists, charged companions, and storm power.",
                common("pikachu", "Pikachu"), common("magnemite", "Magnemite"), common("mareep", "Mareep"),
                common("plusle", "Plusle"), common("minun", "Minun"), common("shinx", "Shinx"),
                common("blitzle", "Blitzle"), common("pawmi", "Pawmi"), rare("electabuzz", "Electabuzz"),
                rare("emolga", "Emolga"), epic("ampharos", "Ampharos"), epic("luxray", "Luxray"),
                legendary("thundurus", "Thundurus"), mythic("zeraora", "Zeraora")));
        banners.add(banner("stone", "Stone & Steel", "Rock-solid defenders and forged battle partners.",
                common("geodude", "Geodude"), common("onix", "Onix"), common("roggenrola", "Roggenrola"),
                common("nosepass", "Nosepass"), common("aron", "Aron"), common("bronzor", "Bronzor"),
                common("nacli", "Nacli"), common("rockruff", "Rockruff"), rare("drilbur", "Drilbur"),
                rare("klawf", "Klawf"), epic("steelix", "Steelix"), epic("aggron", "Aggron"),
                legendary("genesect", "Genesect"), mythic("jirachi", "Jirachi")));
        banners.add(banner("night", "Mystic Night", "Ghosts, dark types, and strange things after sunset.",
                common("zubat", "Zubat"), common("gastly", "Gastly"), common("murkrow", "Murkrow"),
                common("misdreavus", "Misdreavus"), common("duskull", "Duskull"), common("zorua", "Zorua"),
                common("litwick", "Litwick"), common("shuppet", "Shuppet"), rare("sableye", "Sableye"),
                rare("mimikyu", "Mimikyu"), epic("gengar", "Gengar"), epic("chandelure", "Chandelure"),
                legendary("deoxys", "Deoxys"), mythic("zarude", "Zarude")));
        banners.add(banner("dragon", "Dragon's Roost", "Draconic lines from tiny hatchlings to apex predators.",
                common("dratini", "Dratini"), common("bagon", "Bagon"), common("gible", "Gible"),
                common("axew", "Axew"), common("deino", "Deino"), common("goomy", "Goomy"),
                common("jangmo-o", "Jangmo-o"), common("dreepy", "Dreepy"), rare("applin", "Applin"),
                rare("noibat", "Noibat"), epic("dragonair", "Dragonair"), epic("fraxure", "Fraxure"),
                legendary("zygarde", "Zygarde"), mythic("genesect", "Genesect")));
        banners.add(banner("frost", "Frozen Frontier", "Icebound partners and creatures of the polar wilds.",
                common("snorunt", "Snorunt"), common("swinub", "Swinub"), common("spheal", "Spheal"),
                common("bergmite", "Bergmite"), common("cubchoo", "Cubchoo"), common("vanillite", "Vanillite"),
                common("sneasel", "Sneasel"), common("amaura", "Amaura"), rare("delibird", "Delibird"),
                rare("cryogonal", "Cryogonal"), epic("froslass", "Froslass"), epic("mamoswine", "Mamoswine"),
                legendary("necrozma", "Necrozma"), mythic("diancie", "Diancie")));
        banners.add(banner("battle", "Battle Masters", "Fighting partners built for arenas and hard rematches.",
                common("machop", "Machop"), common("meditite", "Meditite"), common("makuhita", "Makuhita"),
                common("tyrogue", "Tyrogue"), common("riolu", "Riolu"), common("timburr", "Timburr"),
                common("hawlucha", "Hawlucha"), common("falinks", "Falinks"), rare("breloom", "Breloom"),
                rare("grapploct", "Grapploct"), epic("machamp", "Machamp"), epic("lucario", "Lucario"),
                legendary("urshifu", "Urshifu"), mythic("deoxys", "Deoxys")));
        banners.add(banner("fossil", "Fossil Archive", "Ancient life restored from stone and amber.",
                common("omanyte", "Omanyte"), common("kabuto", "Kabuto"), common("lileep", "Lileep"),
                common("anorith", "Anorith"), common("cranidos", "Cranidos"), common("shieldon", "Shieldon"),
                common("tirtouga", "Tirtouga"), common("archen", "Archen"), rare("aerodactyl", "Aerodactyl"),
                rare("amaura", "Amaura"), epic("archeops", "Archeops"), epic("tyrantrum", "Tyrantrum"),
                legendary("groudon", "Groudon"), mythic("diancie", "Diancie")));
        banners.add(banner("eon", "Eon Companions", "The full evolution family, with a few impossible secrets.",
                common("eevee", "Eevee"), common("vaporeon", "Vaporeon"), common("jolteon", "Jolteon"),
                common("flareon", "Flareon"), common("espeon", "Espeon"), common("umbreon", "Umbreon"),
                common("leafeon", "Leafeon"), common("glaceon", "Glaceon"), rare("sylveon", "Sylveon"),
                rare("porygon2", "Porygon2"), epic("gardevoir", "Gardevoir"), epic("gallade", "Gallade"),
                legendary("mewtwo", "Mewtwo"), mythic("jirachi", "Jirachi")));
        banners.add(banner("sky", "Skyward Passage", "Flying partners and guardians above the clouds.",
                common("pidgey", "Pidgey"), common("spearow", "Spearow"), common("taillow", "Taillow"),
                common("starly", "Starly"), common("pidove", "Pidove"), common("fletchling", "Fletchling"),
                common("rookidee", "Rookidee"), common("noibat", "Noibat"), rare("noctowl", "Noctowl"),
                rare("staravia", "Staravia"), epic("pidgeot", "Pidgeot"), epic("corviknight", "Corviknight"),
                legendary("tornadus", "Tornadus"), mythic("shaymin", "Shaymin")));
        banners.add(banner("canopy", "Canopy Chorus", "Small forest friends and the rare guardians among them.",
                common("caterpie", "Caterpie"), common("weedle", "Weedle"), common("venonat", "Venonat"),
                common("wurmple", "Wurmple"), common("sewaddle", "Sewaddle"), common("scatterbug", "Scatterbug"),
                common("ledyba", "Ledyba"), common("spinarak", "Spinarak"), rare("scyther", "Scyther"),
                rare("heracross", "Heracross"), epic("butterfree", "Butterfree"), epic("leavanny", "Leavanny"),
                legendary("tapu-bulu", "Tapu Bulu"), mythic("shaymin", "Shaymin")));
        banners.add(banner("urban", "Urban Relays", "Artificial life, machines, and city-dwelling partners.",
                common("porygon", "Porygon"), common("magnemite", "Magnemite"), common("koffing", "Koffing"),
                common("voltorb", "Voltorb"), common("grimer", "Grimer"), common("trubbish", "Trubbish"),
                common("klink", "Klink"), common("bronzor", "Bronzor"), rare("rotom", "Rotom"),
                rare("porygon2", "Porygon2"), epic("porygon-z", "Porygon-Z"), epic("metagross", "Metagross"),
                legendary("miraidon", "Miraidon"), mythic("genesect", "Genesect")));
        banners.add(banner("fairy", "Fairy Garden", "Dreamy, charming, and deceptively powerful partners.",
                common("clefairy", "Clefairy"), common("jigglypuff", "Jigglypuff"), common("ralts", "Ralts"),
                common("snubbull", "Snubbull"), common("mawile", "Mawile"), common("marill", "Marill"),
                common("swirlix", "Swirlix"), common("fidough", "Fidough"), rare("togepi", "Togepi"),
                rare("flabebe", "Flabébé"), epic("gardevoir", "Gardevoir"), epic("sylveon", "Sylveon"),
                legendary("magearna", "Magearna"), mythic("diancie", "Diancie")));
        banners.add(banner("shadow", "Shadow Hunt", "Predators, tricksters, and the darkest encounters.",
                common("poochyena", "Poochyena"), common("houndour", "Houndour"), common("sneasel", "Sneasel"),
                common("purrloin", "Purrloin"), common("scraggy", "Scraggy"), common("nickit", "Nickit"),
                common("impidimp", "Impidimp"), common("maschiff", "Maschiff"), rare("absol", "Absol"),
                rare("zorua", "Zorua"), epic("mightyena", "Mightyena"), epic("weavile", "Weavile"),
                legendary("zarude", "Zarude"), mythic("deoxys", "Deoxys")));
        banners.add(banner("frontier", "Frontier Stars", "A broad rotating pool for players who want a little of everything.",
                common("rattata", "Rattata"), common("pidgey", "Pidgey"), common("zubat", "Zubat"),
                common("geodude", "Geodude"), common("magikarp", "Magikarp"), common("sandshrew", "Sandshrew"),
                common("growlithe", "Growlithe"), common("squirtle", "Squirtle"), rare("eevee", "Eevee"),
                rare("dratini", "Dratini"), epic("snorlax", "Snorlax"), epic("tyranitar", "Tyranitar"),
                legendary("rayquaza", "Rayquaza"), mythic("jirachi", "Jirachi")));
        banners.add(banner("beyond_monuments", "Beyond the Monuments",
                "Legendary and Mythical Pokémon reserved for the banner system rather than monument acquisition.",
                common("ralts", "Ralts"), common("eevee", "Eevee"), common("dratini", "Dratini"),
                common("riolu", "Riolu"), common("shinx", "Shinx"), common("zorua", "Zorua"),
                common("bagon", "Bagon"), common("pichu", "Pichu"), rare("lapras", "Lapras"),
                rare("lucario", "Lucario"), rare("metagross", "Metagross"), rare("gardevoir", "Gardevoir"),
                rare("garchomp", "Garchomp"), epic("snorlax", "Snorlax"), epic("tyranitar", "Tyranitar"),
                epic("dragonite", "Dragonite"), epic("volcarona", "Volcarona"),
                unmonumentedLegendary("kyogre", "Kyogre"), unmonumentedLegendary("groudon", "Groudon"),
                unmonumentedLegendary("rayquaza", "Rayquaza"), unmonumentedLegendary("mewtwo", "Mewtwo"),
                unmonumentedLegendary("thundurus", "Thundurus"), unmonumentedLegendary("tornadus", "Tornadus"),
                unmonumentedLegendary("landorus", "Landorus"), unmonumentedLegendary("zygarde", "Zygarde"),
                unmonumentedLegendary("necrozma", "Necrozma"), unmonumentedLegendary("tapu-koko", "Tapu Koko"),
                unmonumentedLegendary("tapu-lele", "Tapu Lele"), unmonumentedLegendary("tapu-bulu", "Tapu Bulu"),
                unmonumentedLegendary("tapu-fini", "Tapu Fini"), unmonumentedLegendary("urshifu", "Urshifu"),
                unmonumentedLegendary("koraidon", "Koraidon"), unmonumentedLegendary("miraidon", "Miraidon"),
                unmonumentedLegendary("ogerpon", "Ogerpon"), unmonumentedLegendary("terapagos", "Terapagos"),
                mythic("manaphy", "Manaphy"), mythic("jirachi", "Jirachi"), mythic("shaymin", "Shaymin"),
                mythic("deoxys", "Deoxys"), mythic("diancie", "Diancie"), mythic("genesect", "Genesect"),
                mythic("magearna", "Magearna"), mythic("volcanion", "Volcanion"), mythic("zarude", "Zarude"),
                mythic("zeraora", "Zeraora")));
        return banners;
    }

    private static GachaBanner banner(String id, String title, String description, GachaEntry... entries) {
        return new GachaBanner(id, title, description, new ArrayList<>(List.of(entries)));
    }

    private static GachaEntry common(String id, String name) { return new GachaEntry(id, name, GachaRarity.COMMON, 100.0); }
    private static GachaEntry rare(String id, String name) { return new GachaEntry(id, name, GachaRarity.RARE, 30.0); }
    private static GachaEntry epic(String id, String name) { return new GachaEntry(id, name, GachaRarity.EPIC, 8.0); }
    private static GachaEntry legendary(String id, String name) { return new GachaEntry(id, name, GachaRarity.LEGENDARY, 0.88); }
    private static GachaEntry unmonumentedLegendary(String id, String name) {
        return new GachaEntry(id, name, GachaRarity.LEGENDARY, 0.88);
    }
    private static GachaEntry mythic(String id, String name) { return new GachaEntry(id, name, GachaRarity.MYTHIC, 0.88); }
}
