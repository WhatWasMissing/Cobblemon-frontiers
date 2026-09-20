# Research notes

The announcement design was informed by existing Cobblemon add-ons and the current Cobblemon 1.8 API:

* Cobblemon Spawn Alerts demonstrates the established pattern of listening for Cobblemon entity spawn events, classifying shiny/legendary-style rarity, and using broad global alerts. Its published page also documents the trade-off of sending exact species information to clients; Frontiers intentionally does not do that.
* Pebble's Cobblemon Spawn Events demonstrates that server-side event state and a public event history can work as a separate layer from normal Cobblemon spawning.
* Cobblemon 1.8 adds Alpha Pokémon, so alpha is treated as a first-class announcement category rather than inferred from size or a species list.
* Cobblemon: Mega Showdown is supported as an optional integration. Its published project supports Minecraft 1.21.1 on Fabric and NeoForge and adds mechanics such as Mega Evolution, Z-Moves, Terastallization, Dynamax/Gigantamax, Ultra Burst, and fusions; Frontiers discovers its registered shop items without making it a required dependency.

References:

* https://modrinth.com/mod/cobblemon-spawn-alerts
* https://github.com/StainlessStasis/CobblemonSpawnAlerts
* https://modrinth.com/mod/pebbles-cobblemon-spawn-events
* https://wiki.cobblemon.com/
* https://cobblemon.com/
* https://modrinth.com/mod/cobblemon-mega-showdown
* https://github.com/yajatkaul/Mega_Showdown
