package com.simpleeconomy.network;

import com.simpleeconomy.SimpleEconomy;

import com.simpleeconomy.economy.EconomyData;
import com.simpleeconomy.market.MarketMenu;
import com.simpleeconomy.market.MarketPriceData;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record QuickSaleRequest()
        implements CustomPacketPayload {

    public static final Type<QuickSaleRequest> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    SimpleEconomy.MODID,
                    "quick_sale"
            ));

    public static final StreamCodec<ByteBuf, QuickSaleRequest> STREAM_CODEC =
            StreamCodec.unit(new QuickSaleRequest());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send() {

        com.simpleeconomy.market.ClientMarketCache.setStatus("");
        com.simpleeconomy.market.ClientMarketCache.beginAction();

        PacketDistributor.sendToServer(
                new QuickSaleRequest()
        );
    }

    public static void handle(
            QuickSaleRequest payload,
            IPayloadContext context
    ) {

        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        if (!(player.containerMenu instanceof MarketMenu menu)
                || menu.getActiveTab() != 3) {

            sendResult(
                    player,
                    false,
                    "Откройте вкладку быстрой продажи."
            );
            return;
        }

        ItemStack stack = menu.getOfferingStack();

        if (stack.isEmpty()) {
            sendResult(
                    player,
                    false,
                    "Поместите предмет в слот."
            );
            return;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(
                stack.getItem()
        );

        MarketPriceData prices = MarketPriceData.get(player.server);

        long unitPrice = prices.getPrice(itemId);

        if (unitPrice <= 0) {
            sendResult(
                    player,
                    false,
                    "Для этого предмета не установлена цена."
            );
            return;
        }

        long totalPrice;

        try {
            totalPrice = Math.multiplyExact(
                    unitPrice,
                    (long) stack.getCount()
            );

        } catch (ArithmeticException exception) {

            sendResult(
                    player,
                    false,
                    "Сумма продажи слишком большая."
            );
            return;
        }

        ItemStack soldItem = menu.extractOffering();

        if (soldItem.isEmpty()) {
            sendResult(
                    player,
                    false,
                    "Не удалось извлечь предмет из слота."
            );
            return;
        }

        long newBalance;

        try {

            newBalance = EconomyData.get(player.server)
                    .addQuickSaleBalance(
                            player.getUUID(),
                            totalPrice,
                            soldItem.getHoverName().getString(),
                            soldItem.getCount(),
                            unitPrice
                    );

        } catch (IllegalArgumentException exception) {

            // Если начисление не состоялось,
            // возвращаем предмет в слот.
            menu.restoreOffering(soldItem);

            sendResult(
                    player,
                    false,
                    exception.getMessage()
            );
            return;
        }

        // Сохраняем начисление на диск
        player.server.overworld()
                .getDataStorage()
                .save();

        sendResult(
                player,
                true,
                "Продано "
                        + soldItem.getCount()
                        + " шт. "
                        + soldItem.getHoverName().getString()
                        + " за "
                        + formatMoney(totalPrice)
                        + " $. Баланс: "
                        + formatMoney(newBalance)
                        + " $.",
                newBalance
        );
    }

    private static void sendResult(
            ServerPlayer player,
            boolean success,
            String message
    ) {
        long balance = EconomyData.get(player.server)
                .getOrCreateBalance(player.getUUID());

        sendResult(player, success, message, balance);
    }

    private static void sendResult(
            ServerPlayer player,
            boolean success,
            String message,
            long balance
    ) {

        PacketDistributor.sendToPlayer(
                player,
                new QuickSaleResult(
                        success,
                        message,
                        balance
                )
        );
    }

    private static String formatMoney(long amount) {

        return String.format(
                java.util.Locale.US,
                "%,d",
                amount
        ).replace(',', ' ');
    }
}
