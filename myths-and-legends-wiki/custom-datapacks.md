# Custom datapacks

Myths and Legends encounters are data-driven. The official datapack repository is the reference implementation for the layout and syntax. Back up a world and test edits on a copy before deploying them to a live server.

## Where spawn definitions live

Each world spawn-pool JSON file is placed under:

```text
data/cobblemon/spawn_pool_world/<file>.json
```

The top-level object includes an `enabled` flag and a `spawns` array. Each spawn entry defines its Pokémon, level range, bucket, weight, context/preset, and a `condition` object. The Myths and Legends mod must be installed for the custom key-item condition to be understood.

## Example condition

```json
{
  "condition": {
    "biomes": ["minecraft:forest", "#cobblemon:is_jungle"],
    "key_item": "mythsandlegends:old_sea_map"
  }
}
```

This is only the condition fragment, not a complete spawn entry. Add it to a complete Cobblemon spawn definition appropriate for the installed Cobblemon version. The item must be a valid registered ID, and the Pokémon species must exist in the installed species data.

## Useful practices

- Start from a current official datapack spawn JSON and change one field at a time.
- Keep the `mythsandlegends:` namespace exact for mod items.
- Validate JSON commas, quotes, arrays, and IDs before reloading.
- Keep separate spawn entries for alternate routes; do not mix an OR route into one condition when all fields are evaluated together.
- After changes, use `/reload` and inspect `latest.log` for datapack parse or registry errors.
- For a reproducible wiki/catalogue update, record the datapack release or commit used.

The upstream datapack README asks contributors to update its `SpawnListMythsAndLegends.xlsx` alongside encounter changes. Respect the upstream license and redistribution rules when sharing its datapack files.


