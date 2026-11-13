package com.simpleeconomy.network;

import com.simpleeconomy.SimpleEconomy;
import com.simpleeconomy.market.MarketData;
import com.simpleeconomy.market.MarketMenu;
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

public record MarketPublishRequest(long price)
        implements CustomPacketPayload {

    public static final Type<MarketPublishRequest> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    SimpleEconomy.MODID,
                    "market_publish"
            ));

    public static final StreamCodec<ByteBuf, MarketPublishRequest> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_LONG,
                    MarketPublishRequest::price,
                    MarketPublishRequest::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send(long price) {

        ClientMarketCache.beginAction();

        PacketDistributor.sendToServer(
                new MarketPublishRequest(price)
        );
    }

    public static void handle(
            MarketPublishRequest payload,
            IPayloadContext context
    ) {

        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        if (!(player.containerMenu instanceof MarketMenu menu)) {
            return;
        }

        if (menu.getActiveTab() != 2) {
    sendResult(
            player,
            false,
            "Для выставления товара откройте вкладку «Продать»."
    );
    return;
}

        if (payload.price() <= 0) {
            sendResult(player, false, "Цена должна быть больше нуля.");
            return;
        }

        ItemStack item = menu.getOfferingStack();

        if (item.isEmpty()) {
            sendResult(player, false, "Сначала положите предмет в слот.");
            return;
        }

        try {

            var listing = MarketData.get(player.server)
                    .createListing(
                            player.getUUID(),
                            player.getName().getString(),
                            item,
                            payload.price()
                    );

            // Удаляем предмет из слота только после создания объявления
            menu.clearOffering();
            MarketData.saveNow(player.server);

            sendResult(
                    player,
                    true,
                    "Объявление №" + listing.id()
                            + " выставлено за "
                            + String.format("%,d", payload.price())
                            + " $."
            );

        } catch (IllegalArgumentException exception) {

            sendResult(
                    player,
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
    PacketDistributor.sendToPlayer(
            player,
            new MarketActionResult(
                    success,
                    message,
                    true
            )
    );
}
}