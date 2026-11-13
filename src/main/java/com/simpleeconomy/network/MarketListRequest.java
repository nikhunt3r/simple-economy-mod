package com.simpleeconomy.network;

import com.simpleeconomy.SimpleEconomy;
import com.simpleeconomy.market.ClientMarketCache;
import com.simpleeconomy.market.MarketData;
import com.simpleeconomy.market.MarketListing;
import com.simpleeconomy.market.MarketListingView;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public record MarketListRequest(
        boolean ownOnly,
        String search,
        int sortMode,
        int page
) implements CustomPacketPayload {

    public static final int PAGE_SIZE = 7;

    public static final Type<MarketListRequest> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    SimpleEconomy.MODID,
                    "market_list_request"
            ));

    public static final StreamCodec<ByteBuf, MarketListRequest> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    MarketListRequest::ownOnly,

                    ByteBufCodecs.stringUtf8(128),
                    MarketListRequest::search,

                    ByteBufCodecs.VAR_INT,
                    MarketListRequest::sortMode,

                    ByteBufCodecs.VAR_INT,
                    MarketListRequest::page,

                    MarketListRequest::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Клиентский запрос
    public static void request(
            boolean ownOnly,
            String search,
            int sortMode,
            int page
    ) {

        ClientMarketCache.beginRequest(
                ownOnly,
                search,
                sortMode,
                page
        );

        PacketDistributor.sendToServer(
                new MarketListRequest(
                        ownOnly,
                        search,
                        sortMode,
                        page
                )
        );
    }

    // Формирование списка на сервере
    public static void sendToPlayer(
            ServerPlayer player,
            boolean ownOnly,
            String search,
            int sortMode,
            int requestedPage
    ) {

        MarketData market = MarketData.get(player.server);

        List<MarketListing> source = ownOnly
                ? market.getPlayerListings(player.getUUID())
                : market.getListings();

        String query = search == null
                ? ""
                : search.trim().toLowerCase(Locale.ROOT);

        // Поиск по предмету и продавцу
        List<MarketListing> filtered = source.stream()
                .filter(listing -> {

                    if (query.isEmpty()) {
                        return true;
                    }

                    String itemName = listing.item()
                            .getHoverName()
                            .getString()
                            .toLowerCase(Locale.ROOT);

                    String sellerName = listing.sellerName()
                            .toLowerCase(Locale.ROOT);

                    return itemName.contains(query)
                            || sellerName.contains(query);
                })
                .collect(java.util.stream.Collectors.toCollection(
                        java.util.ArrayList::new
                ));

        // Сортировка
        if (sortMode == 1) {

            // Сначала дешёвые
            filtered.sort(
                    Comparator.comparingLong(MarketListing::price)
            );

        } else if (sortMode == 2) {

            // Сначала дорогие
            filtered.sort(
                    Comparator.comparingLong(MarketListing::price)
                            .reversed()
            );

        }
        // Режим 0 сохраняет порядок:
        // сначала новые объявления.

        int totalListings = filtered.size();

        int totalPages = Math.max(
                1,
                (totalListings + PAGE_SIZE - 1) / PAGE_SIZE
        );

        // Защищаемся от несуществующей страницы
        int page = Math.max(
                0,
                Math.min(requestedPage, totalPages - 1)
        );

        List<MarketListingView> result = filtered.stream()
                .skip((long) page * PAGE_SIZE)
                .limit(PAGE_SIZE)
                .map(listing -> new MarketListingView(
                        listing.id(),
                        listing.item().getHoverName().getString(),
                        listing.item().getCount(),
                        listing.price(),
                        listing.sellerName()
                ))
                .toList();

        PacketDistributor.sendToPlayer(
                player,
                new MarketListResult(
                        result,
                        ownOnly,
                        page,
                        totalPages,
                        totalListings,
                        search,
                        sortMode
                )
        );
    }

    public static void handle(
            MarketListRequest payload,
            IPayloadContext context
    ) {

        if (context.player() instanceof ServerPlayer player) {

            String search = payload.search() == null
                    ? ""
                    : payload.search().substring(
                            0,
                            Math.min(payload.search().length(), 32)
                    );

            int sort = Math.max(
                    0,
                    Math.min(payload.sortMode(), 2)
            );

            int page = Math.max(
                    0,
                    Math.min(payload.page(), 100000)
            );

            sendToPlayer(
                    player,
                    payload.ownOnly(),
                    search,
                    sort,
                    page
            );
        }
    }
}
