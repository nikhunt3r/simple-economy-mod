package com.simpleeconomy.market;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MarketPriceData extends SavedData {

    private static final String DATA_NAME =
            "simpleeconomy_market_prices";

    // Ключ: minecraft:iron_ingot
    // Значение: цена за одну штуку
    private final Map<String, Long> prices = new HashMap<>();

    private static final Factory<MarketPriceData> FACTORY =
            new Factory<>(
                    MarketPriceData::new,
                    MarketPriceData::load,
                    null
            );

    public static MarketPriceData get(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(FACTORY, DATA_NAME);
    }

    public long getPrice(ResourceLocation itemId) {
        return prices.getOrDefault(itemId.toString(), 0L);
    }

    public Map<String, Long> getAllPrices() {
        return Collections.unmodifiableMap(prices);
    }

    public void setPrice(ResourceLocation itemId, long price) {

        if (price <= 0) {
            throw new IllegalArgumentException(
                    "Цена должна быть больше нуля."
            );
        }

        prices.put(itemId.toString(), price);
        setDirty();
    }

    public boolean removePrice(ResourceLocation itemId) {

        boolean removed = prices.remove(itemId.toString()) != null;

        if (removed) {
            setDirty();
        }

        return removed;
    }

    public static MarketPriceData load(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {

        MarketPriceData data = new MarketPriceData();

        CompoundTag savedPrices = tag.getCompound("prices");

        for (String key : savedPrices.getAllKeys()) {

            ResourceLocation id = ResourceLocation.tryParse(key);
            long price = savedPrices.getLong(key);

            if (id != null && price > 0) {
                data.prices.put(id.toString(), price);
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {

        CompoundTag savedPrices = new CompoundTag();

        for (Map.Entry<String, Long> entry : prices.entrySet()) {
            savedPrices.putLong(entry.getKey(), entry.getValue());
        }

        tag.put("prices", savedPrices);

        return tag;
    }
}
