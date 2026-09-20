package com.whatwasmissing.cobblemongacha;

import com.mojang.blaze3d.platform.InputConstants;
import com.whatwasmissing.cobblemongacha.gui.GachaMenuTypes;
import com.whatwasmissing.cobblemongacha.gui.GachaScreen;
import com.whatwasmissing.cobblemongacha.gui.UpgradeScreen;
import com.whatwasmissing.cobblemongacha.network.OpenGachaPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** NeoForge client keybind and screen registration. */
@Mod(value = "cobblemon_gacha", dist = Dist.CLIENT)
public final class CobblemonGachaNeoForgeClient {
    private static final KeyMapping OPEN_GACHA = new KeyMapping(
            "key.cobblemon_gacha.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G,
            "category.cobblemon_gacha");

    public CobblemonGachaNeoForgeClient(IEventBus modEventBus) {
        modEventBus.addListener(CobblemonGachaNeoForgeClient::registerMenuScreens);
        modEventBus.addListener(CobblemonGachaNeoForgeClient::registerKeyMapping);
        NeoForge.EVENT_BUS.addListener(CobblemonGachaNeoForgeClient::onClientTick);
    }

    private static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(GachaMenuTypes.GACHA_MENU, GachaScreen::new);
        event.register(GachaMenuTypes.UPGRADE_MENU, UpgradeScreen::new);
    }

    private static void registerKeyMapping(RegisterKeyMappingsEvent event) { event.register(OPEN_GACHA); }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        while (OPEN_GACHA.consumeClick()) {
            if (client.player != null && client.getConnection() != null && client.screen == null) {
                PacketDistributor.sendToServer(new OpenGachaPayload());
            }
        }
    }
}
