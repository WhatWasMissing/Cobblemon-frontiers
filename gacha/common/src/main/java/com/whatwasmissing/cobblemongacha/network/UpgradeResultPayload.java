package com.whatwasmissing.cobblemongacha.network;

import com.whatwasmissing.cobblemongacha.core.GachaService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/** Server-confirmed result used to drive the client-side wager animation. */
public record UpgradeResultPayload(boolean success, String title, String detail, double chance)
        implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static final Type<UpgradeResultPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaService.MOD_ID, "upgrade_result"));
    public static final StreamCodec<FriendlyByteBuf, UpgradeResultPayload> CODEC = new StreamCodec<>() {
        @Override
        public UpgradeResultPayload decode(FriendlyByteBuf buffer) {
            return new UpgradeResultPayload(buffer.readBoolean(), buffer.readUtf(256),
                    buffer.readUtf(256), buffer.readDouble());
        }

        @Override
        public void encode(FriendlyByteBuf buffer, UpgradeResultPayload payload) {
            buffer.writeBoolean(payload.success);
            buffer.writeUtf(payload.title == null ? "" : payload.title, 256);
            buffer.writeUtf(payload.detail == null ? "" : payload.detail, 256);
            buffer.writeDouble(Double.isFinite(payload.chance) ? payload.chance : 0.0);
        }
    };

    @Override
    public @NotNull Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
        return TYPE;
    }
}
