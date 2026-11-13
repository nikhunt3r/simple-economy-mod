
package com.simpleeconomy.network;

import com.simpleeconomy.SimpleEconomy;
import com.simpleeconomy.market.ClientMarketCache;
import com.simpleeconomy.market.MarketListingView;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record MarketListResult(
        List<MarketListingView> listings,
        boolean ownOnly,
        int page,
        int totalPages,
        int totalListings,
        String search,
        int sortMode
) implements CustomPacketPayload {

    public MarketListResult {
        listings = List.copyOf(listings);
    }

    // Служебные данные объединяем в отдельную структуру
    private record Metadata(
            boolean ownOnly,
            int page,
            int totalPages,
            int totalListings,
            String search,
            int sortMode
    ) {}

    // Кодек списка объявлений
    private static final StreamCodec<ByteBuf, List<MarketListingView>>
            LIST_CODEC = MarketListingView.STREAM_CODEC.apply(
                    ByteBufCodecs.list(7)
            );

    // Кодек метаданных: ровно 6 полей
    private static final StreamCodec<ByteBuf, Metadata>
            METADATA_CODEC = StreamCodec.composite(

                    ByteBufCodecs.BOOL,
                    Metadata::ownOnly,

                    ByteBufCodecs.VAR_INT,
                    Metadata::page,

                    ByteBufCodecs.VAR_INT,
                    Metadata::totalPages,

                    ByteBufCodecs.VAR_INT,
                    Metadata::totalListings,

                    ByteBufCodecs.stringUtf8(128),
                    Metadata::search,

                    ByteBufCodecs.VAR_INT,
                    Metadata::sortMode,

                    Metadata::new
            );

    public static final Type<MarketListResult> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    SimpleEconomy.MODID,
                    "market_list_result"
            ));

    // Основной кодек объединяет только два объекта
    public static final StreamCodec<ByteBuf, MarketListResult>
            STREAM_CODEC = StreamCodec.composite(

                    LIST_CODEC,
                    MarketListResult::listings,

                    METADATA_CODEC,
                    result -> new Metadata(
                            result.ownOnly(),
                            result.page(),
                            result.totalPages(),
                            result.totalListings(),
                            result.search(),
                            result.sortMode()
                    ),

                    (list, metadata) -> new MarketListResult(
                            list,
                            metadata.ownOnly(),
                            metadata.page(),
                            metadata.totalPages(),
                            metadata.totalListings(),
                            metadata.search(),
                            metadata.sortMode()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
            MarketListResult payload,
            IPayloadContext context
    ) {

        ClientMarketCache.setListings(
                payload.listings(),
                payload.ownOnly(),
                payload.page(),
                payload.totalPages(),
                payload.totalListings(),
                payload.search(),
                payload.sortMode()
        );
    }
}
