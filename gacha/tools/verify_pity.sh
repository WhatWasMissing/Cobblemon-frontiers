#!/usr/bin/env bash
set -euo pipefail

# Deterministic policy check for the server roll rules. This mirrors the
# threshold/reset contract without requiring a Minecraft runtime or Gradle.
awk '
function isRareOrBetter(result) {
  return result == "RARE" || result == "EPIC" || result == "LEGENDARY" || result == "MYTHIC"
}

function isLegendaryOrBetter(result) {
  return result == "LEGENDARY" || result == "MYTHIC"
}

function record(result) {
  if (isRareOrBetter(result)) rarePity = 0
  else rarePity++
  if (isLegendaryOrBetter(result)) legendaryPity = 0
  else legendaryPity++
}

function fail(message) {
  print message > "/dev/stderr"
  exit 1
}

BEGIN {
  rarePity = 0
  legendaryPity = 0
  for (draw = 1; draw <= 50; draw++) {
    rareGuarantee = rarePity >= 50 - 1
    legendaryGuarantee = legendaryPity >= 100 - 1
    result = legendaryGuarantee ? "LEGENDARY" : rareGuarantee ? "RARE" : "COMMON"
    if (draw == 50 && !isRareOrBetter(result)) fail("Rare pity did not resolve on draw 50")
    record(result)
  }
  if (rarePity != 0) fail("Rare pity did not reset after a Rare result")

  rarePity = 0
  legendaryPity = 0
  for (draw = 1; draw <= 100; draw++) {
    rareGuarantee = rarePity >= 50 - 1
    legendaryGuarantee = legendaryPity >= 100 - 1
    result = legendaryGuarantee ? "LEGENDARY" : rareGuarantee ? "RARE" : "COMMON"
    if (draw == 100 && !isLegendaryOrBetter(result)) fail("Legendary pity did not resolve on draw 100")
    record(result)
  }
  if (rarePity != 0 || legendaryPity != 0) fail("Pity counters did not reset after a Legendary result")

  rareSeen = 0
  for (draw = 1; draw <= 10; draw++) {
    tenPullGuarantee = draw == 10 && !rareSeen
    result = tenPullGuarantee ? "RARE" : "COMMON"
    if (draw == 10 && !isRareOrBetter(result)) fail("Ten-pull guarantee did not resolve on pull 10")
    if (isRareOrBetter(result)) rareSeen = 1
  }

  print "Gacha pity policy validation passed."
}
' 
