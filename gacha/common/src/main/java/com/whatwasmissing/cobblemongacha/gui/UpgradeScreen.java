package com.whatwasmissing.cobblemongacha.gui;

import com.whatwasmissing.cobblemongacha.core.GachaUpgradeService;
import com.whatwasmissing.cobblemongacha.core.ItemValueService;
import com.whatwasmissing.cobblemongacha.core.UpgradeCatalog;
import com.whatwasmissing.cobblemongacha.core.UpgradeTarget;
import com.whatwasmissing.cobblemongacha.network.UpgradeResultPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Locale;

/**
 * Cobblemon-styled adaptation of Upgrader items: source on the left, target on
 * the right, value-based chance in a central wheel, and a visible inventory.
 */
public final class UpgradeScreen extends AbstractContainerScreen<UpgradeMenu> {
    private static final int WIDTH = 820;
    private static final int HEIGHT = 680;
    private static final int BACK = 0xFF111720;
    private static final int PANEL = 0xFF192332;
    private static final int PANEL_DARK = 0xFF101620;
    private static final int LINE = 0xFF3D4861;
    private static final int GOLD = 0xFFE7B63E;
    private static final int TEXT = 0xFFE8EBF2;
    private static final int MUTED = 0xFFA5AEC0;
    private static final int ORANGE = 0xFFE47740;
    private static final int GREEN = 0xFF64C97B;
    private static final int RED = 0xFFE05E5E;
    private long wheelSpinStarted;
    private long wheelSpinUntil;
    private long feedbackSequence = -1L;
    private long feedbackVisibleAt = -1L;
    private long feedbackUntil = -1L;
    private UpgradeResultPayload feedback;

    public UpgradeScreen(UpgradeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = WIDTH;
        imageHeight = HEIGHT;
        inventoryLabelY = HEIGHT + 20;
    }

    @Override
    protected void init() {
        super.init();
        leftPos = 0;
        topPos = 0;
        wheelSpinStarted = 0L;
        wheelSpinUntil = 0L;
        feedbackSequence = -1L;
        feedbackVisibleAt = -1L;
        feedbackUntil = -1L;
        feedback = null;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {}

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        float scale = uiScale();
        double designMouseX = toDesignX(mouseX, scale);
        double designMouseY = toDesignY(mouseY, scale);
        graphics.pose().pushPose();
        graphics.pose().translate(width / 2.0f, height / 2.0f, 0.0f);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.pose().translate(-WIDTH / 2.0f, -HEIGHT / 2.0f, 0.0f);

        graphics.fill(-2000, -2000, 2000, 2000, 0xAA000000);
        graphics.fill(0, 0, WIDTH, HEIGHT, BACK);
        graphics.fill(4, 4, WIDTH - 4, HEIGHT - 4, 0xFF080D14);
        drawHeader(graphics);
        drawSourcePanel(graphics);
        drawWheel(graphics);
        trackUpgradeFeedback();
        drawRollFeedback(graphics);
        drawTargetPanel(graphics, designMouseX, designMouseY);
        drawActionBar(graphics, designMouseX, designMouseY);
        drawInventory(graphics, designMouseX, designMouseY);
        drawHoverTooltip(graphics, designMouseX, designMouseY);

        graphics.pose().popPose();
    }

    private void drawHeader(GuiGraphics graphics) {
        graphics.fill(28, 14, 164, 42, PANEL);
        graphics.fill(28, 14, 31, 42, ORANGE);
        graphics.drawString(font, Component.literal("GACHA DRAWS"), 40, 22, TEXT);
        graphics.drawCenteredString(font, Component.literal("ITEM UPGRADER"), WIDTH / 2, 18, TEXT);
    }

    private void drawSourcePanel(GuiGraphics graphics) {
        panel(graphics, 28, 70, 228, 312);
        drawHoneycomb(graphics, 28, 70, 228, 312);
        graphics.drawCenteredString(font, Component.literal("SOURCE"), 142, 86, MUTED);
        ItemStack source = menu.getSlot(UpgradeMenu.SOURCE_DISPLAY_SLOT).getItem();
        if (!source.isEmpty()) graphics.renderItem(source, 126, 132);
        String sourceName = menu.sourceStack().isEmpty() ? "Place an item" : menu.sourceStack().getHoverName().getString();
        graphics.drawCenteredString(font, Component.literal(shorten(sourceName, 24)),
                142, 236, TEXT);
        graphics.drawCenteredString(font, Component.literal(source.isEmpty() ? "Choose from Inventory" : "Value: " + valueText(ItemValueService.value(source))),
                142, 260, MUTED);
    }

    private void drawWheel(GuiGraphics graphics) {
        int centerX = 410;
        int centerY = 210;
        graphics.fill(278, 78, 542, 342, PANEL_DARK);
        graphics.fill(288, 88, 532, 332, 0xFF202A3E);
        graphics.fill(306, 106, 514, 314, 0xFF0D121B);
        // Pixel-friendly ring/ticks inspired by the reference wheel.
        long now = System.currentTimeMillis();
        boolean spinning = wheelSpinUntil > now;
        double markerAngle = -Math.PI / 2.0;
        if (spinning) {
            double progress = Math.min(1.0, Math.max(0.0, (now - wheelSpinStarted) / 900.0));
            markerAngle += progress * Math.PI * 2.0 * 4.0;
        }
        for (int index = 0; index < 32; index++) {
            double angle = (Math.PI * 2.0 * index) / 32.0;
            int x = centerX + (int) (Math.cos(angle) * 128.0);
            int y = centerY + (int) (Math.sin(angle) * 128.0);
            graphics.fill(x - 2, y - 7, x + 3, y + 7, index == 0 && !spinning ? GOLD : LINE);
        }
        int markerX = centerX + (int) (Math.cos(markerAngle) * 128.0);
        int markerY = centerY + (int) (Math.sin(markerAngle) * 128.0);
        graphics.fill(markerX - 5, markerY - 5, markerX + 6, markerY + 6, spinning ? GOLD : ORANGE);
        graphics.fill(centerX - 2, 80, centerX + 2, 108, spinning ? LINE : GOLD);
        double chance = GachaUpgradeService.chance(menu);
        int chanceColour = chance >= 0.5 ? GREEN : chance > 0.0 ? ORANGE : MUTED;
        graphics.drawCenteredString(font, Component.literal(chance <= 0.0 ? "—" : percent(chance)), centerX, 188, chanceColour);
        graphics.drawCenteredString(font, Component.literal("chance"), centerX, 222, MUTED);
    }

    private void trackUpgradeFeedback() {
        long sequence = menu.upgradeResultSequence();
        if (sequence == feedbackSequence || menu.lastUpgradeResult() == null) return;
        feedbackSequence = sequence;
        feedback = menu.lastUpgradeResult();
        long now = System.currentTimeMillis();
        // The click starts the wheel animation. A slow server response must
        // not restart it, otherwise the marker appears to jump out of sync
        // with the wager the player just made.
        if (wheelSpinStarted <= 0L) {
            wheelSpinStarted = now;
            wheelSpinUntil = now + 900L;
        }
        feedbackVisibleAt = Math.max(now + 250L, wheelSpinUntil);
        feedbackUntil = feedbackVisibleAt + 2400L;
    }

    private void drawRollFeedback(GuiGraphics graphics) {
        long now = System.currentTimeMillis();
        if (feedback == null || now >= feedbackUntil) return;
        if (now < feedbackVisibleAt) {
            graphics.drawCenteredString(font, Component.literal("ROLLING…"), 410, 246, GOLD);
            graphics.drawCenteredString(font, Component.literal("Your result is on the way…"), 410, 264, MUTED);
            return;
        }
        int accent = feedback.success() ? GREEN : RED;
        graphics.fill(304, 164, 516, 278, 0xF20B111A);
        graphics.fill(304, 164, 516, 168, accent);
        graphics.drawCenteredString(font, Component.literal(feedback.success() ? "SUCCESS" : "FAILED"),
                410, 184, accent);
        graphics.drawCenteredString(font, Component.literal(shorten(feedback.detail(), 27)),
                410, 211, TEXT);
        graphics.drawCenteredString(font, Component.literal("Chance: " + percent(feedback.chance())),
                410, 232, feedback.success() ? GREEN : RED);
    }

    private void drawTargetPanel(GuiGraphics graphics, double mouseX, double mouseY) {
        panel(graphics, 564, 70, 228, 312);
        drawHoneycomb(graphics, 564, 70, 228, 312);
        graphics.drawCenteredString(font, Component.literal("TARGET"), 678, 86, MUTED);
        ItemStack target = menu.getSlot(UpgradeMenu.TARGET_DISPLAY_SLOT).getItem();
        if (!target.isEmpty()) graphics.renderItem(target, 662, 112);
        String targetName = menu.selectedTarget() == null ? "Choose a target" : menu.selectedTarget().displayName();
        graphics.drawCenteredString(font, Component.literal(shorten(targetName, 24)),
                678, 166, TEXT);
        String targetInfo = target.isEmpty() ? "Use the catalogue below"
                : (menu.selectedTarget() != null && menu.selectedTarget().pokemon()
                ? (menu.selectedTarget().rarity() == com.whatwasmissing.cobblemongacha.core.GachaRarity.LEGENDARY
                ? "Min wager: " + valueText(menu.legendaryPokemonMinimumSourceValue())
                : "Rarity: " + menu.selectedTarget().rarity().displayName())
                : "Value: " + valueText(ItemValueService.targetValue(target, menu.targetAmount())));
        graphics.drawCenteredString(font, Component.literal(targetInfo), 678, 188, MUTED);
        drawTargetCategoryTabs(graphics, mouseX, mouseY);
        java.util.List<UpgradeTarget> targets = menu.targetsForDisplay();
        for (int index = 0; index < UpgradeCatalog.TARGETS_PER_PAGE; index++) {
            int global = menu.targetPage() * UpgradeCatalog.TARGETS_PER_PAGE + index;
            if (global >= targets.size()) break;
            UpgradeTarget entry = targets.get(global);
            int column = index % 2;
            int row = index / 2;
            int x = 580 + column * 104;
            int y = 236 + row * 38;
            boolean hovered = isInside(mouseX, mouseY, x, y, 96, 32);
            graphics.fill(x, y, x + 96, y + 32, hovered ? 0xFF34445C : 0xFF1A2433);
            graphics.renderItem(entry.displayStack(1), x + 5, y + 8);
            graphics.drawString(font, Component.literal(shorten(entry.displayName(), 11)), x + 27, y + 11, TEXT);
            if (global == selectedTargetIndex()) graphics.fill(x, y + 30, x + 96, y + 32, GOLD);
        }
        graphics.drawString(font, Component.literal("‹"), 580, 354, menu.canGoPrevious() ? GOLD : LINE);
        graphics.drawString(font, Component.literal("Page " + (menu.targetPage() + 1) + "/" + menu.targetPageCount()), 620, 354, MUTED);
        graphics.drawString(font, Component.literal("›"), 762, 354, menu.canGoNext() ? GOLD : LINE);
    }

    private void drawTargetCategoryTabs(GuiGraphics graphics, double mouseX, double mouseY) {
        boolean items = menu.targetCategory() == UpgradeCatalog.TargetCategory.ITEMS;
        boolean itemsHover = isInside(mouseX, mouseY, 580, 204, 96, 24);
        boolean pokemonHover = isInside(mouseX, mouseY, 684, 204, 96, 24);
        graphics.fill(580, 204, 676, 228, items || itemsHover ? 0xFF46516C : 0xFF1A2433);
        graphics.fill(684, 204, 780, 228, !items || pokemonHover ? 0xFF46516C : 0xFF1A2433);
        graphics.fill(items ? 580 : 684, 226, items ? 676 : 780, 228, GOLD);
        graphics.drawCenteredString(font, Component.literal(UpgradeCatalog.TargetCategory.ITEMS.displayName()),
                628, 212, items ? GOLD : TEXT);
        graphics.drawCenteredString(font, Component.literal(UpgradeCatalog.TargetCategory.POKEMON.displayName()),
                732, 212, !items ? GOLD : TEXT);
    }

    private void drawActionBar(GuiGraphics graphics, double mouseX, double mouseY) {
        long cooldown = menu.gamblingCooldownSeconds();
        boolean ready = cooldown <= 0L;
        boolean upgradeHover = ready && isInside(mouseX, mouseY, 28, 404, 390, 58);
        boolean pokemonTarget = menu.selectedTarget() != null && menu.selectedTarget().pokemon();
        graphics.fill(28, 404, 418, 462, ready ? (upgradeHover ? 0xFFE9BE4C : 0xFFD6A832) : 0xFF5B4650);
        graphics.fill(32, 408, 414, 458, ready ? 0xFFB88821 : 0xFF403139);
        graphics.drawCenteredString(font, Component.literal(ready ? "⚒  UPGRADE" : "WAGER LOCKED"), 223, 424,
                ready ? 0xFF151515 : 0xFFF0B7B7);
        graphics.drawCenteredString(font, Component.literal(ready ? "failure loses the source item" : "available in " + shortDuration(cooldown)),
                223, 444, ready ? 0xFF2C2615 : 0xFFE2AEB0);

        for (int index = 0; index < 4; index++) {
            int x = 434 + index * 66;
            int amount = new int[]{1, 2, 4, 8}[index];
            boolean available = !pokemonTarget || index == 0;
            boolean selected = available && amount == menu.targetAmount();
            boolean hover = isInside(mouseX, mouseY, x, 404, 58, 58);
            graphics.fill(x, 404, x + 58, 462, available && (hover || selected) ? 0xFF46516C : PANEL);
            graphics.fill(x + 3, 407, x + 55, 459, selected ? 0xFF5A5360 : PANEL_DARK);
            graphics.drawCenteredString(font, Component.literal(available ? "×" + amount : "—"), x + 29, 421,
                    selected ? GOLD : (available ? TEXT : LINE));
        }
    }

    private void drawInventory(GuiGraphics graphics, double mouseX, double mouseY) {
        graphics.drawString(font, Component.literal("Inventory"), 180, 486, MUTED);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int x = 180 + column * 36;
                int y = 508 + row * 36;
                int containerSlot = 54 + row * 9 + column;
                drawInventorySlot(graphics, containerSlot, x, y, mouseX, mouseY);
            }
        }
        for (int column = 0; column < 9; column++) {
            int x = 180 + column * 36;
            int y = 620;
            drawInventorySlot(graphics, 54 + 27 + column, x, y, mouseX, mouseY);
        }
    }

    private void drawInventorySlot(GuiGraphics graphics, int containerSlot, int x, int y, double mouseX, double mouseY) {
        boolean selected = containerSlot == menu.sourceContainerSlot();
        boolean hovered = isInside(mouseX, mouseY, x, y, 34, 34);
        graphics.fill(x, y, x + 34, y + 34, hovered ? 0xFF384963 : (selected ? 0xFF544932 : 0xFF080B10));
        graphics.fill(x, y, x + 34, y + 2, selected ? GOLD : LINE);
        ItemStack stack = menu.getSlot(containerSlot).getItem();
        if (!stack.isEmpty()) graphics.renderItem(stack, x + 9, y + 9);
    }

    private void drawHoverTooltip(GuiGraphics graphics, double mouseX, double mouseY) {
        java.util.List<UpgradeTarget> targets = menu.targetsForDisplay();
        for (int index = 0; index < UpgradeCatalog.TARGETS_PER_PAGE; index++) {
            int global = menu.targetPage() * UpgradeCatalog.TARGETS_PER_PAGE + index;
            int column = index % 2;
            int row = index / 2;
            if (global < targets.size() && isInside(mouseX, mouseY, 580 + column * 104, 236 + row * 38, 96, 32)) {
                graphics.renderTooltip(font, targets.get(global).displayStack(1), (int) mouseX, (int) mouseY);
                return;
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int x = 180 + column * 36;
                int y = 508 + row * 36;
                if (isInside(mouseX, mouseY, x, y, 34, 34)) {
                    ItemStack stack = menu.getSlot(54 + row * 9 + column).getItem();
                    if (!stack.isEmpty()) {
                        graphics.renderTooltip(font, stack, (int) mouseX, (int) mouseY);
                        return;
                    }
                }
            }
        }
        for (int column = 0; column < 9; column++) {
            int x = 180 + column * 36;
            if (isInside(mouseX, mouseY, x, 620, 34, 34)) {
                ItemStack stack = menu.getSlot(81 + column).getItem();
                if (!stack.isEmpty()) {
                    graphics.renderTooltip(font, stack, (int) mouseX, (int) mouseY);
                    return;
                }
            }
        }
        if (isInside(mouseX, mouseY, 28, 14, 136, 28)) {
            graphics.renderTooltip(font, Component.literal("Open Gacha Draws"), (int) mouseX, (int) mouseY);
            return;
        }
        if (isInside(mouseX, mouseY, 28, 404, 390, 58)) {
            graphics.renderTooltip(font, Component.literal("Attempt upgrade · failure destroys the source"), (int) mouseX, (int) mouseY);
        }
    }

    private int selectedTargetIndex() {
        UpgradeTarget selected = menu.selectedTarget();
        if (selected == null) return -1;
        return menu.targetsForDisplay().indexOf(selected);
    }

    private void clickSource(int containerSlot) {
        if (minecraft.gameMode == null || minecraft.player == null || !menu.getSlot(containerSlot).hasItem()) return;
        menu.clientSelectSource(containerSlot);
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, UpgradeMenu.BUTTON_SOURCE_BASE + containerSlot);
    }

    private void clickTarget(int index) {
        if (minecraft.gameMode == null || minecraft.player == null) return;
        menu.clientSelectTarget(index);
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, UpgradeMenu.BUTTON_TARGET_BASE + index);
    }

    private void clickTargetPage(int buttonId) {
        if (minecraft.gameMode == null || minecraft.player == null) return;
        menu.clientTargetPageButton(buttonId);
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
    }

    private void clickTargetCategory(UpgradeCatalog.TargetCategory category) {
        if (minecraft.gameMode == null || minecraft.player == null) return;
        menu.clientSelectCategory(category);
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                category == UpgradeCatalog.TargetCategory.POKEMON
                        ? UpgradeMenu.BUTTON_POKEMON_TARGETS : UpgradeMenu.BUTTON_ITEM_TARGETS);
    }

    private void clickOpenDraws() {
        if (minecraft.gameMode == null || minecraft.player == null) return;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, UpgradeMenu.BUTTON_OPEN_DRAWS);
    }

    private void clickMultiplier(int index) {
        if (minecraft.gameMode == null || minecraft.player == null) return;
        menu.clientSelectMultiplier(index);
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, UpgradeMenu.BUTTON_MULTIPLIER_BASE + index);
    }

    private void clickUpgrade() {
        if (minecraft.gameMode == null || minecraft.player == null) return;
        if (menu.gamblingCooldownSeconds() > 0L) return;
        wheelSpinStarted = System.currentTimeMillis();
        wheelSpinUntil = wheelSpinStarted + 900L;
        minecraft.gameMode.handleInventoryMouseClick(menu.containerId, UpgradeMenu.UPGRADE_SLOT, 0,
                net.minecraft.world.inventory.ClickType.PICKUP, minecraft.player);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return true;
        float scale = uiScale();
        mouseX = toDesignX(mouseX, scale);
        mouseY = toDesignY(mouseY, scale);
        if (isInside(mouseX, mouseY, 28, 14, 136, 28)) { clickOpenDraws(); return true; }
        if (isInside(mouseX, mouseY, 28, 404, 390, 58)) { clickUpgrade(); return true; }
        for (int index = 0; index < 4; index++) {
            if (isInside(mouseX, mouseY, 434 + index * 66, 404, 58, 58)) {
                if (menu.selectedTarget() == null || !menu.selectedTarget().pokemon() || index == 0) clickMultiplier(index);
                return true;
            }
        }
        if (isInside(mouseX, mouseY, 580, 204, 96, 24)) {
            if (menu.targetCategory() != UpgradeCatalog.TargetCategory.ITEMS) clickTargetCategory(UpgradeCatalog.TargetCategory.ITEMS);
            return true;
        }
        if (isInside(mouseX, mouseY, 684, 204, 96, 24)) {
            if (menu.targetCategory() != UpgradeCatalog.TargetCategory.POKEMON) clickTargetCategory(UpgradeCatalog.TargetCategory.POKEMON);
            return true;
        }
        if (isInside(mouseX, mouseY, 580, 350, 35, 28) && menu.canGoPrevious()) { clickTargetPage(UpgradeMenu.BUTTON_PREVIOUS_TARGET_PAGE); return true; }
        if (isInside(mouseX, mouseY, 752, 350, 35, 28) && menu.canGoNext()) { clickTargetPage(UpgradeMenu.BUTTON_NEXT_TARGET_PAGE); return true; }
        java.util.List<UpgradeTarget> targets = menu.targetsForDisplay();
        for (int index = 0; index < UpgradeCatalog.TARGETS_PER_PAGE; index++) {
            int column = index % 2;
            int row = index / 2;
            if (isInside(mouseX, mouseY, 580 + column * 104, 236 + row * 38, 96, 32)) {
                int global = menu.targetPage() * UpgradeCatalog.TARGETS_PER_PAGE + index;
                if (global < targets.size()) clickTarget(global);
                return true;
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                if (isInside(mouseX, mouseY, 180 + column * 36, 508 + row * 36, 34, 34)) {
                    clickSource(54 + row * 9 + column);
                    return true;
                }
            }
        }
        for (int column = 0; column < 9; column++) {
            if (isInside(mouseX, mouseY, 180 + column * 36, 620, 34, 34)) {
                clickSource(54 + 27 + column);
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { onClose(); return true; }
        if (keyCode == 263 && menu.canGoPrevious()) { clickTargetPage(UpgradeMenu.BUTTON_PREVIOUS_TARGET_PAGE); return true; }
        if (keyCode == 262 && menu.canGoNext()) { clickTargetPage(UpgradeMenu.BUTTON_NEXT_TARGET_PAGE); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private static void panel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, PANEL);
        graphics.fill(x, y, x + width, y + 2, LINE);
        graphics.fill(x, y + height - 2, x + width, y + height, LINE);
        graphics.fill(x, y, x + 2, y + height, LINE);
        graphics.fill(x + width - 2, y, x + width, y + height, LINE);
    }

    private static void drawHoneycomb(GuiGraphics graphics, int x, int y, int width, int height) {
        int radius = 29;
        int colour = 0xFF37404D;
        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 4; column++) {
                int centerX = x + 42 + column * 72 + ((row & 1) == 0 ? 0 : 36);
                int centerY = y + 42 + row * 62;
                // Keep the decorative grid inside its owning panel. The old
                // overscan made the gold outlines spill across the entire UI.
                if (centerX - radius < x || centerX + radius > x + width
                        || centerY - radius < y || centerY + radius > y + height) continue;
                hex(graphics, centerX, centerY, radius, colour);
            }
        }
    }

    private static void hex(GuiGraphics graphics, int centerX, int centerY, int radius, int colour) {
        graphics.fill(centerX - radius + 8, centerY - radius, centerX + radius - 8, centerY - radius + 2, colour);
        graphics.fill(centerX - radius + 8, centerY + radius - 2, centerX + radius - 8, centerY + radius, colour);
        graphics.fill(centerX - radius, centerY - radius + 8, centerX - radius + 2, centerY + radius - 8, colour);
        graphics.fill(centerX + radius - 2, centerY - radius + 8, centerX + radius, centerY + radius - 8, colour);
        graphics.fill(centerX - radius + 2, centerY - radius + 4, centerX - radius + 4, centerY - radius + 8, colour);
        graphics.fill(centerX + radius - 4, centerY - radius + 4, centerX + radius - 2, centerY - radius + 8, colour);
        graphics.fill(centerX - radius + 2, centerY + radius - 8, centerX - radius + 4, centerY + radius - 4, colour);
        graphics.fill(centerX + radius - 4, centerY + radius - 8, centerX + radius - 2, centerY + radius - 4, colour);
    }

    private float uiScale() { return Math.max(0.35f, Math.min(1.0f, Math.min((width - 24.0f) / WIDTH, (height - 24.0f) / HEIGHT))); }
    private double toDesignX(double screenX, float scale) { return (screenX - width / 2.0) / scale + WIDTH / 2.0; }
    private double toDesignY(double screenY, float scale) { return (screenY - height / 2.0) / scale + HEIGHT / 2.0; }
    private static boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
    private static String valueText(double value) { return value >= 1000 ? String.format(Locale.ROOT, "%.0fk", value / 1000.0) : String.format(Locale.ROOT, "%.0f", value); }
    private static String percent(double chance) {
        double percent = chance * 100.0;
        return String.format(Locale.ROOT, percent < 0.1 ? "%.2f%%" : "%.1f%%", percent);
    }
    private static String shortDuration(long seconds) {
        if (seconds >= 60L) return (seconds / 60L) + "m " + (seconds % 60L) + "s";
        return seconds + "s";
    }
    private static String shorten(String text, int max) { return text.length() <= max ? text : text.substring(0, Math.max(0, max - 1)) + "…"; }
}
