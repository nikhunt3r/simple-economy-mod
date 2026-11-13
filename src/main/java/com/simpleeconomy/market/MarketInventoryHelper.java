
package com.simpleeconomy.market;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class MarketInventoryHelper {

    public static boolean canFitCompletely(
            Inventory inventory,
            ItemStack incoming
    ) {
        if (incoming == null || incoming.isEmpty()) {
            return true;
        }

        int remaining = incoming.getCount();

        // Сначала проверяем свободное место
        // в уже существующих подходящих стопках.
        for (ItemStack current : inventory.items) {

            if (current.isEmpty()) {
                continue;
            }

            if (ItemStack.isSameItemSameComponents(current, incoming)) {

                int maxStackSize = Math.min(
                        current.getMaxStackSize(),
                        incoming.getMaxStackSize()
                );

                int available = Math.max(
                        0,
                        maxStackSize - current.getCount()
                );

                remaining -= available;

                if (remaining <= 0) {
                    return true;
                }
            }
        }

        // Затем учитываем полностью свободные слоты.
        for (ItemStack current : inventory.items) {

            if (current.isEmpty()) {

                remaining -= incoming.getMaxStackSize();

                if (remaining <= 0) {
                    return true;
                }
            }
        }

        return false;
    }
}
