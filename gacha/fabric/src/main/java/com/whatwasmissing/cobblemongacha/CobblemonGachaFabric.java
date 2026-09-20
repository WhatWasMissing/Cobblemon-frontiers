package com.whatwasmissing.cobblemongacha;

import com.whatwasmissing.cobblemongacha.commands.GachaCommands;
import com.whatwasmissing.cobblemongacha.core.GachaService;
import com.whatwasmissing.cobblemongacha.gui.GachaMenuTypes;
import com.whatwasmissing.cobblemongacha.network.GachaMenuSyncPayload;
import com.whatwasmissing.cobblemongacha.network.GachaPullResultPayload;
import com.whatwasmissing.cobblemongacha.network.OpenGachaPayload;
import com.whatwasmissing.cobblemongacha.network.UpgradeResultPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/** Fabric entrypoint. */
public final class CobblemonGachaFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.MENU,
                ResourceLocation.fromNamespaceAndPath(GachaService.MOD_ID, "gacha_menu"),
                GachaMenuTypes.GACHA_MENU);
        Registry.register(BuiltInRegistries.MENU,
                ResourceLocation.fromNamespaceAndPath(GachaService.MOD_ID, "upgrade_menu"),
                GachaMenuTypes.UPGRADE_MENU);
        PayloadTypeRegistry.playC2S().register(OpenGachaPayload.TYPE, OpenGachaPayload.CODEC);
        // The server also needs the S2C codec registered because the menus
        // send their authoritative banner/target snapshot after opening.
        PayloadTypeRegistry.playS2C().register(GachaMenuSyncPayload.TYPE, GachaMenuSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(UpgradeResultPayload.TYPE, UpgradeResultPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GachaPullResultPayload.TYPE, GachaPullResultPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(OpenGachaPayload.TYPE, (payload, context) ->
                context.server().execute(() -> GachaService.openMenu(context.player())));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                GachaCommands.register(dispatcher));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> GachaService.flushLedger());
        GachaService.init(FabricLoader.getInstance().getConfigDir());
    }
}
