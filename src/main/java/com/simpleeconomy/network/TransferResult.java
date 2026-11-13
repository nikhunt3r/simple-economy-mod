package com.simpleeconomy.network;

import com.simpleeconomy.SimpleEconomy;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TransferResult(
        boolean success,
        String message,
        long balance
) implements CustomPacketPayload {

    public static final Type<TransferResult> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    SimpleEconomy.MODID,
                    "transfer_result"
            ));

    public static final StreamCodec<ByteBuf, TransferResult> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    TransferResult::success,
                    ByteBufCodecs.stringUtf8(256),
                    TransferResult::message,
                    ByteBufCodecs.VAR_LONG,
                    TransferResult::balance,
                    TransferResult::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
            TransferResult payload,
            IPayloadContext context
    ) {
        ClientBalanceCache.setBalance(payload.balance());

        ClientBalanceCache.setTransferResult(
                payload.success(),
                payload.message()
        );
    }
}
