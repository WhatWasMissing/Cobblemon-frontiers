# Encounter catalogue

> **Source and scope:** Exact requirements below are transcribed from the official Myths and Legends datapack repository checked on 25 September 2026. The server may have a newer, older, or modified datapack. These are spawn-entry conditions, not guaranteed spawn promises.

The source contained 81 spawn-pool files and 159 enabled Pokémon spawn entries covering 81 species. Entries are grouped by species; each numbered route is an alternative entry. Every condition shown within a route must be satisfied together.

## Reading the catalogue

- **Key item** is the `key_item` condition and must be in the eligible player inventory.
- **Biomes** lists exact biome IDs and/or biome tags. `#` means a tag.
- **Other conditions** gives all remaining condition fields as compact JSON so item consumption counts, time, weather, blocks, and party requirements are not lost in paraphrase.
- **Level / bucket / weight** are the source spawn definition values. Weight is relative to competing entries in the bucket, not a percentage.
- If a field is absent, the source entry did not specify it.

## Species and routes

### Arceus

**Route 1** — level `80`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-arceus-1`.
- Key item: `mythsandlegends:azure_flute`
- Biomes: `minecraft:the_end`, `minecraft:end_barrens`, `minecraft:end_highlands`, `minecraft:end_midlands`, `minecraft:small_end_islands`
- Other conditions: none

**Route 2** — level `80`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-arceus-2`.
- Key item: `mythsandlegends:azure_flute`
- Biomes: `minecraft:dark_forest`, `#byg:is_magical`, `#wythers:is_dark_forest`, `byg:skyris_vale`, `terralith:amethyst_canyon`, `terralith:amethyst_rainforest`, `terralith:mirage_isles`, `terralith:moonlight_grove`, `terralith:moonlight_valley`, `wythers:lantern_river`, `wythers:mushroom_island`, `wythers:snowy_thermal_taiga`, `#cobblemon:is_end`
- Other conditions: none

### Articuno

**Route 1** — level `50-60`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-articuno-1`.
- Key item: `mythsandlegends:tidal_bell`
- Biomes: `minecraft:frozen_ocean`, `minecraft:frozen_peaks`, `minecraft:frozen_river`, `minecraft:snowy_beach`, `minecraft:snowy_plains`, `minecraft:snowy_slopes`, `minecraft:snowy_taiga`
- Other conditions: `{"item_requirement":[{"id":"cobblemon:ice_stone","count":3,"consume":true}]}`

**Route 2** — level `50-60`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-articuno-2`.
- Key item: `mythsandlegends:tidal_bell`
- Biomes: `#cobblemon:is_cold`, `#cobblemon:is_freezing`, `#cobblemon:is_snowy_forest`, `#cobblemon:is_glacial`, `#cobblemon:is_frozen_ocean`
- Other conditions: `{"item_requirement":[{"id":"cobblemon:ice_stone","count":3,"consume":true}]}`

### Azelf

**Route 1** — level `50`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-azelf-1`.
- Key item: `mythsandlegends:azelf_fang`
- Biomes: `minecraft:mangrove_swamp`, `minecraft:swamp`, `#byg:is_swamp`, `#c:swamp`, `#forge:is_swamp`, `#wythers:is_swamp`, `terralith:ice_marsh`, `terralith:orchid_swampc`, `wythers:billabong`
- Other conditions: none

**Route 2** — level `50`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-azelf-2`.
- Key item: `mythsandlegends:azelf_fang`
- Biomes: `#cobblemon:is_river`, `wythers:lantern_river`, `#minecraft:is_river`, `wythers:tropical_forest_river`, `#cobblemon:is_freshwater`
- Other conditions: none

### Calyrex

**Route 1** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-calyrex-1`.
- Key item: `mythsandlegends:reins_of_unity`
- Biomes: `#cobblemon:is_autumn`, `minecraft:flower_forest`
- Other conditions: none

### Celebi

**Route 1** — level `30-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-celebi-1`.
- Key item: `mythsandlegends:gs_ball`
- Biomes: `minecraft:birch_forest`, `minecraft:dark_forest`, `minecraft:flower_forest`, `minecraft:forest`, `minecraft:jungle`, `minecraft:mangrove_swamp`, `minecraft:meadow`, `minecraft:old_growth_birch_forest`, `minecraft:old_growth_pine_taiga`, `minecraft:old_growth_spruce_taiga`, `minecraft:swamp`
- Other conditions: none

**Route 2** — level `30-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-celebi-2`.
- Key item: `mythsandlegends:gs_ball`
- Biomes: `#cobblemon:is_forest`, `#cobblemon:is_magical`, `#cobblemon:is_lush`, `minecraft:cherry_grove`, `#cobblemon:is_spring`
- Other conditions: none

### Cobalion

**Route 1** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-cobalion-1`.
- Key item: `mythsandlegends:ironwill_sword`
- Biomes: `#minecraft:is_hill`, `#cobblemon:is_highlands`, `#c:mountain_slope`, `#forge:is_slope`, `terralith:blooming_valley`, `terralith:forested_highlands`, `terralith:lavender_valley`, `terralith:lush_valley`, `terralith:moonlight_valley`, `terralith:sakura_valley`, `terralith:savanna_slopes`, `terralith:temperate_highlands`, `terralith:yosemite_lowlands`, `wythers:autumnal_crags`, `wythers:ayers_rock`, `wythers:icy_crags`, `wythers:old_growth_taiga_crags`, `wythers:taiga_crags`, `wythers:temperate_rainforest_crags`, `wythers:thermal_taiga_crags`, `wythers:windswept_jungle`
- Other conditions: none

**Route 2** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-cobalion-2`.
- Key item: `mythsandlegends:ironwill_sword`
- Biomes: `#minecraft:is_mountain`, `#cobblemon:is_hills`, `#forge:is_mountain`, `terralith:stony_spires`, `terralith:volcanic_peaks`, `terralith:windswept_spires`, `terralith:yosemite_cliffs`, `wythers:tibesti_mountains`
- Other conditions: none

### Cosmog

**Route 1** — level `15`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-cosmog-1`.
- Key item: `mythsandlegends:lillies_bag`
- Biomes: `minecraft:cherry_grove`, `minecraft:flower_forest`, `minecraft:meadow`, `minecraft:sunflower_plains`, `#byg:is_floral`, `#c:floral`, `#c:flower_forests`, `byg:amaranth_fields`, `byg:allium_fields`, `byg:rose_fields`, `byg:skyris_vale`, `byg:cherry_blossom_forest`, `byg:orchard`, `terralith:blooming_plateau`, `terralith:blooming_valley`, `terralith:lavender_forest`, `terralith:lavender_valley`, `terralith:sakura_grove`, `terralith:sakura_valley`, `wythers:autumnal_flower_forest`, `wythers:flowering_pantanal`, `wythers:jacaranda_savanna`, `wythers:lapacho_plains`, `wythers:sakura_forest`, `wythers:spring_flower_fields`, `wythers:spring_flower_forest`
- Other conditions: none

**Route 2** — level `15`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-cosmog-2`.
- Key item: `mythsandlegends:lillies_bag`
- Biomes: `minecraft:the_end`, `minecraft:end_barrens`, `minecraft:end_highlands`, `minecraft:end_midlands`, `minecraft:small_end_islands`
- Other conditions: none

### Cresselia

**Route 1** — level `50-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-cresselia-1`.
- Key item: `mythsandlegends:lunar_feather`
- Biomes: `minecraft:mushroom_fields`, `minecraft:mushroom_field_shore`, `minecraft:snowy_tundra`
- Other conditions: `{"moonPhase":1,"canSeeSky":true,"timeRange":"night"}`

### Darkrai

**Route 1** — level `40-50`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-darkrai-1`.
- Key item: `mythsandlegends:member_card`
- Biomes: `minecraft:dark_forest`, `minecraft:swamp`
- Other conditions: none

**Route 2** — level `40-50`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-darkrai-2`.
- Key item: `mythsandlegends:member_card`
- Biomes: `#cobblemon:is_spooky`, `#cobblemon:is_dark`, `#cobblemon:is_deep_dark`
- Other conditions: none

### Deoxys

**Route 1** — level `75`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-deoxys-1`.
- Key item: `mythsandlegends:aurora_ticket`
- Biomes: `minecraft:the_end`, `minecraft:end_barrens`, `minecraft:end_highlands`, `minecraft:end_midlands`, `minecraft:small_end_islands`
- Other conditions: none

**Route 2** — level `75`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-deoxys-2`.
- Key item: `mythsandlegends:aurora_ticket`
- Biomes: `#cobblemon:is_end`
- Other conditions: none

### Dialga

**Route 1** — level `47-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-dialga-1`.
- Key item: `mythsandlegends:adamant_orb`
- Biomes: `minecraft:mountain_edge`, `minecraft:stony_peaks`
- Other conditions: none

**Route 2** — level `47-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-dialga-2`.
- Key item: `mythsandlegends:adamant_orb`
- Biomes: `#cobblemon:is_highlands`, `#cobblemon:is_peak`, `#cobblemon:is_mountain`
- Other conditions: none

### Diancie

**Route 1** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-diancie-1`.
- Key item: `mythsandlegends:diancies_crown`
- Biomes: `#cobblemon:is_cave`
- Other conditions: `{"maxY":10,"minY":-64}`

**Route 2** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-diancie-2`.
- Key item: `mythsandlegends:diancies_crown`
- Biomes: `minecraft:dripstone_caves`, `minecraft:lush_caves`, `#c:caves`, `#c:underground`, `#forge:is_underground`, `terralith:cave/andesite_caves`, `terralith:cave/desert_caves`, `terralith:cave/diorite_caves`, `terralith:cave/fungal_caves`, `terralith:cave/granite_caves`, `terralith:cave/infested_caves`, `terralith:cave/thermal_caves`, `terralith:cave/underground_jungle`
- Other conditions: `{"maxY":10,"minY":-64}`

### Enamorus

**Route 1** — level `50-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-enamorus-1`.
- Key item: `mythsandlegends:reveal_glass`
- Biomes: `minecraft:flower_forest`, `minecraft:meadow`, `minecraft:cherry_grove`
- Other conditions: `{"isThundering":true}`

### Entei

**Route 1** — level `50-60`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-entei-1`.
- Key item: `mythsandlegends:clear_bell`
- Biomes: `minecraft:badlands`, `minecraft:eroded_badlands`, `minecraft:nether_wastes`, `minecraft:savanna`, `minecraft:windswept_hills`, `minecraft:windswept_forest`
- Other conditions: `{"item_requirement":[{"id":"cobblemon:fire_stone","count":3,"consume":true}]}`

**Route 2** — level `50-60`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-entei-2`.
- Key item: `mythsandlegends:clear_bell`
- Biomes: `#cobblemon:is_volcanic`, `#cobblemon:is_thermal`, `#cobblemon:is_badlands`
- Other conditions: `{"item_requirement":[{"id":"cobblemon:fire_stone","count":3,"consume":true}]}`

### Eternatus

**Route 1** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-eternatus-1`.
- Key item: `mythsandlegends:eternatus_core`
- Biomes: `minecraft:mangrove_swamp`, `minecraft:swamp`, `#byg:is_swamp`, `#c:swamp`, `#forge:is_swamp`, `#wythers:is_swamp`, `terralith:ice_marsh`, `terralith:orchid_swampc`, `wythers:billabong`
- Other conditions: none

**Route 2** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-eternatus-2`.
- Key item: `mythsandlegends:eternatus_core`
- Biomes: `minecraft:the_end`, `minecraft:end_barrens`, `minecraft:end_highlands`, `minecraft:end_midlands`, `minecraft:small_end_islands`
- Other conditions: none

### Genesect

**Route 1** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-genesect-1`.
- Key item: `mythsandlegends:genesect_drive`
- Biomes: `#cobblemon:is_deep_dark`
- Other conditions: `{"neededNearbyBlocks":["minecraft:iron_block"]}`

**Route 2** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-genesect-2`.
- Key item: `mythsandlegends:genesect_drive`
- Biomes: `minecraft:the_end`, `minecraft:end_barrens`, `minecraft:end_highlands`, `minecraft:end_midlands`, `minecraft:small_end_islands`
- Other conditions: `{"neededNearbyBlocks":["minecraft:iron_block"]}`

### Giratina

**Route 1** — level `47-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-giratina-1`.
- Key item: `mythsandlegends:griseous_orb`
- Biomes: `minecraft:soul_sand_valley`, `minecraft:the_end`
- Other conditions: none

**Route 2** — level `47-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-giratina-2`.
- Key item: `mythsandlegends:griseous_orb`
- Biomes: `#cobblemon:is_nether`, `#cobblemon:is_spooky`, `#cobblemon:is_nether_wasteland`
- Other conditions: none

### Glastrier

**Route 1** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-glastrier-1`.
- Key item: `mythsandlegends:iceroot_carrot`
- Biomes: `#cobblemon:is_snowy_forest`, `#cobblemon:is_taiga`, `#cobblemon:is_mountain`
- Other conditions: none

**Route 2** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-glastrier-2`.
- Key item: `mythsandlegends:iceroot_carrot`
- Biomes: `#cobblemon:is_snowy_forest`, `#cobblemon:is_taiga`, `#cobblemon:is_mountain`
- Other conditions: none

### Groudon

**Route 1** — level `45-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-groudon-1`.
- Key item: `mythsandlegends:red_orb`
- Biomes: `#cobblemon:is_volcanic`, `minecraft:badlands`, `minecraft:desert`, `minecraft:eroded_badlands`
- Other conditions: none

**Route 2** — level `45-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-groudon-2`.
- Key item: `mythsandlegends:red_orb`
- Biomes: `#cobblemon:is_volcanic`, `#cobblemon:is_desert`, `#cobblemon:is_arid`, `#cobblemon:is_thermal`
- Other conditions: none

### Heatran

**Route 1** — level `50-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-heatran-1`.
- Key item: `mythsandlegends:magma_stone`
- Biomes: `minecraft:nether_wastes`, `minecraft:basalt_deltas`, `minecraft:crimson_forest`
- Other conditions: none

### Hooh

**Route 1** — level `40-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-hooh-1`.
- Key item: `mythsandlegends:clear_bell`
- Biomes: `minecraft:meadow`, `minecraft:sunflower_plains`, `minecraft:flower_forest`
- Other conditions: none

**Route 2** — level `40-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-hooh-2`.
- Key item: `mythsandlegends:clear_bell`
- Biomes: `#cobblemon:is_sky`, `minecraft:sunflower_plains`, `#cobblemon:is_summer`
- Other conditions: none

**Route 3** — level `40-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-hooh-3`.
- Key item: `mythsandlegends:rainbow_wing`
- Biomes: `minecraft:meadow`, `minecraft:sunflower_plains`, `minecraft:flower_forest`
- Other conditions: none

**Route 4** — level `40-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-hooh-4`.
- Key item: `mythsandlegends:rainbow_wing`
- Biomes: `#cobblemon:is_sky`, `#minecraft:sunflower_plains`, `#cobblemon:is_summer`
- Other conditions: none

### Hoopa

**Route 1** — level `50`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-hoopa-1`.
- Key item: `mythsandlegends:hoopa_ring`
- Biomes: `minecraft:the_end`, `minecraft:end_barrens`, `minecraft:end_highlands`, `minecraft:end_midlands`, `minecraft:small_end_islands`
- Other conditions: none

**Route 2** — level `50`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-hoopa-1`.
- Key item: `mythsandlegends:prism_bottle`
- Biomes: `minecraft:the_end`, `minecraft:end_barrens`, `minecraft:end_highlands`, `minecraft:end_midlands`, `minecraft:small_end_islands`
- Other conditions: none

### Jirachi

**Route 1** — level `5-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-jirachi-1`.
- Key item: `mythsandlegends:bonus_disk`
- Biomes: `minecraft:mushroom_fields`, `minecraft:windswept_gravelly_hills`
- Other conditions: none

**Route 2** — level `5-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-jirachi-2`.
- Key item: `mythsandlegends:bonus_disk`
- Biomes: `#cobblemon:is_magical`, `#cobblemon:is_sparse`
- Other conditions: none

### Keldeo

**Route 1** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-keldeo-1`.
- Key item: `mythsandlegends:sacred_sword`
- Biomes: `#minecraft:is_hill`, `#cobblemon:is_highlands`, `#c:mountain_slope`, `#forge:is_slope`, `terralith:blooming_valley`, `terralith:forested_highlands`, `terralith:lavender_valley`, `terralith:lush_valley`, `terralith:moonlight_valley`, `terralith:sakura_valley`, `terralith:savanna_slopes`, `terralith:temperate_highlands`, `terralith:yosemite_lowlands`, `wythers:autumnal_crags`, `wythers:ayers_rock`, `wythers:icy_crags`, `wythers:old_growth_taiga_crags`, `wythers:taiga_crags`, `wythers:temperate_rainforest_crags`, `wythers:thermal_taiga_crags`, `wythers:windswept_jungle`
- Other conditions: none

**Route 2** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-keldeo-2`.
- Key item: `mythsandlegends:sacred_sword`
- Biomes: `#minecraft:is_mountain`, `#cobblemon:is_hills`, `#forge:is_mountain`, `terralith:stony_spires`, `terralith:volcanic_peaks`, `terralith:windswept_spires`, `terralith:yosemite_cliffs`, `wythers:tibesti_mountains`
- Other conditions: none

### Koraidon

**Route 1** — level `68-72`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-koraidon-1`.
- Key item: `mythsandlegends:scarlet_book`
- Biomes: `minecraft:badlands`, `minecraft:eroded_badlands`, `minecraft:savanna`, `minecraft:savanna_plateau`
- Other conditions: none

**Route 2** — level `68-.72`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-koraidon-2`.
- Key item: `mythsandlegends:scarlet_book`
- Biomes: `#cobblemon:is_savanna`, `#cobblemon:is_plateau`, `#cobblemon:is_mountain`
- Other conditions: none

### Kubfu

**Route 1** — level `15`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-kubfu-1`.
- Key item: `mythsandlegends:kubfus_band`
- Biomes: `#minecraft:is_jungle`, `terralith:cave/underground_jungle`, `wythers:dripleaf_swamp`, `wythers:eucalyptus_deanei_forest`, `wythers:highland_tropical_rainforest`, `wythers:humid_tropical_grassland`, `wythers:jungle_canyon`, `wythers:subtropical_forest`, `wythers:subtropical_forest_edge`, `wythers:subtropical_grassland`, `wythers:tropical_forest`, `wythers:tropical_forest_canyon`, `wythers:tropical_grassland`, `wythers:tropical_island`, `wythers:tropical_rainforest`
- Other conditions: none

**Route 2** — level `15`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-kubfu-2`.
- Key item: `mythsandlegends:kubfus_band`
- Biomes: `wythers:bamboo_jungle_highlands`, `#cobblemon:is_bamboo`, `wythers:bamboo_jungle_canyon`, `wythers:bamboo_jungle_highlands`, `wythers:bamboo_jungle_swamp`, `wythers:bamboo_swamp`, `wythers:sakura_forest`, `wythers:sandy_jungle`, `wythers:sparse_bamboo_jungle`
- Other conditions: none

### Kyogre

**Route 1** — level `45-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-kyogre-1`.
- Key item: `mythsandlegends:blue_orb`
- Biomes: `#cobblemon:is_ocean`, `minecraft:deep_cold_ocean`, `minecraft:deep_frozen_ocean`, `minecraft:deep_ocean`, `minecraft:ocean`
- Other conditions: none

**Route 2** — level `45-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-kyogre-2`.
- Key item: `mythsandlegends:blue_orb`
- Biomes: `#cobblemon:is_deep_ocean`, `#cobblemon:is_ocean`
- Other conditions: none

### Kyurem

**Route 1** — level `75`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-kyurem-1`.
- Key item: `mythsandlegends:dna_splicer`
- Biomes: `minecraft:frozen_peaks`, `minecraft:snowy_slopes`, `minecraft:snowy_taiga`
- Other conditions: none

**Route 2** — level `75`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-kyurem-2`.
- Key item: `mythsandlegends:dna_splicer`
- Biomes: `#cobblemon:is_freezing`, `#cobblemon:is_glacial`, `#cobblemon:is_snowy_forest`
- Other conditions: none

### Landorus

**Route 1** — level `50-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-landorus-1`.
- Key item: `mythsandlegends:reveal_glass`
- Biomes: `minecraft:savanna`, `minecraft:sunflower_plains`, `minecraft:desert`
- Other conditions: `{"isThundering":true}`

### Latias

**Route 1** — level `30-50`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-latias-1`.
- Key item: `mythsandlegends:eon_ticket`
- Biomes: `terralith:mirage_isles`, `minecraft:flower_forest`, `minecraft:birch_forest`
- Other conditions: none

**Route 2** — level `30-50`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-latias-2`.
- Key item: `mythsandlegends:eon_ticket`
- Biomes: `#cobblemon:is_coast`, `#cobblemon:is_temperate`, `#cobblemon:is_beach`
- Other conditions: none

### Latios

**Route 1** — level `30-50`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-latios-1`.
- Key item: `mythsandlegends:eon_ticket`
- Biomes: `terralith:mirage_isles`, `minecraft:plains`, `minecraft:mountain_edge`
- Other conditions: none

**Route 2** — level `30-50`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-latios-2`.
- Key item: `mythsandlegends:eon_ticket`
- Biomes: `#cobblemon:is_coast`, `#cobblemon:is_temperate`, `#cobblemon:is_beach`
- Other conditions: none

### Lugia

**Route 1** — level `40-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-lugia-1`.
- Key item: `mythsandlegends:tidal_bell`
- Biomes: `#minecraft:is_deep_ocean`, `minecraft:deep_ocean`, `minecraft:ocean`
- Other conditions: none

**Route 2** — level `40-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-lugia-2`.
- Key item: `mythsandlegends:tidal_bell`
- Biomes: `#cobblemon:is_deep_ocean`, `#cobblemon:is_ocean`, `#cobblemon:is_cold_ocean`
- Other conditions: none

**Route 3** — level `40-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-lugia-3`.
- Key item: `mythsandlegends:silver_wing`
- Biomes: `#cobblemon:is_deep_ocean`, `minecraft:deep_ocean`, `minecraft:ocean`
- Other conditions: none

**Route 4** — level `40-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-lugia-4`.
- Key item: `mythsandlegends:silver_wing`
- Biomes: `#cobblemon:is_deep_ocean`, `#cobblemon:is_ocean`, `#cobblemon:is_cold_ocean`
- Other conditions: none

### Lunala

**Route 1** — level `70-80`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-lunala-1`.
- Key item: `mythsandlegends:moon_flute`
- Biomes: `minecraft:dark_forest`, `byg:warped_desert`, `#cobblemon:is_hills`
- Other conditions: `{"timeRange":"night"}`

### Magearna

**Route 1** — level `50`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-magearna-1`.
- Key item: `mythsandlegends:antique_pokeball`
- Biomes: `#cobblemon:is_deep_dark`
- Other conditions: `{"neededNearbyBlocks":["minecraft:iron_block"]}`

**Route 2** — level `50`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-magearna-2`.
- Key item: `mythsandlegends:antique_pokeball`
- Biomes: `minecraft:the_end`, `minecraft:end_barrens`, `minecraft:end_highlands`, `minecraft:end_midlands`, `minecraft:small_end_islands`
- Other conditions: `{"neededNearbyBlocks":["minecraft:iron_block"]}`

**Route 3** — level `50`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-magearna-3`.
- Key item: `mythsandlegends:soul_heart`
- Biomes: `#cobblemon:is_deep_dark`
- Other conditions: `{"neededNearbyBlocks":["minecraft:iron_block"]}`

**Route 4** — level `50`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-magearna-4`.
- Key item: `mythsandlegends:soul_heart`
- Biomes: `minecraft:the_end`, `minecraft:end_barrens`, `minecraft:end_highlands`, `minecraft:end_midlands`, `minecraft:small_end_islands`
- Other conditions: `{"neededNearbyBlocks":["minecraft:iron_block"]}`

### Marshadow

**Route 1** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-marshadow-1`.
- Key item: `mythsandlegends:marshadow_hood`
- Biomes: `minecraft:dark_forest`, `minecraft:swamp`
- Other conditions: none

**Route 2** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-marshadow-2`.
- Key item: `mythsandlegends:marshadow_hood`
- Biomes: `#cobblemon:is_spooky`, `#cobblemon:is_dark`, `#cobblemon:is_deep_dark`
- Other conditions: none

### Meloetta

**Route 1** — level `50`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-meloetta-1`.
- Key item: `mythsandlegends:meloetta_headset`
- Biomes: `minecraft:cherry_grove`, `minecraft:flower_forest`, `minecraft:meadow`, `minecraft:sunflower_plains`, `#byg:is_floral`, `#c:floral`, `#c:flower_forests`, `byg:amaranth_fields`, `byg:allium_fields`, `byg:rose_fields`, `byg:skyris_vale`, `byg:cherry_blossom_forest`, `byg:orchard`, `terralith:blooming_plateau`, `terralith:blooming_valley`, `terralith:lavender_forest`, `terralith:lavender_valley`, `terralith:sakura_grove`, `terralith:sakura_valley`, `wythers:autumnal_flower_forest`, `wythers:flowering_pantanal`, `wythers:jacaranda_savanna`, `wythers:lapacho_plains`, `wythers:sakura_forest`, `wythers:spring_flower_fields`, `wythers:spring_flower_forest`
- Other conditions: none

### Meltan

**Route 1** — level `20-40`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-meltan-1`.
- Key item: `mythsandlegends:mystery_box`
- Biomes: `minecraft:taiga`, `minecraft:snowy_taiga`, `minecraft:mountains`
- Other conditions: `{"neededNearbyBlocks":["minecraft:iron_block"]}`

### Mesprit

**Route 1** — level `50`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-mesprit-1`.
- Key item: `mythsandlegends:mesprit_plume`
- Biomes: `minecraft:mangrove_swamp`, `minecraft:swamp`, `#byg:is_swamp`, `#c:swamp`, `#forge:is_swamp`, `#wythers:is_swamp`, `terralith:ice_marsh`, `terralith:orchid_swamp`, `wythers:billabong`
- Other conditions: none

**Route 2** — level `50`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-mesprit-2`.
- Key item: `mythsandlegends:mesprit_plume`
- Biomes: `#cobblemon:is_river`, `wythers:lantern_river`, `#minecraft:is_river`, `wythers:tropical_forest_river`, `#cobblemon:is_freshwater`
- Other conditions: none

### Mew

**Route 1** — level `30-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-mew-1`.
- Key item: `mythsandlegends:old_sea_map`
- Biomes: `#cobblemon:is_jungle`, `minecraft:bamboo_jungle`, `minecraft:jungle`
- Other conditions: none

**Route 2** — level `30-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-mew-2`.
- Key item: `mythsandlegends:old_sea_map`
- Biomes: `#cobblemon:is_jungle`, `#cobblemon:is_lush`, `#cobblemon:is_bamboo`
- Other conditions: none

### Mewtwo

**Route 1** — level `70-75`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-mewtwo-1`.
- Key item: `mythsandlegends:dr_fujis_diary`
- Biomes: `minecraft:windswept_gravelly_hills`, `minecraft:dark_forest`, `minecraft:deep_dark`
- Other conditions: none

**Route 2** — level `70-75`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-mewtwo-2`.
- Key item: `mythsandlegends:dr_fujis_diary`
- Biomes: `#cobblemon:is_cave`, `#cobblemon:is_deep_dark`, `#cobblemon:is_spooky`
- Other conditions: none

### Miraidon

**Route 1** — level `68-72`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-miraidon-1`.
- Key item: `mythsandlegends:violet_book`
- Biomes: `minecraft:windswept_gravelly_hills`, `minecraft:deep_dark`
- Other conditions: none

**Route 2** — level `68-72`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-miraidon-2`.
- Key item: `mythsandlegends:violet_book`
- Biomes: `#cobblemon:is_deep_dark`
- Other conditions: none

### Moltres

**Route 1** — level `50-60`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-moltres-1`.
- Key item: `mythsandlegends:tidal_bell`
- Biomes: `minecraft:badlands`, `minecraft:windswept_hills`
- Other conditions: `{"item_requirement":[{"id":"cobblemon:fire_stone","count":3,"consume":true}]}`

**Route 2** — level `50-60`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-moltres-2`.
- Key item: `mythsandlegends:tidal_bell`
- Biomes: `#cobblemon:is_volcanic`, `#cobblemon:is_thermal`, `#cobblemon:is_sky`
- Other conditions: `{"item_requirement":[{"id":"cobblemon:fire_stone","count":3,"consume":true}]}`

### Necrozma

**Route 1** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-necrozma-1`.
- Key item: `mythsandlegends:necro_prism`
- Biomes: `#cobblemon:is_deep_dark`
- Other conditions: `{"shiny_stone_requirement":"3"}`

**Route 2** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-necrozma-2`.
- Key item: `mythsandlegends:necro_prism`
- Biomes: `minecraft:the_end`, `minecraft:end_barrens`, `minecraft:end_highlands`, `minecraft:end_midlands`, `minecraft:small_end_islands`
- Other conditions: `{"shiny_stone_requirement":"3"}`

### Ogerpon

**Route 1** — level `40-60`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-ogerpon-1`.
- Key item: `mythsandlegends:teal_mask`
- Biomes: `#cobblemon:is_floral`, `#cobblemon:is_bamboo`, `#cobblemon:is_jungle`
- Other conditions: `{"moonPhase":1}`

### Palkia

**Route 1** — level `47-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-palkia-1`.
- Key item: `mythsandlegends:lustrous_orb`
- Biomes: `terralith:skylands_winter`, `minecraft:the_end`, `minecraft:end_barrens`, `minecraft:end_highlands`, `minecraft:end_midlands`, `minecraft:small_end_islands`
- Other conditions: none

**Route 2** — level `47-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-palkia-2`.
- Key item: `mythsandlegends:lustrous_orb`
- Biomes: `terralith:skylands_winter`, `minecraft:the_end`, `minecraft:end_barrens`, `minecraft:end_highlands`, `minecraft:end_midlands`, `#cobblemon:is_end`, `minecraft:small_end_islands`
- Other conditions: none

### Pecharunt

**Route 1** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-pecharunt-1`.
- Key item: `mythsandlegends:mythical_pecha_berry`
- Biomes: `minecraft:dark_forest`, `minecraft:swamp`
- Other conditions: none

**Route 2** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-pecharunt-2`.
- Key item: `mythsandlegends:mythical_pecha_berry`
- Biomes: `minecraft:woodland_mansion`, `minecraft:swamp_hut`, `minecraft:pillager_outpost`
- Other conditions: none

### Raikou

**Route 1** — level `50-60`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-raikou-1`.
- Key item: `mythsandlegends:clear_bell`
- Biomes: `minecraft:plains`, `minecraft:savanna`, `minecraft:savanna_plateau`
- Other conditions: `{"item_requirement":[{"id":"cobblemon:thunder_stone","count":3,"consume":true}]}`

**Route 2** — level `50-60`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-raikou-2`.
- Key item: `mythsandlegends:clear_bell`
- Biomes: `#cobblemon:is_plains`, `#cobblemon:is_grassland`, `#cobblemon:is_arid`
- Other conditions: `{"item_requirement":[{"id":"cobblemon:thunder_stone","count":3,"consume":true}]}`

### Rayquaza

**Route 1** — level `50-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-rayquaza-1`.
- Key item: `mythsandlegends:jade_orb`
- Biomes: `terralith:skylands_autumn`, `terralith:skylands_spring`, `terralith:skylands_summer`, `terralith:skylands_winter`, `#cobblemon:is_jungle`, `#minecraft:is_jungle`
- Other conditions: none

**Route 2** — level `50-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-rayquaza-2`.
- Key item: `mythsandlegends:jade_orb`
- Biomes: `#cobblemon:is_sky`, `#cobblemon:is_highlands`, `minecraft:end_highlands`, `#minecraft:is_end`, `#cobblemon:is_end`, `#cobblemon:is_jungle`, `#minecraft:is_jungle`
- Other conditions: none

### Regice

**Route 1** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-regice-1`.
- Key item: `mythsandlegends:ice_tablet`
- Biomes: `minecraft:frozen_river`, `minecraft:jagged_peaks`, `minecraft:snowy_beach`, `minecraft:snowy_plains`, `minecraft:snowy_slopes`, `#cobblemon:is_frozen_ocean`, `#cobblemon:is_glacial`, `#cobblemon:is_snowy_forest`, `#byg:is_snowy`, `#c:snowy`, `#forge:is_snowy`, `byg:cardinal_tundra`, `terralith:emerald_peaks`, `terralith:scarlet_mountains`, `terralith:skylands_winter`, `terralith:snowy_badlands`, `wythers:crimson_tundra`, `wythers:frozen_island`, `wythers:snowy_bog`, `wythers:snowy_canyon`, `wythers:snowy_peaks`, `wythers:snowy_tundra`
- Other conditions: none

**Route 2** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-regice-2`.
- Key item: `mythsandlegends:ice_tablet`
- Biomes: `#cobblemon:is_freezing`, `#cobblemon:is_peak`, `#cobblemon:is_taiga`, `#cobblemon:is_tundra`, `#byg:is_cold`, `#c:climate_cold`, `#forge:is_cold/overworld`, `wythers:berry_bog`
- Other conditions: none

**Route 3** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-regice-3`.
- Key item: `mythsandlegends:ancient_tablet`
- Biomes: `minecraft:frozen_river`, `minecraft:jagged_peaks`, `minecraft:snowy_beach`, `minecraft:snowy_plains`, `minecraft:snowy_slopes`, `#cobblemon:is_frozen_ocean`, `#cobblemon:is_glacial`, `#cobblemon:is_snowy_forest`, `#byg:is_snowy`, `#c:snowy`, `#forge:is_snowy`, `byg:cardinal_tundra`, `terralith:emerald_peaks`, `terralith:scarlet_mountains`, `terralith:skylands_winter`, `terralith:snowy_badlands`, `wythers:crimson_tundra`, `wythers:frozen_island`, `wythers:snowy_bog`, `wythers:snowy_canyon`, `wythers:snowy_peaks`, `wythers:snowy_tundra`
- Other conditions: none

**Route 4** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-regice-4`.
- Key item: `mythsandlegends:ancient_tablet`
- Biomes: `#cobblemon:is_freezing`, `#cobblemon:is_peak`, `#cobblemon:is_taiga`, `#cobblemon:is_tundra`, `#byg:is_cold`, `#c:climate_cold`, `#forge:is_cold/overworld`, `wythers:berry_bog`
- Other conditions: none

### Regidrago

**Route 1** — level `40`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-regidrago-1`.
- Key item: `mythsandlegends:scaly_tablet`
- Biomes: `#minecraft:is_nether`
- Other conditions: none

**Route 2** — level `40`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-regidrago-2`.
- Key item: `mythsandlegends:scaly_tablet`
- Biomes: `minecraft:the_end`, `minecraft:end_barrens`, `minecraft:end_highlands`, `minecraft:end_midlands`, `minecraft:small_end_islands`
- Other conditions: none

**Route 3** — level `40`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-regidrago-3`.
- Key item: `mythsandlegends:ancient_tablet`
- Biomes: `minecraft:the_end`, `minecraft:end_barrens`, `minecraft:end_highlands`, `minecraft:end_midlands`, `minecraft:small_end_islands`
- Other conditions: none

**Route 4** — level `40`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-regidrago-4`.
- Key item: `mythsandlegends:ancient_tablet`
- Biomes: `minecraft:the_end`, `minecraft:end_barrens`, `minecraft:end_highlands`, `minecraft:end_midlands`, `minecraft:small_end_islands`
- Other conditions: none

### Regieleki

**Route 1** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-regieleki-1`.
- Key item: `mythsandlegends:plasma_tablet`
- Biomes: `#cobblemon:is_deep_dark`
- Other conditions: none

**Route 2** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-regieleki-2`.
- Key item: `mythsandlegends:ancient_tablet`
- Biomes: `#cobblemon:is_deep_dark`
- Other conditions: none

### Regigigas

**Route 1** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-regigigas-1`.
- Key item: `mythsandlegends:ancient_tablet`
- Biomes: `#cobblemon:is_deep_dark`
- Other conditions: none

**Route 2** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-regigigas-2`.
- Key item: `mythsandlegends:ancient_tablet`
- Biomes: `#minecraft:is_savanna`, `#minecraft:savanna_plateau`, `#wythers:is_dark_forest`, `terralith:savanna_slopes`, `terralith:ashen_savanna`, `terralith:fractured_savanna`, `terralith:savanna_badlands`, `terralith:savanna_slopes`, `minecraft:savanna_plateau`, `#cobblemon:is_savanna`
- Other conditions: none

**Route 3** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-regigigas-3`.
- Key item: `—`
- Biomes: `#minecraft:is_savanna`, `#minecraft:savanna_plateau`, `#wythers:is_dark_forest`, `terralith:savanna_slopes`, `terralith:ashen_savanna`, `terralith:fractured_savanna`, `terralith:savanna_badlands`, `terralith:savanna_slopes`, `minecraft:savanna_plateau`, `#cobblemon:is_savanna`
- Other conditions: `{"pokemon_in_party_requirement":[{"species":"regice","count":1},{"species":"regidrago","count":1},{"species":"regieleki","count":1},{"species":"regirock","count":1},{"species":"registeel","count":1}]}`

### Regirock

**Route 1** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-regirock-1`.
- Key item: `mythsandlegends:stone_tablet`
- Biomes: `minecraft:desert`, `#byg:is_desert`, `#c:desert`, `#wythers:is_desert`, `terralith:ancient_sands`, `terralith:desert_canyon`, `terralith:cave/desert_caves`, `terralith:desert_oasis`, `terralith:desert_spires`, `terralith:lush_desert`, `terralith:red_oasis`, `terralith:sandstone_valley`, `wythers:badlands_desert`, `wythers:desert_island`, `wythers:kwongan_heath`, `wythers:outback_desert`, `wythers:red_desert`, `wythers:sandy_jungle`
- Other conditions: `{"maxY":63}`

**Route 2** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-regirock-2`.
- Key item: `mythsandlegends:stone_tablet`
- Biomes: `#minecraft:is_badlands`, `#c:mesa`, `terralith:ashen_savanna`, `terralith:red_oasis`, `terralith:warped_mesa`, `terralith:white_mesa`, `wythers:danakil_desert`
- Other conditions: `{"maxY":63}`

**Route 3** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-regirock-3`.
- Key item: `mythsandlegends:ancient_tablet`
- Biomes: `#minecraft:is_badlands`, `#c:mesa`, `terralith:ashen_savanna`, `terralith:red_oasis`, `terralith:warped_mesa`, `terralith:white_mesa`, `wythers:danakil_desert`
- Other conditions: `{"maxY":63}`

### Registeel

**Route 1** — level `40`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-registeel-1`.
- Key item: `mythsandlegends:steel_tablet`
- Biomes: `minecraft:deep_dark`, `#cobblemon:is_deep_dark`
- Other conditions: `{"neededNearbyBlocks":["minecraft:iron_block"]}`

**Route 2** — level `40`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-registeel-2`.
- Key item: `mythsandlegends:steel_tablet`
- Biomes: `#minecraft:village`
- Other conditions: `{"neededNearbyBlocks":["minecraft:iron_block"]}`

**Route 3** — level `40`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-registeel-2`.
- Key item: `mythsandlegends:ancient_tablet`
- Biomes: `#minecraft:village`
- Other conditions: `{"neededNearbyBlocks":["minecraft:iron_block"]}`

### Reshiram

**Route 1** — level `60-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-reshiram-1`.
- Key item: `mythsandlegends:light_stone`
- Biomes: `minecraft:savanna`, `minecraft:sunflower_plains`, `minecraft:desert`
- Other conditions: `{"minLight":8,"maxLight":15}`

### Shaymin

**Route 1** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-shaymin-1`.
- Key item: `mythsandlegends:oaks_letter`
- Biomes: `minecraft:sunflower_plains`, `minecraft:flower_forest`, `minecraft:flower_forest`, `minecraft:meadow`, `terralith:blooming_plateau`, `terralith:blooming_valley`, `terralith:lavender_valley`, `terralith:sakura_grove`, `terralith:sakura_valley`, `#cobblemon:is_floral`, `#cobblemon:is_grassland`
- Other conditions: none

**Route 2** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-shaymin-2`.
- Key item: `mythsandlegends:oaks_letter`
- Biomes: `#cobblemon:is_lush`
- Other conditions: none

### Solgaleo

**Route 1** — level `70-80`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-solgaleo-1`.
- Key item: `mythsandlegends:sun_flute`
- Biomes: `#cobblemon:is_plateau`, `minecraft:desert`, `wythers:badlands_desert`
- Other conditions: `{"timeRange":"day"}`

### Spectrier

**Route 1** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-spectrier-1`.
- Key item: `mythsandlegends:shaderoot_carrot`
- Biomes: `#cobblemon:is_forest`, `#cobblemon:is_dark_forest`, `#cobblemon:is_plains`
- Other conditions: none

**Route 2** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-spectrier-2`.
- Key item: `mythsandlegends:shaderoot_carrot`
- Biomes: `#cobblemon:is_forest`, `#cobblemon:is_spooky`, `#cobblemon:is_plains`
- Other conditions: none

### Suicune

**Route 1** — level `50-60`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-suicune-1`.
- Key item: `mythsandlegends:clear_bell`
- Biomes: `minecraft:frozen_river`, `minecraft:river`
- Other conditions: `{"item_requirement":[{"id":"cobblemon:water_stone","count":3,"consume":true}]}`

**Route 2** — level `50-60`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-suicune-2`.
- Key item: `mythsandlegends:clear_bell`
- Biomes: `#cobblemon:is_freshwater`, `#cobblemon:is_cold_ocean`
- Other conditions: `{"item_requirement":[{"id":"cobblemon:water_stone","count":3,"consume":true}]}`

### Tapubulu

**Route 1** — level `60`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-tapubulu-1`.
- Key item: `mythsandlegends:bulu_totem`
- Biomes: `#minecraft:is_jungle`, `terralith:cave/underground_jungle`, `wythers:dripleaf_swamp`, `wythers:eucalyptus_deanei_forest`, `wythers:highland_tropical_rainforest`, `wythers:humid_tropical_grassland`, `wythers:jungle_canyon`, `wythers:subtropical_forest`, `wythers:subtropical_forest_edge`, `wythers:subtropical_grassland`, `wythers:tropical_forest`, `wythers:tropical_forest_canyon`, `wythers:tropical_grassland`, `wythers:tropical_island`, `wythers:tropical_rainforest`
- Other conditions: none

**Route 2** — level `60`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-tapubulu-2`.
- Key item: `mythsandlegends:bulu_totem`
- Biomes: `minecraft:stony_shore`, `#cobblemon:is_beach`, `#c:stony_shores`, `terralith:basalt_cliffs`, `terralith:granite_cliffs`, `terralith:white_cliffs`, `wythers:calcite_coast`, `wythers:coastal_mangroves`, `wythers:cold_island`, `wythers:cold_stony_shore`, `wythers:deepslate_shore`, `wythers:frigid_island`, `wythers:frozen_island`, `wythers:gravelly_beach`, `wythers:icy_shore`, `wythers:mediterranean_island`, `wythers:temperate_island`, `wythers:tropical_island`, `wythers:warm_stony_shore`
- Other conditions: none

### Tapufini

**Route 1** — level `60`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-tapufini-1`.
- Key item: `mythsandlegends:fini_totem`
- Biomes: `#minecraft:is_jungle`, `terralith:cave/underground_jungle`, `wythers:dripleaf_swamp`, `wythers:eucalyptus_deanei_forest`, `wythers:highland_tropical_rainforest`, `wythers:humid_tropical_grassland`, `wythers:jungle_canyon`, `wythers:subtropical_forest`, `wythers:subtropical_forest_edge`, `wythers:subtropical_grassland`, `wythers:tropical_forest`, `wythers:tropical_forest_canyon`, `wythers:tropical_grassland`, `wythers:tropical_island`, `wythers:tropical_rainforest`
- Other conditions: none

**Route 2** — level `60`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-tapufini-2`.
- Key item: `mythsandlegends:fini_totem`
- Biomes: `minecraft:stony_shore`, `#cobblemon:is_beach`, `#c:stony_shores`, `terralith:basalt_cliffs`, `terralith:granite_cliffs`, `terralith:white_cliffs`, `wythers:calcite_coast`, `wythers:coastal_mangroves`, `wythers:cold_island`, `wythers:cold_stony_shore`, `wythers:deepslate_shore`, `wythers:frigid_island`, `wythers:frozen_island`, `wythers:gravelly_beach`, `wythers:icy_shore`, `wythers:mediterranean_island`, `wythers:temperate_island`, `wythers:tropical_island`, `wythers:warm_stony_shore`
- Other conditions: none

### Tapukoko

**Route 1** — level `60`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-tapukoko-1`.
- Key item: `mythsandlegends:koko_totem`
- Biomes: `#minecraft:is_jungle`, `terralith:cave/underground_jungle`, `wythers:dripleaf_swamp`, `wythers:eucalyptus_deanei_forest`, `wythers:highland_tropical_rainforest`, `wythers:humid_tropical_grassland`, `wythers:jungle_canyon`, `wythers:subtropical_forest`, `wythers:subtropical_forest_edge`, `wythers:subtropical_grassland`, `wythers:tropical_forest`, `wythers:tropical_forest_canyon`, `wythers:tropical_grassland`, `wythers:tropical_island`, `wythers:tropical_rainforest`
- Other conditions: none

**Route 2** — level `60`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-tapukoko-2`.
- Key item: `mythsandlegends:koko_totem`
- Biomes: `minecraft:stony_shore`, `#cobblemon:is_beach`, `#c:stony_shores`, `terralith:basalt_cliffs`, `terralith:granite_cliffs`, `terralith:white_cliffs`, `wythers:calcite_coast`, `wythers:coastal_mangroves`, `wythers:cold_island`, `wythers:cold_stony_shore`, `wythers:deepslate_shore`, `wythers:frigid_island`, `wythers:frozen_island`, `wythers:gravelly_beach`, `wythers:icy_shore`, `wythers:mediterranean_island`, `wythers:temperate_island`, `wythers:tropical_island`, `wythers:warm_stony_shore`
- Other conditions: none

### Tapulele

**Route 1** — level `60`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-tapulele-1`.
- Key item: `mythsandlegends:lele_totem`
- Biomes: `#minecraft:is_jungle`, `terralith:cave/underground_jungle`, `wythers:dripleaf_swamp`, `wythers:eucalyptus_deanei_forest`, `wythers:highland_tropical_rainforest`, `wythers:humid_tropical_grassland`, `wythers:jungle_canyon`, `wythers:subtropical_forest`, `wythers:subtropical_forest_edge`, `wythers:subtropical_grassland`, `wythers:tropical_forest`, `wythers:tropical_forest_canyon`, `wythers:tropical_grassland`, `wythers:tropical_island`, `wythers:tropical_rainforest`
- Other conditions: none

**Route 2** — level `60`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-tapulele-2`.
- Key item: `mythsandlegends:lele_totem`
- Biomes: `minecraft:stony_shore`, `#cobblemon:is_beach`, `#c:stony_shores`, `terralith:basalt_cliffs`, `terralith:granite_cliffs`, `terralith:white_cliffs`, `wythers:calcite_coast`, `wythers:coastal_mangroves`, `wythers:cold_island`, `wythers:cold_stony_shore`, `wythers:deepslate_shore`, `wythers:frigid_island`, `wythers:frozen_island`, `wythers:gravelly_beach`, `wythers:icy_shore`, `wythers:mediterranean_island`, `wythers:temperate_island`, `wythers:tropical_island`, `wythers:warm_stony_shore`
- Other conditions: none

### Terapagos

**Route 1** — level `75`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-terapagos-1`.
- Key item: `mythsandlegends:prismatic_shell`
- Biomes: `#cobblemon:is_mountain`, `#cobblemon:is_magical`, `#cobblemon:is_river`, `#cobblemon:is_jungle`
- Other conditions: none

**Route 2** — level `75`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-terapagos-2`.
- Key item: `mythsandlegends:prismatic_shell`
- Biomes: `#cobblemon:is_mountain`, `#cobblemon:is_magical`, `#cobblemon:is_river`, `#cobblemon:is_jungle`
- Other conditions: none

### Terrakion

**Route 1** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-terrakion-1`.
- Key item: `mythsandlegends:cavern_shield`
- Biomes: `#minecraft:is_hill`, `#cobblemon:is_highlands`, `#c:mountain_slope`, `#forge:is_slope`, `terralith:blooming_valley`, `terralith:forested_highlands`, `terralith:lavender_valley`, `terralith:lush_valley`, `terralith:moonlight_valley`, `terralith:sakura_valley`, `terralith:savanna_slopes`, `terralith:temperate_highlands`, `terralith:yosemite_lowlands`, `wythers:autumnal_crags`, `wythers:ayers_rock`, `wythers:icy_crags`, `wythers:old_growth_taiga_crags`, `wythers:taiga_crags`, `wythers:temperate_rainforest_crags`, `wythers:thermal_taiga_crags`, `wythers:windswept_jungle`
- Other conditions: none

**Route 2** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-terrakion-2`.
- Key item: `mythsandlegends:cavern_shield`
- Biomes: `#minecraft:is_mountain`, `#cobblemon:is_hills`, `#forge:is_mountain`, `terralith:stony_spires`, `terralith:volcanic_peaks`, `terralith:windswept_spires`, `terralith:yosemite_cliffs`, `wythers:tibesti_mountains`
- Other conditions: none

### Thundurus

**Route 1** — level `50-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-thundurus-1`.
- Key item: `mythsandlegends:reveal_glass`
- Biomes: `minecraft:plains`, `minecraft:mountains`, `minecraft:badlands`
- Other conditions: `{"isThundering":true}`

### Tornadus

**Route 1** — level `50-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-tornadus-1`.
- Key item: `mythsandlegends:reveal_glass`
- Biomes: `minecraft:jungle`, `minecraft:bamboo_jungle`, `minecraft:jungle_edge`
- Other conditions: `{"isThundering":true}`

### Typenull

**Route 1** — level `60`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-typenull-1`.
- Key item: `mythsandlegends:type_null_mask`
- Biomes: `#cobblemon:is_deep_dark`
- Other conditions: none

**Route 2** — level `60`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-typenull-2`.
- Key item: `mythsandlegends:type_null_mask`
- Biomes: `minecraft:the_end`, `minecraft:end_barrens`, `minecraft:end_highlands`, `minecraft:end_midlands`, `minecraft:small_end_islands`
- Other conditions: none

### Uxie

**Route 1** — level `50`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-uxie-1`.
- Key item: `mythsandlegends:uxie_claw`
- Biomes: `minecraft:mangrove_swamp`, `minecraft:swamp`, `#byg:is_swamp`, `#c:swamp`, `#forge:is_swamp`, `#wythers:is_swamp`, `terralith:ice_marsh`, `terralith:orchid_swampc`, `wythers:billabong`
- Other conditions: none

**Route 2** — level `50`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-uxie-2`.
- Key item: `mythsandlegends:uxie_claw`
- Biomes: `#cobblemon:is_river`, `wythers:lantern_river`, `#minecraft:is_river`, `wythers:tropical_forest_river`, `#cobblemon:is_freshwater`
- Other conditions: none

### Victini

**Route 1** — level `70-100`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-victini-1`.
- Key item: `mythsandlegends:liberty_pass`
- Biomes: `minecraft:badlands`, `minecraft:eroded_badlands`, `minecraft:savanna`
- Other conditions: none

**Route 2** — level `70-100`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-victini-2`.
- Key item: `mythsandlegends:liberty_pass`
- Biomes: `#cobblemon:is_volcanic`, `#cobblemon:is_thermal`, `#cobblemon:is_savanna`
- Other conditions: none

### Virizion

**Route 1** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-virizion-1`.
- Key item: `mythsandlegends:grassland_blade`
- Biomes: `#minecraft:is_hill`, `#cobblemon:is_highlands`, `#c:mountain_slope`, `#forge:is_slope`, `terralith:blooming_valley`, `terralith:forested_highlands`, `terralith:lavender_valley`, `terralith:lush_valley`, `terralith:moonlight_valley`, `terralith:sakura_valley`, `terralith:savanna_slopes`, `terralith:temperate_highlands`, `terralith:yosemite_lowlands`, `wythers:autumnal_crags`, `wythers:ayers_rock`, `wythers:icy_crags`, `wythers:old_growth_taiga_crags`, `wythers:taiga_crags`, `wythers:temperate_rainforest_crags`, `wythers:thermal_taiga_crags`, `wythers:windswept_jungle`
- Other conditions: none

**Route 2** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-virizion-2`.
- Key item: `mythsandlegends:grassland_blade`
- Biomes: `#minecraft:is_mountain`, `#cobblemon:is_hills`, `#forge:is_mountain`, `terralith:stony_spires`, `terralith:volcanic_peaks`, `terralith:windswept_spires`, `terralith:yosemite_cliffs`, `wythers:tibesti_mountains`
- Other conditions: none

### Volcanion

**Route 1** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-volcanion-1`.
- Key item: `mythsandlegends:steam_valve`
- Biomes: `#minecraft:is_nether`
- Other conditions: none

**Route 2** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-volcanion-2`.
- Key item: `mythsandlegends:steam_valve`
- Biomes: `#cobblemon:is_volcanic`, `terralith:cave/mantle_caves`, `terralith:volcanic_crater`, `terralith:volcanic_peaks`, `wythers:icy_volcano`, `wythers:tropical_volcano`, `wythers:volcano`, `wythers:volcanic_chamber`, `wythers:volcanic_crater`
- Other conditions: none

### Xerneas

**Route 1** — level `50`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-xerneas-1`.
- Key item: `mythsandlegends:sapling_of_life`
- Biomes: `minecraft:flower_forest`, `minecraft:forest`, `minecraft:meadow`
- Other conditions: none

**Route 2** — level `50`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-xerneas-2`.
- Key item: `mythsandlegends:sapling_of_life`
- Biomes: `#cobblemon:is_forest`, `#cobblemon:is_magical`, `#cobblemon:is_floral`
- Other conditions: none

### Yveltal

**Route 1** — level `50`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-yveltal-1`.
- Key item: `mythsandlegends:cocoon_of_destruction`
- Biomes: `minecraft:dark_forest`, `minecraft:swamp`
- Other conditions: none

**Route 2** — level `50`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-yveltal-2`.
- Key item: `mythsandlegends:cocoon_of_destruction`
- Biomes: `#cobblemon:is_spooky`, `#cobblemon:is_tundra`
- Other conditions: none

### Zacian

**Route 1** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-zacian-1`.
- Key item: `mythsandlegends:rusted_sword`
- Biomes: `minecraft:flower_forest`, `minecraft:meadow`
- Other conditions: none

**Route 2** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-zacian-2`.
- Key item: `mythsandlegends:rusted_sword`
- Biomes: `#cobblemon:is_magical`, `#cobblemon:is_forest`, `#cobblemon:is_highlands`
- Other conditions: none

### Zamazenta

**Route 1** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-zamazenta-1`.
- Key item: `mythsandlegends:rusted_shield`
- Biomes: `minecraft:taiga`, `minecraft:windswept_forest`
- Other conditions: none

**Route 2** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-zamazenta-2`.
- Key item: `mythsandlegends:rusted_shield`
- Biomes: `#cobblemon:is_plateau`, `#cobblemon:is_savanna`, `#cobblemon:is_highlands`
- Other conditions: none

### Zapdos

**Route 1** — level `50-60`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-zapdos-1`.
- Key item: `mythsandlegends:tidal_bell`
- Biomes: `minecraft:plains`, `minecraft:savanna`
- Other conditions: `{"item_requirement":[{"id":"cobblemon:thunder_stone","count":3,"consume":true}]}`

**Route 2** — level `50-60`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-zapdos-2`.
- Key item: `mythsandlegends:tidal_bell`
- Biomes: `#cobblemon:is_sky`, `#cobblemon:is_mountain`
- Other conditions: `{"item_requirement":[{"id":"cobblemon:thunder_stone","count":3,"consume":true}]}`

### Zarude

**Route 1** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-zarude-1`.
- Key item: `mythsandlegends:zarudes_cape`
- Biomes: `#minecraft:is_jungle`, `terralith:cave/underground_jungle`, `wythers:dripleaf_swamp`, `wythers:eucalyptus_deanei_forest`, `wythers:highland_tropical_rainforest`, `wythers:humid_tropical_grassland`, `wythers:jungle_canyon`, `wythers:subtropical_forest`, `wythers:subtropical_forest_edge`, `wythers:subtropical_grassland`, `wythers:tropical_forest`, `wythers:tropical_forest_canyon`, `wythers:tropical_grassland`, `wythers:tropical_island`, `wythers:tropical_rainforest`
- Other conditions: none

### Zekrom

**Route 1** — level `60-70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-zekrom-1`.
- Key item: `mythsandlegends:dark_stone`
- Biomes: `minecraft:swamp`, `minecraft:dark_forest`, `minecraft:roofed_forest`
- Other conditions: `{"minLight":0,"maxLight":7}`

### Zeraora

**Route 1** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-zeraora-1`.
- Key item: `mythsandlegends:zeraoras_thunderclaw`
- Biomes: `#minecraft:is_savanna`, `terralith:arid_highlands`, `terralith:ashen_savanna`, `terralith:brushland`, `terralith:desert_oasis`, `terralith:fractured_savanna`, `terralith:hot_shrubland`, `terralith:red_oasis`, `terralith:savanna_badlands`, `terralith:savanna_slopes`, `terralith:shrubland`, `wythers:granite_canyon`, `wythers:tropical_forest_canyon`, `wythers:tropical_forest`
- Other conditions: none

### Zygarde

**Route 1** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-zygarde-1`.
- Key item: `—`
- Biomes: `minecraft:birch_forest`, `minecraft:forest`, `minecraft:jungle`, `minecraft:mangrove_swamp`, `minecraft:swamp`
- Other conditions: `{"required_cells":95,"required_cores":5}`

**Route 2** — level `70`, bucket `ultra-rare`, weight `0.1`. Source entry `mythsandlegends-zygarde-2`.
- Key item: `—`
- Biomes: `#cobblemon:is_forest`, `#cobblemon:is_cave`, `#cobblemon:is_swamp`
- Other conditions: `{"required_cells":95,"required_cores":5}`

