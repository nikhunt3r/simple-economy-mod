package com.simpleeconomy.network;

import com.simpleeconomy.SimpleEconomy;
import com.simpleeconomy.economy.TransactionEntry;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record HistoryResult(
        List<TransactionEntry> entries
) implements CustomPacketPayload {

    public HistoryResult {
        entries = List.copyOf(entries);
    }

    public static final Type<HistoryResult> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    SimpleEconomy.MODID,
                    "history_result"
            ));

    private static final StreamCodec<ByteBuf, List<TransactionEntry>>
            LIST_CODEC = TransactionEntry.STREAM_CODEC.apply(
                    ByteBufCodecs.list(10)
            );

    public static final StreamCodec<ByteBuf, HistoryResult> STREAM_CODEC =
            LIST_CODEC.map(
                    HistoryResult::new,
                    HistoryResult::entries
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
            HistoryResult payload,
            IPayloadContext context
    ) {
        ClientHistoryCache.setEntries(payload.entries());
    }
}
