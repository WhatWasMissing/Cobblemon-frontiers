package com.whatwasmissing.spawnannouncements;

import com.whatwasmissing.spawnannouncements.command.AnnouncementCommands;
import com.whatwasmissing.spawnannouncements.core.AnnouncementService;
import com.whatwasmissing.spawnannouncements.gui.FrontierMenuTypes;
import com.whatwasmissing.spawnannouncements.gui.FrontierShopCatalog;
import com.whatwasmissing.spawnannouncements.network.OpenFrontierShopPayload;
import com.whatwasmissing.spawnannouncements.network.FrontierShopStatePayload;
import com.whatwasmissing.spawnannouncements.network.FrontierShopFilterPayload;
import com.whatwasmissing.spawnannouncements.gui.FrontierShopMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** NeoForge server entrypoint. */
@Mod(AnnouncementService.MOD_ID)
public final class CobblemonFrontiersNeoForge {
    private static final DeferredRegister<net.minecraft.world.inventory.MenuType<?>> MENU_TYPES =
            DeferredRegister.create(BuiltInRegistries.MENU, AnnouncementService.MOD_ID);

    static {
        MENU_TYPES.register("frontier_dashboard", () -> FrontierMenuTypes.FRONTIER_DASHBOARD);
    }

    public CobblemonFrontiersNeoForge() {
        var modEventBus = ModLoadingContext.get().getActiveContainer().getEventBus();
        MENU_TYPES.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(CobblemonFrontiersNeoForge::registerCommands);
        NeoForge.EVENT_BUS.addListener((ServerStoppingEvent event) -> AnnouncementService.flushLedger());
        modEventBus.addListener(CobblemonFrontiersNeoForge::commonSetup);
        modEventBus.addListener(CobblemonFrontiersNeoForge::registerPayloads);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        AnnouncementService.init(FMLPaths.CONFIGDIR.get());
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        AnnouncementCommands.register(event.getDispatcher());
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(OpenFrontierShopPayload.TYPE, OpenFrontierShopPayload.CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        AnnouncementService.openShop(player);
                    }
                }));
        registrar.playToServer(FrontierShopFilterPayload.TYPE, FrontierShopFilterPayload.CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player
                            && player.containerMenu instanceof FrontierShopMenu menu
                            && menu.containerId == payload.menuId()) {
                        menu.applyFilter(player, payload.searchQuery(),
                                FrontierShopCatalog.Category.parse(payload.category()),
                                FrontierShopCatalog.SortMode.parse(payload.sortMode()));
                    }
                }));
        registrar.playToClient(FrontierShopStatePayload.TYPE, FrontierShopStatePayload.CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof Player player
                            && player.containerMenu instanceof FrontierShopMenu menu) {
                        menu.applyServerState(payload);
                    }
                }));
    }
}
