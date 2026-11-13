package com.simpleeconomy.util;

import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.Map;

/**
 * Shared translation helpers for user-facing messages.
 *
 * Messages sent through custom packets are encoded as a translation key plus
 * string arguments, then reconstructed on the client so the player's language
 * setting is respected.
 */
public final class EconomyText {

    private static final Map<String, String> TEXT_KEYS = Map.ofEntries(
            Map.entry("Simple Economy", "screen.simpleeconomy.title"),
            Map.entry("История операций", "screen.simpleeconomy.history.title"),
            Map.entry("Назад", "screen.simpleeconomy.history.back"),
            Map.entry("ИСТОРИЯ ОПЕРАЦИЙ", "screen.simpleeconomy.history.heading"),
            Map.entry("Последние движения средств на вашем счёте", "screen.simpleeconomy.history.subtitle"),
            Map.entry("ОПЕРАЦИЯ", "screen.simpleeconomy.history.column.operation"),
            Map.entry("ОПИСАНИЕ", "screen.simpleeconomy.history.column.description"),
            Map.entry("ДАТА", "screen.simpleeconomy.history.column.date"),
            Map.entry("Загрузка истории...", "screen.simpleeconomy.history.loading"),
            Map.entry("Операций пока нет", "screen.simpleeconomy.history.empty"),
            Map.entry("Перевод денег", "screen.simpleeconomy.transfer.title"),
            Map.entry("Ник игрока", "screen.simpleeconomy.transfer.player_name"),
            Map.entry("Ник получателя", "screen.simpleeconomy.transfer.recipient_placeholder"),
            Map.entry("Сумма", "screen.simpleeconomy.transfer.amount"),
            Map.entry("Например, 500", "screen.simpleeconomy.transfer.amount_example"),
            Map.entry("Отправить деньги", "screen.simpleeconomy.dashboard.transfer.subtitle"),
            Map.entry("ПЕРЕВОД МЕЖДУ ИГРОКАМИ", "screen.simpleeconomy.transfer.heading"),
            Map.entry("Ваш баланс: ", "screen.simpleeconomy.transfer.balance"),
            Map.entry("Загрузка баланса...", "screen.simpleeconomy.transfer.loading_balance"),
            Map.entry("ПОЛУЧАТЕЛЬ", "screen.simpleeconomy.transfer.recipient"),
            Map.entry("СУММА ПЕРЕВОДА", "screen.simpleeconomy.transfer.amount_label"),
            Map.entry("Отправка перевода...", "screen.simpleeconomy.transfer.sending"),
            Map.entry("ОБЗОР", "screen.simpleeconomy.dashboard.overview"),
            Map.entry("Переводы", "screen.simpleeconomy.dashboard.transfers"),
            Map.entry("Магазин", "screen.simpleeconomy.dashboard.market"),
            Map.entry("История", "screen.simpleeconomy.dashboard.history"),
            Map.entry("Главная", "screen.simpleeconomy.dashboard.home"),
            Map.entry("Ваш финансовый кабинет", "screen.simpleeconomy.dashboard.subtitle"),
            Map.entry("ОБЩИЙ БАЛАНС", "screen.simpleeconomy.dashboard.balance.title"),
            Map.entry("Ваши игровые средства", "screen.simpleeconomy.dashboard.balance.subtitle"),
            Map.entry("Покупка товаров", "screen.simpleeconomy.dashboard.market.subtitle"),
            Map.entry("Последние операции", "screen.simpleeconomy.dashboard.history.title"),
            Map.entry("Загрузка...", "screen.simpleeconomy.dashboard.loading"),
            Map.entry("Общий рынок", "screen.simpleeconomy.market.tab.global"),
            Map.entry("Мои объявления", "screen.simpleeconomy.market.tab.own"),
            Map.entry("Продать", "screen.simpleeconomy.market.tab.sell"),
            Map.entry("Быстрая продажа", "screen.simpleeconomy.market.tab.quick_sell"),
            Map.entry("Купить", "screen.simpleeconomy.market.buy"),
            Map.entry("Поиск", "screen.simpleeconomy.market.search"),
            Map.entry("Предмет или продавец", "screen.simpleeconomy.market.search_placeholder"),
            Map.entry("Найти", "screen.simpleeconomy.market.search_button"),
            Map.entry("Сначала новые", "screen.simpleeconomy.market.sort.newest"),
            Map.entry("Дешевле", "screen.simpleeconomy.market.sort.cheapest"),
            Map.entry("Дороже", "screen.simpleeconomy.market.sort.expensive"),
            Map.entry("Цена", "screen.simpleeconomy.market.price"),
            Map.entry("Цена за весь лот", "screen.simpleeconomy.market.lot_price"),
            Map.entry("Выставить на рынок", "screen.simpleeconomy.market.create_listing"),
            Map.entry("Продать предметы", "screen.simpleeconomy.market.sell_items"),
            Map.entry("SIMPLE ECONOMY / РЫНОК", "screen.simpleeconomy.market.header"),
            Map.entry("ДОСТУПНЫЕ ОБЪЯВЛЕНИЯ", "screen.simpleeconomy.market.available_listings"),
            Map.entry("ВАШИ ОБЪЯВЛЕНИЯ", "screen.simpleeconomy.market.own_listings"),
            Map.entry("Загрузка объявлений...", "screen.simpleeconomy.market.loading"),
            Map.entry("Объявлений пока нет.", "screen.simpleeconomy.market.empty"),
            Map.entry("По вашему запросу ничего не найдено.", "screen.simpleeconomy.market.no_search_results"),
            Map.entry("Снять", "screen.simpleeconomy.market.remove"),
            Map.entry("ВЫСТАВЛЕНИЕ ТОВАРА", "screen.simpleeconomy.market.listing.heading"),
            Map.entry("ПОМЕСТИТЕ ПРЕДМЕТ В СЛОТ", "screen.simpleeconomy.market.listing.slot_hint"),
            Map.entry("Цена указывается за весь лот.", "screen.simpleeconomy.market.listing.price_hint"),
            Map.entry("БЫСТРАЯ ПРОДАЖА", "screen.simpleeconomy.market.quick_sell.heading"),
            Map.entry("Положите предмет в слот.", "screen.simpleeconomy.market.quick_sell.slot_hint"),
            Map.entry("ИНВЕНТАРЬ", "screen.simpleeconomy.market.quick_sell.inventory"),
            Map.entry("Цена не установлена.", "screen.simpleeconomy.market.quick_sell.price_missing"),
            Map.entry("Продать этот предмет нельзя.", "screen.simpleeconomy.market.quick_sell.unavailable")
    );

    private static final String PREFIX = "\u0002";
    private static final String SEPARATOR = "\u0003";

    private EconomyText() {
    }

    public static String text(String key, Object... arguments) {
        return Component.translatable(key, arguments).getString();
    }

    public static String encode(String key, Object... arguments) {
        StringBuilder result = new StringBuilder(PREFIX).append(key);
        for (Object argument : arguments) {
            result.append(SEPARATOR).append(argument == null ? "" : argument);
        }
        return result.toString();
    }

    public static Component component(String value) {
        if (value == null || !value.startsWith(PREFIX)) {
            return value == null ? Component.literal("") : TEXT_KEYS.containsKey(value) ? Component.translatable(TEXT_KEYS.get(value)) : Component.literal(value);
        }

        String[] parts = value.substring(PREFIX.length()).split(SEPARATOR, -1);
        if (parts.length == 0 || parts[0].isBlank()) {
            return Component.literal("");
        }

        Object[] arguments = Arrays.copyOfRange(parts, 1, parts.length, Object[].class);
        return Component.translatable(parts[0], arguments);
    }

    public static Component exception(IllegalArgumentException exception) {
        String message = exception == null ? null : exception.getMessage();
        String key = exceptionKey(message);
        return key == null
                ? Component.literal(message == null ? "Unknown error" : message)
                : Component.translatable(key);
    }

    public static String encodeException(IllegalArgumentException exception) {
        String message = exception == null ? null : exception.getMessage();
        String key = exceptionKey(message);
        return key == null
                ? (message == null ? "" : message)
                : encode(key);
    }

    public static Component transactionType(String type) {
        String key = switch (type) {
            case "Стартовый баланс" -> "history.type.starting_balance";
            case "Начисление" -> "history.type.deposit";
            case "Списание" -> "history.type.withdrawal";
            case "Установка баланса" -> "history.type.balance_set";
            case "Перевод отправлен" -> "history.type.transfer_sent";
            case "Перевод получен" -> "history.type.transfer_received";
            case "Покупка на рынке" -> "history.type.market_purchase";
            case "Продажа на рынке" -> "history.type.market_sale";
            case "Комиссия рынка" -> "history.type.market_commission";
            case "Быстрая продажа" -> "history.type.quick_sale";
            case "Награда за контракт" -> "history.type.contract_reward";
            default -> null;
        };
        return key == null ? Component.literal(type) : Component.translatable(key);
    }

    public static Component transactionDescription(String description) {
        if (description == null || description.isBlank()) {
            return Component.literal("");
        }
        if (description.startsWith("Получатель: ")) {
            return Component.translatable("history.description.recipient", description.substring("Получатель: ".length()));
        }
        if (description.startsWith("Отправитель: ")) {
            return Component.translatable("history.description.sender", description.substring("Отправитель: ".length()));
        }
        if (description.startsWith("Контракт: ")) {
            return Component.translatable("history.description.contract", description.substring("Контракт: ".length()));
        }
        if (description.equals("Администратор")) {
            return Component.translatable("history.description.admin");
        }
        if (description.equals("Первое подключение")) {
            return Component.translatable("history.description.first_join");
        }
        return Component.literal(description);
    }

    private static String exceptionKey(String message) {
        if (message == null) {
            return null;
        }
        return switch (message) {
            case "Сумма должна быть больше нуля." -> "message.simpleeconomy.error.amount_positive";
            case "Слишком большое значение баланса." -> "message.simpleeconomy.error.balance_too_large";
            case "Награда должна быть больше нуля." -> "message.simpleeconomy.error.reward_positive";
            case "Некорректный идентификатор награды." -> "message.simpleeconomy.error.reward_token_invalid";
            case "Баланс не может быть отрицательным." -> "message.simpleeconomy.error.balance_negative";
            case "У игрока недостаточно средств." -> "message.simpleeconomy.error.insufficient_funds";
            case "Сумма перевода должна быть больше нуля." -> "message.simpleeconomy.error.transfer_amount_positive";
            case "Нельзя переводить деньги самому себе." -> "message.simpleeconomy.error.transfer_self";
            case "Недостаточно средств для перевода." -> "message.simpleeconomy.error.transfer_insufficient";
            case "Баланс получателя слишком велик." -> "message.simpleeconomy.error.receiver_balance_too_large";
            case "Некорректная цена товара." -> "message.simpleeconomy.error.invalid_purchase_price";
            case "Нельзя покупать собственные объявления." -> "message.simpleeconomy.error.purchase_self";
            case "Недостаточно денег для покупки." -> "message.simpleeconomy.error.purchase_insufficient";
            case "Баланс продавца переполнен." -> "message.simpleeconomy.error.seller_balance_too_large";
            case "Сумма продажи должна быть больше нуля." -> "message.simpleeconomy.error.sale_amount_positive";
            case "Баланс игрока слишком велик." -> "message.simpleeconomy.error.player_balance_too_large";
            case "Нельзя выставить пустой предмет." -> "message.simpleeconomy.error.empty_listing";
            case "Цена должна быть больше нуля." -> "message.simpleeconomy.error.price_positive";
            case "Не удалось создать новое объявление." -> "message.simpleeconomy.error.listing_create_failed";
            default -> null;
        };
    }
}
