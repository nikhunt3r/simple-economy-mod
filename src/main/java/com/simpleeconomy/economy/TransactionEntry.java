
package com.simpleeconomy.economy;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TransactionEntry(
        long timestamp,
        String type,
        long amount,
        String description
) {

    public static final StreamCodec<ByteBuf, TransactionEntry> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_LONG,
                    TransactionEntry::timestamp,

                    ByteBufCodecs.stringUtf8(64),
                    TransactionEntry::type,

                    ByteBufCodecs.VAR_LONG,
                    TransactionEntry::amount,

                    ByteBufCodecs.stringUtf8(128),
                    TransactionEntry::description,

                    TransactionEntry::new
            );
}
