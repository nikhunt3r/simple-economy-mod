package com.simpleeconomy.network;

import com.simpleeconomy.SimpleEconomy;
import com.simpleeconomy.economy.EconomyData;
import com.simpleeconomy.market.MarketData;
import com.simpleeconomy.market.MarketListing;
import com.simpleeconomy.market.MarketMailboxData;
import com.simpleeconomy.market.ClientMarketCache;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MarketActionRequest(
        int action,
        long listingId,
        boolean ownOnly
) implements CustomPacketPayload {

    public static final int BUY = 0;
    public static final int CANCEL = 1;

    public static final Type<MarketActionRequest> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    SimpleEconomy.MODID,
                    "market_action"
            ));

    public static final StreamCodec<ByteBuf, MarketActionRequest> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    MarketActionRequest::action,

                    ByteBufCodecs.VAR_LONG,
                    MarketActionRequest::listingId,

                    ByteBufCodecs.BOOL,
                    MarketActionRequest::ownOnly,

                    MarketActionRequest::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send(
            int action,
            long listingId,
            boolean ownOnly
    ) {

        ClientMarketCache.beginAction();

        PacketDistributor.sendToServer(
                new MarketActionRequest(
                        action,
                        listingId,
                        ownOnly
                )
        );
    }

    public static void handle(
            MarketActionRequest payload,
            IPayloadContext context
    ) {

        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        if (payload.listingId() <= 0) {
            sendResult(player, false, "Некорректный номер объявления.", payload.ownOnly());
            return;
        }

        if (payload.action() == BUY) {
            buy(player, payload);
        } else if (payload.action() == CANCEL) {
            cancel(player, payload);
        } else {
            sendResult(player, false, "Неизвестное действие.", payload.ownOnly());
        }
    }

    private static void buy(
            ServerPlayer buyer,
            MarketActionRequest request
    ) {

        MarketData market = MarketData.get(buyer.server);

        MarketListing listing = market.getListing(request.listingId());

        if (listing == null) {
            sendResult(buyer, false, "Объявление уже недоступно.", request.ownOnly());
            return;
        }

        if (listing.sellerId().equals(buyer.getUUID())) {
            sendResult(buyer, false, "Нельзя покупать собственный товар.", request.ownOnly());
            return;
        }

        // Снимаем лот с рынка
        MarketListing removed = market.removeListing(request.listingId());

        if (removed == null) {
            sendResult(buyer, false, "Объявление уже купили или сняли.", request.ownOnly());
            return;
        }

        long payout;

        try {

            payout = EconomyData.get(buyer.server).purchaseMarketItem(
                    buyer.getUUID(),
                    buyer.getName().getString(),
                    removed.sellerId(),
                    removed.sellerName(),
                    removed.id(),
                    removed.item().getHoverName().getString(),
                    removed.price()
            );

        } catch (IllegalArgumentException exception) {

            // Возвращаем лот, если оплата не удалась
            market.restoreListing(removed);
            MarketData.saveNow(buyer.server);

            sendResult(
                    buyer,
                    false,
                    exception.getMessage(),
                    request.ownOnly()
            );

            return;
        }

        // Передаём предмет покупателю
        ItemStack purchasedItem = removed.item();

        buyer.getInventory().add(purchasedItem);

        // Остаток отправляем в почту
        if (!purchasedItem.isEmpty()) {

            MarketMailboxData.get(buyer.server).addItem(
                    buyer.getUUID(),
                    purchasedItem
            );
        }
        MarketData.saveNow(buyer.server);

        long commission = removed.price() / 20L;

        // Уведомляем продавца, если он в сети
        ServerPlayer seller = buyer.server
                .getPlayerList()
                .getPlayer(removed.sellerId());

        if (seller != null) {
            seller.sendSystemMessage(
                    Component.literal(
                            "Ваш товар купили. Начислено "
                                    + formatMoney(payout) + " $."
                    )
            );
        }

        sendResult(
                buyer,
                true,
                "Покупка завершена. Комиссия: "
                        + formatMoney(commission)
                        + " $. Продавцу начислено: "
                        + formatMoney(payout)
                        + " $.",
                request.ownOnly()
        );
    }

    private static void cancel(
            ServerPlayer player,
            MarketActionRequest request
    ) {

        MarketData market = MarketData.get(player.server);

        MarketListing removed = market.cancelListing(
                request.listingId(),
                player.getUUID()
        );

        if (removed == null) {
            sendResult(
                    player,
                    false,
                    "Объявление не найдено или принадлежит другому игроку.",
                    request.ownOnly()
            );
            return;
        }

        ItemStack returnedItem = removed.item();

        player.getInventory().add(returnedItem);

        if (!returnedItem.isEmpty()) {
            MarketMailboxData.get(player.server).addItem(
                    player.getUUID(),
                    returnedItem
            );
        }
        MarketData.saveNow(player.server);

        sendResult(
                player,
                true,
                "Объявление снято. Предмет возвращён.",
                request.ownOnly()
        );
    }

    private static void sendResult(
        ServerPlayer player,
        boolean success,
        String message,
        boolean ownOnly
) {
    PacketDistributor.sendToPlayer(
            player,
            new MarketActionResult(
                    success,
                    message,
                    ownOnly
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