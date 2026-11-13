package com.simpleeconomy.market;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MarketMailboxData extends SavedData {

    private static final String DATA_NAME =
            "simpleeconomy_market_mail";

    private final Map<UUID, List<ItemStack>> mailboxes =
            new HashMap<>();

    private static final Factory<MarketMailboxData> FACTORY =
            new Factory<>(
                    MarketMailboxData::new,
                    MarketMailboxData::load,
                    null
            );

    public static MarketMailboxData get(MinecraftServer server) {

        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(FACTORY, DATA_NAME);
    }

    // Добавление предмета в почту
    public void addItem(UUID playerId, ItemStack item) {

        if (item == null || item.isEmpty()) {
            return;
        }

        mailboxes.computeIfAbsent(
                playerId,
                id -> new ArrayList<>()
        ).add(item.copy());

        setDirty();
    }

    // Количество записей в почте
    public int getPendingStackCount(UUID playerId) {

        List<ItemStack> items = mailboxes.get(playerId);

        return items == null ? 0 : items.size();
    }

    // Общее количество предметов в почте
    public long getPendingItemCount(UUID playerId) {

        List<ItemStack> items = mailboxes.get(playerId);

        if (items == null) {
            return 0;
        }

        long count = 0;

        for (ItemStack item : items) {
            count += item.getCount();
        }

        return count;
    }

    // Забираем предметы в инвентарь
    public long claimItems(
            UUID playerId,
            Inventory inventory
    ) {

        List<ItemStack> stored = mailboxes.get(playerId);

        if (stored == null || stored.isEmpty()) {
            return 0;
        }

        List<ItemStack> remaining = new ArrayList<>();

        long delivered = 0;

        for (ItemStack savedItem : stored) {

            ItemStack item = savedItem.copy();

            int previousCount = item.getCount();

            inventory.add(item);

            delivered += previousCount - item.getCount();

            // Непоместившийся остаток остаётся в почте
            if (!item.isEmpty()) {
                remaining.add(item);
            }
        }

        if (remaining.isEmpty()) {
            mailboxes.remove(playerId);
        } else {
            mailboxes.put(playerId, remaining);
        }

        setDirty();

        return delivered;
    }

    // Загрузка почты
    public static MarketMailboxData load(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {

        MarketMailboxData data = new MarketMailboxData();

        CompoundTag savedMail = tag.getCompound("mailboxes");

        for (String key : savedMail.getAllKeys()) {

            try {

                UUID playerId = UUID.fromString(key);

                ListTag savedItems = savedMail.getList(
                        key,
                        Tag.TAG_COMPOUND
                );

                List<ItemStack> items = new ArrayList<>();

                for (int i = 0; i < savedItems.size(); i++) {

                    ItemStack item = ItemStack.parseOptional(
                            registries,
                            savedItems.getCompound(i)
                    );

                    if (!item.isEmpty()) {
                        items.add(item);
                    }
                }

                if (!items.isEmpty()) {
                    data.mailboxes.put(playerId, items);
                }

            } catch (IllegalArgumentException exception) {
                // Пропускаем некорректную запись
            }
        }

        return data;
    }

    // Сохранение почты
    @Override
    public CompoundTag save(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {

        CompoundTag savedMail = new CompoundTag();

        for (Map.Entry<UUID, List<ItemStack>> entry
                : mailboxes.entrySet()) {

            ListTag savedItems = new ListTag();

            for (ItemStack item : entry.getValue()) {

                savedItems.add(
                item.save(registries)
            );
            }

            savedMail.put(
                    entry.getKey().toString(),
                    savedItems
            );
        }

        tag.put("mailboxes", savedMail);

        return tag;
    }
}