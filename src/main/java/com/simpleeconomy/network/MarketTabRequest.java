package com.simpleeconomy.network;

import com.simpleeconomy.SimpleEconomy;
import com.simpleeconomy.market.MarketMenu;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MarketTabRequest(int tab)
        implements CustomPacketPayload {

    public static final Type<MarketTabRequest> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    SimpleEconomy.MODID,
                    "market_tab"
            ));

    public static final StreamCodec<ByteBuf, MarketTabRequest> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    MarketTabRequest::tab,
                    MarketTabRequest::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send(int tab) {

        PacketDistributor.sendToServer(
                new MarketTabRequest(tab)
        );
    }

    public static void handle(
            MarketTabRequest payload,
            IPayloadContext context
    ) {

        if (context.player() instanceof ServerPlayer player) {

            if (player.containerMenu instanceof MarketMenu menu) {
                menu.setActiveTab(payload.tab());
            }
        }
    }
}
