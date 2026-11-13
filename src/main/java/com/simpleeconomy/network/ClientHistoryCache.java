package com.simpleeconomy.network;

import com.simpleeconomy.economy.TransactionEntry;

import java.util.List;

public class ClientHistoryCache {

    private static List<TransactionEntry> entries = List.of();

    private static boolean received = false;

    public static void beginRequest() {
        entries = List.of();
        received = false;
    }

    public static void setEntries(List<TransactionEntry> value) {
        entries = List.copyOf(value);
        received = true;
    }

    public static List<TransactionEntry> getEntries() {
        return entries;
    }

    public static boolean hasReceived() {
        return received;
    }
}
