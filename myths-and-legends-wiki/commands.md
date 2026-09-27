# Commands

Commands vary by Myths and Legends release and server permissions. In the local NeoForge 1.9.0 build, the root command is `/mythsandlegends`, with `/mal` as an alias. The registered command tree includes:

| Command | Use |
| --- | --- |
| `/mythsandlegends items [player]` | Inspect tracked key-item state. |
| `/mythsandlegends syncitems [player]` | Resynchronize tracked item data. |
| `/mythsandlegends party [player]` | Inspect the selected player's Pokémon party. |
| `/mythsandlegends haspokemon <species> [player]` | Check whether the selected player has a Pokémon species. |
| `/mythsandlegends placeifhas ...` | Conditional block-placement utility; command suggestions show the required arguments. |
| `/mythsandlegends settings` | Display or manage settings exposed by this build. |
| `/mythsandlegends cooldown ...` | Inspect or clear force-spawn cooldown/voucher status. |
| `/mythsandlegends forcespawn ...` | Run the mod's force-spawn command, subject to permissions and configuration. |

The upstream CurseForge page also documents `/mythsandlegends checkinventory` and `/mythsandlegends listpokemon [player_name]` for its documented release. Those names do not appear in the local 1.9.0 command class, so use command suggestions on the server to see the exact syntax for the installed build. Some subcommands are operator-only.

The exact argument requirements and permission level can vary. Use Minecraft command suggestions (`/mal` then press Tab) to see the syntax registered by the installed version. The separate `/checkspawn ultra-rare` command mentioned on the project page belongs to Cobblemon's spawn-check tooling; it does not remove encounter conditions or guarantee a specific Pokémon.

## Reloading data

After changing datapack files, run `/reload` or restart the server. This reloads data packs; it does not edit the Myths and Legends config. To verify the pack is active, run `/datapack list`.

