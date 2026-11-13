package com.simpleeconomy.economy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.server.MinecraftServer;

public class EconomyData extends SavedData {

    private static final String DATA_NAME = "simpleeconomy_balances";

    public static final long STARTING_BALANCE = 1000L;

    private static final int MAX_HISTORY = 100;
    private static final int HISTORY_TO_SEND = 10;

    private final Map<UUID, Long> balances = new HashMap<>();

    private final Map<UUID, List<TransactionEntry>> histories =
            new HashMap<>();

    private static final Factory<EconomyData> FACTORY =
            new Factory<>(
                    EconomyData::new,
                    EconomyData::load,
                    null
            );

    public static EconomyData get(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(FACTORY, DATA_NAME);
    }

    // Получение баланса
    public long getOrCreateBalance(UUID playerId) {

        Long balance = balances.get(playerId);

        if (balance == null) {

            balance = STARTING_BALANCE;

            balances.put(playerId, balance);

            addHistory(
                    playerId,
                    "Стартовый баланс",
                    STARTING_BALANCE,
                    "Первое подключение"
            );

            setDirty();
        }

        return balance;
    }

    // Добавление записи в историю
    private void addHistory(
            UUID playerId,
            String type,
            long amount,
            String description
    ) {

        List<TransactionEntry> history =
                histories.computeIfAbsent(
                        playerId,
                        id -> new ArrayList<>()
                );

        history.add(
                0,
                new TransactionEntry(
                        System.currentTimeMillis(),
                        type,
                        amount,
                        description
                )
        );

        // Храним не более 100 последних операций
        while (history.size() > MAX_HISTORY) {
            history.remove(history.size() - 1);
        }

        setDirty();
    }

    // Получение последних операций игрока
    public List<TransactionEntry> getHistory(UUID playerId) {

        List<TransactionEntry> history = histories.get(playerId);

        if (history == null || history.isEmpty()) {
            return List.of();
        }

        int count = Math.min(
                history.size(),
                HISTORY_TO_SEND
        );

        return List.copyOf(history.subList(0, count));
    }

    // Начисление денег
    public long addBalance(UUID playerId, long amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Сумма должна быть больше нуля."
            );
        }

        long current = getOrCreateBalance(playerId);
        long updated;

        try {
            updated = Math.addExact(current, amount);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Слишком большое значение баланса."
            );
        }

        balances.put(playerId, updated);

        addHistory(
                playerId,
                "Начисление",
                amount,
                "Администратор"
        );

        setDirty();

        return updated;
    }

    // Списание денег
    public long removeBalance(UUID playerId, long amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Сумма должна быть больше нуля."
            );
        }

        long current = getOrCreateBalance(playerId);

        if (amount > current) {
            throw new IllegalArgumentException(
                    "У игрока недостаточно средств."
            );
        }

        long updated = current - amount;

        balances.put(playerId, updated);

        addHistory(
                playerId,
                "Списание",
                amount,
                "Администратор"
        );

        setDirty();

        return updated;
    }

    // Установка конкретного баланса
    public long setBalance(UUID playerId, long amount) {

        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Баланс не может быть отрицательным."
            );
        }

        balances.put(playerId, amount);

        addHistory(
                playerId,
                "Установка баланса",
                amount,
                "Администратор"
        );

        setDirty();

        return amount;
    }

    // Перевод между игроками
    public void transferBalance(
            UUID senderId,
            String senderName,
            UUID receiverId,
            String receiverName,
            long amount
    ) {

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Сумма перевода должна быть больше нуля."
            );
        }

        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException(
                    "Нельзя переводить деньги самому себе."
            );
        }

        long senderBalance = getOrCreateBalance(senderId);
        long receiverBalance = getOrCreateBalance(receiverId);

        if (senderBalance < amount) {
            throw new IllegalArgumentException(
                    "Недостаточно средств для перевода."
            );
        }

        long newReceiverBalance;

        try {
            newReceiverBalance = Math.addExact(
                    receiverBalance,
                    amount
            );
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Баланс получателя слишком велик."
            );
        }

        balances.put(senderId, senderBalance - amount);
        balances.put(receiverId, newReceiverBalance);

        // Запись отправителю
        addHistory(
                senderId,
                "Перевод отправлен",
                amount,
                "Получатель: " + receiverName
        );

        // Запись получателю
        addHistory(
                receiverId,
                "Перевод получен",
                amount,
                "Отправитель: " + senderName
        );

        setDirty();
    }

    // Загрузка данных из файла мира
    public static EconomyData load(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {

        EconomyData data = new EconomyData();

        // Загрузка балансов
        CompoundTag savedBalances =
                tag.getCompound("balances");

        for (String key : savedBalances.getAllKeys()) {

            try {
                UUID playerId = UUID.fromString(key);

                long balance = savedBalances.getLong(key);

                data.balances.put(playerId, balance);

            } catch (IllegalArgumentException exception) {
                // Пропускаем некорректную запись
            }
        }

        // Загрузка истории
        CompoundTag savedHistories =
                tag.getCompound("histories");

        for (String key : savedHistories.getAllKeys()) {

            try {

                UUID playerId = UUID.fromString(key);

                ListTag savedList = savedHistories.getList(
                        key,
                        Tag.TAG_COMPOUND
                );

                List<TransactionEntry> history =
                        new ArrayList<>();

                for (int i = 0; i < savedList.size(); i++) {

                    CompoundTag entry = savedList.getCompound(i);

                    history.add(
                            new TransactionEntry(
                                    entry.getLong("timestamp"),
                                    entry.getString("type"),
                                    entry.getLong("amount"),
                                    entry.getString("description")
                            )
                    );
                }

                if (history.size() > MAX_HISTORY) {
                    history = new ArrayList<>(
                            history.subList(0, MAX_HISTORY)
                    );
                }

                data.histories.put(playerId, history);

            } catch (IllegalArgumentException exception) {
                // Пропускаем некорректную запись
            }
        }

        return data;
    }

    public long purchaseMarketItem(
        UUID buyerId,
        String buyerName,
        UUID sellerId,
        String sellerName,
        long listingId,
        String itemName,
        long price
) {
    if (price <= 0) {
        throw new IllegalArgumentException(
                "Некорректная цена товара."
        );
    }

    if (buyerId.equals(sellerId)) {
        throw new IllegalArgumentException(
                "Нельзя покупать собственные объявления."
        );
    }

    long buyerBalance = getOrCreateBalance(buyerId);
    long sellerBalance = getOrCreateBalance(sellerId);

    if (buyerBalance < price) {
        throw new IllegalArgumentException(
                "Недостаточно денег для покупки."
        );
    }

    // 5% комиссии, округление вниз до целого доллара
    long commission = price / 20L;
    long sellerPayout = price - commission;

    long newSellerBalance;

    try {
        newSellerBalance = Math.addExact(
                sellerBalance,
                sellerPayout
        );
    } catch (ArithmeticException exception) {
        throw new IllegalArgumentException(
                "Баланс продавца переполнен."
        );
    }

    long newBuyerBalance = buyerBalance - price;

    // Изменяем балансы только после всех проверок
    balances.put(buyerId, newBuyerBalance);
    balances.put(sellerId, newSellerBalance);

    // История покупателя
    addHistory(
            buyerId,
            "Покупка на рынке",
            price,
            "Лот #" + listingId + " · " + itemName
                    + " · Продавец: " + sellerName
    );

    // История продавца
    addHistory(
            sellerId,
            "Продажа на рынке",
            sellerPayout,
            "Лот #" + listingId + " · Покупатель: " + buyerName
    );

    // Отдельно записываем комиссию
    if (commission > 0) {
        addHistory(
                sellerId,
                "Комиссия рынка",
                commission,
                "5% · Лот #" + listingId
        );
    }

    setDirty();

    return sellerPayout;
}
public long addQuickSaleBalance(
        UUID playerId,
        long amount,
        String itemName,
        int count,
        long unitPrice
) {
    if (amount <= 0) {
        throw new IllegalArgumentException(
                "Сумма продажи должна быть больше нуля."
        );
    }

    long current = getOrCreateBalance(playerId);
    long updated;

    try {
        updated = Math.addExact(current, amount);
    } catch (ArithmeticException exception) {
        throw new IllegalArgumentException(
                "Баланс игрока слишком велик."
        );
    }

    balances.put(playerId, updated);

    addHistory(
            playerId,
            "Быстрая продажа",
            amount,
            count + " шт. " + itemName
                    + " · " + unitPrice + " $/шт."
    );

    setDirty();

    return updated;
}

    // Сохранение данных
    @Override
    public CompoundTag save(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {

        // Сохранение балансов
        CompoundTag savedBalances = new CompoundTag();

        for (Map.Entry<UUID, Long> entry : balances.entrySet()) {

            savedBalances.putLong(
                    entry.getKey().toString(),
                    entry.getValue()
            );
        }

        tag.put("balances", savedBalances);

        // Сохранение истории
        CompoundTag savedHistories = new CompoundTag();

        for (Map.Entry<UUID, List<TransactionEntry>> entry
                : histories.entrySet()) {

            ListTag historyList = new ListTag();

            for (TransactionEntry transaction : entry.getValue()) {

                CompoundTag savedEntry = new CompoundTag();

                savedEntry.putLong(
                        "timestamp",
                        transaction.timestamp()
                );

                savedEntry.putString(
                        "type",
                        transaction.type()
                );

                savedEntry.putLong(
                        "amount",
                        transaction.amount()
                );

                savedEntry.putString(
                        "description",
                        transaction.description()
                );

                historyList.add(savedEntry);
            }

            savedHistories.put(
                    entry.getKey().toString(),
                    historyList
            );
        }

        tag.put("histories", savedHistories);

        return tag;
    }
}