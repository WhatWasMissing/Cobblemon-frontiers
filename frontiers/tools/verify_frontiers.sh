#!/usr/bin/env sh
set -eu

project_dir=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)

jq empty "$project_dir/docs/default-config.json"

test -f "$project_dir/fabric/src/main/resources/fabric.mod.json"
test -f "$project_dir/neoforge/src/main/resources/META-INF/neoforge.mods.toml"
test -f "$project_dir/common/src/main/java/com/whatwasmissing/spawnannouncements/core/AnnouncementService.java"
test -f "$project_dir/common/src/main/java/com/whatwasmissing/spawnannouncements/network/OpenFrontierShopPayload.java"
test -f "$project_dir/common/src/main/java/com/whatwasmissing/spawnannouncements/network/FrontierShopFilterPayload.java"
test -f "$project_dir/common/src/main/java/com/whatwasmissing/spawnannouncements/network/FrontierShopClientNetworking.java"
test -f "$project_dir/common/src/main/java/com/whatwasmissing/spawnannouncements/gui/FrontierDashboardScreen.java"
test -f "$project_dir/common/src/main/java/com/whatwasmissing/spawnannouncements/gui/FrontierMenuTypes.java"
test -f "$project_dir/fabric/src/main/java/com/whatwasmissing/spawnannouncements/CobblemonFrontiersFabricClient.java"
test -f "$project_dir/neoforge/src/main/java/com/whatwasmissing/spawnannouncements/CobblemonFrontiersNeoForgeClient.java"

if ! rg -q 'open_frontier_dashboard' \
    "$project_dir/fabric/src/main/java" \
    "$project_dir/neoforge/src/main/java" \
    "$project_dir/common/src/main/java"; then
  echo "frontier dashboard payload is not registered" >&2
  exit 1
fi

if ! rg -q 'cobblemon.*(poke_ball|great_ball|ultra_ball|potion|rare_candy|ability_patch)' \
    "$project_dir/common/src/main/java/com/whatwasmissing/spawnannouncements/gui/FrontierShopCatalog.java"; then
  echo "Cobblemon exchange catalogue is missing expected item groups" >&2
  exit 1
fi

if ! rg -q 'mega_showdown' \
    "$project_dir/common/src/main/java/com/whatwasmissing/spawnannouncements/gui/FrontierShopCatalog.java"; then
  echo "optional Mega Showdown catalogue discovery is missing" >&2
  exit 1
fi

if ! rg -q 'enum Category|CAPTURE|HEALING|EVOLUTION|MEGA_SHOWDOWN' \
    "$project_dir/common/src/main/java/com/whatwasmissing/spawnannouncements/gui/FrontierShopCatalog.java"; then
  echo "exchange category catalogue is missing expected groups" >&2
  exit 1
fi

if rg -n 'ChatScreen|sendCommand' \
    "$project_dir/common/src/main/java/com/whatwasmissing/spawnannouncements/gui/FrontierDashboardScreen.java" >/dev/null; then
  echo "dashboard filtering regressed to chat or command interaction" >&2
  exit 1
fi

if ! rg -q 'setMaxLength\(64\)|exchange_filter|readUtf\(64\)' \
    "$project_dir/common/src/main/java/com/whatwasmissing/spawnannouncements/gui/FrontierDashboardScreen.java" \
    "$project_dir/common/src/main/java/com/whatwasmissing/spawnannouncements/network/FrontierShopFilterPayload.java"; then
  echo "direct exchange search is missing a bounded client/server path" >&2
  exit 1
fi

if ! rg -q 'return new ItemStack\(item, amount\)' \
    "$project_dir/common/src/main/java/com/whatwasmissing/spawnannouncements/gui/FrontierShopProduct.java"; then
  echo "shop products are not returning base item stacks" >&2
  exit 1
fi

if ! rg -q 'handleInventoryButtonClick|BUTTON_PREVIOUS_PAGE|BUTTON_NEXT_PAGE' \
    "$project_dir/common/src/main/java/com/whatwasmissing/spawnannouncements/gui/FrontierDashboardScreen.java" \
    "$project_dir/common/src/main/java/com/whatwasmissing/spawnannouncements/gui/FrontierShopMenu.java"; then
  echo "dashboard page navigation is not using menu buttons" >&2
  exit 1
fi

if ! rg -q 'rememberRareSpawn|rareBonus' \
    "$project_dir/common/src/main/java/com/whatwasmissing/spawnannouncements/core/AnnouncementService.java" \
    "$project_dir/common/src/main/java/com/whatwasmissing/spawnannouncements/core/FrontierLedger.java"; then
  echo "silent rare capture bounty is missing" >&2
  exit 1
fi

if ! jq -e '.announceRareSpawns == false and .announceUltraRareSpawns == true' \
    "$project_dir/docs/default-config.json" >/dev/null; then
  echo "default-config.json must disable rare and enable ultra-rare announcements" >&2
  exit 1
fi

if ! jq -e '.messages | to_entries | all(.value | contains(" || "))' \
    "$project_dir/docs/default-config.json" >/dev/null; then
  echo "default announcements are missing message variants" >&2
  exit 1
fi

# Public templates may use a broad region and the opt-in nearby-player clause,
# but must never expose species or precise position placeholders.
if jq -e '.messages | to_entries[] | select(.value | test("\\{(x|y|z|coords|coordinates|species|pokemon|player)\\}"; "i"))' \
    "$project_dir/docs/default-config.json" >/dev/null; then
  echo "default-config.json contains a forbidden precision placeholder" >&2
  exit 1
fi

if rg -n 'getX\(|getZ\(|send.*position|send.*coordinate' \
    "$project_dir/common/src/main/java" >/dev/null; then
  echo "announcement code appears to expose precise position data" >&2
  exit 1
fi

echo "Cobblemon Frontiers static validation passed."
