package com.simpleeconomy.network;

import com.simpleeconomy.SimpleEconomy;
import com.simpleeconomy.economy.EconomyData;
import com.simpleeconomy.economy.TransactionEntry;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record HistoryRequest() implements CustomPacketPayload {

    public static final Type<HistoryRequest> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    SimpleEconomy.MODID,
                    "history_request"
            ));

    public static final StreamCodec<ByteBuf, HistoryRequest> STREAM_CODEC =
            StreamCodec.unit(new HistoryRequest());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void request() {
        PacketDistributor.sendToServer(new HistoryRequest());
    }

    public static void handle(
            HistoryRequest payload,
            IPayloadContext context
    ) {

        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        EconomyData data = EconomyData.get(player.server);

        List<TransactionEntry> history =
                data.getHistory(player.getUUID());

        PacketDistributor.sendToPlayer(
                player,
                new HistoryResult(history)
        );
    }
}
