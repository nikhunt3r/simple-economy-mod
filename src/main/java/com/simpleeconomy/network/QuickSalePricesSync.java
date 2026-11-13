package com.simpleeconomy.network;

import com.simpleeconomy.SimpleEconomy;
import com.simpleeconomy.market.ClientQuickSaleCache;
import com.simpleeconomy.market.MarketPriceData;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.Map;

public record QuickSalePricesSync(
        List<QuickSalePriceEntry> entries
) implements CustomPacketPayload {

    public QuickSalePricesSync {
        entries = List.copyOf(entries);
    }

    private static final StreamCodec<ByteBuf, List<QuickSalePriceEntry>>
            LIST_CODEC = QuickSalePriceEntry.STREAM_CODEC.apply(
                    ByteBufCodecs.list(2048)
            );

    public static final Type<QuickSalePricesSync> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    SimpleEconomy.MODID,
                    "quick_sale_prices"
            ));

    public static final StreamCodec<ByteBuf, QuickSalePricesSync> STREAM_CODEC =
            LIST_CODEC.map(
                    QuickSalePricesSync::new,
                    QuickSalePricesSync::entries
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Отправка всего прайса игроку
    public static void sendToPlayer(ServerPlayer player) {

        Map<String, Long> prices = MarketPriceData.get(
                player.server
        ).getAllPrices();

        List<QuickSalePriceEntry> entries = prices.entrySet()
                .stream()
                .map(entry -> new QuickSalePriceEntry(
                        entry.getKey(),
                        entry.getValue()
                ))
                .toList();

        PacketDistributor.sendToPlayer(
                player,
                new QuickSalePricesSync(entries)
        );
    }

    public static void handle(
            QuickSalePricesSync payload,
            IPayloadContext context
    ) {

        ClientQuickSaleCache.setPrices(payload.entries());
    }
}