# Cobblemon Gacha

`cobblemon_gacha` is a separate Cobblemon mod. It does not share source code, data, ledgers, menus, or identifiers with Cobblemon Frontiers.

The main interface is now based on the [Upgrader items](https://www.curseforge.com/minecraft/mc-mods/upgrader-items) flow: source item on the left, target catalogue on the right, value-based chance in the centre, and a high-risk upgrade button.

## Player flow

1. Capture Cobblemon normally. Every configurable number of successful captures awards a Gacha Ticket for the secondary summon system; new players also receive the configured starting tickets.
2. Press **G** (rebindable) for the dashboard, or use the player commands below when no custom client screen is available.
3. Use **Gacha Draws** in the upgrader header to access the ticket/banner draw dashboard; use **Item Upgrader** there to return.
4. Click an item in your inventory to place it in the source slot.
5. Switch between the **Items** and **Pokémon** tabs, choose a target from that catalogue, and inspect the calculated chance. Item targets can use a multiplier; Pokémon contracts always award one Pokémon.
6. Click **Upgrade**. The server consumes one source item; success grants the target stack, while failure destroys the source item. Pokémon wagers also use a per-player server cooldown to prevent rapid repeated attempts.

The catalogue also contains Pokémon contracts. Common, Rare, Epic, and Mythic
contracts use progressively higher target values, so rarer Pokémon have lower
success odds for the same wager. A successful contract delivers the Pokémon to
the player's party or PC; if the installed Cobblemon API cannot accept it, the
player receives an identity-preserving paper voucher containing the species,
rarity, and shiny state. Hold that voucher in the main hand and use
`/cobblemon_gacha redeem_voucher` after making room in the party or PC. Pokémon contracts always award one
Pokémon, so item multipliers cannot be used to multiply Pokémon rewards.

Legendary contracts are intentionally separate from the normal value curve.
They require a source worth at least `legendaryPokemonMinimumSourceValue` and
then roll the extremely rare `legendaryPokemonChance` (0.1% by default).

## Included systems

- Upgrader-style source/target gamble based on item value.
- Audited value-share formula: `0.9 × (source value ÷ (source value + target value))`, clamped between 0.1% and 90%; the chance is calculated from both item values before the server rolls.
- Common materials are deliberately low-value: sticks are `0.1`, stone-like blocks are `0.2`, wood is `0.5`, and unknown/basic items default to `1.0`; valuable crafting chains preserve their relative ratios.
- Optional Mega Showdown targets are treated as high-tier progression items: standard targets are valued at `900`, transformation catalysts at `600–1,200`, and master-tier items at `2,500`, preventing generic-item fallback from producing inflated odds.
- Damaged tools and weapons lose value in proportion to remaining durability.
- Separate **Items** and **Pokémon** target tabs, each with its own pagination.
- Cobblemon items appear in the item catalogue, with optional Mega Showdown entries detected from the item registry.
- Pokémon wager contracts span Common through Legendary; Legendary contracts use a separate extremely rare chance.
- Pokémon listed in `legendaryMonumentSpecies` are excluded from all banner tiers and Pokémon wager contracts, preventing the gacha mod from duplicating the monument acquisition route even if a custom config changes their displayed rarity.
- A weighted ticket-draw dashboard with banners, pity, and history.
- Ten region-based Pokémon banners: Kanto, Johto, Hoenn, Sinnoh, Unova, Kalos, Alola, Galar, Hisui, and Paldea.
- A dedicated **Beyond the Monuments** pool containing Legendary and Mythical Pokémon not reserved for the Legendary Monuments route.
- Exactly one region is active at a time; the active region rotates deterministically every real-world hour by default. Other regions can be previewed but cannot be drawn until their rotation window.
- Weighted Common, Rare, Epic, Legendary, and Mythic entries.
- Ten-pull guarantee: the tenth result is at least Rare if the first nine were not.
- Independent Rare+ pity and Legendary pity counters, both configurable and carried through banner rotations.
- Independent shiny chance for every result.
- Configurable server-wide announcements for Legendary/Mythic pulls and shiny results. Ordinary pulls remain private and announcements never reveal coordinates.
- Tickets earned from captures, not from a per-tick scan.
- Server-authoritative menu actions and persistence.
- The server synchronises the banner catalogue, configured odds/costs, filtered wager targets, tickets, and pity to the client dashboard; clients never use their local config to resolve a reward.
- Both ticket draws and item upgrades are reachable from the keybind; each dashboard has a clear route to the other.
- The custom Cobblemon-inspired card UI is optional. All draws and wagers can also be resolved through server commands, so a client-side screen is not required for gameplay.
- Optional species ids can be namespaced in `config/cobblemon_gacha/config.json`.
- Optional integrations can call `GachaCompatibilityHooks.registerOptionalNamespace("mod_id")` to opt their registry items into the guarded target catalogue and high-tier value curve without a hard dependency.

## Configuration

On first server start, the mod creates:

```text
config/cobblemon_gacha/config.json
config/cobblemon_gacha/gacha_ledger.json
```

`config.json` contains ticket pacing, draw costs, pity thresholds, shiny chance,
banner rotation settings, announcement settings, Pokémon contract settings, and
complete banner pools. The important settings are:

- `startingTickets`: default `3` for a new player.
- `capturesPerTicket`: default `10`; captures are currently the normal ticket source.
- `bannerRotationHours`: default `1`; the active region changes every hour.
- `announceExceptionalDrops`: default `true`; enables server-wide exceptional-drop messages.
- `serverAnnouncementMinimumRarity`: default `LEGENDARY`; lower this only if the server wants more public messages.
- `announceShinyDrops`: default `true`; shiny results are announced even when their normal tier is lower.
- `legendaryMonumentSpecies`: the built-in Legendary Monuments roster plus any server-specific additions. Matching entries at every rarity are removed during config normalisation, including manually added entries and hyphen/underscore variants. The built-in roster is merged into older configs automatically, so it cannot be accidentally shortened by a stale generated file.
- `legendaryPokemonChance`: default `0.001` (0.1%) for a valid legendary wager; Legendary contracts remain separate from the ordinary value curve.
- `legendaryPokemonMinimumSourceValue`: default `5000`, normally requiring a Nether Star or comparable item.
- `gamblingCooldownSeconds`: default `3`; applies to every ticket draw and item/Pokémon wager. Set to `0` to disable the shared anti-spam cooldown.
- `pokemonWagerCooldownSeconds`: default `30`; Pokémon contracts use the longer of this value and `gamblingCooldownSeconds`.

## Player commands

- `/cobblemon_gacha balance` — tickets, capture progress, draws, and pity.
- `/cobblemon_gacha banners` — regional banner list and current rotation.
- `/cobblemon_gacha draw [1|10]` — draw from the active regional banner.
- `/cobblemon_gacha targets [all|items|pokemon]` — list valid target IDs.
- `/cobblemon_gacha odds <target_id> [1|2|4|8]` — preview held-item value, target value, and odds.
- `/cobblemon_gacha wager <target_id> [1|2|4|8]` — wager one item held in the main hand; a failed roll consumes it.
- `/cobblemon_gacha redeem_voucher` — redeem an identity-preserving Pokémon voucher held in the main hand.
- `/cobblemon_gacha test_pulls [amount]` — operator-only test tickets (900 by default).

Edit the generated pool to add species from other Cobblemon add-ons. A species
that is not installed will never be delivered into a party; the adapter falls
back to a voucher so the result is still visible. Existing installations with
an older generated config will need to merge the new fields/pools or remove
that config once so the expanded defaults can be generated. The gacha project
does not hard-depend on Legendary Monuments. If that mod expands its official roster
in the future, add the new species to this config before enabling them in a custom pool.

Featured sprites are resolved from the active Cobblemon and addon resource packs
at runtime. The mod does not copy Pokémon art into its own jar, so optional
species and server resource-pack overrides remain compatible. If a matching
texture is unavailable, the UI uses the rarity emblem and continues normally.

## Build handoff

The source is pinned to Minecraft `1.21.1`, Cobblemon `1.8.1+1.21.1`, and NeoForge `21.1.247` to match the current installation.

The project is intentionally returned as source only. Run the normal platform task from the project root when ready:

```text
./gradlew :neoforge:build
```

The NeoForge artifact will be named like `cobblemon_gacha-neoforge-1.0.0.jar`.
