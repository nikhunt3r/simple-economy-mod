package com.simpleeconomy.network;

import com.simpleeconomy.SimpleEconomy;
import com.simpleeconomy.economy.EconomyData;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TransferRequest(
        String targetName,
        long amount
) implements CustomPacketPayload {

    public static final Type<TransferRequest> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    SimpleEconomy.MODID,
                    "transfer_request"
            ));

    public static final StreamCodec<ByteBuf, TransferRequest> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.stringUtf8(16),
                    TransferRequest::targetName,
                    ByteBufCodecs.VAR_LONG,
                    TransferRequest::amount,
                    TransferRequest::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Отправка запроса с клиента
    public static void send(String targetName, long amount) {
        PacketDistributor.sendToServer(
                new TransferRequest(targetName, amount)
        );
    }

    // Обработка запроса на сервере
    public static void handle(
            TransferRequest payload,
            IPayloadContext context
    ) {

        if (!(context.player() instanceof ServerPlayer sender)) {
            return;
        }

        String targetName = payload.targetName().trim();

        if (!targetName.matches("[A-Za-z0-9_]{1,16}")) {
            sendResult(sender, false, "Некорректный ник игрока.");
            return;
        }

        ServerPlayer target = sender.server
                .getPlayerList()
                .getPlayerByName(targetName);

        if (target == null) {
            sendResult(sender, false, "Игрок не найден или не в сети.");
            return;
        }

        EconomyData data = EconomyData.get(sender.server);

        try {

            data.transferBalance(
        sender.getUUID(),
        sender.getName().getString(),
        target.getUUID(),
        target.getName().getString(),
        payload.amount()
);

            long senderBalance = data.getOrCreateBalance(
                    sender.getUUID()
            );

            long receiverBalance = data.getOrCreateBalance(
                    target.getUUID()
            );

            // Уведомление получателю
            target.sendSystemMessage(
                    Component.literal(
                            "Игрок " + sender.getName().getString()
                                    + " перевёл вам "
                                    + formatMoney(payload.amount())
                                    + " $"
                    )
            );

            // Обновляем баланс получателя
            PacketDistributor.sendToPlayer(
                    target,
                    new BalanceSync(true, receiverBalance)
            );

            // Отправляем результат отправителю
            sendResult(
                    sender,
                    true,
                    "Перевод " + formatMoney(payload.amount())
                            + " $ игроку "
                            + target.getName().getString()
                            + " выполнен."
            );

            // Обновляем баланс отправителя
            PacketDistributor.sendToPlayer(
                    sender,
                    new BalanceSync(true, senderBalance)
            );

        } catch (IllegalArgumentException exception) {

            sendResult(
                    sender,
                    false,
                    exception.getMessage()
            );
        }
    }

    private static void sendResult(
            ServerPlayer player,
            boolean success,
            String message
    ) {

        long balance = EconomyData.get(player.server)
                .getOrCreateBalance(player.getUUID());

        PacketDistributor.sendToPlayer(
                player,
                new TransferResult(success, message, balance)
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
