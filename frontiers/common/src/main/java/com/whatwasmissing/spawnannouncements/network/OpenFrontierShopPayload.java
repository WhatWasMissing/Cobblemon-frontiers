package com.whatwasmissing.spawnannouncements.network;

import com.whatwasmissing.spawnannouncements.core.AnnouncementService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/** Client request used by the configurable keybind to open the server-owned dashboard. */
public record OpenFrontierShopPayload() implements CustomPacketPayload {
    public static final Type<OpenFrontierShopPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AnnouncementService.MOD_ID, "open_frontier_dashboard"));
    public static final StreamCodec<FriendlyByteBuf, OpenFrontierShopPayload> CODEC =
            StreamCodec.unit(new OpenFrontierShopPayload());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
