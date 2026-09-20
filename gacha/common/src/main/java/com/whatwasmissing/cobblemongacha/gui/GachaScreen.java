package com.whatwasmissing.cobblemongacha.gui;

import com.whatwasmissing.cobblemongacha.core.GachaBanner;
import com.whatwasmissing.cobblemongacha.core.GachaEntry;
import com.whatwasmissing.cobblemongacha.core.GachaLedger;
import com.whatwasmissing.cobblemongacha.core.GachaRarity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** A compact Cobblemon-inspired presentation: cards, tabs, rarity chips, and no chest grid. */
public final class GachaScreen extends AbstractContainerScreen<GachaMenu> {
    private static final int WIDTH = 760;
    private static final int HEIGHT = 456;
    private static final int NAVY = 0xF20C1520;
    private static final int PANEL = 0xFF172634;
    private static final int PANEL_LIGHT = 0xFF223A4A;
    private static final int PANEL_DARK = 0xFF101A25;
    private static final int TEAL = 0xFF70D8D4;
    private static final int TEXT = 0xFFF3F7F8;
    private static final int MUTED = 0xFFA9BBC4;
    private static final int GOLD = 0xFFF6CC72;
    private static final long REVEAL_DURATION_MS = 2400L;
    private static final int FEATURED_PREVIOUS_X = 638;
    private static final int FEATURED_NEXT_X = 674;
    private static final int FEATURED_NAV_Y = 96;
    private static final int FEATURED_NAV_WIDTH = 28;
    private static final int FEATURED_NAV_HEIGHT = 28;
    private static final int CURRENT_BANNER_X = 42;
    private static final int CURRENT_BANNER_Y = 164;
    private static final int CURRENT_BANNER_WIDTH = 142;
    private static final int CURRENT_BANNER_HEIGHT = 18;

    private List<String> lastHistorySignatures = List.of();
    private boolean historyBaselineReady;
    private long revealStartedAt = -1L;
    private String revealLabel = "";
    private GachaRarity revealRarity = GachaRarity.COMMON;
    private boolean revealShiny;
    private boolean closing;

    public GachaScreen(GachaMenu menu, Inventory inventory, Component title) {
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
        lastHistorySignatures = historySignatures();
        historyBaselineReady = false;
        closing = false;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {}

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        // Keep JEI and the world visually behind the modal, never as a second
        // interactive-looking layer visible through the gacha screen.
        graphics.fill(0, 0, width, height, 0xF20A1018);
        trackPullReveal();
        float scale = uiScale();
        double designMouseX = toDesignX(mouseX, scale);
        double designMouseY = toDesignY(mouseY, scale);
        graphics.pose().pushPose();
        graphics.pose().translate(width / 2.0f, height / 2.0f, 0.0f);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.pose().translate(-WIDTH / 2.0f, -HEIGHT / 2.0f, 0.0f);
        drawShell(graphics, designMouseX, designMouseY);
        renderBanner(graphics, designMouseX, designMouseY);
        renderOdds(graphics);
        renderHistory(graphics);
        renderPullReveal(graphics);
        graphics.pose().popPose();
    }

    private void drawShell(GuiGraphics graphics, double mouseX, double mouseY) {
        graphics.fill(-2000, -2000, 2000, 2000, 0xE6081018);
        graphics.fill(-4, -4, WIDTH + 4, HEIGHT + 4, 0xFF081018);
        graphics.fill(0, 0, WIDTH, HEIGHT, NAVY);
        graphics.fill(0, 0, WIDTH, 62, PANEL_DARK);
        graphics.fill(0, 60, WIDTH, 62, TEAL);
        graphics.drawString(font, Component.literal("COBBLEMON GACHA"), 24, 14, TEXT);
        ItemStack ticket = menu.getSlot(4).getItem();
        graphics.fill(WIDTH - 224, 15, WIDTH - 48, 52, PANEL_LIGHT);
        if (!ticket.isEmpty()) graphics.renderItem(ticket, WIDTH - 216, 21);
        graphics.drawString(font, Component.literal("Tickets: " + menu.tickets()),
                 WIDTH - 180, 25, TEXT);
        graphics.drawString(font, Component.literal("Next ticket: " + menu.capturesUntilNextTicket() + " Pokémon"),
                WIDTH - 180, 38, MUTED);
        drawCloseIcon(graphics, WIDTH - 26, 22,
                isInside(mouseX, mouseY, WIDTH - 44, 8, 36, 30) ? TEAL : MUTED);
    }

    private void renderBanner(GuiGraphics graphics, double mouseX, double mouseY) {
        GachaBanner banner = menu.displayBanner(menu.bannerIndex());
        boolean active = menu.isActiveBanner();
        BannerTheme theme = theme(banner);
        themedCard(graphics, 24, 82, 712, 142, theme);
        graphics.drawString(font, Component.literal(active ? "ACTIVE THIS HOUR" : "BANNER PREVIEW"), 42, 96,
                active ? theme.bright : GOLD);
        graphics.drawString(font, Component.literal(shorten(banner.title, 30)), 42, 115, TEXT);
        graphics.drawString(font, Component.literal(shorten(banner.description, 43)), 42, 135, MUTED);
        graphics.drawString(font, Component.literal("Banner " + (menu.bannerIndex() + 1) + "/" + Math.max(1, menu.bannerCount())),
                42, 150, MUTED);
        graphics.drawString(font, Component.literal(active
                        ? "Next rotation in " + formatDuration(menu.secondsUntilRotation())
                        : "Preview only · wait for its rotation window to draw"),
                190, 150, active ? GOLD : MUTED);

        drawBannerSymbol(graphics, theme);
        drawFeaturedLineup(graphics, banner, theme);

        pageButton(graphics, FEATURED_PREVIOUS_X, FEATURED_NAV_Y, FEATURED_NAV_WIDTH, true,
                menu.canGoPrevious(), mouseX, mouseY, theme.accent);
        pageButton(graphics, FEATURED_NEXT_X, FEATURED_NAV_Y, FEATURED_NAV_WIDTH, false,
                menu.canGoNext(), mouseX, mouseY, theme.accent);

        drawCurrentBannerButton(graphics, mouseX, mouseY, theme);
        long cooldown = menu.gamblingCooldownSeconds();
        boolean drawReady = active && cooldown <= 0L;
        drawButton(graphics, 510, 184, 105, 40, "DRAW 1", menu.getSlot(GachaMenu.DRAW_ONE_SLOT).getItem(),
                drawReady, mouseX, mouseY, theme.accent);
        drawButton(graphics, 627, 184, 105, 40, "DRAW 10", menu.getSlot(GachaMenu.DRAW_TEN_SLOT).getItem(),
                drawReady, mouseX, mouseY, theme.bright);
        if (cooldown > 0L) {
            graphics.drawString(font, Component.literal("Ready in " + shortDuration(cooldown)), 220, 192, GOLD);
        }
        drawUpgraderButton(graphics, mouseX, mouseY);
    }

    private void renderOdds(GuiGraphics graphics) {
        GachaBanner banner = menu.displayBanner(menu.bannerIndex());
        BannerTheme theme = theme(banner);
        themedCard(graphics, 24, 232, 712, 72, theme);
        graphics.drawString(font, Component.literal("ODDS & SAFETY NET"), 42, 244, theme.bright);
        double total = banner.entries.stream().filter(entry -> entry != null && entry.weight > 0
                && Double.isFinite(entry.weight)).mapToDouble(entry -> entry.weight).sum();
        int x = 42;
        for (GachaRarity rarity : GachaRarity.values()) {
            double weight = banner.entries.stream().filter(entry -> entry != null && entry.rarity == rarity
                            && entry.weight > 0 && Double.isFinite(entry.weight))
                    .mapToDouble(entry -> entry.weight).sum();
            String chance = total <= 0 ? "0.0%" : String.format(Locale.ROOT, "%.1f%%", weight * 100.0 / total);
            graphics.fill(x, 263, x + 122, 288, rarityColor(rarity));
            graphics.drawString(font, Component.literal(rarity.displayName()), x + 8, 268, 0xFF101A25);
            graphics.drawString(font, Component.literal(chance), x + 8, 278, 0xFF101A25);
            x += 136;
        }
        boolean legendaryAvailable = banner.entries.stream().anyMatch(entry -> entry != null
                && entry.rarity != null && entry.rarity.atLeast(GachaRarity.LEGENDARY)
                && entry.weight > 0.0 && Double.isFinite(entry.weight));
        String legendaryPity = legendaryAvailable
                ? "Legendary pity: " + menu.legendaryPityDraws()
                : "Legendary pity: unavailable in this pool";
        graphics.drawString(font, Component.literal("Rare+ pity: " + menu.rarePityDraws()
                + " · " + legendaryPity + " · Shiny: "
                + String.format(Locale.ROOT, "%.2f%%", menu.shinyChance() * 100.0)),
                42, 294, MUTED);
    }

    private void renderHistory(GuiGraphics graphics) {
        BannerTheme theme = theme(menu.displayBanner(menu.bannerIndex()));
        graphics.drawString(font, Component.literal("RECENT PULLS"), 24, 322, theme.bright);
        boolean hasPulls = false;
        for (int index = 0; index < 8; index++) {
            int x = 24 + index * 90;
            int y = 340;
            ItemStack stack = menu.getSlot(GachaMenu.HISTORY_START + index).getItem();
            graphics.fill(x, y, x + 84, y + 48, PANEL);
            if (!isPane(stack)) {
                hasPulls = true;
                String label = stack.getHoverName().getString();
                if (!PokemonSpriteRenderer.render(graphics, label, isShinyLabel(label), x + 4, y + 4, 24)) {
                    graphics.renderItem(stack, x + 7, y + 12);
                }
                graphics.drawString(font, Component.literal(shorten(label, 8)), x + 30, y + 16, TEXT);
            }
        }
        if (!hasPulls) graphics.drawCenteredString(font, Component.literal("No pulls yet"), WIDTH / 2, 356, MUTED);
        graphics.drawString(font, Component.literal("Rare pity: " + menu.rarePity() + " / "
                + menu.rarePityDraws() + " · Legendary pity: "
                + menu.legendaryPity() + " / " + menu.legendaryPityDraws()),
                24, 402, GOLD);
        graphics.drawString(font, Component.literal("ARROWS switch · ESC close"), WIDTH - 160, 402, MUTED);
    }

    private void trackPullReveal() {
        List<String> current = historySignatures();
        // The initial container contents arrive before or alongside the custom
        // server snapshot. Establish that state as the baseline so an old
        // legendary pull is not replayed as a new reveal when the screen opens.
        if (!historyBaselineReady) {
            lastHistorySignatures = current;
            historyBaselineReady = menu.hasServerSnapshot();
            return;
        }
        if (!lastHistorySignatures.isEmpty() && !current.equals(lastHistorySignatures)) {
            Map<String, Integer> oldCounts = new HashMap<>();
            for (String signature : lastHistorySignatures) {
                oldCounts.merge(signature, 1, Integer::sum);
            }
            ItemStack bestStack = ItemStack.EMPTY;
            GachaRarity bestRarity = GachaRarity.COMMON;
            boolean bestShiny = false;
            for (int index = 0; index < current.size(); index++) {
                String signature = current.get(index);
                int oldCount = oldCounts.getOrDefault(signature, 0);
                if (oldCount > 0) {
                    oldCounts.put(signature, oldCount - 1);
                    continue;
                }
                ItemStack stack = menu.getSlot(GachaMenu.HISTORY_START + index).getItem();
                if (stack.isEmpty() || isPane(stack)) continue;
                String label = stack.getHoverName().getString();
                GachaRarity rarity = historyRarity(stack);
                boolean shiny = isShinyLabel(label);
                if (bestStack.isEmpty() || rarity.rank() > bestRarity.rank()
                        || (rarity == bestRarity && shiny && !bestShiny)) {
                    bestStack = stack;
                    bestRarity = rarity;
                    bestShiny = shiny;
                }
            }
            if (!bestStack.isEmpty() && (bestRarity.atLeast(GachaRarity.LEGENDARY) || bestShiny)) {
                revealLabel = bestStack.getHoverName().getString();
                revealRarity = bestRarity;
                revealShiny = bestShiny;
                revealStartedAt = System.currentTimeMillis();
            }
        }
        lastHistorySignatures = current;
    }

    private List<String> historySignatures() {
        List<String> signatures = new ArrayList<>(8);
        for (int index = 0; index < 8; index++) {
            ItemStack stack = menu.getSlot(GachaMenu.HISTORY_START + index).getItem();
            signatures.add(stack.isEmpty() ? "" : stack.getHoverName().getString() + "|" + stack.getItem());
        }
        return signatures;
    }

    private void renderPullReveal(GuiGraphics graphics) {
        if (revealStartedAt < 0L) return;
        long elapsed = System.currentTimeMillis() - revealStartedAt;
        if (elapsed >= REVEAL_DURATION_MS) {
            revealStartedAt = -1L;
            return;
        }

        float progress = Math.max(0.0f, Math.min(1.0f, elapsed / (float) REVEAL_DURATION_MS));
        float eased = 1.0f - (float) Math.pow(1.0f - progress, 3.0f);
        GachaBanner banner = menu.displayBanner(menu.bannerIndex());
        BannerTheme theme = theme(banner);
        graphics.fill(24, 82, 736, 224, 0xC8081018);
        graphics.fill(210, 88, 550, 220, theme.surface);
        graphics.fill(210, 88, 550, 91, theme.accent);
        drawRevealMotif(graphics, banner.id, theme, progress);

        String heading = revealShiny ? "SHINY DISCOVERY"
                : revealRarity == GachaRarity.MYTHIC ? "MYTHIC ARRIVAL" : "LEGENDARY ARRIVAL";
        graphics.drawCenteredString(font, Component.literal(heading), 380, 97, theme.bright);
        int spriteSize = Math.max(18, (int) (18 + eased * 68));
        int spriteX = 380 - spriteSize / 2;
        int spriteY = 105 + (int) ((1.0f - eased) * 12.0f);
        if (!PokemonSpriteRenderer.render(graphics, revealLabel, revealShiny, spriteX, spriteY, spriteSize)) {
            graphics.renderItem(new ItemStack(theme.icon), 372, 128);
        }
        graphics.drawCenteredString(font, Component.literal(shorten(revealLabel, 28)), 380, 185, TEXT);
    }

    private static void drawRevealMotif(GuiGraphics graphics, String bannerId, BannerTheme theme, float progress) {
        int color = tint(theme.bright, (int) (70 + 110 * (1.0f - progress)));
        int centerX = 380;
        int centerY = 145;
        int pulse = (int) (progress * 34.0f);
        switch (bannerId == null ? "" : bannerId) {
            case "verdant" -> {
                for (int index = 0; index < 6; index++) {
                    int x = 250 + index * 52;
                    int y = 116 + (index % 2) * 30 - (int) (progress * 20.0f);
                    graphics.fill(x, y, x + 8, y + 14, color);
                    graphics.fill(x + 4, y - 4, x + 12, y + 6, color);
                }
            }
            case "ember" -> {
                for (int index = 0; index < 8; index++) {
                    int x = 250 + index * 36;
                    int y = 184 - (int) (progress * (18 + index * 4)) % 68;
                    graphics.fill(x, y, x + 4, y + 7, color);
                }
            }
            case "tidal" -> {
                for (int ring = 0; ring < 3; ring++) {
                    int radius = 50 + ring * 32 + pulse;
                    graphics.fill(centerX - radius, centerY + ring * 8, centerX + radius, centerY + ring * 8 + 2, color);
                }
            }
            case "voltage" -> {
                for (int step = 0; step < 6; step++) {
                    int x = 250 + step * 28;
                    int y = 118 + ((step + (int) (progress * 4)) % 2) * 36;
                    graphics.fill(x, y, x + 18, y + 2, color);
                    graphics.fill(x + 16, y, x + 18, y + 20, color);
                }
            }
            case "stone" -> {
                drawSquareRing(graphics, centerX, centerY, 38 + pulse, color);
                drawSquareRing(graphics, centerX, centerY, 70 + pulse, tint(color, 80));
            }
            case "night" -> {
                drawSpark(graphics, 270, 115, color, 7);
                drawSpark(graphics, 490, 115, color, 5);
                drawSpark(graphics, 270, 177, color, 4);
                drawSpark(graphics, 490, 177, color, 8);
            }
            case "dragon" -> {
                for (int wing = 0; wing < 4; wing++) {
                    graphics.fill(252 + wing * 16, 120 + wing * 8, 282 + wing * 16, 122 + wing * 8, color);
                    graphics.fill(478 - wing * 16, 120 + wing * 8, 508 - wing * 16, 122 + wing * 8, color);
                }
            }
            case "frost" -> {
                drawSpark(graphics, 270, 145, color, 15);
                drawSpark(graphics, 490, 145, color, 15);
                drawSpark(graphics, centerX, 105, color, 9);
            }
            case "battle" -> {
                graphics.fill(250, centerY, 330, centerY + 3, color);
                graphics.fill(430, centerY, 510, centerY + 3, color);
                graphics.fill(centerX, 102, centerX + 3, 132, color);
                graphics.fill(centerX, 158, centerX + 3, 188, color);
            }
            case "fossil" -> {
                drawSquareRing(graphics, centerX, centerY, 66 + pulse, color);
                graphics.fill(270, 111, 278, 179, color);
                graphics.fill(482, 111, 490, 179, color);
            }
            case "eon" -> {
                drawSquareRing(graphics, centerX, centerY, 50 + pulse, color);
                graphics.fill(264, 145, 496, 147, tint(color, 100));
                graphics.fill(378, 101, 380, 189, tint(color, 100));
            }
            case "sky" -> {
                for (int line = 0; line < 5; line++) {
                    int y = 111 + line * 17;
                    graphics.fill(250 + line * 8, y, 320 + line * 18, y + 2, color);
                    graphics.fill(440 - line * 8, y, 510 - line * 18, y + 2, color);
                }
            }
            case "canopy" -> {
                for (int index = 0; index < 5; index++) {
                    int x = 260 + index * 30;
                    graphics.fill(x, 108 + (index % 2) * 46, x + 14, 120 + (index % 2) * 46, color);
                    graphics.fill(x + 5, 116 + (index % 2) * 46, x + 9, 137 + (index % 2) * 46, color);
                }
            }
            case "urban" -> {
                for (int index = 0; index < 4; index++) {
                    int x = 245 + index * 58;
                    graphics.fill(x, 112, x + 24, 114, color);
                    graphics.fill(x + 22, 112, x + 24, 178, color);
                    graphics.fill(x + 22, 176, x + 43, 178, color);
                }
            }
            case "fairy" -> {
                drawSpark(graphics, 264, 120, color, 6);
                drawSpark(graphics, 496, 120, color, 6);
                drawSpark(graphics, 264, 170, color, 4);
                drawSpark(graphics, 496, 170, color, 4);
                drawSpark(graphics, centerX, 108, color, 4);
            }
            case "shadow" -> {
                drawSquareRing(graphics, centerX, centerY, 44 + pulse, tint(theme.accent, 130));
                drawSquareRing(graphics, centerX, centerY, 78 + pulse, tint(theme.accent, 75));
            }
            case "frontier" -> {
                drawSpark(graphics, 260, 112, color, 5);
                drawSpark(graphics, 500, 112, color, 5);
                drawSpark(graphics, 260, 178, color, 4);
                drawSpark(graphics, 500, 178, color, 7);
                drawSpark(graphics, 310, 104, color, 3);
                drawSpark(graphics, 450, 186, color, 3);
            }
            case "beyond_monuments" -> {
                graphics.fill(258, 120, 274, 182, color);
                graphics.fill(486, 120, 502, 182, color);
                graphics.fill(250, 118, 282, 122, color);
                graphics.fill(478, 118, 510, 122, color);
                drawSquareRing(graphics, centerX, centerY, 42 + pulse, color);
            }
            default -> drawSpark(graphics, centerX, centerY, color, 18 + pulse / 2);
        }
    }

    private static void drawSpark(GuiGraphics graphics, int x, int y, int color, int radius) {
        graphics.fill(x - radius, y - 1, x + radius + 1, y + 2, color);
        graphics.fill(x - 1, y - radius, x + 2, y + radius + 1, color);
    }

    private static void drawSquareRing(GuiGraphics graphics, int centerX, int centerY, int radius, int color) {
        graphics.fill(centerX - radius, centerY - radius, centerX + radius, centerY - radius + 2, color);
        graphics.fill(centerX - radius, centerY + radius - 2, centerX + radius, centerY + radius, color);
        graphics.fill(centerX - radius, centerY - radius, centerX - radius + 2, centerY + radius, color);
        graphics.fill(centerX + radius - 2, centerY - radius, centerX + radius, centerY + radius, color);
    }

    private static int tint(int color, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0x00FFFFFF);
    }

    private void drawButton(GuiGraphics graphics, int x, int y, int width, int height, String label, ItemStack stack,
                            boolean enabled, double mouseX, double mouseY, int accent) {
        boolean hovered = enabled && isInside(mouseX, mouseY, x, y, width, height);
        graphics.fill(x, y, x + width, y + height, hovered ? PANEL_LIGHT : PANEL);
        graphics.fill(x, y, x + 3, y + height, enabled ? accent : 0xFF53656F);
        if (!stack.isEmpty()) graphics.renderItem(stack, x + 8, y + 12);
        graphics.drawString(font, Component.literal(label), x + 35, y + 10, enabled && hovered ? TEXT : MUTED);
        graphics.drawString(font, Component.literal(stack.isEmpty() ? "…" : shorten(stack.getHoverName().getString(), 13)),
                x + 35, y + 25, enabled ? MUTED : 0xFF71818A);
    }

    private void drawUpgraderButton(GuiGraphics graphics, double mouseX, double mouseY) {
        boolean hovered = isInside(mouseX, mouseY, 24, 184, 180, 40);
        graphics.fill(24, 184, 204, 224, hovered ? PANEL_LIGHT : PANEL);
        graphics.fill(24, 184, 27, 224, GOLD);
        graphics.drawString(font, Component.literal("ITEM UPGRADER"), 38, 192, TEXT);
        graphics.drawString(font, Component.literal("Risk items for rewards"), 38, 207, MUTED);
    }

    private void drawCurrentBannerButton(GuiGraphics graphics, double mouseX, double mouseY, BannerTheme theme) {
        boolean current = menu.isActiveBanner();
        boolean hovered = !current && isInside(mouseX, mouseY, CURRENT_BANNER_X, CURRENT_BANNER_Y,
                CURRENT_BANNER_WIDTH, CURRENT_BANNER_HEIGHT);
        graphics.fill(CURRENT_BANNER_X, CURRENT_BANNER_Y,
                CURRENT_BANNER_X + CURRENT_BANNER_WIDTH, CURRENT_BANNER_Y + CURRENT_BANNER_HEIGHT,
                hovered ? PANEL_LIGHT : PANEL_DARK);
        graphics.fill(CURRENT_BANNER_X, CURRENT_BANNER_Y, CURRENT_BANNER_X + 3,
                CURRENT_BANNER_Y + CURRENT_BANNER_HEIGHT, current ? theme.accent : GOLD);
        graphics.drawCenteredString(font, Component.literal(current ? "CURRENT BANNER" : "CURRENT BANNER · JUMP"),
                CURRENT_BANNER_X + CURRENT_BANNER_WIDTH / 2, CURRENT_BANNER_Y + 5,
                current ? theme.bright : TEXT);
    }

    private void pageButton(GuiGraphics graphics, int x, int y, int width, boolean previous, boolean enabled,
                            double mouseX, double mouseY, int accent) {
        boolean hovered = enabled && isInside(mouseX, mouseY, x, y, width, 28);
        graphics.fill(x, y, x + width, y + 28, hovered ? PANEL_LIGHT : PANEL_DARK);
        drawChevron(graphics, x + width / 2, y + 14, previous,
                enabled ? accent : 0xFF53656F);
    }

    private void themedCard(GuiGraphics graphics, int x, int y, int width, int height, BannerTheme theme) {
        graphics.fill(x, y, x + width, y + height, theme.surface);
        graphics.fill(x, y, x + width, y + 2, theme.accent);
        if (height >= 100) {
            graphics.fill(x + 352, y + 2, x + 354, y + height, theme.accent & 0x66FFFFFF);
            graphics.fill(x + 354, y + 2, x + 356, y + height, theme.bright & 0x22FFFFFF);
        }
    }

    private void drawBannerSymbol(GuiGraphics graphics, BannerTheme theme) {
        graphics.fill(348, 101, 384, 137, theme.accent & 0x44FFFFFF);
        graphics.fill(348, 101, 350, 137, theme.accent);
        graphics.renderItem(new ItemStack(theme.icon), 358, 110);
    }

    private void drawFeaturedLineup(GuiGraphics graphics, GachaBanner banner, BannerTheme theme) {
        graphics.drawString(font, Component.literal("FEATURED DROPS"), 404, 92, theme.bright);
        List<GachaEntry> featured = featuredEntries(banner);
        for (int index = 0; index < 5; index++) {
            int x = 404 + index * 46;
            GachaEntry entry = index < featured.size() ? featured.get(index) : null;
            graphics.fill(x, 108, x + 42, 164, PANEL_DARK);
            if (entry == null) continue;
            GachaRarity rarity = entry.rarity == null ? GachaRarity.COMMON : entry.rarity;
            graphics.fill(x, 108, x + 42, 110, rarityColor(rarity));
            if (!PokemonSpriteRenderer.render(graphics, entry.species, false, x + 9, 112, 22)) {
                graphics.renderItem(new ItemStack(rarityItem(rarity)), x + 13, 112);
            }
            graphics.drawCenteredString(font, Component.literal(shorten(entryLabel(entry), 6)), x + 21, 138, TEXT);
            graphics.drawCenteredString(font, Component.literal(shorten(rarity.displayName(), 6)), x + 21, 151,
                    rarityColor(rarity));
        }
    }

    private static List<GachaEntry> featuredEntries(GachaBanner banner) {
        if (banner == null || banner.entries == null) return List.of();
        return banner.entries.stream()
                .filter(entry -> entry != null && entry.rarity != null && entry.weight > 0.0 && Double.isFinite(entry.weight))
                .sorted(Comparator.comparingInt((GachaEntry entry) -> entry.rarity.rank()).reversed()
                        .thenComparingDouble(entry -> -entry.weight))
                .limit(5)
                .toList();
    }

    private static String entryLabel(GachaEntry entry) {
        if (entry.displayName != null && !entry.displayName.isBlank()) return entry.displayName;
        if (entry.species == null || entry.species.isBlank()) return "Unknown";
        int separator = entry.species.indexOf(':');
        return separator >= 0 ? entry.species.substring(separator + 1) : entry.species;
    }

    private static Item rarityItem(GachaRarity rarity) {
        return switch (rarity) {
            case COMMON -> Items.PAPER;
            case RARE -> Items.EMERALD;
            case EPIC -> Items.AMETHYST_SHARD;
            case LEGENDARY -> Items.DIAMOND;
            case MYTHIC -> Items.NETHER_STAR;
        };
    }

    private static void drawChevron(GuiGraphics graphics, int centerX, int centerY, boolean left, int color) {
        for (int offset = 0; offset < 5; offset++) {
            int x = left ? centerX + 4 - offset : centerX - 6 + offset;
            graphics.fill(x, centerY - 5 + offset, x + 2, centerY - 3 + offset, color);
            x = left ? centerX + 4 - offset : centerX - 6 + offset;
            graphics.fill(x, centerY + 3 - offset, x + 2, centerY + 5 - offset, color);
        }
    }

    private static void drawCloseIcon(GuiGraphics graphics, int centerX, int centerY, int color) {
        for (int offset = 0; offset < 6; offset++) {
            graphics.fill(centerX - 6 + offset, centerY - 6 + offset,
                    centerX - 4 + offset, centerY - 4 + offset, color);
            graphics.fill(centerX + 4 - offset, centerY - 6 + offset,
                    centerX + 6 - offset, centerY - 4 + offset, color);
        }
    }

    private static BannerTheme theme(GachaBanner banner) {
        String id = banner == null || banner.id == null ? "" : banner.id;
        return switch (id) {
            case "kanto" -> new BannerTheme(0xFFE75D5D, 0xFFFFB0A1, 0xFF42252D, Items.RED_DYE);
            case "johto" -> new BannerTheme(0xFFE0A455, 0xFFFFD28B, 0xFF423223, Items.BELL);
            case "hoenn" -> new BannerTheme(0xFF56B5E6, 0xFFA8E7FF, 0xFF14344A, Items.HEART_OF_THE_SEA);
            case "sinnoh" -> new BannerTheme(0xFF8A9DE5, 0xFFD5DDFF, 0xFF252D4A, Items.SNOWBALL);
            case "unova" -> new BannerTheme(0xFF6F9FD7, 0xFFB9D7FF, 0xFF20334D, Items.ENDER_PEARL);
            case "kalos" -> new BannerTheme(0xFFB78AE4, 0xFFE0C5FF, 0xFF322645, Items.AMETHYST_SHARD);
            case "alola" -> new BannerTheme(0xFFF2B55D, 0xFFFFE2A4, 0xFF44301F, Items.SUNFLOWER);
            case "galar" -> new BannerTheme(0xFF6EC7C0, 0xFFB9FFF4, 0xFF1E3E42, Items.IRON_SWORD);
            case "hisui" -> new BannerTheme(0xFFB78E70, 0xFFFFD5B4, 0xFF3B2B27, Items.FEATHER);
            case "paldea" -> new BannerTheme(0xFFE16EBA, 0xFFFFBCE6, 0xFF45243C, Items.CLAY_BALL);
            case "verdant" -> new BannerTheme(0xFF67C78F, 0xFFA6E5B5, 0xFF183A34, Items.OAK_SAPLING);
            case "ember" -> new BannerTheme(0xFFE97753, 0xFFFFB36A, 0xFF3C2529, Items.MAGMA_CREAM);
            case "tidal" -> new BannerTheme(0xFF56B5E6, 0xFF9BE4F5, 0xFF14344A, Items.HEART_OF_THE_SEA);
            case "voltage" -> new BannerTheme(0xFFF0D05B, 0xFFFFF0A4, 0xFF3C3A20, Items.REDSTONE);
            case "stone" -> new BannerTheme(0xFF9BA7B2, 0xFFE2E8EE, 0xFF2A3238, Items.IRON_INGOT);
            case "night" -> new BannerTheme(0xFFA995ED, 0xFFE0C6FF, 0xFF2A2442, Items.ENDER_EYE);
            case "dragon" -> new BannerTheme(0xFFE56D73, 0xFFFFA7A8, 0xFF3C252F, Items.DRAGON_BREATH);
            case "frost" -> new BannerTheme(0xFF85D4EB, 0xFFC5F6FF, 0xFF1A3641, Items.SNOW_BLOCK);
            case "battle" -> new BannerTheme(0xFFE48A7A, 0xFFFFC36B, 0xFF3A2932, Items.IRON_SWORD);
            case "fossil" -> new BannerTheme(0xFFD9AA71, 0xFFFFD6A2, 0xFF3B3026, Items.BONE);
            case "eon" -> new BannerTheme(0xFFB49FF1, 0xFFF1C8FF, 0xFF312A48, Items.AMETHYST_SHARD);
            case "sky" -> new BannerTheme(0xFF8CB7F0, 0xFFD5E8FF, 0xFF223A50, Items.FEATHER);
            case "canopy" -> new BannerTheme(0xFF5CCA9B, 0xFFB5F2C8, 0xFF1A4038, Items.FLOWERING_AZALEA);
            case "urban" -> new BannerTheme(0xFF64D5D6, 0xFFB7FFFF, 0xFF1B3B42, Items.REDSTONE_TORCH);
            case "fairy" -> new BannerTheme(0xFFE38FC4, 0xFFFFCEE7, 0xFF432B40, Items.PINK_DYE);
            case "shadow" -> new BannerTheme(0xFF9177D5, 0xFFD2BCFF, 0xFF28213E, Items.OBSIDIAN);
            case "frontier" -> new BannerTheme(0xFFE6B95E, 0xFFFFE4A4, 0xFF3A3120, Items.SPYGLASS);
            case "beyond_monuments" -> new BannerTheme(0xFFE16EBA, 0xFFFFBCE6, 0xFF45243C, Items.NETHER_STAR);
            default -> new BannerTheme(0xFF70D8D4, 0xFFB9FFFF, 0xFF17343D, Items.AMETHYST_SHARD);
        };
    }

    private record BannerTheme(int accent, int bright, int surface, Item icon) {}

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (closing || button != 0) return true;
        float scale = uiScale();
        mouseX = toDesignX(mouseX, scale);
        mouseY = toDesignY(mouseY, scale);
        if (isInside(mouseX, mouseY, WIDTH - 44, 8, 36, 30)) {
            closing = true;
            onClose();
            return true;
        }
        if (revealActive()) return true;
        if (isInside(mouseX, mouseY, 24, 184, 180, 40)) { clickMenuButton(GachaMenu.BUTTON_OPEN_UPGRADER); return true; }
        if (isInside(mouseX, mouseY, CURRENT_BANNER_X, CURRENT_BANNER_Y,
                CURRENT_BANNER_WIDTH, CURRENT_BANNER_HEIGHT) && !menu.isActiveBanner()) {
            clickBannerButton(GachaMenu.BUTTON_CURRENT_BANNER);
            return true;
        }
        if (isInside(mouseX, mouseY, FEATURED_PREVIOUS_X, FEATURED_NAV_Y,
                FEATURED_NAV_WIDTH, FEATURED_NAV_HEIGHT) && menu.canGoPrevious()) {
            clickBannerButton(GachaMenu.BUTTON_PREVIOUS_BANNER);
            return true;
        }
        if (isInside(mouseX, mouseY, FEATURED_NEXT_X, FEATURED_NAV_Y,
                FEATURED_NAV_WIDTH, FEATURED_NAV_HEIGHT) && menu.canGoNext()) {
            clickBannerButton(GachaMenu.BUTTON_NEXT_BANNER);
            return true;
        }
        if (isInside(mouseX, mouseY, 510, 184, 105, 40)) { clickMenuSlot(GachaMenu.DRAW_ONE_SLOT); return true; }
        if (isInside(mouseX, mouseY, 627, 184, 105, 40)) { clickMenuSlot(GachaMenu.DRAW_TEN_SLOT); return true; }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        return true;
    }

    private void clickMenuSlot(int slot) {
        Player player = minecraft.player;
        if (minecraft.gameMode != null && player != null) {
            minecraft.gameMode.handleInventoryMouseClick(menu.containerId, slot, 0,
                    net.minecraft.world.inventory.ClickType.PICKUP, player);
        }
    }

    private void clickBannerButton(int buttonId) {
        if (minecraft.gameMode != null && minecraft.player != null) {
            menu.clientBannerButton(buttonId);
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }

    private void clickMenuButton(int buttonId) {
        if (minecraft.gameMode != null && minecraft.player != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (closing) return true;
        if (keyCode == 256) { closing = true; onClose(); return true; }
        if (revealActive()) return true;
        if (keyCode == 263 && menu.canGoPrevious()) { clickBannerButton(GachaMenu.BUTTON_PREVIOUS_BANNER); return true; }
        if (keyCode == 262 && menu.canGoNext()) { clickBannerButton(GachaMenu.BUTTON_NEXT_BANNER); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private static boolean isPane(ItemStack stack) {
        return !stack.isEmpty() && stack.is(Items.GRAY_STAINED_GLASS_PANE);
    }

    private boolean revealActive() {
        return revealStartedAt >= 0L
                && System.currentTimeMillis() - revealStartedAt < REVEAL_DURATION_MS;
    }

    private static GachaRarity historyRarity(ItemStack stack) {
        if (stack.is(Items.NETHER_STAR)) return GachaRarity.MYTHIC;
        if (stack.is(Items.DIAMOND)) return GachaRarity.LEGENDARY;
        if (stack.is(Items.AMETHYST_SHARD)) return GachaRarity.EPIC;
        if (stack.is(Items.EMERALD)) return GachaRarity.RARE;
        return GachaRarity.COMMON;
    }

    private static boolean isShinyLabel(String label) {
        return label != null && label.regionMatches(true, 0, "Shiny ", 0, 6);
    }

    private static int rarityColor(GachaRarity rarity) {
        return switch (rarity) {
            case COMMON -> 0xFFB8C0C4;
            case RARE -> 0xFF75A9E6;
            case EPIC -> 0xFFC68BDF;
            case LEGENDARY -> 0xFFF0C26E;
            case MYTHIC -> 0xFFF29BD5;
        };
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static String shorten(String text, int max) {
        if (text == null || text.isBlank()) return "—";
        return text.length() <= max ? text : text.substring(0, Math.max(0, max - 1)) + "…";
    }

    private static String formatDuration(long seconds) {
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long remaining = seconds % 60L;
        if (hours > 0) return hours + "h " + minutes + "m";
        return minutes + "m " + remaining + "s";
    }

    private static String shortDuration(long seconds) {
        if (seconds >= 60L) return (seconds / 60L) + "m " + (seconds % 60L) + "s";
        return seconds + "s";
    }

    private float uiScale() {
        return Math.max(0.35f, Math.min(1.0f, Math.min((width - 24.0f) / WIDTH, (height - 24.0f) / HEIGHT)));
    }

    private double toDesignX(double screenX, float scale) { return (screenX - width / 2.0) / scale + WIDTH / 2.0; }
    private double toDesignY(double screenY, float scale) { return (screenY - height / 2.0) / scale + HEIGHT / 2.0; }

}
