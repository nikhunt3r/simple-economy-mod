package com.simpleeconomy.market;

import com.simpleeconomy.network.QuickSalePriceEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientQuickSaleCache {

    private static final Map<String, Long> prices =
            new HashMap<>();

    public static void setPrices(
            List<QuickSalePriceEntry> entries
    ) {

        prices.clear();

        for (QuickSalePriceEntry entry : entries) {

            if (entry.price() > 0) {
                prices.put(entry.itemId(), entry.price());
            }
        }
    }

    public static long getPrice(String itemId) {
        return prices.getOrDefault(itemId, 0L);
    }

    public static boolean canQuickSell(String itemId) {
        return getPrice(itemId) > 0;
    }
}