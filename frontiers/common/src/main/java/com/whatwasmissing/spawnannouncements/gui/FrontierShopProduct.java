package com.whatwasmissing.spawnannouncements.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record FrontierShopProduct(
        String id,
        String displayName,
        Item item,
        int amount,
        int cost,
        ChatFormatting colour,
        FrontierShopCatalog.Category category
) {
    public FrontierShopProduct(String id, String displayName, Item item, int amount, int cost, ChatFormatting colour) {
        this(id, displayName, item, amount, cost, colour, FrontierShopCatalog.Category.SUPPLIES);
    }

    public ItemStack displayStack() {
        // Keep the real registry item name and components intact. The GUI draws
        // price/category metadata separately, so shop stacks remain stackable
        // with ordinary items from Cobblemon and other mods.
        return new ItemStack(item, amount);
    }

    public String baseName() {
        return new ItemStack(item).getHoverName().getString();
    }

    public ItemStack rewardStack() {
        return new ItemStack(item, amount);
    }
}
