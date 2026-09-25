# Getting started

Myths and Legends makes special encounters possible through key items and spawn conditions. In the normal mod flow, an item does not summon a Pokémon when right-clicked. Carrying the required item makes the Pokémon eligible to appear naturally when Cobblemon checks spawn pools and the encounter's other conditions are met.

## Find the Pokémon's requirements

1. Look up the Pokémon in the [Encounter catalogue](encounters.md).
2. Obtain the listed key item and any additional required items.
3. Go to one of the listed biome IDs. Biome tags beginning with `#` refer to groups of biomes; a plain `minecraft:` ID names a specific biome.
4. Keep the key item in your inventory while exploring. The Pokémon must still pass the other listed conditions, such as time, weather, nearby blocks, or party requirements.
5. Give the spawn system time to check. The local 1.9.0 config uses 3600 ticks (3 minutes at 20 ticks per second); this interval is configurable.

## Important distinction

An eligible encounter is not a guaranteed immediate spawn. The encounter still uses its configured spawn bucket, weight, level range, context, and conditions. A biome or item mismatch makes it ineligible; meeting every condition only lets the spawn system attempt it.

If your server also has the separate **Myths and Legends Direct Spawn Fix** compatibility add-on, using a supported key item has a different effect: it directly spawns a linked Pokémon. Read [Direct Spawn Fix](direct-spawn-fix.md) before interpreting that behavior.

