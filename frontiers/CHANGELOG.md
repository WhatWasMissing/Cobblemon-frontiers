# Changelog

## 1.0.0

* Added server-side Field Intelligence announcements for shiny, legendary, mythical, Ultra Beast, Paradox, and alpha spawns.
* Rare spawn buckets are silent and award a +6 private capture bounty; ultra-rare buckets are publicly announced anonymously.
* Added broad-region messages with no species, coordinates, or waypoints; nearby player names remain optional and configurable.
* Added global and per-kind cooldowns plus dimension filtering.
* Added persistent Frontier Signals history and signal-linked capture bonuses.
* Added persistent Field Research points, regional capture summaries, leaderboard, and milestone rewards.
* Added separate lifetime-earned and spendable research balances.
* Added the keybound Frontier Intelligence dashboard with balance, milestones, streaks, anonymous signals, and a paged Cobblemon-inspired custom screen.
* Added a large vanilla and Cobblemon exchange catalogue covering specialty Poké Balls, healing items, Rare Candy, EXP Candy, ability items, evolution stones, and Metal Alloy.
* Added optional registry discovery for Mega Showdown items without making Mega Showdown a required dependency.
* Fixed dashboard overlap in the exchange header, added responsive GUI scaling, and moved page navigation onto reliable menu button packets so previous-page navigation works.
* Added server-owned exchange categories and a direct GUI search field with clear/Enter controls; category, search, and sort changes no longer open chat or send commands.
* Added varied context-aware announcement templates with dimension/time flavour and preserved custom ` || ` variants.
* Removed custom names from shop display/reward stacks so Cobblemon and optional-mod items retain normal registry names and stacking behaviour.
* Hardened keybind opening by avoiding duplicate requests, closing stale containers before opening, and reporting a server-side open failure.
* Added Fabric and NeoForge Minecraft 1.21.1 targets for Cobblemon 1.8.0+.
* Fixed signal-linked research so a capture only claims the signal created for that Pokémon UUID.
* Added atomic batched ledger persistence, server-stop flushing, and `.corrupt-*` recovery backups.
* Added depth-aware region detection, configurable biome aliases, and optional rarity/region compatibility hooks.
* Added rotating daily expedition contracts, weekly community censuses, and five-minute signal aftermath rewards.
* Added per-player public-alert and capture-feedback preferences with `/spawnannounce notifications`.
* Added a private Field Guide collection journal with command and dashboard access.
