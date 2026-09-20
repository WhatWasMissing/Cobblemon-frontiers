package com.whatwasmissing.spawnannouncements.gui;

import com.whatwasmissing.spawnannouncements.core.AnnouncementService;
import com.whatwasmissing.spawnannouncements.core.FrontierLedger;
import com.whatwasmissing.spawnannouncements.network.FrontierShopStatePayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/** Server-owned data/menu backing the client-side Frontier Intelligence screen. */
public final class FrontierShopMenu extends ChestMenu {
    /** Button ids used by the custom Cobblemon-style screen. */
    public static final int BUTTON_PREVIOUS_PAGE = 0;
    public static final int BUTTON_NEXT_PAGE = 1;

    private static final int SHOP_SIZE = 54;
    private static final int[] PRODUCT_SLOTS = {
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38
    };
    private static final int[] SIGNAL_SLOTS = {18, 19, 20, 21, 22, 23, 24, 25, 26};

    private final SimpleContainer shopInventory;
    private int page;
    private String searchQuery = "";
    private FrontierShopCatalog.Category category = FrontierShopCatalog.Category.ALL;
    private FrontierShopCatalog.SortMode sortMode = FrontierShopCatalog.SortMode.FEATURED;

    public FrontierShopMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(SHOP_SIZE));
    }

    private FrontierShopMenu(int containerId, Inventory playerInventory, SimpleContainer shopInventory) {
        super(FrontierMenuTypes.FRONTIER_DASHBOARD, containerId, playerInventory, shopInventory, 6);
        this.shopInventory = shopInventory;
        populate(playerInventory.player);
    }

    private void populate(Player player) {
        for (int slot = 0; slot < SHOP_SIZE; slot++) {
            shopInventory.setItem(slot, pane(Items.GRAY_STAINED_GLASS_PANE));
        }
        shopInventory.setItem(4, balanceStack(player));
        shopInventory.setItem(13, surveyStack(player));
        shopInventory.setItem(14, challengeStack(player));
        shopInventory.setItem(15, expeditionStack(player));
        shopInventory.setItem(16, communityStack());
        shopInventory.setItem(17, fieldGuideStack(player));

        List<FrontierLedger.SignalRecord> signals = AnnouncementService.recentSignals();
        for (int index = 0; index < signals.size() && index < SIGNAL_SLOTS.length; index++) {
            shopInventory.setItem(SIGNAL_SLOTS[index], signalStack(signals.get(index)));
        }

        List<FrontierShopProduct> products = currentProducts();
        int pageCount = pageCount();
        page = Math.min(page, pageCount - 1);
        int start = page * FrontierShopCatalog.PRODUCTS_PER_PAGE;
        for (int index = 0; index < PRODUCT_SLOTS.length && start + index < products.size(); index++) {
            shopInventory.setItem(PRODUCT_SLOTS[index], products.get(start + index).displayStack());
        }
        shopInventory.setItem(49, pageControl(Items.ARROW, "Previous page", page > 0));
        shopInventory.setItem(53, pageControl(Items.ARROW, "Next page", page + 1 < pageCount));
    }

    private static ItemStack pane(net.minecraft.world.item.Item item) {
        ItemStack stack = new ItemStack(item);
        return named(stack, Component.literal(" "));
    }

    private static ItemStack balanceStack(Player player) {
        ItemStack stack = new ItemStack(Items.BOOK);
        return named(stack, Component.literal("Research balance: " + AnnouncementService.points(player.getUUID())
                        + " · lifetime: " + AnnouncementService.totalEarned(player.getUUID())
                        + " · next milestone: " + milestoneText(player))
                .withStyle(ChatFormatting.AQUA));
    }

    private static String milestoneText(Player player) {
        int next = AnnouncementService.nextMilestone(player.getUUID());
        return next < 0 ? "complete" : Integer.toString(next);
    }

    private static ItemStack surveyStack(Player player) {
        ItemStack stack = new ItemStack(Items.COMPASS);
        int regionalPoints = AnnouncementService.regionResearchPoints(player.getUUID()).values().stream()
                .mapToInt(Integer::intValue).sum();
        return named(stack, Component.literal("Survey streak: " + AnnouncementService.currentStreak(player.getUUID())
                        + " · best: " + AnnouncementService.bestStreak(player.getUUID())
                        + " · regional research: " + regionalPoints)
                .withStyle(ChatFormatting.GREEN));
    }

    private static ItemStack challengeStack(Player player) {
        FrontierLedger.DailyChallengeProgress challenge = AnnouncementService.dailyChallenge(player.getUUID());
        ItemStack stack = new ItemStack(Items.CLOCK);
        String progress = challenge.completed() ? "complete" : challenge.captures() + "/" + challenge.goal();
        return named(stack, Component.literal("Daily captures: " + progress + " · +"
                        + challenge.reward() + " research")
                .withStyle(ChatFormatting.GOLD));
    }

    private static ItemStack expeditionStack(Player player) {
        List<FrontierLedger.ExpeditionProgress> expeditions = AnnouncementService.expeditions(player.getUUID());
        ItemStack stack = new ItemStack(Items.MAP);
        long completed = expeditions.stream().filter(FrontierLedger.ExpeditionProgress::completed).count();
        return named(stack, Component.literal("Expedition contracts: " + completed + "/" + expeditions.size()
                        + " complete · /spawnannounce expeditions")
                .withStyle(ChatFormatting.GOLD));
    }

    private static ItemStack communityStack() {
        FrontierLedger.CommunityEventProgress event = AnnouncementService.communityEvent();
        ItemStack stack = new ItemStack(Items.GREEN_DYE);
        return named(stack, Component.literal("Community: " + event.title() + " · " + event.progress() + "/" + event.goal()
                        + " · /spawnannounce community")
                .withStyle(ChatFormatting.GREEN));
    }

    private static ItemStack fieldGuideStack(Player player) {
        ItemStack stack = new ItemStack(Items.WRITABLE_BOOK);
        return named(stack, Component.literal("Private Field Guide: "
                        + AnnouncementService.fieldGuide(player.getUUID()).size()
                        + " species · /spawnannounce guide")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    private static ItemStack signalStack(FrontierLedger.SignalRecord signal) {
        ItemStack stack = new ItemStack("active".equals(signal.status) ? Items.PAPER : Items.GRAY_DYE);
        return named(stack, Component.literal(signal.kindEnum().displayName() + " signal · " + signal.region
                        + " · " + signal.status)
                .withStyle("active".equals(signal.status) ? ChatFormatting.GOLD : ChatFormatting.GRAY));
    }

    private static ItemStack pageControl(net.minecraft.world.item.Item item, String label, boolean enabled) {
        ItemStack stack = new ItemStack(enabled ? item : Items.GRAY_DYE);
        return named(stack, Component.literal(label + (enabled ? " · click" : " · unavailable"))
                .withStyle(enabled ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY));
    }

    private static ItemStack named(ItemStack stack, Component name) {
        stack.set(DataComponents.CUSTOM_NAME, name);
        return stack;
    }

    private int productAt(int slotId) {
        for (int index = 0; index < PRODUCT_SLOTS.length; index++) {
            if (PRODUCT_SLOTS[index] == slotId) return page * FrontierShopCatalog.PRODUCTS_PER_PAGE + index;
        }
        return -1;
    }

    public int page() {
        return page;
    }

    public int pageCount() {
        return Math.max(1, (currentProducts().size() + FrontierShopCatalog.PRODUCTS_PER_PAGE - 1)
                / FrontierShopCatalog.PRODUCTS_PER_PAGE);
    }

    public String searchQuery() { return searchQuery; }
    public FrontierShopCatalog.Category category() { return category; }
    public FrontierShopCatalog.SortMode sortMode() { return sortMode; }
    public List<FrontierShopProduct> currentProducts() {
        return FrontierShopCatalog.filteredProducts(searchQuery, category, sortMode);
    }
    public FrontierShopProduct productAtPageIndex(int index) {
        int global = page * FrontierShopCatalog.PRODUCTS_PER_PAGE + index;
        List<FrontierShopProduct> products = currentProducts();
        return global >= 0 && global < products.size() ? products.get(global) : null;
    }

    public void applySearch(ServerPlayer player, String query) {
        applyFilter(player, query, category, sortMode);
    }

    public void applySort(ServerPlayer player, FrontierShopCatalog.SortMode mode) {
        applyFilter(player, searchQuery, category, mode);
    }

    public void applyCategory(ServerPlayer player, FrontierShopCatalog.Category selectedCategory) {
        applyFilter(player, searchQuery, selectedCategory, sortMode);
    }

    public void applyFilter(ServerPlayer player, String query, FrontierShopCatalog.Category selectedCategory,
                            FrontierShopCatalog.SortMode selectedSort) {
        searchQuery = sanitiseQuery(query);
        category = selectedCategory == null ? FrontierShopCatalog.Category.ALL : selectedCategory;
        sortMode = selectedSort == null ? FrontierShopCatalog.SortMode.FEATURED : selectedSort;
        page = 0;
        populate(player);
        broadcastChanges();
        syncToClient(player);
    }

    public void refreshForServer(ServerPlayer player) {
        if (player.containerMenu != this) return;
        populate(player);
        broadcastChanges();
        syncToClient(player);
    }

    public void syncToClient(ServerPlayer player) {
        if (player.containerMenu != this) return;
        player.connection.send(new ClientboundCustomPayloadPacket(new FrontierShopStatePayload(
                containerId, page, searchQuery, category.id(), sortMode.id())));
    }

    public void applyServerState(FrontierShopStatePayload payload) {
        if (payload.menuId() != containerId) return;
        String newQuery = sanitiseQuery(payload.searchQuery());
        FrontierShopCatalog.Category newCategory = FrontierShopCatalog.Category.parse(payload.category());
        FrontierShopCatalog.SortMode newSort = FrontierShopCatalog.SortMode.parse(payload.sortMode());
        if (!searchQuery.equals(newQuery) || newCategory != category || newSort != sortMode) {
            searchQuery = newQuery;
            category = newCategory == null ? FrontierShopCatalog.Category.ALL : newCategory;
            sortMode = newSort == null ? FrontierShopCatalog.SortMode.FEATURED : newSort;
        }
        page = Math.max(0, Math.min(payload.page(), pageCount() - 1));
    }

    public boolean canGoPrevious() {
        return page > 0;
    }

    public boolean canGoNext() {
        return page + 1 < pageCount();
    }

    public int productSlotAt(int pageIndex) {
        return pageIndex >= 0 && pageIndex < PRODUCT_SLOTS.length ? PRODUCT_SLOTS[pageIndex] : -1;
    }

    /** Updates the client cursor before the server's slot synchronization arrives. */
    public void clientPageButton(int buttonId) {
        if (buttonId == BUTTON_PREVIOUS_PAGE && canGoPrevious()) page--;
        if (buttonId == BUTTON_NEXT_PAGE && canGoNext()) page++;
    }

    private static String sanitiseQuery(String query) {
        String value = query == null ? "" : query.trim();
        return value.length() > 64 ? value.substring(0, 64) : value;
    }

    private boolean changePage(Player player, int delta) {
        int target = page + delta;
        if (target < 0 || target >= pageCount()) return false;
        page = target;
        // The server owns the catalogue contents. The client only updates its
        // page cursor optimistically and receives the new stacks through the
        // normal container synchronization packet.
        if (player instanceof ServerPlayer serverPlayer) {
            populate(serverPlayer);
            broadcastChanges();
            syncToClient(serverPlayer);
        }
        return true;
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == BUTTON_PREVIOUS_PAGE) return changePage(player, -1);
        if (buttonId == BUTTON_NEXT_PAGE) return changePage(player, 1);
        return false;
    }

    private void purchase(ServerPlayer player, int productIndex) {
        List<FrontierShopProduct> products = currentProducts();
        if (productIndex < 0 || productIndex >= products.size()) return;
        purchase(player, products.get(productIndex));
    }

    public static boolean purchase(ServerPlayer player, FrontierShopProduct product) {
        if (product == null) return false;
        if (!AnnouncementService.spendPoints(player.getUUID(), product.cost())) {
            player.sendSystemMessage(Component.literal("You need " + product.cost() + " spendable research points for that item.")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        ItemStack reward = product.rewardStack();
        if (!player.getInventory().add(reward)) {
            player.drop(reward, false);
        }
        player.sendSystemMessage(Component.literal("Purchased " + product.baseName() + " for " + product.cost() + " research points.")
                .withStyle(ChatFormatting.GREEN));
        if (player.containerMenu instanceof FrontierShopMenu menu) menu.refreshForServer(player);
        return true;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        // Every top-inventory interaction is consumed by the shop. This prevents
        // display items being removed, shift-clicked, dragged, or swapped away.
        if (slotId >= 0 && slotId < SHOP_SIZE) {
            if (clickType == ClickType.PICKUP) {
                if (slotId == 49 && page > 0) {
                    changePage(player, -1);
                    return;
                }
                if (slotId == 53 && page + 1 < pageCount()) {
                    changePage(player, 1);
                    return;
                }
                if (player instanceof ServerPlayer serverPlayer) {
                    int productIndex = productAt(slotId);
                    if (productIndex >= 0 && productIndex < currentProducts().size()) {
                        purchase(serverPlayer, productIndex);
                    }
                }
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotId) {
        // A research shop never accepts or moves ordinary inventory items.
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return !player.isRemoved();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        shopInventory.clearContent();
    }
}
