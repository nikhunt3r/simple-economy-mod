package com.simpleeconomy.market;

import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record MarketListing(
        long id,
        UUID sellerId,
        String sellerName,
        ItemStack item,
        long price,
        long createdAt
) {

    public MarketListing {
        item = item.copy();
    }

    // Возвращаем копию, чтобы внешний код
    // не мог изменить предмет внутри объявления.
    @Override
    public ItemStack item() {
        return item.copy();
    }
}