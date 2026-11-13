package com.simpleeconomy.market;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MarketData extends SavedData {

    private static final String DATA_NAME = "simpleeconomy_market";

    public static final int COMMISSION_PERCENT = 5;

    private long nextListingId = 1;

    private final Map<Long, MarketListing> listings =
            new LinkedHashMap<>();

    private static final Factory<MarketData> FACTORY =
            new Factory<>(
                    MarketData::new,
                    MarketData::load,
                    null
            );

    // Получение хранилища рынка
    public static MarketData get(MinecraftServer server) {

        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(FACTORY, DATA_NAME);
    }
    // Немедленно записываем изменённые данные на диск
    public static void saveNow(MinecraftServer server) {

    server.overworld()
            .getDataStorage()
            .save();
    }

    // Выставление предмета
    public MarketListing createListing(
            UUID sellerId,
            String sellerName,
            ItemStack item,
            long price
    ) {

        if (item == null || item.isEmpty()) {
            throw new IllegalArgumentException(
                    "Нельзя выставить пустой предмет."
            );
        }

        if (price <= 0) {
            throw new IllegalArgumentException(
                    "Цена должна быть больше нуля."
            );
        }

        if (nextListingId <= 0 || nextListingId == Long.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Не удалось создать новое объявление."
            );
        }

        MarketListing listing = new MarketListing(
                nextListingId++,
                sellerId,
                sellerName,
                item,
                price,
                System.currentTimeMillis()
        );

        listings.put(listing.id(), listing);

        setDirty();

        return listing;
    }

    // Получение объявления
    public MarketListing getListing(long id) {
        return listings.get(id);
    }

    // Получение всех объявлений
    public List<MarketListing> getListings() {

        List<MarketListing> result =
                new ArrayList<>(listings.values());

        // Сначала показываем новые объявления
        Collections.reverse(result);

        return List.copyOf(result);
    }

    // Получение объявлений конкретного игрока
    public List<MarketListing> getPlayerListings(UUID playerId) {

        return getListings()
                .stream()
                .filter(listing ->
                        listing.sellerId().equals(playerId)
                )
                .toList();
    }

    // Отмена объявления владельцем
    public MarketListing cancelListing(
            long listingId,
            UUID sellerId
    ) {

        MarketListing listing = listings.get(listingId);

        if (listing == null) {
            return null;
        }

        if (!listing.sellerId().equals(sellerId)) {
            return null;
        }

        listings.remove(listingId);

        setDirty();

        return listing;
    }

    public int getListingCount() {
        return listings.size();
    }

    // Загрузка рынка из файла мира
    public static MarketData load(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {

        MarketData data = new MarketData();

        data.nextListingId = Math.max(
                1,
                tag.getLong("nextListingId")
        );

        ListTag savedListings = tag.getList(
                "listings",
                Tag.TAG_COMPOUND
        );

        for (int i = 0; i < savedListings.size(); i++) {

            CompoundTag entry = savedListings.getCompound(i);

            try {

                long id = entry.getLong("id");

                UUID sellerId = UUID.fromString(
                        entry.getString("sellerId")
                );

                String sellerName = entry.getString("sellerName");

                long price = entry.getLong("price");

                long createdAt = entry.getLong("createdAt");

                ItemStack item = ItemStack.parseOptional(
                        registries,
                        entry.getCompound("item")
                );

                if (id <= 0 || price <= 0 || item.isEmpty()) {
                    continue;
                }

                MarketListing listing = new MarketListing(
                        id,
                        sellerId,
                        sellerName,
                        item,
                        price,
                        createdAt
                );

                data.listings.put(id, listing);

                if (id < Long.MAX_VALUE) {
                    data.nextListingId = Math.max(
                            data.nextListingId,
                            id + 1
                    );
                }

            } catch (IllegalArgumentException exception) {
                // Пропускаем повреждённое объявление
            }
        }

        return data;
    }

    // Сохранение рынка
    @Override
    public CompoundTag save(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {

        tag.putLong("nextListingId", nextListingId);

        ListTag savedListings = new ListTag();

        for (MarketListing listing : listings.values()) {

            CompoundTag entry = new CompoundTag();

            entry.putLong("id", listing.id());

            entry.putString(
                    "sellerId",
                    listing.sellerId().toString()
            );

            entry.putString(
                    "sellerName",
                    listing.sellerName()
            );

            entry.putLong("price", listing.price());

            entry.putLong("createdAt", listing.createdAt());

            entry.put(
        "item",
        listing.item().save(registries)
            );

            savedListings.add(entry);
        }

        tag.put("listings", savedListings);

        return tag;
    }
    // Удаление объявления после покупки
public MarketListing removeListing(long id) {

    MarketListing listing = listings.remove(id);

    if (listing != null) {
        setDirty();
    }

    return listing;
}

// Восстановление объявления, если покупка не состоялась
public void restoreListing(MarketListing listing) {

    if (listing == null) {
        return;
    }

    listings.putIfAbsent(listing.id(), listing);

    setDirty();
}
}
