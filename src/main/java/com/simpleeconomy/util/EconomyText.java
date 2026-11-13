package com.simpleeconomy.util;

import net.minecraft.network.chat.Component;

import java.util.Arrays;

/**
 * Shared translation helpers for user-facing messages.
 *
 * Messages sent through custom packets are encoded as a translation key plus
 * string arguments, then reconstructed on the client so the player's language
 * setting is respected.
 */
public final class EconomyText {

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
            return Component.literal(value == null ? "" : value);
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
