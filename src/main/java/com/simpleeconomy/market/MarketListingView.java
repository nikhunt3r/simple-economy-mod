package com.simpleeconomy.market;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record MarketListingView(
        long id,
        String itemName,
        int count,
        long price,
        String sellerName
) {

    public static final StreamCodec<ByteBuf, MarketListingView> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_LONG,
                    MarketListingView::id,

                    ByteBufCodecs.stringUtf8(128),
                    MarketListingView::itemName,

                    ByteBufCodecs.VAR_INT,
                    MarketListingView::count,

                    ByteBufCodecs.VAR_LONG,
                    MarketListingView::price,

                    ByteBufCodecs.stringUtf8(16),
                    MarketListingView::sellerName,

                    MarketListingView::new
            );
}