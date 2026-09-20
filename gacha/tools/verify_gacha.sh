#!/usr/bin/env bash
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"

test -f "$root/settings.gradle.kts"
test -f "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/core/GachaService.java"
test -f "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/core/GachaUpgradeService.java"
test -f "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/UpgradeScreen.java"
test -f "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/network/GachaPullResultPayload.java"
test -f "$root/neoforge/src/main/resources/META-INF/neoforge.mods.toml"
test -f "$root/fabric/src/main/resources/fabric.mod.json"

if rg -n "cobblemon_frontiers|spawnannouncements|FrontierLedger|AnnouncementService" \
    "$root/common/src" "$root/fabric/src" "$root/neoforge/src"; then
  echo "Gacha project contains Frontiers identifiers" >&2
  exit 1
fi

rg -q 'key\.cobblemon_gacha\.open' "$root/common/src/main/resources/assets/cobblemon_gacha/lang/en_us.json"
rg -q 'GLFW_KEY_G' "$root/fabric/src/main/java/com/whatwasmissing/cobblemongacha/CobblemonGachaFabricClient.java"
rg -q 'GLFW_KEY_G' "$root/neoforge/src/main/java/com/whatwasmissing/cobblemongacha/CobblemonGachaNeoForgeClient.java"
rg -q 'rarePityDraws|legendaryPityDraws|shinyChance' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/core/GachaService.java"
rg -q 'POKEMON_CAPTURED' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/core/GachaService.java"
rg -q 'handleInventoryMouseClick' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/GachaScreen.java"
rg -q 'GachaMenuSyncPayload|applyServerSnapshot|serverEpochSeconds|gamblingCooldownSeconds' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/GachaMenu.java"
rg -q 'GachaMenuSyncPayload|applyServerSnapshot|targetsForDisplay|gamblingCooldownSeconds' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/UpgradeMenu.java"
rg -q 'playS2C\(\)|registerGlobalReceiver|GachaMenuSyncPayload' "$root/fabric/src/main/java/com/whatwasmissing/cobblemongacha/CobblemonGachaFabricClient.java"
rg -q 'playToClient|GachaMenuSyncPayload' "$root/neoforge/src/main/java/com/whatwasmissing/cobblemongacha/CobblemonGachaNeoForge.java"
rg -q 'drawFeaturedLineup|featuredEntries|themedCard' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/GachaScreen.java"
rg -q 'BUTTON_CURRENT_BANNER|CURRENT_BANNER' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/GachaMenu.java" "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/GachaScreen.java"
rg -q 'captureProgress|capturesUntilNextTicket|Next ticket' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/GachaMenu.java" "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/GachaScreen.java"
rg -q 'PokemonSpriteRenderer|renderPullReveal|REVEAL_DURATION_MS' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/GachaScreen.java"
rg -q 'GachaPullResultPayload|pullResultSequence|parseRarity' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/GachaScreen.java" "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/GachaMenu.java"
rg -q 'PULL_RESPONSE_TIMEOUT_MS|pullPending|Submitting draw' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/GachaScreen.java"
if rg -q 'HashMap|oldCounts|historySignatures|historyBaselineReady|historyRarity' \
    "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/GachaScreen.java"; then
  echo "Gacha reveal still infers results from history presentation" >&2
  exit 1
fi
rg -q 'isShinyLabel' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/GachaScreen.java"
rg -q 'GachaPullResultPayload|pull_result|sendPullReveal' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/core/GachaService.java" \
    "$root/fabric/src/main/java/com/whatwasmissing/cobblemongacha/CobblemonGachaFabric.java" \
    "$root/fabric/src/main/java/com/whatwasmissing/cobblemongacha/CobblemonGachaFabricClient.java" \
    "$root/neoforge/src/main/java/com/whatwasmissing/cobblemongacha/CobblemonGachaNeoForge.java"
rg -q 'WHEEL_MIN_SPIN_MS|targetWheelAngle|wheelPending|server-confirmed odds' \
    "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/UpgradeScreen.java"
rg -q 'GuiUtilsKt|PokemonSpecies|FloatingState|drawProfile' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/PokemonSpriteRenderer.java"
rg -q 'historyIcon' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/GachaMenu.java"
for banner_id in kanto johto hoenn sinnoh unova kalos alola galar hisui paldea; do
  rg -q "case \"$banner_id\"" "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/GachaScreen.java"
done
if rg -q 'pageButton\(graphics, 654, 102|pageButton\(graphics, 698, 102|isInside\(mouseX, mouseY, 654, 102|isInside\(mouseX, mouseY, 698, 102' \
    "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/GachaScreen.java"; then
  echo "Banner navigation render and hitbox coordinates drifted apart" >&2
  exit 1
fi
rg -q 'source value|sourceValue' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/core/GachaUpgradeService.java"
rg -q '0.90|0.001' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/core/ItemValueService.java"
rg -q 'minecraft:stick.*0.1|return 1.0' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/core/ItemValueService.java"
rg -q 'OPTIONAL_HIGH_TIER_NAMESPACES|optionalIntegrationValue|mega_showdown' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/core/ItemValueService.java"
rg -q 'BUTTON_SOURCE_BASE|BUTTON_TARGET_BASE' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/UpgradeMenu.java"
rg -q 'BUTTON_MULTIPLIER_BASE' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/UpgradeMenu.java"
rg -q 'addPokemon|GachaRarity\.LEGENDARY' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/core/UpgradeCatalog.java"
rg -q 'availableTargets|isLegendaryMonumentSpecies' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/core/UpgradeCatalog.java"
rg -q 'legendaryPokemonChance|legendaryPokemonMinimumSourceValue' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/config/GachaConfig.java"
rg -q 'gamblingCooldownSeconds' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/config/GachaConfig.java"
rg -q 'pokemonWagerCooldownSeconds' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/config/GachaConfig.java"
rg -q 'gamblingCooldownRemainingNanos|startGamblingCooldown' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/core/GachaService.java"
rg -q 'gamblingCooldownRemainingSeconds|grantTestPulls' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/core/GachaService.java"
rg -q 'test_pulls|GachaCommands' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/commands/GachaCommands.java"
rg -q 'GachaCommands' "$root/fabric/src/main/java/com/whatwasmissing/cobblemongacha/CobblemonGachaFabric.java"
rg -q 'GachaCommands|RegisterCommandsEvent' "$root/neoforge/src/main/java/com/whatwasmissing/cobblemongacha/CobblemonGachaNeoForge.java"
rg -q 'bannerRotationHours|announceExceptionalDrops|serverAnnouncementMinimumRarity|announceShinyDrops' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/config/GachaConfig.java"
rg -q 'legendaryMonumentSpecies|defaultLegendaryMonumentSpecies|unmonumentedLegendary' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/config/GachaConfig.java"
rg -q 'replace\(\x27_\x27, \x27-\x27\)' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/config/GachaConfig.java"
rg -q 'banner\.entries\.removeIf\(entry -> legendaryMonumentSpecies\.contains\(speciesKey\(entry\.species\)\)\)' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/config/GachaConfig.java"
rg -q 'target\.pokemon\(\) && GachaService\.isLegendaryMonumentSpecies\(target\.id\(\)\)' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/core/UpgradeCatalog.java"
rg -q 'writeDefaults|config\.normalise\(\)' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/config/GachaConfig.java"
rg -q 'never recurse|banners = defaultBanners\(\)' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/config/GachaConfig.java"
rg -q 'activeBannerIndex|secondsUntilBannerRotation|announceExceptionalDrop' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/core/GachaService.java"
rg -q 'currentLegendaryPity|hasWeightedEntry' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/core/GachaService.java"
rg -q 'legendaryPity' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/core/GachaLedger.java"
rg -q 'SERVER_STOPPING|flushLedger' "$root/fabric/src/main/java/com/whatwasmissing/cobblemongacha/CobblemonGachaFabric.java"
rg -q 'ServerStoppingEvent|flushLedger' "$root/neoforge/src/main/java/com/whatwasmissing/cobblemongacha/CobblemonGachaNeoForge.java"
rg -q 'PokemonRewardAdapter|GachaRarity\.LEGENDARY' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/core/GachaUpgradeService.java"
rg -q 'CUSTOM_DATA|cobblemon_gacha_voucher|redeemVoucher' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/core/PokemonRewardAdapter.java"
rg -q 'GachaCompatibilityHooks|registerOptionalNamespace' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/api/GachaCompatibilityHooks.java"
rg -q 'config\.enabled' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/core/GachaUpgradeService.java"
rg -q 'BUTTON_ITEM_TARGETS|BUTTON_POKEMON_TARGETS' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/UpgradeMenu.java"
rg -q 'TargetCategory\.ITEMS|TargetCategory\.POKEMON' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/UpgradeScreen.java"
rg -q 'BUTTON_OPEN_UPGRADER|OPEN_UPGRADER_SLOT' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/GachaMenu.java"
rg -q 'BUTTON_OPEN_DRAWS|OPEN_DRAWS_SLOT' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/UpgradeMenu.java"
rg -q 'slotId >= SIZE' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/GachaMenu.java"
rg -q 'clickType == ClickType\.PICKUP && button == 0' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/GachaMenu.java"
rg -q 'clickType == ClickType\.PICKUP && button == 0' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/UpgradeMenu.java"
rg -q 'menu\.getSlot\(containerSlot\)\.hasItem' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/UpgradeScreen.java"
rg -q 'int containerSlot = 54 \+ row \* 9 \+ column' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/UpgradeScreen.java"
if rg -q '54 \+ 9 \+ row \* 9 \+ column' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/UpgradeScreen.java"; then
  echo "Upgrade inventory slot mapping still skips the first inventory row" >&2
  exit 1
fi
rg -q 'Double\.isFinite' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/core/GachaService.java"
rg -q 'Double\.isFinite' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/core/ItemValueService.java"
rg -q 'preserveUnreadableLedger|Keeping the file untouched|Objects::isNull' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/core/GachaLedger.java"
if rg -q 'HashMap|oldCounts|historySignatures|historyBaselineReady|hasServerSnapshot' \
    "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/GachaScreen.java"; then
  echo "Gacha reveal still depends on the client history baseline" >&2
  exit 1
fi
rg -q 'drawProfile\(species\.getResourceIdentifier' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/gui/PokemonSpriteRenderer.java"

banner_count="$(rg -c 'banners\.add\(banner\(' "$root/common/src/main/java/com/whatwasmissing/cobblemongacha/config/GachaConfig.java")"
if [ "$banner_count" -lt 10 ]; then
  echo "Expected at least 10 regional banner groups, found $banner_count" >&2
  exit 1
fi

echo "Cobblemon Gacha static validation passed."
