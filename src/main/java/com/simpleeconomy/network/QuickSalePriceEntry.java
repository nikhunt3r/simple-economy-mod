package com.simpleeconomy.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record QuickSalePriceEntry(
        String itemId,
        long price
) {

    public static final StreamCodec<ByteBuf, QuickSalePriceEntry> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.stringUtf8(256),
                    QuickSalePriceEntry::itemId,

                    ByteBufCodecs.VAR_LONG,
                    QuickSalePriceEntry::price,

                    QuickSalePriceEntry::new
            );
}