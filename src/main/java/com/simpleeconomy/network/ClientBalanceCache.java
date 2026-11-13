package com.simpleeconomy.network;

public class ClientBalanceCache {

    private static long balance = 0;
    private static boolean received = false;

    private static boolean transferPending = false;
    private static boolean transferSuccess = false;
    private static boolean transferResultAvailable = false;
    private static String transferMessage = "";

    public static void setBalance(long value) {
        balance = value;
        received = true;
    }

    public static long getBalance() {
        return balance;
    }

    public static boolean hasBalance() {
        return received;
    }

    public static void beginTransfer() {
        transferPending = true;
        transferResultAvailable = false;
        transferMessage = "";
    }

    public static void setTransferResult(
            boolean success,
            String message
    ) {
        transferPending = false;
        transferSuccess = success;
        transferMessage = message;
        transferResultAvailable = true;
    }

    public static boolean isTransferPending() {
        return transferPending;
    }

    public static boolean hasTransferResult() {
        return transferResultAvailable;
    }

    public static boolean wasTransferSuccessful() {
        return transferSuccess;
    }

    public static String getTransferMessage() {
        return transferMessage;
    }

    public static void clearTransferResult() {
        transferResultAvailable = false;
        transferMessage = "";
    }
}
