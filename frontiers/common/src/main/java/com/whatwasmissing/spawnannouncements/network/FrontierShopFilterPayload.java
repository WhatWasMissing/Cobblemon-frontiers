package com.whatwasmissing.spawnannouncements.network;

import com.whatwasmissing.spawnannouncements.core.AnnouncementService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/** Client request for the visible exchange category, search query, and sort order. */
public record FrontierShopFilterPayload(int menuId, String searchQuery, String category, String sortMode)
        implements CustomPacketPayload {
    public static final Type<FrontierShopFilterPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AnnouncementService.MOD_ID, "exchange_filter"));

    public static final StreamCodec<FriendlyByteBuf, FrontierShopFilterPayload> CODEC = new StreamCodec<>() {
        @Override
        public FrontierShopFilterPayload decode(FriendlyByteBuf buffer) {
            return new FrontierShopFilterPayload(buffer.readVarInt(), buffer.readUtf(64),
                    buffer.readUtf(24), buffer.readUtf(16));
        }

        @Override
        public void encode(FriendlyByteBuf buffer, FrontierShopFilterPayload payload) {
            buffer.writeVarInt(payload.menuId());
            buffer.writeUtf(payload.searchQuery() == null ? "" : payload.searchQuery(), 64);
            buffer.writeUtf(payload.category() == null ? "all" : payload.category(), 24);
            buffer.writeUtf(payload.sortMode() == null ? "featured" : payload.sortMode(), 16);
        }
    };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
