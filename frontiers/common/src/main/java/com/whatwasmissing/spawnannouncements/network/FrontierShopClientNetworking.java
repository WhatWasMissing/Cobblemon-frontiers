package com.whatwasmissing.spawnannouncements.network;

import java.util.Objects;
import java.util.function.Consumer;

/** Loader-neutral hook used by the common dashboard to send a filter request. */
public final class FrontierShopClientNetworking {
    private static Consumer<FrontierShopFilterPayload> sender = payload -> { };

    private FrontierShopClientNetworking() { }

    public static void register(Consumer<FrontierShopFilterPayload> filterSender) {
        sender = Objects.requireNonNull(filterSender, "filterSender");
    }

    public static void sendFilter(FrontierShopFilterPayload payload) {
        sender.accept(payload);
    }
}
