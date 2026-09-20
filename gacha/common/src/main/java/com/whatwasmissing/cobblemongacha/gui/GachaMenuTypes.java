package com.whatwasmissing.cobblemongacha.gui;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class GachaMenuTypes {
    public static final MenuType<GachaMenu> GACHA_MENU = new MenuType<>(GachaMenu::new, FeatureFlags.DEFAULT_FLAGS);
    public static final MenuType<UpgradeMenu> UPGRADE_MENU = new MenuType<>(UpgradeMenu::new, FeatureFlags.DEFAULT_FLAGS);

    private GachaMenuTypes() {}
}
