package com.whatwasmissing.cobblemongacha.gui;

import com.whatwasmissing.cobblemongacha.core.GachaBanner;
import com.whatwasmissing.cobblemongacha.core.GachaLedger;
import com.whatwasmissing.cobblemongacha.core.GachaRarity;
import com.whatwasmissing.cobblemongacha.core.GachaService;
import com.whatwasmissing.cobblemongacha.config.GachaConfig;
import com.whatwasmissing.cobblemongacha.network.GachaMenuSyncPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.UUID;
import java.time.Instant;

/** Server-owned backing menu; the client screen renders it as a Cobblemon-style dashboard. */
public final class GachaMenu extends ChestMenu {
    public static final int BUTTON_PREVIOUS_BANNER = 0;
    public static final int BUTTON_NEXT_BANNER = 1;
    public static final int BUTTON_CURRENT_BANNER = 2;
    public static final int BUTTON_OPEN_UPGRADER = 4;
    public static final int BANNER_SLOT = 13;
    public static final int PREVIOUS_SLOT = 18;
    public static final int NEXT_SLOT = 26;
    public static final int OPEN_UPGRADER_SLOT = 27;
    public static final int DRAW_ONE_SLOT = 45;
    public static final int DRAW_TEN_SLOT = 53;
    public static final int HISTORY_START = 35;
    private static final int SIZE = 54;

    private final SimpleContainer gachaInventory;
    private final UUID ownerId;
    private int bannerIndex;
    private List<GachaBanner> syncedBanners = List.of();
    private boolean serverSnapshot;
    private int syncedActiveBannerIndex;
    private long syncedSecondsUntilRotation;
    private long syncedGamblingCooldownSeconds;
    private long syncedServerEpochSeconds;
    private long syncedLocalEpochSeconds;
    private int syncedBannerRotationHours = 1;
    private int syncedTickets;
    private int syncedCapturesPerTicket = 10;
    private int syncedCaptureProgress;
    private int syncedSingleDrawCost = 1;
    private int syncedTenDrawCost = 10;
    private int syncedRarePityDraws = 50;
    private int syncedRarePity;
    private int syncedLegendaryPityDraws = 100;
    private int syncedLegendaryPity;
    private double syncedShinyChance = 0.01;

    public GachaMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(SIZE));
    }

    private GachaMenu(int containerId, Inventory playerInventory, SimpleContainer gachaInventory) {
        super(GachaMenuTypes.GACHA_MENU, containerId, playerInventory, gachaInventory, 6);
        this.gachaInventory = gachaInventory;
        this.ownerId = playerInventory.player.getUUID();
        this.bannerIndex = GachaService.activeBannerIndex();
        populate(playerInventory.player);
    }

    private void populate(Player player) {
        for (int slot = 0; slot < SIZE; slot++) gachaInventory.setItem(slot, pane(Items.GRAY_STAINED_GLASS_PANE));
        gachaInventory.setItem(4, infoStack(Items.AMETHYST_SHARD,
                "Gacha Tickets: " + GachaService.tickets(player.getUUID())
                        + " · next ticket in " + capturesUntilNextTicket() + " Pokémon",
                ChatFormatting.LIGHT_PURPLE));
        GachaBanner banner = GachaService.banner(bannerIndex);
        boolean active = GachaService.isActiveBanner(bannerIndex);
        gachaInventory.setItem(BANNER_SLOT, infoStack(Items.NETHER_STAR,
                banner.title + " · " + (active ? "active this hour" : "preview only")
                        + " · group " + (bannerIndex + 1) + "/" + GachaService.bannerCount(),
                active ? ChatFormatting.AQUA : ChatFormatting.GOLD));
        gachaInventory.setItem(PREVIOUS_SLOT, controlStack(Items.ARROW, "Previous banner", bannerIndex > 0));
        gachaInventory.setItem(NEXT_SLOT, controlStack(Items.ARROW, "Next banner", bannerIndex + 1 < GachaService.bannerCount()));
        gachaInventory.setItem(OPEN_UPGRADER_SLOT, infoStack(Items.COMPASS, "Open Item Upgrader", ChatFormatting.GOLD));
        gachaInventory.setItem(DRAW_ONE_SLOT, active
                ? infoStack(Items.ENDER_EYE, "Draw 1 · " + singleDrawCost() + " ticket", ChatFormatting.GREEN)
                : controlStack(Items.ENDER_EYE, "Draw 1 · active banner only", false));
        gachaInventory.setItem(DRAW_TEN_SLOT, active
                ? infoStack(Items.NETHER_STAR, "Draw 10 · " + tenDrawCost() + " tickets", ChatFormatting.GOLD)
                : controlStack(Items.NETHER_STAR, "Draw 10 · active banner only", false));

        List<GachaLedger.HistoryEntry> history = GachaService.history(player.getUUID());
        for (int index = 0; index < 8 && index < history.size(); index++) {
            GachaLedger.HistoryEntry entry = history.get(index);
            GachaRarity rarity = rarity(entry.rarity);
            gachaInventory.setItem(HISTORY_START + index, infoStack(
                    historyIcon(rarity),
                    entry.label, rarity.formatting()));
        }
    }

    private static Item historyIcon(GachaRarity rarity) {
        return switch (rarity) {
            case COMMON -> Items.PAPER;
            case RARE -> Items.EMERALD;
            case EPIC -> Items.AMETHYST_SHARD;
            case LEGENDARY -> Items.DIAMOND;
            case MYTHIC -> Items.NETHER_STAR;
        };
    }

    private static GachaRarity rarity(String value) {
        try { return GachaRarity.valueOf(value); }
        catch (IllegalArgumentException | NullPointerException ignored) { return GachaRarity.COMMON; }
    }

    private static ItemStack pane(Item item) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(" "));
        return stack;
    }

    private static ItemStack infoStack(Item item, String label, ChatFormatting formatting) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(label).withStyle(formatting));
        return stack;
    }

    private static ItemStack controlStack(Item item, String label, boolean enabled) {
        return infoStack(enabled ? item : Items.GRAY_DYE, label + (enabled ? " · click" : " · unavailable"),
                enabled ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY);
    }

    public int bannerIndex() { return bannerIndex; }
    public boolean hasServerSnapshot() { return serverSnapshot; }
    public GachaBanner displayBanner(int index) {
        List<GachaBanner> banners = displayBanners();
        if (banners.isEmpty()) {
            return serverSnapshot
                    ? new GachaBanner("empty", "No banners", "No usable banners are configured.", new java.util.ArrayList<>())
                    : GachaService.banner(index);
        }
        int safe = Math.max(0, Math.min(index, banners.size() - 1));
        return banners.get(safe);
    }
    public int bannerCount() { return displayBanners().size(); }
    public boolean isActiveBanner() { return bannerCount() > 0 && bannerIndex == activeBannerIndex(); }
    public int activeBannerIndex() {
        if (!serverSnapshot) return GachaService.activeBannerIndex();
        int count = bannerCount();
        if (count <= 0) return 0;
        long periodSeconds = Math.max(1L, syncedBannerRotationHours) * 3600L;
        long estimatedServerNow = syncedServerEpochSeconds
                + (Instant.now().getEpochSecond() - syncedLocalEpochSeconds);
        return (int) Math.floorMod(Math.floorDiv(estimatedServerNow, periodSeconds), count);
    }
    public long secondsUntilRotation() {
        if (!serverSnapshot) return GachaService.secondsUntilBannerRotation();
        long periodSeconds = Math.max(1L, syncedBannerRotationHours) * 3600L;
        long estimatedServerNow = syncedServerEpochSeconds
                + (Instant.now().getEpochSecond() - syncedLocalEpochSeconds);
        return Math.max(1L, periodSeconds - Math.floorMod(estimatedServerNow, periodSeconds));
    }
    public long gamblingCooldownSeconds() {
        if (!serverSnapshot) return 0L;
        long elapsed = Math.max(0L, Instant.now().getEpochSecond() - syncedLocalEpochSeconds);
        return Math.max(0L, syncedGamblingCooldownSeconds - elapsed);
    }
    public int captureProgress() {
        return serverSnapshot ? syncedCaptureProgress : GachaService.captureProgress(playerId());
    }
    public int capturesUntilNextTicket() {
        int capturesPerTicket = capturesPerTicket();
        return Math.max(1, capturesPerTicket - Math.floorMod(captureProgress(), capturesPerTicket));
    }
    public int tickets() { return serverSnapshot ? syncedTickets : GachaService.tickets(playerId()); }
    public int capturesPerTicket() {
        GachaConfig config = GachaService.config();
        return serverSnapshot ? syncedCapturesPerTicket : config == null ? 10 : config.capturesPerTicket;
    }
    public int singleDrawCost() {
        GachaConfig config = GachaService.config();
        return serverSnapshot ? syncedSingleDrawCost : config == null ? 1 : config.singleDrawCost;
    }
    public int tenDrawCost() {
        GachaConfig config = GachaService.config();
        return serverSnapshot ? syncedTenDrawCost : config == null ? 10 : config.tenDrawCost;
    }
    public int rarePityDraws() {
        GachaConfig config = GachaService.config();
        return serverSnapshot ? syncedRarePityDraws : config == null ? 50 : config.rarePityDraws;
    }
    public int rarePity() { return serverSnapshot ? syncedRarePity : GachaService.pity(playerId()); }
    public int legendaryPityDraws() {
        GachaConfig config = GachaService.config();
        return serverSnapshot ? syncedLegendaryPityDraws : config == null ? 100 : config.legendaryPityDraws;
    }
    public int legendaryPity() { return serverSnapshot ? syncedLegendaryPity : GachaService.legendaryPity(playerId()); }
    public double shinyChance() {
        GachaConfig config = GachaService.config();
        return serverSnapshot ? syncedShinyChance : config == null ? 0.01 : config.shinyChance;
    }

    private List<GachaBanner> displayBanners() {
        if (serverSnapshot) return syncedBanners;
        GachaConfig config = GachaService.config();
        return config == null || config.banners == null ? List.of() : config.banners;
    }

    private UUID playerId() {
        return ownerId;
    }

    public void syncToClient(ServerPlayer player) {
        if (player.containerMenu != this) return;
        player.connection.send(new ClientboundCustomPayloadPacket(
                GachaMenuSyncPayload.forDrawMenu(containerId, bannerIndex, player.getUUID())));
    }

    public void refreshForServer(ServerPlayer player) {
        populate(player);
        broadcastChanges();
        syncToClient(player);
    }

    public void applyServerSnapshot(GachaMenuSyncPayload payload) {
        if (!payload.drawMenu() || payload.menuId() != containerId) return;
        syncedBanners = payload.toBanners();
        serverSnapshot = true;
        bannerIndex = Math.max(0, Math.min(payload.bannerIndex(), Math.max(0, syncedBanners.size() - 1)));
        syncedActiveBannerIndex = payload.activeBannerIndex();
        syncedSecondsUntilRotation = payload.secondsUntilRotation();
        syncedGamblingCooldownSeconds = Math.max(0L, payload.gamblingCooldownSeconds());
        syncedServerEpochSeconds = payload.serverEpochSeconds();
        syncedLocalEpochSeconds = Instant.now().getEpochSecond();
        syncedBannerRotationHours = Math.max(1, payload.bannerRotationHours());
        syncedTickets = payload.tickets();
        syncedCapturesPerTicket = Math.max(1, payload.capturesPerTicket());
        syncedCaptureProgress = Math.max(0, payload.captureProgress());
        syncedSingleDrawCost = Math.max(1, payload.singleDrawCost());
        syncedTenDrawCost = Math.max(syncedSingleDrawCost, payload.tenDrawCost());
        syncedRarePityDraws = Math.max(1, payload.rarePityDraws());
        syncedRarePity = Math.max(0, payload.rarePity());
        syncedLegendaryPityDraws = Math.max(syncedRarePityDraws, payload.legendaryPityDraws());
        syncedLegendaryPity = Math.max(0, payload.legendaryPity());
        syncedShinyChance = Math.max(0.0, Math.min(1.0, payload.shinyChance()));
    }

    public boolean canGoPrevious() { return bannerIndex > 0; }
    public boolean canGoNext() { return bannerIndex + 1 < bannerCount(); }

    public void clientBannerButton(int buttonId) {
        if (buttonId == BUTTON_PREVIOUS_BANNER && canGoPrevious()) bannerIndex--;
        if (buttonId == BUTTON_NEXT_BANNER && canGoNext()) bannerIndex++;
        if (buttonId == BUTTON_CURRENT_BANNER && bannerCount() > 0) bannerIndex = activeBannerIndex();
    }

    private boolean changeBanner(Player player, int delta) {
        int target = bannerIndex + delta;
        return setBanner(player, target);
    }

    private boolean setBanner(Player player, int target) {
        if (target < 0 || target >= bannerCount()) {
            if (player instanceof ServerPlayer serverPlayer) syncToClient(serverPlayer);
            return false;
        }
        bannerIndex = target;
        if (player instanceof ServerPlayer serverPlayer) {
            populate(serverPlayer);
            broadcastChanges();
            syncToClient(serverPlayer);
        }
        return true;
    }

    private boolean selectCurrentBanner(Player player) {
        return setBanner(player, activeBannerIndex());
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == BUTTON_OPEN_UPGRADER) {
            if (player instanceof ServerPlayer serverPlayer) GachaService.openUpgradeMenu(serverPlayer);
            return true;
        }
        if (buttonId == BUTTON_PREVIOUS_BANNER) return changeBanner(player, -1);
        if (buttonId == BUTTON_NEXT_BANNER) return changeBanner(player, 1);
        if (buttonId == BUTTON_CURRENT_BANNER) return selectCurrentBanner(player);
        return false;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < SIZE) {
            if (clickType == ClickType.PICKUP && button == 0 && player instanceof ServerPlayer serverPlayer) {
                if (slotId == PREVIOUS_SLOT && canGoPrevious()) changeBanner(player, -1);
                else if (slotId == NEXT_SLOT && canGoNext()) changeBanner(player, 1);
                else if (slotId == OPEN_UPGRADER_SLOT) GachaService.openUpgradeMenu(serverPlayer);
                else if (slotId == DRAW_ONE_SLOT) {
                    if (!GachaService.isActiveBanner(bannerIndex)) {
                        serverPlayer.sendSystemMessage(Component.literal("Only the active hourly banner can be drawn.")
                                .withStyle(ChatFormatting.YELLOW));
                    } else {
                        GachaService.draw(serverPlayer, 1, bannerIndex);
                    }
                    populate(serverPlayer);
                    broadcastChanges();
                    syncToClient(serverPlayer);
                } else if (slotId == DRAW_TEN_SLOT) {
                    if (!GachaService.isActiveBanner(bannerIndex)) {
                        serverPlayer.sendSystemMessage(Component.literal("Only the active hourly banner can be drawn.")
                                .withStyle(ChatFormatting.YELLOW));
                    } else {
                        GachaService.draw(serverPlayer, 10, bannerIndex);
                    }
                    populate(serverPlayer);
                    broadcastChanges();
                    syncToClient(serverPlayer);
                }
            }
            return;
        }
        // Do not pass player-inventory clicks through while the custom
        // dashboard is open; the screen is not a storage container.
        if (slotId >= SIZE) return;
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotId) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player player) { return !player.isRemoved(); }

    @Override
    public void removed(Player player) {
        super.removed(player);
        gachaInventory.clearContent();
    }
}
