package com.simpleeconomy.market;

import java.util.List;

public class ClientMarketCache {

    private static List<MarketListingView> listings = List.of();

    private static boolean received = false;
    private static boolean pending = false;
    private static boolean actionPending = false;
    private static boolean ownListings = false;

    private static int page = 0;
    private static int totalPages = 1;
    private static int totalListings = 0;
    private static int sortMode = 0;

    private static String search = "";
    private static String status = "";

    private static int statusTicks = 0;

    public static void beginRequest(
            boolean ownOnly,
            String searchText,
            int sort,
            int requestedPage
    ) {
        received = false;
        pending = true;
        ownListings = ownOnly;
        search = searchText;
        sortMode = sort;
        page = requestedPage;
    }

    public static void setListings(
            List<MarketListingView> value,
            boolean ownOnly,
            int currentPage,
            int pages,
            int total,
            String searchText,
            int sort
    ) {
        listings = List.copyOf(value);
        ownListings = ownOnly;

        page = currentPage;
        totalPages = Math.max(1, pages);
        totalListings = total;

        search = searchText;
        sortMode = sort;

        received = true;
        pending = false;
    }

    public static List<MarketListingView> getListings() {
        return listings;
    }

    public static boolean hasReceived() {
        return received;
    }

    public static boolean isOwnListings() {
        return ownListings;
    }

    public static int getPage() {
        return page;
    }

    public static int getTotalPages() {
        return totalPages;
    }

    public static int getTotalListings() {
        return totalListings;
    }

    public static int getSortMode() {
        return sortMode;
    }

    public static String getSearch() {
        return search;
    }

    // Состояние загрузки списка
    public static void setPending(boolean value) {
        pending = value;
    }

    public static boolean isPending() {
        return pending;
    }

    // Отдельное состояние покупки, снятия или продажи
    public static void beginAction() {
        actionPending = true;
        clearStatus();
    }

    public static void finishAction(String message) {
        actionPending = false;
        status = message;
        statusTicks = 100;
    }

    public static boolean isActionPending() {
        return actionPending;
    }

    // Сообщения интерфейса
    public static void setStatus(String value) {
        finishAction(value);
    }

    public static String getStatus() {
        return status;
    }

    public static void clearStatus() {
        status = "";
        statusTicks = 0;
    }

    // Таймер сообщений: 100 игровых тиков = примерно 5 секунд
    public static void tickStatus() {

        if (statusTicks > 0) {

            statusTicks--;

            if (statusTicks == 0) {
                status = "";
            }
        }
    }
}