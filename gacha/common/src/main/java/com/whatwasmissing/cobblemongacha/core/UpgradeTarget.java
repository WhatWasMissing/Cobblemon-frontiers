package com.whatwasmissing.cobblemongacha.core;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** A target item exposed by the Upgrader-style catalogue. */
public record UpgradeTarget(String id, String displayName, Item item, ChatFormatting formatting,
                            boolean pokemon, GachaRarity rarity) {
    public UpgradeTarget(String id, String displayName, Item item, ChatFormatting formatting) {
        this(id, displayName, item, formatting, false, GachaRarity.COMMON);
    }

    public static UpgradeTarget pokemon(String species, String displayName, GachaRarity rarity,
                                        ChatFormatting formatting) {
        return new UpgradeTarget("cobblemon:" + species, displayName, Items.PAPER, formatting, true, rarity);
    }

    public ItemStack displayStack(int amount) {
        ItemStack stack = new ItemStack(pokemon ? Items.PAPER : item, Math.max(1, amount));
        String label = pokemon ? displayName + " · " + rarity.displayName() : displayName;
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(label).withStyle(formatting));
        return stack;
    }

    /** The actual delivered reward, deliberately free of GUI-only custom data. */
    public ItemStack rewardStack(int amount) {
        return new ItemStack(pokemon ? Items.PAPER : item, Math.max(1, amount));
    }
}
