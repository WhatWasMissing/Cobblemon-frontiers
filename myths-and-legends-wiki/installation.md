# Installation

## Client and server

Myths and Legends is a client-and-server mod. Install the same loader-compatible Myths and Legends release on the clients and server, along with the matching Cobblemon and Minecraft versions listed on the download page.

## Companion datapack

For mod versions newer than 1.3, the official pre-made encounters are supplied by a separate datapack. Install the datapack in the world's `datapacks` directory (for a dedicated server, the active world folder's `datapacks` directory), then run `/reload` or restart the server. Verify it is enabled with `/datapack list`.

The CurseForge project links the [official datapack download](https://www.curseforge.com/minecraft/data-packs/mythsandlegends-datapack) and its [source repository](https://github.com/D0ctorLeon/mythsandlegends-datapack). Follow the instructions for the specific datapack release; do not install a datapack archive inside `mods`.

## Compatibility checklist

- Minecraft, loader, Cobblemon, mod, and datapack versions are mutually compatible.
- Every player and the server have the required mod version.
- The official or custom datapack is in the active world's `datapacks` folder and enabled.
- The encounter JSON files are loaded from `data/cobblemon/spawn_pool_world/`.
- If this workspace's direct-spawn patch is used, install its JAR on the server only and retain Myths and Legends plus the datapack.


