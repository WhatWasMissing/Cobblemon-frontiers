package com.whatwasmissing.spawnannouncements;

import com.mojang.blaze3d.platform.InputConstants;
import com.whatwasmissing.spawnannouncements.core.AnnouncementService;
import com.whatwasmissing.spawnannouncements.gui.FrontierDashboardScreen;
import com.whatwasmissing.spawnannouncements.gui.FrontierMenuTypes;
import com.whatwasmissing.spawnannouncements.gui.FrontierShopMenu;
import com.whatwasmissing.spawnannouncements.network.OpenFrontierShopPayload;
import com.whatwasmissing.spawnannouncements.network.FrontierShopClientNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Proxy;

/** Registers the client-side shortcut; the server remains authoritative over the dashboard. */
@Mod(value = AnnouncementService.MOD_ID, dist = Dist.CLIENT)
public final class CobblemonFrontiersNeoForgeClient {
    private static final KeyMapping OPEN_FRONTIER_DASHBOARD = new KeyMapping(
            "key.cobblemon_frontiers.open_exchange",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "category.cobblemon_frontiers");

    public CobblemonFrontiersNeoForgeClient() {
        var modEventBus = ModLoadingContext.get().getActiveContainer().getEventBus();
        FrontierShopClientNetworking.register(PacketDistributor::sendToServer);
        modEventBus.addListener(CobblemonFrontiersNeoForgeClient::registerMenuScreens);
        modEventBus.addListener(CobblemonFrontiersNeoForgeClient::registerKeyMapping);
        NeoForge.EVENT_BUS.addListener(CobblemonFrontiersNeoForgeClient::onClientTick);
    }

    private static void registerMenuScreens(RegisterMenuScreensEvent event) {
        // NeoForge 21.1 exposes MenuScreens.ScreenConstructor as a private
        // nested type in the named mappings. Register through the public event
        // method without taking a compile-time dependency on that visibility.
        try {
            Class<?> screenConstructor = Class.forName(
                    "net.minecraft.client.gui.screens.MenuScreens$ScreenConstructor");
            Object constructor = Proxy.newProxyInstance(
                    screenConstructor.getClassLoader(),
                    new Class<?>[]{screenConstructor},
                    (proxy, method, args) -> {
                        if (method.getName().equals("create")) {
                            return new FrontierDashboardScreen(
                                    (FrontierShopMenu) args[0],
                                    (Inventory) args[1],
                                    (Component) args[2]);
                        }
                        return null;
                    });
            event.getClass().getMethod("register", MenuType.class, screenConstructor)
                    .invoke(event, FrontierMenuTypes.FRONTIER_DASHBOARD, constructor);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not register the Frontier dashboard screen", exception);
        }
    }

    private static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        event.register(OPEN_FRONTIER_DASHBOARD);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        while (OPEN_FRONTIER_DASHBOARD.consumeClick()) {
            if (client.player != null && client.getConnection() != null
                    && !(client.screen instanceof FrontierDashboardScreen)) {
                PacketDistributor.sendToServer(new OpenFrontierShopPayload());
            }
        }
    }
}
