
package com.simpleeconomy.network;

import com.simpleeconomy.SimpleEconomy;
import com.simpleeconomy.economy.EconomyData;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BalanceSync(boolean response, long balance)
        implements CustomPacketPayload {

    public static final Type<BalanceSync> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    SimpleEconomy.MODID,
                    "balance_sync"
            ));

    public static final StreamCodec<ByteBuf, BalanceSync> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    BalanceSync::response,
                    ByteBufCodecs.VAR_LONG,
                    BalanceSync::balance,
                    BalanceSync::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void requestBalance() {
        PacketDistributor.sendToServer(new BalanceSync(false, 0));
    }

    public static void handle(
            BalanceSync payload,
            IPayloadContext context
    ) {
        if (context.player() instanceof ServerPlayer player) {

            // Сервер принимает только запрос баланса
            if (!payload.response()) {
                EconomyData data = EconomyData.get(player.server);

                long balance = data.getOrCreateBalance(
                        player.getUUID()
                );

                PacketDistributor.sendToPlayer(
                        player,
                        new BalanceSync(true, balance)
                );
            }

        } else if (payload.response()) {

            // Клиент получает подтверждённый сервером баланс
            ClientBalanceCache.setBalance(payload.balance());
        }
    }
}
