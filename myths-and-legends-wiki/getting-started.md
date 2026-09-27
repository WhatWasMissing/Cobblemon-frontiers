# Getting started

If you’ve picked up a mysterious key item and expected a Legendary to appear right away, here’s the catch: in the normal setup, the item doesn’t summon anything when you use it. Keep it with you and it can make the linked Pokémon eligible to appear naturally, as long as the place and the other encounter conditions are right.

## Get your setup ready

1. Look up the Pokémon in the [Encounter catalogue](encounters.md).
2. Obtain the listed key item and any additional required items.
3. Go to one of the listed biome IDs. Biome tags beginning with `#` refer to groups of biomes; a plain `minecraft:` ID names a specific biome.
4. Keep the key item in your inventory while exploring. The Pokémon must still pass the other listed conditions, such as time, weather, nearby blocks, or party requirements.
5. Keep exploring for a while. The spawn system checks periodically; the local 1.9.0 config sets that interval to 3600 ticks (3 minutes at 20 ticks per second), and server owners can change it.

## Important distinction

Meeting the requirements puts the Pokémon in the running; it doesn’t guarantee an immediate appearance. Cobblemon still considers the route’s spawn bucket, weight, level range, context, and conditions. If the biome or item is wrong, that route can’t spawn at all. If everything lines up, it can be picked during a spawn attempt.

If your server also has the separate **Myths and Legends Direct Spawn Fix** compatibility add-on, using a supported key item has a different effect: it directly spawns a linked Pokémon. Read [Direct Spawn Fix](direct-spawn-fix.md) before interpreting that behavior.

