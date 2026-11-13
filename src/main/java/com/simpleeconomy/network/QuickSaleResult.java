package com.simpleeconomy.network;

import com.simpleeconomy.SimpleEconomy;

import com.simpleeconomy.market.ClientMarketCache;
import com.simpleeconomy.network.ClientBalanceCache;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.network.handling.IPayloadContext;

public record QuickSaleResult(
        boolean success,
        String message,
        long balance
) implements CustomPacketPayload {

    public static final Type<QuickSaleResult> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    SimpleEconomy.MODID,
                    "quick_sale_result"
            ));

    public static final StreamCodec<ByteBuf, QuickSaleResult> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    QuickSaleResult::success,

                    ByteBufCodecs.stringUtf8(256),
                    QuickSaleResult::message,

                    ByteBufCodecs.VAR_LONG,
                    QuickSaleResult::balance,

                    QuickSaleResult::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
        QuickSaleResult payload,
        IPayloadContext context
) {

    ClientBalanceCache.setBalance(payload.balance());

    ClientMarketCache.finishAction(payload.message());
}
}