package com.simpleeconomy.network;

import com.simpleeconomy.SimpleEconomy;
import com.simpleeconomy.market.ClientMarketCache;
import com.simpleeconomy.network.MarketListRequest;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MarketActionResult(
        boolean success,
        String message,
        boolean ownOnly
) implements CustomPacketPayload {

    public static final Type<MarketActionResult> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    SimpleEconomy.MODID,
                    "market_action_result"
            ));

    public static final StreamCodec<ByteBuf, MarketActionResult> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    MarketActionResult::success,

                    ByteBufCodecs.stringUtf8(256),
                    MarketActionResult::message,

                    ByteBufCodecs.BOOL,
                    MarketActionResult::ownOnly,

                    MarketActionResult::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
        MarketActionResult payload,
        IPayloadContext context
) {

    ClientMarketCache.finishAction(payload.message());

    MarketListRequest.request(
            payload.ownOnly(),
            ClientMarketCache.getSearch(),
            ClientMarketCache.getSortMode(),
            ClientMarketCache.getPage()
    );
}
}