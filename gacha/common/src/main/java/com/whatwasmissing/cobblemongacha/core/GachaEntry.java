package com.whatwasmissing.cobblemongacha.core;

/** One weighted species entry in a banner. Species ids may be namespaced. */
public final class GachaEntry {
    public String species;
    public String displayName;
    public GachaRarity rarity;
    public double weight;

    public GachaEntry() {}

    public GachaEntry(String species, String displayName, GachaRarity rarity, double weight) {
        this.species = species;
        this.displayName = displayName;
        this.rarity = rarity;
        this.weight = weight;
    }
}
