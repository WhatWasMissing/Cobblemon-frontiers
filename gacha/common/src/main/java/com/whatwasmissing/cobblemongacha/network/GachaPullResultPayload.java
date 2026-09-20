package com.whatwasmissing.cobblemongacha.network;

import com.whatwasmissing.cobblemongacha.core.GachaService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/** Server-authoritative highlight used to drive the client-side pull reveal. */
public record GachaPullResultPayload(String species, String label, String rarity,
                                     boolean shiny, int resultCount)
        implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    private static final int MAX_STRING_LENGTH = 256;

    public static final Type<GachaPullResultPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaService.MOD_ID, "pull_result"));

    public static final StreamCodec<FriendlyByteBuf, GachaPullResultPayload> CODEC = new StreamCodec<>() {
        @Override
        public GachaPullResultPayload decode(FriendlyByteBuf buffer) {
            return new GachaPullResultPayload(buffer.readUtf(MAX_STRING_LENGTH),
                    buffer.readUtf(MAX_STRING_LENGTH), buffer.readUtf(MAX_STRING_LENGTH),
                    buffer.readBoolean(), Math.max(1, buffer.readVarInt()));
        }

        @Override
        public void encode(FriendlyByteBuf buffer, GachaPullResultPayload payload) {
            buffer.writeUtf(limit(payload.species), MAX_STRING_LENGTH);
            buffer.writeUtf(limit(payload.label), MAX_STRING_LENGTH);
            buffer.writeUtf(limit(payload.rarity), MAX_STRING_LENGTH);
            buffer.writeBoolean(payload.shiny);
            buffer.writeVarInt(Math.max(1, payload.resultCount));
        }
    };

    private static String limit(String value) {
        if (value == null) return "";
        return value.length() <= MAX_STRING_LENGTH ? value : value.substring(0, MAX_STRING_LENGTH);
    }

    @Override
    public @NotNull Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
        return TYPE;
    }
}
