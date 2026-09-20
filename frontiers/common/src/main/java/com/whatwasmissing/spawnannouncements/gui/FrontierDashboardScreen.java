package com.whatwasmissing.spawnannouncements.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.whatwasmissing.spawnannouncements.network.FrontierShopClientNetworking;
import com.whatwasmissing.spawnannouncements.network.FrontierShopFilterPayload;

/**
 * Cobblemon-inspired presentation for the Frontier dashboard. It deliberately
 * renders its own cards and tabs while retaining the server menu underneath for
 * authoritative slots, purchases, and page changes.
 */
public final class FrontierDashboardScreen extends AbstractContainerScreen<FrontierShopMenu> {
    private static final int PANEL_WIDTH = 780;
    private static final int PANEL_HEIGHT = 474;
    private static final int CARD_WIDTH = 177;
    private static final int CARD_HEIGHT = 84;
    private static final int CARD_GAP = 8;
    private static final int GRID_COLUMNS = 4;
    private static final int EXCHANGE_Y = 145;
    private static final int PAGE_Y = 438;
    private static final int PAGE_BUTTON_WIDTH = 40;
    private static final int CATEGORY_Y = 94;
    private static final int CATEGORY_WIDTH = 68;
    private static final int CATEGORY_GAP = 2;

    private static final int NAVY = 0xF20E1825;
    private static final int PANEL = 0xFF172634;
    private static final int PANEL_LIGHT = 0xFF213747;
    private static final int PANEL_DARK = 0xFF101A26;
    private static final int TEAL = 0xFF6ED3D0;
    private static final int TEXT = 0xFFF1F5F7;
    private static final int MUTED = 0xFFAABBC5;
    private static final int GOLD = 0xFFF4C86A;

    private boolean exchangeTab = true;
    private EditBox searchBox;

    public FrontierDashboardScreen(FrontierShopMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = PANEL_WIDTH;
        imageHeight = PANEL_HEIGHT;
        inventoryLabelY = imageHeight + 20;
    }

    @Override
    protected void init() {
        super.init();
        // Coordinates below are in a virtual 780x474 design space. render()
        // scales that space down for high Minecraft GUI scales so the dashboard
        // remains usable instead of clipping off-screen.
        leftPos = 0;
        topPos = 0;

        searchBox = new EditBox(font, 0, 0, 1, 1, Component.literal("Search exchange"));
        searchBox.setMaxLength(64);
        searchBox.setBordered(false);
        searchBox.setTextColor(TEXT);
        searchBox.setHint(Component.literal("item name or id").withStyle(ChatFormatting.DARK_GRAY));
        searchBox.visible = exchangeTab;
        addRenderableWidget(searchBox);
        positionSearchBox();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // The actual UI is rendered in render so the normal chest background and
        // vanilla slot grid never leak through the Cobblemon-style surface.
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        float scale = uiScale();
        double designMouseX = toDesignX(mouseX, scale);
        double designMouseY = toDesignY(mouseY, scale);

        graphics.pose().pushPose();
        graphics.pose().translate(width / 2.0f, height / 2.0f, 0.0f);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.pose().translate(-PANEL_WIDTH / 2.0f, -PANEL_HEIGHT / 2.0f, 0.0f);
        drawShell(graphics, designMouseX, designMouseY);
        if (exchangeTab) renderExchange(graphics, designMouseX, designMouseY);
        else renderOverview(graphics, designMouseX, designMouseY);
        if (isInside(designMouseX, designMouseY, PANEL_WIDTH - 36, 13, 24, 24)) {
            graphics.renderTooltip(font, Component.literal("Close"), (int) designMouseX, (int) designMouseY);
        }
        graphics.pose().popPose();

        if (exchangeTab && searchBox != null) {
            if (!searchBox.isFocused() && !searchBox.getValue().equals(menu.searchQuery())) {
                searchBox.setValue(menu.searchQuery());
            }
            searchBox.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    private void drawShell(GuiGraphics graphics, double mouseX, double mouseY) {
        graphics.fill(-2000, -2000, 2000, 2000, 0x99000000);
        graphics.fill(-4, -4, PANEL_WIDTH + 4, PANEL_HEIGHT + 4, 0xFF0A1119);
        graphics.fill(0, 0, PANEL_WIDTH, PANEL_HEIGHT, NAVY);
        graphics.fill(0, 0, PANEL_WIDTH, 64, PANEL_DARK);
        graphics.fill(0, 62, PANEL_WIDTH, 64, TEAL);

        graphics.drawString(font, Component.literal("FRONTIER INTELLIGENCE"), 24, 15, TEXT);
        graphics.drawString(font, Component.literal("×"), PANEL_WIDTH - 30, 15,
                isInside(mouseX, mouseY, PANEL_WIDTH - 36, 13, 24, 24) ? TEAL : MUTED);

        tab(graphics, 24, 76, 112, "Overview", !exchangeTab, mouseX, mouseY);
        tab(graphics, 144, 76, 112, "Exchange", exchangeTab, mouseX, mouseY);
    }

    private void renderOverview(GuiGraphics graphics, double mouseX, double mouseY) {
        card(graphics, 24, 118, 440, 334);
        card(graphics, 480, 118, 276, 334);
        graphics.drawString(font, Component.literal("LIVE SIGNALS"), 42, 137, TEAL);
        graphics.drawString(font, Component.literal("Anonymous sightings near the frontier"), 42, 153, MUTED);

        boolean hasSignals = false;
        for (int index = 0; index < 9; index++) {
            int slot = 18 + index;
            int x = 42 + (index % 3) * 132;
            int y = 184 + (index / 3) * 78;
            ItemStack stack = menu.getSlot(slot).getItem();
            graphics.fill(x, y, x + 122, y + 62, PANEL_DARK);
            if (!isPane(stack)) {
                hasSignals = true;
                graphics.renderItem(stack, x + 8, y + 8);
                graphics.drawString(font, Component.literal(shorten(stack.getHoverName().getString(), 17)), x + 36, y + 12, TEXT);
            }
        }
        if (!hasSignals) graphics.drawCenteredString(font, Component.literal("No recent sightings"), 244, 292, MUTED);

        graphics.drawString(font, Component.literal("FIELD RESEARCH"), 500, 137, TEAL);
        renderInfoStack(graphics, menu.getSlot(4).getItem(), 500, 186);
        renderInfoStack(graphics, menu.getSlot(13).getItem(), 500, 252);
        ItemStack challenge = menu.getSlot(14).getItem();
        graphics.drawString(font, Component.literal(shorten(challenge.getHoverName().getString(), 35)), 500, 319, GOLD);
        graphics.fill(500, 338, 736, 392, PANEL_LIGHT);
        graphics.drawCenteredString(font, Component.literal("Open the exchange"), 618, 350, TEXT);
        if (isInside(mouseX, mouseY, 500, 338, 236, 54)) {
            graphics.fill(500, 338, 736, 392, 0x223EC7C2);
        }
    }

    private void renderExchange(GuiGraphics graphics, double mouseX, double mouseY) {
        renderCategoryTabs(graphics, mouseX, mouseY);
        renderBalanceChip(graphics);
        searchControl(graphics, mouseX, mouseY);

        ItemStack hovered = ItemStack.EMPTY;
        for (int index = 0; index < FrontierShopCatalog.PRODUCTS_PER_PAGE; index++) {
            int column = index % GRID_COLUMNS;
            int row = index / GRID_COLUMNS;
            int x = 24 + column * (CARD_WIDTH + CARD_GAP);
            int y = EXCHANGE_Y + row * (CARD_HEIGHT + CARD_GAP);
            boolean hoveredCard = isInside(mouseX, mouseY, x, y, CARD_WIDTH, CARD_HEIGHT);
            graphics.fill(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, hoveredCard ? PANEL_LIGHT : PANEL);
            graphics.fill(x, y, x + 3, y + CARD_HEIGHT, hoveredCard ? TEAL : 0xFF2D5668);

            int slot = menu.productSlotAt(index);
            ItemStack stack = slot < 0 ? ItemStack.EMPTY : menu.getSlot(slot).getItem();
            if (stack.isEmpty() || isPane(stack)) continue;
            FrontierShopProduct product = menu.productAtPageIndex(index);
            if (product == null) continue;
            graphics.renderItem(stack, x + 14, y + 16);
            graphics.drawString(font, Component.literal(shorten(product.baseName(), 20)), x + 48, y + 14, TEXT);
            graphics.drawString(font, Component.literal(product.amount() + " item" + (product.amount() == 1 ? "" : "s")),
                    x + 48, y + 31, MUTED);
            graphics.drawString(font, Component.literal(product.cost() + " research pts"), x + 48, y + 48, GOLD);
            graphics.drawString(font, Component.literal("BUY"), x + CARD_WIDTH - 35, y + CARD_HEIGHT - 18,
                    hoveredCard ? TEAL : MUTED);
            if (hoveredCard) hovered = stack;
        }

        pageButton(graphics, 24, PAGE_Y, PAGE_BUTTON_WIDTH, "‹", menu.canGoPrevious(), mouseX, mouseY);
        pageButton(graphics, PANEL_WIDTH - 64, PAGE_Y, PAGE_BUTTON_WIDTH, "›", menu.canGoNext(), mouseX, mouseY);
        graphics.drawCenteredString(font, Component.literal("Page " + (menu.page() + 1) + " / " + menu.pageCount()),
                PANEL_WIDTH / 2, PAGE_Y + 9, MUTED);
        if (!hovered.isEmpty()) graphics.renderTooltip(font, hovered, (int) mouseX, (int) mouseY);
    }

    private void searchControl(GuiGraphics graphics, double mouseX, double mouseY) {
        boolean searchHover = isInside(mouseX, mouseY, 24, 122, 250, 18);
        boolean clearHover = isInside(mouseX, mouseY, 236, 122, 38, 18);
        boolean sortHover = isInside(mouseX, mouseY, 284, 122, 150, 18);
        graphics.fill(24, 122, 274, 140, searchHover ? PANEL_LIGHT : PANEL_DARK);
        graphics.fill(236, 122, 274, 140, clearHover ? 0xFF2B4C5B : PANEL_DARK);
        graphics.fill(284, 122, 434, 140, sortHover ? PANEL_LIGHT : PANEL_DARK);
        graphics.drawString(font, Component.literal("Search"), 31, 127, searchHover ? TEAL : MUTED);
        graphics.drawCenteredString(font, Component.literal("×"), 255, 127,
                menu.searchQuery().isBlank() ? 0xFF53656F : (clearHover ? TEAL : MUTED));
        graphics.drawString(font, Component.literal("Sort: " + menu.sortMode().label()), 291, 127, sortHover ? TEAL : MUTED);
    }

    private void renderCategoryTabs(GuiGraphics graphics, double mouseX, double mouseY) {
        FrontierShopCatalog.Category[] categories = FrontierShopCatalog.Category.values();
        for (int index = 0; index < categories.length; index++) {
            int x = 24 + index * (CATEGORY_WIDTH + CATEGORY_GAP);
            FrontierShopCatalog.Category category = categories[index];
            boolean active = category == menu.category();
            boolean hovered = isInside(mouseX, mouseY, x, CATEGORY_Y, CATEGORY_WIDTH, 22);
            graphics.fill(x, CATEGORY_Y, x + CATEGORY_WIDTH, CATEGORY_Y + 22,
                    active ? PANEL_LIGHT : (hovered ? 0xFF1C3040 : PANEL_DARK));
            if (active) graphics.fill(x, CATEGORY_Y + 20, x + CATEGORY_WIDTH, CATEGORY_Y + 22, TEAL);
            String label = category == FrontierShopCatalog.Category.MEGA_SHOWDOWN ? "Mega" : category.label();
            graphics.drawCenteredString(font, Component.literal(label), x + CATEGORY_WIDTH / 2,
                    CATEGORY_Y + 7, active ? TEXT : MUTED);
        }
    }

    private void submitSearch() {
        if (searchBox == null) return;
        sendFilter(searchBox.getValue(), menu.category(), menu.sortMode());
    }

    private void sendFilter(String query, FrontierShopCatalog.Category category,
                            FrontierShopCatalog.SortMode sortMode) {
        FrontierShopClientNetworking.sendFilter(new FrontierShopFilterPayload(
                menu.containerId, query == null ? "" : query, category.id(), sortMode.id()));
    }

    private String activeSearchQuery() {
        return searchBox == null ? menu.searchQuery() : searchBox.getValue();
    }

    private void positionSearchBox() {
        if (searchBox == null) return;
        float scale = uiScale();
        int panelLeft = Math.round(width / 2.0f - PANEL_WIDTH * scale / 2.0f);
        int panelTop = Math.round(height / 2.0f - PANEL_HEIGHT * scale / 2.0f);
        searchBox.setX(Math.round(panelLeft + 74 * scale));
        searchBox.setY(Math.round(panelTop + 122 * scale));
        searchBox.setWidth(Math.max(48, Math.round(156 * scale)));
        searchBox.setHeight(Math.max(10, Math.round(18 * scale)));
    }

    private void renderBalanceChip(GuiGraphics graphics) {
        ItemStack balance = menu.getSlot(4).getItem();
        graphics.fill(544, 104, 756, 136, PANEL_LIGHT);
        if (!balance.isEmpty()) graphics.renderItem(balance, 552, 112);
        String label = balance.isEmpty() ? "Syncing balance…" : balance.getHoverName().getString();
        graphics.drawString(font, Component.literal(shorten(label, 31)), 578, 115, TEXT);
    }

    private void renderInfoStack(GuiGraphics graphics, ItemStack stack, int x, int y) {
        graphics.fill(x, y, x + 236, y + 52, PANEL_LIGHT);
        if (!stack.isEmpty()) graphics.renderItem(stack, x + 10, y + 10);
        graphics.drawString(font, Component.literal(shorten(stack.getHoverName().getString(), 29)), x + 48, y + 19, TEXT);
    }

    private void card(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, PANEL);
        graphics.fill(x, y, x + width, y + 2, 0xFF2D5668);
    }

    private void tab(GuiGraphics graphics, int x, int y, int width, String label, boolean active, double mouseX, double mouseY) {
        boolean hovered = isInside(mouseX, mouseY, x, y, width, 26);
        graphics.fill(x, y, x + width, y + 26, active ? PANEL_LIGHT : (hovered ? 0xFF1C3040 : PANEL_DARK));
        if (active) graphics.fill(x, y + 24, x + width, y + 26, TEAL);
        graphics.drawCenteredString(font, Component.literal(label), x + width / 2, y + 8, active ? TEXT : MUTED);
    }

    private void pageButton(GuiGraphics graphics, int x, int y, int width, String label, boolean enabled,
                            double mouseX, double mouseY) {
        boolean hovered = enabled && isInside(mouseX, mouseY, x, y, width, 28);
        graphics.fill(x, y, x + width, y + 28, hovered ? PANEL_LIGHT : PANEL_DARK);
        graphics.drawCenteredString(font, Component.literal(label), x + width / 2, y + 8, enabled ? TEAL : 0xFF53656F);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return true;
        double screenMouseX = mouseX;
        double screenMouseY = mouseY;
        if (exchangeTab && searchBox != null && searchBox.mouseClicked(screenMouseX, screenMouseY, button)) {
            return true;
        }
        float scale = uiScale();
        mouseX = toDesignX(mouseX, scale);
        mouseY = toDesignY(mouseY, scale);
        if (isInside(mouseX, mouseY, PANEL_WIDTH - 36, 13, 24, 24)) {
            onClose();
            return true;
        }
        if (isInside(mouseX, mouseY, 24, 76, 112, 26)) {
            exchangeTab = false;
            if (searchBox != null) {
                searchBox.visible = false;
                searchBox.setFocused(false);
            }
            return true;
        }
        if (isInside(mouseX, mouseY, 144, 76, 112, 26)) {
            exchangeTab = true;
            if (searchBox != null) searchBox.visible = true;
            return true;
        }
        if (!exchangeTab && isInside(mouseX, mouseY, 500, 338, 236, 54)) {
            exchangeTab = true;
            if (searchBox != null) searchBox.visible = true;
            return true;
        }
        if (exchangeTab) {
            for (int index = 0; index < FrontierShopCatalog.Category.values().length; index++) {
                int x = 24 + index * (CATEGORY_WIDTH + CATEGORY_GAP);
                if (isInside(mouseX, mouseY, x, CATEGORY_Y, CATEGORY_WIDTH, 22)) {
                    unfocusSearch();
                    sendFilter(activeSearchQuery(), FrontierShopCatalog.Category.values()[index], menu.sortMode());
                    return true;
                }
            }
            if (isInside(mouseX, mouseY, 24, 122, 250, 18)) {
                if (isInside(mouseX, mouseY, 236, 122, 38, 18)) {
                    if (searchBox != null) {
                        searchBox.setValue("");
                        searchBox.setFocused(true);
                    }
                    submitSearch();
                } else if (searchBox != null) {
                    searchBox.setFocused(true);
                }
                return true;
            }
            if (isInside(mouseX, mouseY, 284, 122, 150, 18)) {
                unfocusSearch();
                sendFilter(activeSearchQuery(), menu.category(), menu.sortMode().next());
                return true;
            }
            if (isInside(mouseX, mouseY, 24, PAGE_Y, PAGE_BUTTON_WIDTH, 28) && menu.canGoPrevious()) {
                unfocusSearch();
                clickPageButton(FrontierShopMenu.BUTTON_PREVIOUS_PAGE);
                return true;
            }
            if (isInside(mouseX, mouseY, PANEL_WIDTH - 64, PAGE_Y, PAGE_BUTTON_WIDTH, 28) && menu.canGoNext()) {
                unfocusSearch();
                clickPageButton(FrontierShopMenu.BUTTON_NEXT_PAGE);
                return true;
            }
            for (int index = 0; index < FrontierShopCatalog.PRODUCTS_PER_PAGE; index++) {
                int column = index % GRID_COLUMNS;
                int row = index / GRID_COLUMNS;
                int x = 24 + column * (CARD_WIDTH + CARD_GAP);
                int y = EXCHANGE_Y + row * (CARD_HEIGHT + CARD_GAP);
                if (isInside(mouseX, mouseY, x, y, CARD_WIDTH, CARD_HEIGHT)) {
                    unfocusSearch();
                    int slot = menu.productSlotAt(index);
                    if (slot >= 0 && !menu.getSlot(slot).getItem().isEmpty() && !isPane(menu.getSlot(slot).getItem())) {
                        clickMenuSlot(slot);
                    }
                    return true;
                }
            }
        }
        return true;
    }

    private void clickMenuSlot(int slot) {
        Player player = minecraft.player;
        if (minecraft.gameMode != null && player != null) {
            minecraft.gameMode.handleInventoryMouseClick(menu.containerId, slot, 0,
                    net.minecraft.world.inventory.ClickType.PICKUP, player);
        }
    }

    private void unfocusSearch() {
        if (searchBox != null) searchBox.setFocused(false);
    }

    private void clickPageButton(int buttonId) {
        if (minecraft.gameMode != null && minecraft.player != null) {
            menu.clientPageButton(buttonId);
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            if (searchBox != null && searchBox.isFocused()) {
                searchBox.setFocused(false);
                return true;
            }
            onClose();
            return true;
        }
        if (keyCode == 257 && exchangeTab && searchBox != null && searchBox.isFocused()) {
            submitSearch();
            return true;
        }
        if (keyCode == 263 && exchangeTab && (searchBox == null || !searchBox.isFocused()) && menu.canGoPrevious()) {
            clickPageButton(FrontierShopMenu.BUTTON_PREVIOUS_PAGE);
            return true;
        }
        if (keyCode == 262 && exchangeTab && (searchBox == null || !searchBox.isFocused()) && menu.canGoNext()) {
            clickPageButton(FrontierShopMenu.BUTTON_NEXT_PAGE);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static boolean isPane(ItemStack stack) {
        return !stack.isEmpty() && stack.is(Items.GRAY_STAINED_GLASS_PANE);
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static String shorten(String text, int max) {
        return text.length() <= max ? text : text.substring(0, Math.max(0, max - 1)) + "…";
    }

    private float uiScale() {
        return Math.max(0.35f, Math.min(1.0f,
                Math.min((width - 24.0f) / PANEL_WIDTH, (height - 24.0f) / PANEL_HEIGHT)));
    }

    private double toDesignX(double screenX, float scale) {
        return (screenX - width / 2.0) / scale + PANEL_WIDTH / 2.0;
    }

    private double toDesignY(double screenY, float scale) {
        return (screenY - height / 2.0) / scale + PANEL_HEIGHT / 2.0;
    }

    @Override
    public void resize(net.minecraft.client.Minecraft minecraft, int width, int height) {
        String query = searchBox == null ? menu.searchQuery() : searchBox.getValue();
        super.resize(minecraft, width, height);
        if (searchBox != null) {
            searchBox.setValue(query);
            positionSearchBox();
        }
    }
}
