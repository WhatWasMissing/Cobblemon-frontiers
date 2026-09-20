package com.whatwasmissing.cobblemongacha.network;

import com.whatwasmissing.cobblemongacha.core.GachaService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/** Client request only; the server decides whether the menu can open. */
public record OpenGachaPayload() implements CustomPacketPayload {
    public static final Type<OpenGachaPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaService.MOD_ID, "open_gacha"));
    public static final StreamCodec<FriendlyByteBuf, OpenGachaPayload> CODEC =
            StreamCodec.unit(new OpenGachaPayload());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}
