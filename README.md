# Cobblemon Frontiers

Current compiled release jars are in [`built-jars/`](built-jars/):

- `cobblemon_spawn_announcements-fabric-1.0.0.jar` — Frontiers for Fabric
- `cobblemon_spawn_announcements-neoforge-1.0.0.jar` — Frontiers for NeoForge
- `cobblemon_gacha-fabric-1.0.0.jar` — Gacha for Fabric
- `cobblemon_gacha-neoforge-1.0.0.jar` — Gacha for NeoForge

Use only the jar matching the loader used by the server. The Gacha jars
contain server-confirmed spin feedback and identity-preserving Pokémon voucher
fallbacks; the Frontiers jars contain rotating research, community events, and
the spendable regional research breakdown.

## Canonical source layout

The repository now tracks the buildable projects directly instead of keeping
their Java source only inside release archives:

- [`frontiers/`](frontiers/) — Cobblemon Frontiers for Fabric and NeoForge.
- [`gacha/`](gacha/) — Cobblemon Gacha for Fabric and NeoForge.
- [`releases/`](releases/) — published jars and source archives for historical distribution.

Both projects target Java 21 and Minecraft 1.21.1. From either project folder,
run `bash ./gradlew :neoforge:build` (or the matching Fabric task). Dependency
downloads may be required on the first build.

Static checks are available as `bash tools/verify_frontiers.sh` and
`bash tools/verify_gacha.sh` inside their respective project folders.
