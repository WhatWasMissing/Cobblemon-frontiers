package com.whatwasmissing.cobblemongacha.core;

import net.minecraft.ChatFormatting;

/** Ordered reward tiers used by the server-side roll engine. */
public enum GachaRarity {
    COMMON("Common", ChatFormatting.GRAY, 0),
    RARE("Rare", ChatFormatting.BLUE, 1),
    EPIC("Epic", ChatFormatting.DARK_PURPLE, 2),
    LEGENDARY("Legendary", ChatFormatting.GOLD, 3),
    MYTHIC("Mythic", ChatFormatting.LIGHT_PURPLE, 4);

    private final String displayName;
    private final ChatFormatting formatting;
    private final int rank;

    GachaRarity(String displayName, ChatFormatting formatting, int rank) {
        this.displayName = displayName;
        this.formatting = formatting;
        this.rank = rank;
    }

    public String displayName() { return displayName; }
    public ChatFormatting formatting() { return formatting; }
    public int rank() { return rank; }

    public boolean atLeast(GachaRarity other) { return rank >= other.rank; }
}
