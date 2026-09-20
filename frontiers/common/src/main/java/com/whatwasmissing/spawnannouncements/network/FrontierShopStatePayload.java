package com.whatwasmissing.spawnannouncements.network;

import com.whatwasmissing.spawnannouncements.core.AnnouncementService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/** Server-owned paging and filter state for an open exchange screen. */
public record FrontierShopStatePayload(int menuId, int page, String searchQuery, String category, String sortMode)
        implements CustomPacketPayload {
    public static final Type<FrontierShopStatePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AnnouncementService.MOD_ID, "exchange_state"));

    public static final StreamCodec<FriendlyByteBuf, FrontierShopStatePayload> CODEC = new StreamCodec<>() {
        @Override
        public FrontierShopStatePayload decode(FriendlyByteBuf buffer) {
            return new FrontierShopStatePayload(buffer.readVarInt(), buffer.readVarInt(),
                    buffer.readUtf(64), buffer.readUtf(24), buffer.readUtf(16));
        }

        @Override
        public void encode(FriendlyByteBuf buffer, FrontierShopStatePayload payload) {
            buffer.writeVarInt(payload.menuId());
            buffer.writeVarInt(payload.page());
            buffer.writeUtf(payload.searchQuery(), 64);
            buffer.writeUtf(payload.category(), 24);
            buffer.writeUtf(payload.sortMode(), 16);
        }
    };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
