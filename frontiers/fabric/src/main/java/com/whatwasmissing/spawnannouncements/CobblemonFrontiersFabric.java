package com.whatwasmissing.spawnannouncements;

import com.whatwasmissing.spawnannouncements.command.AnnouncementCommands;
import com.whatwasmissing.spawnannouncements.core.AnnouncementService;
import com.whatwasmissing.spawnannouncements.gui.FrontierMenuTypes;
import com.whatwasmissing.spawnannouncements.gui.FrontierShopCatalog;
import com.whatwasmissing.spawnannouncements.gui.FrontierShopMenu;
import com.whatwasmissing.spawnannouncements.network.FrontierShopFilterPayload;
import com.whatwasmissing.spawnannouncements.network.OpenFrontierShopPayload;
import com.whatwasmissing.spawnannouncements.network.FrontierShopStatePayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/** Fabric server entrypoint. */
public final class CobblemonFrontiersFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.MENU,
                ResourceLocation.fromNamespaceAndPath(AnnouncementService.MOD_ID, "frontier_dashboard"),
                FrontierMenuTypes.FRONTIER_DASHBOARD);
        PayloadTypeRegistry.playC2S().register(OpenFrontierShopPayload.TYPE, OpenFrontierShopPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(FrontierShopFilterPayload.TYPE, FrontierShopFilterPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(FrontierShopStatePayload.TYPE, FrontierShopStatePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(OpenFrontierShopPayload.TYPE, (payload, context) ->
                context.server().execute(() -> AnnouncementService.openShop(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(FrontierShopFilterPayload.TYPE, (payload, context) ->
                context.server().execute(() -> {
                    if (context.player().containerMenu instanceof FrontierShopMenu menu
                            && menu.containerId == payload.menuId()) {
                        menu.applyFilter(context.player(), payload.searchQuery(),
                                FrontierShopCatalog.Category.parse(payload.category()),
                                FrontierShopCatalog.SortMode.parse(payload.sortMode()));
                    }
                }));
        AnnouncementService.init(FabricLoader.getInstance().getConfigDir());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> AnnouncementService.flushLedger());
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                AnnouncementCommands.register(dispatcher));
    }
}
