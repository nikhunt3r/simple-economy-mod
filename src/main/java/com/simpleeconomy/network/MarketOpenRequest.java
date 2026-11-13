package com.simpleeconomy.network;

import com.simpleeconomy.SimpleEconomy;
import com.simpleeconomy.market.MarketMenu;
import com.simpleeconomy.market.ModMenus;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.simpleeconomy.network.QuickSalePricesSync;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.world.SimpleMenuProvider;

public record MarketOpenRequest() implements CustomPacketPayload {

    public static final Type<MarketOpenRequest> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    SimpleEconomy.MODID,
                    "market_open"
            ));

    public static final StreamCodec<ByteBuf, MarketOpenRequest> STREAM_CODEC =
            StreamCodec.unit(new MarketOpenRequest());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send() {
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                new MarketOpenRequest()
        );
    }

    public static void handle(
            MarketOpenRequest payload,
            IPayloadContext context
    ) {

        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        player.openMenu(
                new SimpleMenuProvider(
                        (id, inventory, owner) ->
                                new MarketMenu(
                                        id,
                                        inventory,
                                        new ItemStackHandler(1)
                                ),
                        Component.literal("Рынок Simple Economy")
                )
        );
        QuickSalePricesSync.sendToPlayer(player);
    }
}