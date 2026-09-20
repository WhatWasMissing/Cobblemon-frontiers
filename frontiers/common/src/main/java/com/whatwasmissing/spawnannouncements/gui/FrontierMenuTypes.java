package com.whatwasmissing.spawnannouncements.gui;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

/** Shared menu instance registered by each loader under the same resource ID. */
public final class FrontierMenuTypes {
    public static final MenuType<FrontierShopMenu> FRONTIER_DASHBOARD =
            new MenuType<>(FrontierShopMenu::new, FeatureFlags.DEFAULT_FLAGS);

    private FrontierMenuTypes() {}
}
