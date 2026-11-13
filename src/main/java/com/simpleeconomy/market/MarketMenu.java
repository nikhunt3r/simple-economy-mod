package com.simpleeconomy.market;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.List;

public class MarketMenu extends AbstractContainerMenu {

    private final ItemStackHandler offering;
    private final MarketOfferSlot offeringSlot;
    private final List<InventorySlot> playerInventorySlots =
        new ArrayList<>();
    private int activeTab = 0;
    // Клиентский конструктор
    public MarketMenu(int containerId, Inventory inventory) {
        this(
                containerId,
                inventory,
                new ItemStackHandler(1)
        );
    }

    // Серверный конструктор
    public MarketMenu(
            int containerId,
            Inventory inventory,
            ItemStackHandler offering
    ) {

        super(ModMenus.MARKET_MENU.get(), containerId);

        this.offering = offering;

        // Слот для выставляемого товара
        this.offeringSlot = new MarketOfferSlot(
                offering,
                0,
                30,
                95
        );

        this.addSlot(offeringSlot);

        // 27 слотов основного инвентаря
for (int row = 0; row < 3; row++) {

    for (int column = 0; column < 9; column++) {

        InventorySlot slot = new InventorySlot(
                inventory,
                column + row * 9 + 9,
                169 + column * 18,
                184 + row * 18
        );

        playerInventorySlots.add(slot);
        this.addSlot(slot);
    }
}

// 9 слотов быстрого доступа
for (int column = 0; column < 9; column++) {

    InventorySlot slot = new InventorySlot(
            inventory,
            column,
            169 + column * 18,
            242
    );

    playerInventorySlots.add(slot);
    this.addSlot(slot);
}
setActiveTab(0);
    }

    public int getActiveTab() {
    return activeTab;
}

public void setActiveTab(int tab) {

    activeTab = Math.max(0, Math.min(3, tab));

    boolean hasItemSlot = activeTab == 2 || activeTab == 3;

    setOfferingVisible(hasItemSlot);
    setInventoryVisible(hasItemSlot);
}

// Извлекаем предмет для быстрой продажи
public ItemStack extractOffering() {

    return offering.extractItem(
            0,
            Integer.MAX_VALUE,
            false
    );
}

// Возвращаем предмет, если операция не состоялась
public void restoreOffering(ItemStack item) {

    if (item == null || item.isEmpty()) {
        return;
    }

    offering.setStackInSlot(0, item.copy());
}

    public ItemStack getOfferingStack() {
        return offering.getStackInSlot(0).copy();
    }

    public void clearOffering() {
        offering.extractItem(0, Integer.MAX_VALUE, false);
    }

    public void setOfferingVisible(boolean visible) {
        offeringSlot.setEnabled(visible);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    // Быстрое перемещение предметов через Shift
    @Override
    public ItemStack quickMoveStack(
            Player player,
            int index
    ) {

        Slot slot = this.slots.get(index);

        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack original = slot.getItem();
        ItemStack result = original.copy();

        if (index == 0) {

            // Из слота продажи в инвентарь
            if (!this.moveItemStackTo(
                    original,
                    1,
                    37,
                    true
            )) {
                return ItemStack.EMPTY;
            }

        } else {

            // Из инвентаря в слот продажи
            if (!this.moveItemStackTo(
                    original,
                    0,
                    1,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        }

        if (original.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        slot.onTake(player, original);

        return result;
    }

    // Возвращаем незабранный предмет при закрытии окна
    @Override
    public void removed(Player player) {

        super.removed(player);

        if (!player.level().isClientSide) {

            ItemStack remaining = offering.extractItem(
                    0,
                    Integer.MAX_VALUE,
                    false
            );

            if (!remaining.isEmpty()) {

                player.getInventory().add(remaining);

                if (!remaining.isEmpty()) {
                    player.drop(remaining, false);
                }
            }
        }
    }

    private static class MarketOfferSlot extends SlotItemHandler {

        private boolean enabled = true;

        public MarketOfferSlot(
                ItemStackHandler handler,
                int index,
                int x,
                int y
        ) {
            super(handler, index, x, y);
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public boolean isActive() {
            return enabled;
        }
    }
    // Управление видимостью инвентаря
public void setInventoryVisible(boolean visible) {

    for (InventorySlot slot : playerInventorySlots) {
        slot.setVisible(visible);
    }
}

// Собственный слот игрока с переключаемой видимостью
private static class InventorySlot extends Slot {

    private boolean visible = true;

    public InventorySlot(
            Inventory inventory,
            int index,
            int x,
            int y
    ) {
        super(inventory, index, x, y);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isActive() {
        return visible;
    }
}
}