package com.whatwasmissing.cobblemongacha;

import com.mojang.blaze3d.platform.InputConstants;
import com.whatwasmissing.cobblemongacha.gui.GachaMenuTypes;
import com.whatwasmissing.cobblemongacha.gui.GachaScreen;
import com.whatwasmissing.cobblemongacha.gui.GachaMenu;
import com.whatwasmissing.cobblemongacha.gui.UpgradeScreen;
import com.whatwasmissing.cobblemongacha.gui.UpgradeMenu;
import com.whatwasmissing.cobblemongacha.network.OpenGachaPayload;
import com.whatwasmissing.cobblemongacha.network.UpgradeResultPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import org.lwjgl.glfw.GLFW;

/** Client keybind and presentation registration. */
public final class CobblemonGachaFabricClient implements ClientModInitializer {
    private static KeyMapping openGacha;

    @Override
    public void onInitializeClient() {
        MenuScreens.register(GachaMenuTypes.GACHA_MENU, GachaScreen::new);
        MenuScreens.register(GachaMenuTypes.UPGRADE_MENU, UpgradeScreen::new);
        ClientPlayNetworking.registerGlobalReceiver(com.whatwasmissing.cobblemongacha.network.GachaMenuSyncPayload.TYPE, (payload, context) ->
                context.client().execute(() -> {
                    if (context.client().player == null) return;
                    if (context.client().player.containerMenu instanceof GachaMenu menu) {
                        menu.applyServerSnapshot(payload);
                    } else if (context.client().player.containerMenu instanceof UpgradeMenu menu) {
                        menu.applyServerSnapshot(payload);
                    }
                }));
        ClientPlayNetworking.registerGlobalReceiver(UpgradeResultPayload.TYPE, (payload, context) ->
                context.client().execute(() -> {
                    if (context.client().player != null
                            && context.client().player.containerMenu instanceof UpgradeMenu menu) {
                        menu.applyUpgradeResult(payload);
                    }
                }));
        openGacha = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.cobblemon_gacha.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G,
                "category.cobblemon_gacha"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGacha.consumeClick()) {
                if (client.player != null && client.getConnection() != null && client.screen == null) {
                    ClientPlayNetworking.send(new OpenGachaPayload());
                }
            }
        });
    }
}
