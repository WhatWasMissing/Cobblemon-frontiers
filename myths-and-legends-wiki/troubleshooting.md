# Troubleshooting

## “I’ve met the requirements, so where is it?”

1. Confirm Myths and Legends and Cobblemon load without errors.
2. Confirm the companion datapack is installed in the active world and enabled with `/datapack list`.
3. Check the exact Pokémon entry in the [Encounter catalogue](encounters.md): correct key item, biome, and any other requirements.
4. Carry the key item in your player inventory. A Pokémon holding the item, a chest nearby, or an item in a different player's inventory may not satisfy the player inventory condition.
5. Wait for spawn attempts. Meeting conditions makes the entry eligible; it does not force an immediate spawn.
6. If it’s still quiet, check `latest.log` for malformed JSON, unknown item or Pokémon IDs, and datapack load errors.

## The game crashes when I use an item

Some Myths and Legends releases clash with particular Cobblemon versions when a key item is used. The first exception in the crash log is the best clue; compare the Minecraft, loader, Cobblemon, and mod versions together. The **Myths and Legends Direct Spawn Fix** described here is a separate server-side add-on for the specific Cobblemon 1.8.1+ / NeoForge 1.21.1 setup on its page. Check your versions before using it.

## Encounter seems eligible but does not spawn

Check the spawn bucket, weight, spawn context, and level range, and remember the encounter competes with all other entries. If using a custom datapack, validate the active copy of the JSON: another datapack may override an entry with the same resource ID.

## Item not found in loot

The mod page says key items appear in selected loot chests by default and their rarity/loot tables can be configured. Loot tables can be changed by modpacks or datapacks. Check the installed version's config and other datapacks rather than assuming every world structure receives every item.

## “We can only catch one Legendary”

Myths and Legends provides encounters; it does not by itself set a universal one-catch limit. If a player can only catch a legendary once, inspect other installed mods and server rules. In this workspace's earlier diagnosis, `Limited Legends` had `SetGlobalLimits: true` and `GlobalLimitPerSpecies: 1`; disabling that global setting or raising the number addresses that separate mod's cap.

