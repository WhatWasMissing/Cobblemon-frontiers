# Gacha design

## The intended feeling

This is a lightweight collection loop, not a second progression game. Captures remain the reason to explore; the gacha is a paced celebration of that play rather than a command-driven casino bolted onto the server.

The UI has three jobs:

- make the current banner and its safety net obvious;
- make the two actions—draw one and draw ten—unambiguous;
- make results feel collectible without filling public chat.

## Banner visual language

The banner dashboard uses one shared Cobblemon-inspired frame, but each rotation
gets its own regional identity. The banner id selects a dark regional surface,
accent/highlight pair, and a vanilla item emblem: leaves for Verdant Trails,
heart-of-the-sea for Tidal Current, magma for Ember & Ash, and so on. This keeps
the screen readable at a glance while making the hourly rotation feel like a
collection of destinations rather than a list of interchangeable menus.

The hero panel combines the banner title, rotation state, and a five-entry
featured drop rail. Each featured entry uses Cobblemon's native profile renderer,
which resolves the correct profile sprite or model instead of drawing a raw
model texture atlas. Namespaced addon species and texture-pack overrides are
supported; an unknown or unavailable species falls back to its rarity emblem
without breaking the layout. Featured entries are chosen client-side from the
server-provided pool by rarity and weight; the roll engine remains
server-authoritative.
The odds/safety-net panel and private history remain in fixed positions so the
screen does not move important controls as the banner changes. Previous/next
controls are drawn as authored chevrons and remain available by mouse and arrow
keys, with disabled states when the player reaches either end of the rotation.

Exceptional results get a short, bounded reveal moment while the dashboard is
open. Shiny results use the shiny sprite when the resource pack provides it;
Legendary and Mythic results use the matching sprite and tier heading. The
motif is selected by banner identity—waves, circuitry, embers, frost sparks,
orbit lines, monument pillars, and similar shapes—so the rarest moment feels
specific to the active banner without adding sounds or a server-side animation
loop. The reveal lasts 2.4 seconds and temporarily takes over the hero action
area; close remains available, while draw and page actions safely pause until
the moment ends. It is entirely client-side, so it does not add spawn polling
or TPS work.

The featured rail keeps an inset navigation gutter so its previous/next controls
stay distinct from JEI's neighboring controls at the screen edge. The controls
also retain keyboard arrow navigation, with disabled states when the player
reaches either end of the rotation.

The direction was cross-checked against established Minecraft UI patterns:

- FTB Quests' customizable, task-first interface informed the persistent
  navigation and the decision to keep the active task (the current banner)
  visually dominant.
- JEI's stability and ease-of-use focus informed the fixed geometry, familiar
  item icons, and low-animation presentation.
- Create's Ponder documentation informed the idea of a focused, themed panel
  that explains the current mechanic without turning the screen into a dense
  inventory grid.
- Applied Energistics 2 informed the compact item-driven information rail and
  keeping high-value details visible at the same time as the primary action.
- Upgrader Items remains the functional reference for the separate wager flow;
  its source/target presentation is intentionally not copied into the banner
  dashboard.

## Economy guardrails

- Tickets are earned from captures and are not checked every tick.
- A ten-pull costs exactly ten tickets by default.
- Pity resets on Rare or better.
- The default pool is intentionally broad, with the strongest rewards gated by low weights and pity rather than a hidden command.
- Operators can tune the entire system in JSON without recompiling.

## Item-to-Pokémon contracts

The Upgrader-style menu also exposes Pokémon as wager targets. Their target
values are intentionally tiered: Common 500, Rare 1500, Epic 4500, and Mythic
12500. This makes a fixed source item progressively less likely to succeed as
the target gets rarer. Legendary targets are exempt from that ratio and use a
separate 0.1% default contract chance, gated behind a 5000-value source item.
Both legendary settings are server-configurable.

Pokémon rewards are single-result contracts. The x2/x4/x8 controls only apply
to item targets, preventing a successful Pokémon gamble from multiplying a
party or PC insertion. Delivery is isolated in `PokemonRewardAdapter`; a
version mismatch produces a plain, stackable paper voucher rather than deleting a successful
reward.

## Legendary Monuments separation

The protected acquisition roster is based on the current [Legendary Monuments
Modrinth page](https://modrinth.com/mod/legendary-monuments) and its linked
[wiki](https://legendarymonuments.com/). The Modrinth page lists 53 direct
structures/mechanics; the wiki also documents Cosmoem, Lunala, Solgaleo, Meltan,
and Melmetal through its evolution and item mechanics. The source therefore
ships a 58-species protected roster. It is merged into older generated configs
on load, and every Pokémon banner tier plus every Pokémon wager target is
filtered against it. The **Beyond the Monuments** pool is limited to species
outside that roster.

## Extension points

The banner format can be extended with:

- seasonal pools;
- form or skin metadata in the species id/display name;
- optional-mod species;
- item rewards through a future `RewardAdapter` implementation;
- server-wide banner rotations.

Keep reward delivery isolated from the roll engine so an optional add-on can be absent without breaking the core gacha.
