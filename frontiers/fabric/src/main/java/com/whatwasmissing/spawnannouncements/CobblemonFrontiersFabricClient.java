package com.whatwasmissing.spawnannouncements;

import com.mojang.blaze3d.platform.InputConstants;
import com.whatwasmissing.spawnannouncements.gui.FrontierDashboardScreen;
import com.whatwasmissing.spawnannouncements.gui.FrontierMenuTypes;
import com.whatwasmissing.spawnannouncements.network.OpenFrontierShopPayload;
import com.whatwasmissing.spawnannouncements.network.FrontierShopStatePayload;
import com.whatwasmissing.spawnannouncements.network.FrontierShopClientNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import org.lwjgl.glfw.GLFW;

/** Registers the client-side shortcut; the server remains authoritative over the dashboard. */
public final class CobblemonFrontiersFabricClient implements ClientModInitializer {
    private static KeyMapping openFrontierDashboard;

    @Override
    public void onInitializeClient() {
        MenuScreens.register(FrontierMenuTypes.FRONTIER_DASHBOARD, FrontierDashboardScreen::new);
        FrontierShopClientNetworking.register(ClientPlayNetworking::send);
        ClientPlayNetworking.registerGlobalReceiver(FrontierShopStatePayload.TYPE, (payload, context) ->
                context.client().execute(() -> {
                    if (context.client().player != null
                            && context.client().player.containerMenu instanceof com.whatwasmissing.spawnannouncements.gui.FrontierShopMenu menu) {
                        menu.applyServerState(payload);
                    }
                }));
        openFrontierDashboard = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.cobblemon_frontiers.open_exchange",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.cobblemon_frontiers"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openFrontierDashboard.consumeClick()) {
                if (client.player != null && client.getConnection() != null
                        && !(client.screen instanceof FrontierDashboardScreen)) {
                    ClientPlayNetworking.send(new OpenFrontierShopPayload());
                }
            }
        });
    }
}
