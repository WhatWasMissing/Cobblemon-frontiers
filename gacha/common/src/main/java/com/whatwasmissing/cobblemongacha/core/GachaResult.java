package com.whatwasmissing.cobblemongacha.core;

/** Immutable result sent to the delivery layer and rendered in the pull history. */
public record GachaResult(String species, String displayName, GachaRarity rarity, boolean shiny) {
    public String label() {
        return (shiny ? "Shiny " : "") + displayName;
    }
}
