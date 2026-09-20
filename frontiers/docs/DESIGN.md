# Cobblemon Frontiers design

## Player experience

Most wild Pokémon remain completely silent. Only a shiny, legendary, mythical, Ultra Beast, Paradox, alpha, or ultra-rare trait can create a notice. The Cobblemon rare spawn bucket is intentionally silent and awards a private capture bounty instead. The highest-priority matching trait wins, so a shiny legendary receives one coherent message rather than several stacked alerts.

The default notice gives three useful pieces of information:

1. the significance of the sighting;
2. the broad region, such as the savanna, wetlands, frozen highlands, or underground;
3. no information that turns the hunt into a coordinate-following exercise. By default, the closest player within 96 blocks may be named; no position is sent or persisted, and servers can disable player names.

Two cooldowns prevent a busy server from becoming a notification feed: one global cooldown and one cooldown per announcement kind.

## Frontier Signals

Every notice creates an anonymous signal record lasting fifteen minutes. The record contains only its category, broad region, dimension identifier, timestamp, lifecycle status, and the spawned entity UUID needed for exact server-side matching. It does not contain a species or position. A successful capture resolves only the signal belonging to that captured UUID and tells the server that the signal has gone quiet. That resolution creates a five-minute, broad-region aftermath window for a small follow-up bonus.

The last twenty-four records are retained in `frontier_ledger.json`; older resolved records are removed during normal ledger maintenance.

## Field Research

Captures award one base point. Trait bonuses are additive:

| Trait | Bonus |
|---|---:|
| Shiny | +9 |
| Legendary | +14 |
| Mythical | +14 |
| Ultra Beast | +12 |
| Alpha | +5 |

Capturing the Pokémon tied to an active signal grants a further +3 signal bonus. The bonus is matched by UUID rather than by “any active signal in this region,” preventing unrelated captures from claiming another player’s sighting. Follow-up captures in the secured signal’s broad region and dimension receive +1 during the five-minute aftermath window.

Rare spawn buckets use a separate private ledger entry keyed only by the spawned Pokémon UUID and an expiry timestamp. Capturing one grants +6 research points. Ultra-rare spawns follow the normal anonymous announcement path and do not create a private rare-bounty record. The rare entries are not shown in Frontier Signals and contain no species, region, or coordinates.

The ledger also keeps capture counts and a spendable point allocation by broad region, allowing `/spawnannounce research regions` to become a lightweight exploration log without building a minimap or storing a route. Regional allocations are a breakdown of the same wallet, not extra currency; the server consumes them automatically when a player buys from the exchange.

Captures in the same broad region build a regional survey streak. Every fifth consecutive capture in that region awards +2 research points, while moving between regions naturally starts a new survey. The best streak is retained in the ledger for future profile screens.

The daily field challenge asks each player to catch five Pokémon in a UTC day. Completing it awards +10 points into the same spendable wallet, and progress is persisted with the player's research profile. `/spawnannounce challenges` reports progress without opening the custom screen.

Three expedition contracts rotate deterministically each UTC day from a larger pool. Contract state is kept per player, so one player completing a contract does not remove it for anyone else. A weekly community census selects one broad region, tracks total captures and contributing players, and pays each contributor once when the shared goal is reached.

Each capture also adds the species identifier to that player’s private Field Guide set. The collection is never placed in a public signal, announcement, leaderboard, or community-event payload; `/spawnannounce guide` and the dashboard expose it only to the owning player.

Milestones are 25, 100, 250, and 500 points. Rewards are intentionally modest and use vanilla items so the feature does not compete with Cobblemon's own item economy.

Research points have two values: lifetime earned points, which drive milestones and the leaderboard, and a spendable balance. The default `K` key opens the Frontier Intelligence dashboard; it is a server-owned custom client screen with Cobblemon-inspired dark panels, tabs, signal cards, and item cards. The dashboard shows the player's research balance, lifetime total, next milestone, survey streak, recent anonymous signals, and a paged exchange.

The exchange has compact in-GUI categories for Supplies, Capture, Healing, Evolution, Rare, and optional Mega Showdown items, plus an All view. The search field is a real dashboard input: press Enter to submit a name or registry-ID search, use the clear button to reset it, and cycle the sort control without opening chat or issuing a command. Products are resolved by Cobblemon registry ID, so an unavailable optional entry is omitted instead of replacing it with the wrong item. If Mega Showdown is installed, its registered items are discovered automatically and added without a hard dependency. Search/category/sort requests are bounded, tied to the current menu id, and server-validated before slots are rebuilt. Chat commands also list, filter, sort, and buy products when the custom client screen is unavailable. The screen scales its virtual layout to the available Minecraft GUI viewport, and paging uses menu button packets rather than synthetic inventory clicks. Shop display and reward stacks retain their base registry item names/components so they stack normally with the same item outside the shop; price and category metadata are rendered by the dashboard.

Default announcement strings contain three variants separated by ` || ` and may use `{dimension}` and `{time}` for contextual flavour. Servers can replace any entry with one custom string or provide their own ` || ` variants. The announcement contract remains privacy-safe: no species or precise position placeholders are accepted by the public templates.

## Safety and compatibility

The implementation subscribes to Cobblemon's `POKEMON_ENTITY_SPAWN` and `POKEMON_CAPTURED` events. It does not replace Cobblemon's spawner, alter spawn weights, inject structures, add client mixins, or expose coordinate data over the network. The nearest player's username can appear in an alert, but coordinates and player positions are never sent or stored. Command-spawned Pokémon are not announced because the add-on does not hook or reinterpret Cobblemon's spawn command. The ledger batches mutations, writes through a temporary file with an atomic move where supported, flushes on server stop, and preserves unreadable files as timestamped `.corrupt-*` backups.

Biome classification first honors server `regionOverrides` and the public `FrontierCompatibilityHooks` API, then checks underground sky access/depth before broad biome-name matching. Optional integrations can register a biome alias or rarity provider without making Frontiers hard-depend on another mod. Private Field Guide species data is kept separate from the anonymous signal records.

The same common implementation is packaged for Fabric and NeoForge, keeping behaviour consistent between server platforms.
