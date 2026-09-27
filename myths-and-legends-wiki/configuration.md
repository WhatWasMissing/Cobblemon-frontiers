# Configuration

The mod configuration is server-side. In the local NeoForge 1.9.0 installation, the main file is `config/mythsandlegends/config.toml`; companion files include `loot_tables_config.json`, `transformations.json`, and `debts.json`. Older releases use different names and settings. Stop the server before editing and preserve TOML/JSON syntax.

## Main settings in the local 1.9.0 NeoForge config

| Setting | What it controls |
| --- | --- |
| `inventory_check_interval` | Inventory scan period in ticks. The local generated config uses `3600`, which is 3 minutes at 20 ticks/second. |
| `item_consumption_mode` | Item consumption policy: `0` none; `1` legacy consumption; `2` consume force-spawn trigger and requirements only after successful forced spawns; `3` always consume requirements for regular/forced spawns; `4` consume only custom entries explicitly marked `consume: true` on regular/forced spawns, with key/Zygarde items consumed only as force-spawn triggers. |
| `inventory_check_shulker_boxes`, `inventory_check_bundles` | Whether inventory scans look inside those containers. |
| `isBroadcastEnabled`, `broadcast_settings` | Master switch and per-category controls for spawn announcements, including names, legendary/mythical types, location, shiny status, and level. |
| `enable_force_spawning` | Enables the original mod's key-item force-spawn feature. The local config is `true`; another release may default differently. |
| `force_spawning_spawn_pool` | Spawn-pool rarity used when evaluating force spawns; local value is `ultra-rare`. This selects candidate entries; it does not set the entry's own rarity. |
| `enable_vouchers`, `force_spawn_item_cooldown`, `global_item_cooldown`, `force_spawning_vouchers`, `global_item_vouchers` | Optional force-spawn use limits and cooldowns. With vouchers disabled, these limits are inactive. |
| `force_spawn_check_width`, `force_spawn_check_height` | Search area for valid force-spawn positions. `-1` delegates dimensions to Cobblemon; very large values can add lag. |
| `ULTRA_RARE_ITEMS`, `RARE_ITEMS`, `COMMON_ITEMS` | Item classifications used by the mod. They do not by themselves define Pokémon spawn entries. |
| `common_chest_chances`, `rare_chest_chances` | Loot table and rarity/chance tuning. The standalone `loot_tables_config.json` contains loot groups, entries, weights, and count ranges. |
| `zygarde_cell_min_count`, `zygarde_cell_max_count`, `zygarde_core_min_count`, `zygarde_core_max_count` | Generated/dropped Zygarde component counts. |
| `form_changes` | Enables item-triggered form changes, such as DNA Splicers or orbs. |
| `debug_mode` | Verbose diagnostic logging; keep disabled for normal play. |

For regular encounter entries, Pokémon, biome, level, bucket, weight, and most spawn requirements are defined in datapack JSON. Use `loot_tables_config.json` to inspect or tune chest loot groups. Configuration keys and semantics can change by version; trust the generated config that came with the server rather than copying this table blindly.

For the official config and condition documentation, see the [CurseForge project page](https://www.curseforge.com/minecraft/mc-mods/myths-and-legends-cobblemon-addon).

