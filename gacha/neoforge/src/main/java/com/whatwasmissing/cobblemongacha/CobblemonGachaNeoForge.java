package com.whatwasmissing.cobblemongacha;

import com.whatwasmissing.cobblemongacha.commands.GachaCommands;
import com.whatwasmissing.cobblemongacha.core.GachaService;
import com.whatwasmissing.cobblemongacha.gui.GachaMenu;
import com.whatwasmissing.cobblemongacha.gui.GachaMenuTypes;
import com.whatwasmissing.cobblemongacha.gui.UpgradeMenu;
import com.whatwasmissing.cobblemongacha.network.GachaMenuSyncPayload;
import com.whatwasmissing.cobblemongacha.network.OpenGachaPayload;
import com.whatwasmissing.cobblemongacha.network.UpgradeResultPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** NeoForge entrypoint. */
@Mod(GachaService.MOD_ID)
public final class CobblemonGachaNeoForge {
    private static final DeferredRegister<net.minecraft.world.inventory.MenuType<?>> MENU_TYPES =
            DeferredRegister.create(BuiltInRegistries.MENU, GachaService.MOD_ID);

    static {
        MENU_TYPES.register("gacha_menu", () -> GachaMenuTypes.GACHA_MENU);
        MENU_TYPES.register("upgrade_menu", () -> GachaMenuTypes.UPGRADE_MENU);
    }

    public CobblemonGachaNeoForge(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
        modEventBus.addListener(CobblemonGachaNeoForge::commonSetup);
        modEventBus.addListener(CobblemonGachaNeoForge::registerPayloads);
        NeoForge.EVENT_BUS.addListener((ServerStoppingEvent event) -> GachaService.flushLedger());
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) ->
                GachaCommands.register(event.getDispatcher()));
    }

    private static void commonSetup(FMLCommonSetupEvent event) { GachaService.init(FMLPaths.CONFIGDIR.get()); }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(OpenGachaPayload.TYPE, OpenGachaPayload.CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) GachaService.openMenu(player);
                }));
        registrar.playToClient(GachaMenuSyncPayload.TYPE, GachaMenuSyncPayload.CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (!(context.player() instanceof Player player)) return;
                    if (player.containerMenu instanceof GachaMenu menu) {
                        menu.applyServerSnapshot(payload);
                    } else if (player.containerMenu instanceof UpgradeMenu menu) {
                        menu.applyServerSnapshot(payload);
                    }
                }));
        registrar.playToClient(UpgradeResultPayload.TYPE, UpgradeResultPayload.CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof Player player
                            && player.containerMenu instanceof UpgradeMenu menu) {
                        menu.applyUpgradeResult(payload);
                    }
                }));
    }
}
