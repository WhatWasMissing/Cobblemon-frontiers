# How encounters work

## Natural encounter flow

The mod supplies additional spawn-condition checks. The official datapack defines encounters that use those checks. In the standard setup:

1. The datapack contributes a spawn entry to Cobblemon's world spawn pools.
2. The player carries the entry's `key_item`.
3. The player is in an accepted biome and satisfies every other condition on that entry.
4. Cobblemon evaluates the entry according to the spawn bucket, weight, level range, and spawn context.
5. A successful spawn is an ordinary wild Cobblemon encounter.

An entry's condition fields are combined: all fields on that particular entry must pass. Multiple spawn entries for one Pokémon are alternate routes; satisfying any one complete entry can make it eligible. The catalogue preserves the source conditions so administrators can verify exact details.

## Key item versus custom item requirement

- `key_item` checks whether a named Myths and Legends key item is in the player's inventory.
- `item_requirement` checks for a required stack, optionally consuming it when the encounter is selected/succeeds according to the condition implementation.
- `custom_key_items` is an additional mod condition for custom item IDs and counts; each element can specify `consume`.

Do not assume a required item is consumed merely because it appears in a datapack condition. Check its `consume` value and the exact mod version's behavior.

## Other condition types in the official datapack

The shipped entries use combinations of biome, sky visibility, thunder, light, height, moon phase, time range, nearby blocks, party Pokémon, required Zygarde cells or cores, and stone requirements. See the [Encounter catalogue](encounters.md) for the fields on each route.

## Spawn chance

`weight` is a relative weight within its spawn bucket, not a direct percentage. Other entries in the same bucket compete for spawn attempts. A low-weight `ultra-rare` entry can take substantial time even when the player is meeting all conditions.


