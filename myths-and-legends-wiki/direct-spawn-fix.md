# Direct Spawn Fix compatibility add-on

This page documents the separate `mythsandlegends-direct-spawn-neoforge-1.0.0.jar` compatibility mod in this workspace. It is not an official feature of Myths and Legends.

## What it changes

On the supported NeoForge/Cobblemon setup, right-clicking a Myths and Legends key item directly spawns a Pokémon associated with that key item in the active datapack's `spawn_pool_world` entries. When the item maps to multiple Pokémon, one target is selected at random. The configured level range is used, and one key item is consumed only after the Pokémon is successfully added to the world. Shift-right-click passes through to the original behavior.

The patch uses the datapack to identify item-to-Pokémon mappings. It does not enforce the natural encounter's biome, weather, time, held/party requirements, spawn weight, or bucket when summoning. Thus direct use can bypass the intended hunting challenge. Key items without a linked spawn entry are blocked with a message.

## Installation and limits

- Install the patch JAR on the server only; clients do not need it.
- Keep Myths and Legends and the matching datapack installed.
- The local README targets Minecraft 1.21.1, NeoForge 21.1, and Cobblemon 1.8.1 or later.
- The patch handles item-use crashes caused by the original mod's incompatible spawn invocation in this target setup. It is not a general-purpose support promise for other versions/loaders.
- If the item appears to do nothing, confirm it is a `mythsandlegends:` KeyItem, the active datapack has a matching spawn entry, and the direct-spawn JAR is loaded on the server.


