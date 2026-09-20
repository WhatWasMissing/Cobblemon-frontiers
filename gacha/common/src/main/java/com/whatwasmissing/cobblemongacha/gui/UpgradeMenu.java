package com.whatwasmissing.cobblemongacha.gui;

import com.whatwasmissing.cobblemongacha.core.GachaService;
import com.whatwasmissing.cobblemongacha.core.GachaUpgradeService;
import com.whatwasmissing.cobblemongacha.core.ItemValueService;
import com.whatwasmissing.cobblemongacha.core.UpgradeCatalog;
import com.whatwasmissing.cobblemongacha.core.UpgradeTarget;
import com.whatwasmissing.cobblemongacha.config.GachaConfig;
import com.whatwasmissing.cobblemongacha.network.GachaMenuSyncPayload;
import com.whatwasmissing.cobblemongacha.network.UpgradeResultPayload;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.time.Instant;

/** Server state for the Upgrader items-inspired source/target gamble. */
public final class UpgradeMenu extends ChestMenu {
    public static final int BUTTON_PREVIOUS_TARGET_PAGE = 0;
    public static final int BUTTON_NEXT_TARGET_PAGE = 1;
    public static final int BUTTON_ITEM_TARGETS = 2;
    public static final int BUTTON_POKEMON_TARGETS = 3;
    public static final int BUTTON_OPEN_DRAWS = 4;
    public static final int BUTTON_SOURCE_BASE = 1000;
    public static final int BUTTON_TARGET_BASE = 2000;
    public static final int BUTTON_MULTIPLIER_BASE = 3000;
    public static final int SOURCE_DISPLAY_SLOT = 4;
    public static final int TARGET_DISPLAY_SLOT = 22;
    public static final int UPGRADE_SLOT = 31;
    public static final int OPEN_DRAWS_SLOT = 49;
    public static final int MULTIPLIER_START = 35;
    private static final int SIZE = 54;
    private static final int[] MULTIPLIERS = {1, 2, 4, 8};

    private final SimpleContainer upgradeInventory;
    private int sourceContainerSlot = -1;
    private int targetIndex = -1;
    private int targetPage;
    private int targetAmount = 1;
    private UpgradeCatalog.TargetCategory targetCategory = UpgradeCatalog.TargetCategory.ITEMS;
    private List<UpgradeTarget> syncedItemTargets = List.of();
    private List<UpgradeTarget> syncedPokemonTargets = List.of();
    private boolean serverSnapshot;
    private long syncedServerEpochSeconds;
    private long syncedLocalEpochSeconds;
    private long syncedGamblingCooldownSeconds;
    private double syncedLegendaryPokemonChance = 0.001;
    private double syncedLegendaryPokemonMinimumSourceValue = 5000.0;
    private long upgradeResultSequence;
    private UpgradeResultPayload lastUpgradeResult;

    public UpgradeMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(SIZE));
    }

    private UpgradeMenu(int containerId, Inventory inventory, SimpleContainer upgradeInventory) {
        super(GachaMenuTypes.UPGRADE_MENU, containerId, inventory, upgradeInventory, 6);
        this.upgradeInventory = upgradeInventory;
        refresh(inventory.player);
    }

    public void refresh(Player player) {
        for (int slot = 0; slot < SIZE; slot++) upgradeInventory.setItem(slot, pane(Items.GRAY_STAINED_GLASS_PANE));
        ItemStack source = sourceStack();
        if (!source.isEmpty()) {
            ItemStack display = source.copy();
            display.setCount(Math.min(64, source.getCount()));
            display.set(DataComponents.CUSTOM_NAME, Component.literal("Source · value " + valueText(ItemValueService.value(source)))
                    .withStyle(ChatFormatting.GRAY));
            upgradeInventory.setItem(SOURCE_DISPLAY_SLOT, display);
        } else {
            upgradeInventory.setItem(SOURCE_DISPLAY_SLOT, named(Items.IRON_INGOT, "Place source item", ChatFormatting.GRAY));
        }

        UpgradeTarget target = selectedTarget();
        if (target != null) {
            int effectiveAmount = targetAmount();
            ItemStack display = target.displayStack(effectiveAmount);
            String targetInfo = target.pokemon() ? "Rarity · " + target.rarity().displayName()
                    : "Target · value " + valueText(ItemValueService.targetValue(target.displayStack(1), effectiveAmount));
            display.set(DataComponents.CUSTOM_NAME, Component.literal(targetInfo)
                    .withStyle(target.formatting()));
            upgradeInventory.setItem(TARGET_DISPLAY_SLOT, display);
        } else {
            upgradeInventory.setItem(TARGET_DISPLAY_SLOT, named(Items.DIAMOND, "Choose a target", ChatFormatting.AQUA));
        }
        upgradeInventory.setItem(UPGRADE_SLOT, named(Items.GOLD_BLOCK,
                target == null || source.isEmpty() ? "Select source and target" : "Upgrade · "
                        + percent(GachaUpgradeService.chance(this)),
                target == null || source.isEmpty() ? ChatFormatting.DARK_GRAY : ChatFormatting.GOLD));
        upgradeInventory.setItem(OPEN_DRAWS_SLOT, named(Items.COMPASS, "Open Gacha Draws", ChatFormatting.AQUA));

        for (int index = 0; index < MULTIPLIERS.length; index++) {
            int amount = MULTIPLIERS[index];
            upgradeInventory.setItem(MULTIPLIER_START + index, named(amount == targetAmount() ? Items.GOLD_NUGGET : Items.IRON_NUGGET,
                    "×" + amount + " target", amount == targetAmount() ? ChatFormatting.GOLD : ChatFormatting.GRAY));
        }
    }

    private static String valueText(double value) {
        if (value >= 1000.0) return String.format(java.util.Locale.ROOT, "%.0fk", value / 1000.0);
        return String.format(java.util.Locale.ROOT, "%.0f", value);
    }

    private static String percent(double chance) {
        double percent = chance * 100.0;
        return String.format(java.util.Locale.ROOT, percent < 0.1 ? "%.2f%%" : "%.1f%%", percent);
    }

    private static ItemStack pane(net.minecraft.world.item.Item item) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(" "));
        return stack;
    }

    private static ItemStack named(net.minecraft.world.item.Item item, String label, ChatFormatting formatting) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(label).withStyle(formatting));
        return stack;
    }

    public ItemStack sourceStack() {
        if (sourceContainerSlot < 0 || sourceContainerSlot >= slots.size()) return ItemStack.EMPTY;
        return getSlot(sourceContainerSlot).getItem();
    }

    public boolean consumeSourceOne() {
        if (sourceContainerSlot < 0 || sourceContainerSlot >= slots.size()) return false;
        if (!getSlot(sourceContainerSlot).hasItem()) return false;
        getSlot(sourceContainerSlot).remove(1);
        return true;
    }

    public UpgradeTarget selectedTarget() {
        List<UpgradeTarget> targets = targetsForCategory();
        return targetIndex >= 0 && targetIndex < targets.size() ? targets.get(targetIndex) : null;
    }

    public UpgradeCatalog.TargetCategory targetCategory() { return targetCategory; }
    public int targetPage() { return targetPage; }
    public int targetPageCount() {
        List<UpgradeTarget> targets = targetsForCategory();
        return Math.max(1, (targets.size() + UpgradeCatalog.TARGETS_PER_PAGE - 1) / UpgradeCatalog.TARGETS_PER_PAGE);
    }
    public int targetAmount() {
        UpgradeTarget target = selectedTarget();
        return target != null && target.pokemon() ? 1 : targetAmount;
    }
    public int sourceContainerSlot() { return sourceContainerSlot; }
    public double legendaryPokemonChance() {
        GachaConfig config = GachaService.config();
        return serverSnapshot ? syncedLegendaryPokemonChance : config == null ? 0.001 : config.legendaryPokemonChance;
    }
    public double legendaryPokemonMinimumSourceValue() {
        GachaConfig config = GachaService.config();
        return serverSnapshot ? syncedLegendaryPokemonMinimumSourceValue
                : config == null ? 5000.0 : config.legendaryPokemonMinimumSourceValue;
    }

    public long gamblingCooldownSeconds() {
        if (!serverSnapshot) return 0L;
        long elapsed = Math.max(0L, Instant.now().getEpochSecond() - syncedLocalEpochSeconds);
        return Math.max(0L, syncedGamblingCooldownSeconds - elapsed);
    }

    private List<UpgradeTarget> targetsForCategory() {
        if (!serverSnapshot) return UpgradeCatalog.targets(targetCategory);
        return targetCategory == UpgradeCatalog.TargetCategory.POKEMON ? syncedPokemonTargets : syncedItemTargets;
    }

    public List<UpgradeTarget> targetsForDisplay() { return targetsForCategory(); }

    public long upgradeResultSequence() { return upgradeResultSequence; }
    public UpgradeResultPayload lastUpgradeResult() { return lastUpgradeResult; }

    public void applyUpgradeResult(UpgradeResultPayload payload) {
        lastUpgradeResult = payload;
        upgradeResultSequence++;
    }

    public void syncToClient(ServerPlayer player) {
        if (player.containerMenu != this) return;
        player.connection.send(new ClientboundCustomPayloadPacket(
                GachaMenuSyncPayload.forUpgradeMenu(containerId, player.getUUID())));
    }

    public void applyServerSnapshot(GachaMenuSyncPayload payload) {
        if (payload.drawMenu() || payload.menuId() != containerId) return;
        syncedItemTargets = payload.toItemTargets();
        syncedPokemonTargets = payload.toPokemonTargets();
        serverSnapshot = true;
        syncedServerEpochSeconds = payload.serverEpochSeconds();
        syncedLocalEpochSeconds = Instant.now().getEpochSecond();
        syncedGamblingCooldownSeconds = Math.max(0L, payload.gamblingCooldownSeconds());
        syncedLegendaryPokemonChance = Math.max(0.0, Math.min(1.0, payload.legendaryPokemonChance()));
        syncedLegendaryPokemonMinimumSourceValue = Math.max(0.0, payload.legendaryPokemonMinimumSourceValue());
    }

    public boolean canGoPrevious() { return targetPage > 0; }
    public boolean canGoNext() { return targetPage + 1 < targetPageCount(); }

    public void clientTargetPageButton(int buttonId) {
        if (buttonId == BUTTON_PREVIOUS_TARGET_PAGE && canGoPrevious()) targetPage--;
        if (buttonId == BUTTON_NEXT_TARGET_PAGE && canGoNext()) targetPage++;
    }

    public void clientSelectCategory(UpgradeCatalog.TargetCategory category) {
        targetCategory = category;
        targetPage = 0;
        targetIndex = -1;
        targetAmount = 1;
    }

    public void clientSelectTarget(int index) {
        targetIndex = index;
        if (selectedTarget() != null && selectedTarget().pokemon()) targetAmount = 1;
    }
    public void clientSelectSource(int containerSlot) { sourceContainerSlot = containerSlot; }
    public void clientSelectMultiplier(int index) {
        if (selectedTarget() != null && selectedTarget().pokemon()) {
            targetAmount = 1;
        } else if (index >= 0 && index < MULTIPLIERS.length) {
            targetAmount = MULTIPLIERS[index];
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == BUTTON_OPEN_DRAWS) {
            if (player instanceof ServerPlayer serverPlayer) GachaService.openDrawMenu(serverPlayer);
            return true;
        }
        if (buttonId == BUTTON_ITEM_TARGETS) return changeCategory(player, UpgradeCatalog.TargetCategory.ITEMS);
        if (buttonId == BUTTON_POKEMON_TARGETS) return changeCategory(player, UpgradeCatalog.TargetCategory.POKEMON);
        if (buttonId == BUTTON_PREVIOUS_TARGET_PAGE) return changePage(player, -1);
        if (buttonId == BUTTON_NEXT_TARGET_PAGE) return changePage(player, 1);
        if (buttonId >= BUTTON_SOURCE_BASE && buttonId < BUTTON_SOURCE_BASE + 90) {
            return selectSource(player, buttonId - BUTTON_SOURCE_BASE);
        }
        if (buttonId >= BUTTON_MULTIPLIER_BASE && buttonId < BUTTON_MULTIPLIER_BASE + 4) {
            if (selectedTarget() != null && selectedTarget().pokemon()) targetAmount = 1;
            else targetAmount = new int[]{1, 2, 4, 8}[buttonId - BUTTON_MULTIPLIER_BASE];
            refresh(player);
            broadcastChanges();
            return true;
        }
        if (buttonId >= BUTTON_TARGET_BASE && buttonId < BUTTON_TARGET_BASE + targetsForCategory().size()) {
            int index = buttonId - BUTTON_TARGET_BASE;
            if (index / UpgradeCatalog.TARGETS_PER_PAGE != targetPage) return false;
            targetIndex = index;
            if (selectedTarget() != null && selectedTarget().pokemon()) targetAmount = 1;
            refresh(player);
            broadcastChanges();
            return true;
        }
        return false;
    }

    private boolean selectSource(Player player, int containerSlot) {
        if (containerSlot < 54 || containerSlot >= slots.size() || !getSlot(containerSlot).hasItem()) return false;
        sourceContainerSlot = containerSlot;
        refresh(player);
        broadcastChanges();
        return true;
    }

    private boolean changeCategory(Player player, UpgradeCatalog.TargetCategory category) {
        if (targetCategory == category) return false;
        targetCategory = category;
        targetPage = 0;
        targetIndex = -1;
        targetAmount = 1;
        refresh(player);
        broadcastChanges();
        return true;
    }

    private boolean changePage(Player player, int delta) {
        int next = targetPage + delta;
        if (next < 0 || next >= targetPageCount()) return false;
        targetPage = next;
        refresh(player);
        broadcastChanges();
        return true;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < SIZE) {
            if (clickType == ClickType.PICKUP && button == 0 && player instanceof ServerPlayer serverPlayer) {
                if (slotId == UPGRADE_SLOT) {
                    GachaUpgradeService.attempt(serverPlayer, this);
                } else if (slotId == OPEN_DRAWS_SLOT) {
                    GachaService.openDrawMenu(serverPlayer);
                } else if (slotId == MULTIPLIER_START) {
                    targetAmount = 1;
                    refresh(serverPlayer);
                    broadcastChanges();
                } else if (slotId >= MULTIPLIER_START && slotId < MULTIPLIER_START + MULTIPLIERS.length) {
                    targetAmount = selectedTarget() != null && selectedTarget().pokemon()
                            ? 1 : MULTIPLIERS[slotId - MULTIPLIER_START];
                    refresh(serverPlayer);
                    broadcastChanges();
                }
            }
            return;
        }
        // The custom client sends source selection through menu buttons. Consume
        // ordinary inventory clicks so a wager cannot be moved while open.
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
        upgradeInventory.clearContent();
    }
}
