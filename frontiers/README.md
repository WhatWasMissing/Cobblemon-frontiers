# Cobblemon Frontiers

Cobblemon Frontiers is a server-side Cobblemon 1.8.0+ add-on for Fabric and NeoForge on Minecraft 1.21.1.

It has two connected systems:

* **Field Intelligence** announces meaningful trait spawns without revealing species or coordinates. Alerts may name the nearest player within a configurable radius (96 blocks by default), and servers can disable names. Shiny, legendary, mythical, Ultra Beast, Paradox, alpha, and ultra-rare spawns can create broad-region notices; rare spawn buckets stay silent and instead pay capture bounties.
* **Frontier Signals** records a short anonymous history of these sightings. Players can read the public board with `/spawnannounce signals`.
* **Field Research** awards persistent research points for captures, with bonus points for shiny, legendary, mythical, Ultra Beast, alpha, and other exceptional Pokémon. Milestones provide small vanilla rewards.
* **Daily field challenge** rewards +10 spendable research points for catching five Pokémon per UTC day. Check progress with `/spawnannounce challenges` or in the dashboard.
* **Rotating expedition contracts** offer three server-wide daily objectives such as frozen-region surveys, night watches, signal responses, and exceptional captures. Progress and rewards are personal and reset on the UTC day boundary.
* **Community research** runs a shared weekly census for one broad region. Contributors are recorded anonymously by UUID and receive a modest completion reward when the server reaches the target.
* **Signal aftermath** keeps a secured signal's broad region active for five minutes, giving follow-up captures there a small bonus without creating a new coordinate trail.
* **Notification preferences** let each player mute public alerts or move personal capture feedback between chat, actionbar, and silent mode.
* **Private Field Guide** records each species a player has captured and shows the collection journal only to that player.
* **Frontier Intelligence dashboard** opens with the default `K` key (rebindable in Controls). It combines your balance, lifetime progress, survey streak, recent anonymous signals, and a paged exchange GUI.
* **Cobblemon exchange** includes Poké Balls, specialty balls, healing items, Rare Candy, EXP Candy, Ability Capsule/Patch, evolution stones, and Metal Alloy alongside a broad vanilla supply catalogue. The dashboard has in-GUI categories, direct search, sorting, and optional Mega Showdown discovery; its item stacks keep their base names/components for normal stacking.
* **Regional Surveys** track broad-region capture streaks and keep a spendable regional breakdown of the same research wallet. Regional allocations are consumed automatically by the exchange, so regional research is useful without creating duplicate currency.

Gameplay is server-authoritative. A matching client mod is needed for the keybind and custom dashboard screen; without it, research and exchange purchases remain available through commands.

## Commands

* `/spawnannounce signals` — recent anonymous signals and their broad regions.
* `/spawnannounce research` — your field-research points and next milestone.
* `/spawnannounce research regions` — your capture counts by broad region.
* `/spawnannounce research top` — the top five research scores.
* `/spawnannounce challenges` — daily capture-goal progress and reward.
* `/spawnannounce expeditions` — today’s three rotating contracts and progress.
* `/spawnannounce community` — this week’s shared census and contributor count.
* `/spawnannounce guide` — view your private species collection journal.
* `/spawnannounce notifications [on|off|chat|actionbar|silent]` — manage alert and capture-feedback preferences.
* `/spawnannounce exchange list [page]` — browse the exchange from chat.
* `/spawnannounce exchange search <words>` — filter the catalogue and refresh the open dashboard if present; the dashboard itself also has direct search.
* `/spawnannounce exchange sort <featured|name|cost>` — sort the catalogue; also updates an open dashboard.
* `/spawnannounce exchange clear` — clear the current dashboard search.
* `/spawnannounce exchange buy <product_id>` — buy a catalogue item without opening the custom screen.
* `/spawnannounce reload` — reload `config/cobblemon_frontiers/config.json` (operator only).
* `/spawnannounce status` — show the current alert settings (operator only).
* `/spawnannounce test <kind>` — preview a message without creating a signal (operator only).

## Build

Use Java 21 and run one of:

```text
./gradlew :neoforge:build
```

The NeoForge jar is written to `neoforge/build/libs/`.

For a dependency-free privacy/configuration check, run `sh tools/verify_frontiers.sh`.

## Design notes

See `docs/DESIGN.md`, `docs/RESEARCH.md`, and `docs/default-config.json`.
