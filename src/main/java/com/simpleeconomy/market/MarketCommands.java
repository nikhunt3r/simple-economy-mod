package com.simpleeconomy.market;

import java.util.List;
import java.util.Locale;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.simpleeconomy.SimpleEconomy;
import com.simpleeconomy.economy.EconomyData;
import com.simpleeconomy.market.MarketInventoryHelper;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = SimpleEconomy.MODID)
public class MarketCommands {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {

        event.getDispatcher().register(
                Commands.literal("market")

                        // Выставление предмета
                        .then(Commands.literal("sell")
                                .then(Commands.argument(
                                        "price",
                                        LongArgumentType.longArg(1)
                                )
                                .executes(MarketCommands::sellItem))
                        )

                        // Просмотр рынка
                        .then(Commands.literal("list")
                                .executes(MarketCommands::showListings)
                        )

                        // Отмена своего объявления
                        .then(Commands.literal("cancel")
                                .then(Commands.argument(
                                        "id",
                                        LongArgumentType.longArg(1)
                                )
                                .executes(MarketCommands::cancelListing))
                        )
                        // Покупка объявления
                        .then(Commands.literal("buy")
                                .then(Commands.argument(
                                        "id",
                                        LongArgumentType.longArg(1)
                                )
                                .executes(MarketCommands::buyListing))
                        )
        );
    }

    // Выставление предмета из основной руки
    private static int sellItem(
            CommandContext<CommandSourceStack> context
    ) {

        ServerPlayer player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(
                    Component.literal(
                            "Эту команду может использовать только игрок."
                    )
            );

            return 0;
        }

        long price = LongArgumentType.getLong(
                context,
                "price"
        );

        ItemStack heldItem = player.getMainHandItem();

        if (heldItem.isEmpty()) {

            player.sendSystemMessage(
                    Component.literal(
                            "Возьми предмет в основную руку."
                    )
            );

            return 0;
        }

        MarketData market = MarketData.get(
                context.getSource().getServer()
        );

        try {

            MarketListing listing = market.createListing(
                    player.getUUID(),
                    player.getName().getString(),
                    heldItem.copy(),
                    price
            );

            // Убираем предмет из руки только после
            // успешного создания объявления
            player.setItemInHand(
                    InteractionHand.MAIN_HAND,
                    ItemStack.EMPTY
            );
            MarketData.saveNow(player.server);

            String message = "Объявление №"
                    + listing.id()
                    + " создано: "
                    + heldItem.getHoverName().getString()
                    + " x"
                    + heldItem.getCount()
                    + " за "
                    + formatMoney(price)
                    + " $";

            player.sendSystemMessage(
                    Component.literal(message)
            );

            return Command.SINGLE_SUCCESS;

        } catch (IllegalArgumentException exception) {

            player.sendSystemMessage(
                    Component.literal(exception.getMessage())
            );

            return 0;
        }
    }

    // Просмотр объявлений
    private static int showListings(
            CommandContext<CommandSourceStack> context
    ) {

        CommandSourceStack source = context.getSource();

        MarketData market = MarketData.get(source.getServer());

        List<MarketListing> listings = market.getListings();

        source.sendSuccess(
                () -> Component.literal(
                        "РЫНОК: активных объявлений "
                                + listings.size()
                ),
                false
        );

        if (listings.isEmpty()) {

            source.sendSuccess(
                    () -> Component.literal(
                            "На рынке пока нет товаров."
                    ),
                    false
            );

            return Command.SINGLE_SUCCESS;
        }

        int count = Math.min(20, listings.size());

        for (int i = 0; i < count; i++) {

            MarketListing listing = listings.get(i);

            ItemStack item = listing.item();

            String message = "#"
                    + listing.id()
                    + " | "
                    + item.getHoverName().getString()
                    + " x"
                    + item.getCount()
                    + " | "
                    + formatMoney(listing.price())
                    + " $"
                    + " | Продавец: "
                    + listing.sellerName();

            source.sendSuccess(
                    () -> Component.literal(message),
                    false
            );
        }

        if (listings.size() > count) {

            int remaining = listings.size() - count;

            source.sendSuccess(
                    () -> Component.literal(
                            "Ещё объявлений: " + remaining
                    ),
                    false
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    // Отмена объявления
    private static int cancelListing(
            CommandContext<CommandSourceStack> context
    ) {

        ServerPlayer player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(
                    Component.literal(
                            "Эту команду может использовать только игрок."
                    )
            );

            return 0;
        }

        long id = LongArgumentType.getLong(context, "id");

        MarketData market = MarketData.get(
                context.getSource().getServer()
        );

        MarketListing currentListing = market.getListing(id);

if (currentListing == null ||
        !currentListing.sellerId().equals(player.getUUID())) {

    player.sendSystemMessage(
            Component.literal(
                    "Объявление не найдено или принадлежит другому игроку."
            )
    );
    return 0;
}

if (!MarketInventoryHelper.canFitCompletely(
        player.getInventory(),
        currentListing.item()
)) {
    player.sendSystemMessage(
            Component.literal(
                    "Недостаточно места в инвентаре. Освободите слоты и попробуйте снова."
            )
    );
    return 0;
}

        MarketListing listing = market.cancelListing(
                id,
                player.getUUID()
        );

        if (listing == null) {

            player.sendSystemMessage(
                    Component.literal(
                            "Объявление не найдено или принадлежит другому игроку."
                    )
            );

            return 0;
        }

        ItemStack returnedItem = listing.item();

        // Возвращаем предмет в инвентарь.
        // Если места нет, остаток выпадет рядом с игроком.
        player.getInventory().add(listing.item());

MarketData.saveNow(player.server);

player.sendSystemMessage(
        Component.literal(
                "Объявление №" + id + " отменено. Предмет возвращён."
        )
);

        return Command.SINGLE_SUCCESS;
    }
            
    // Покупка товара
    private static int buyListing(
            CommandContext<CommandSourceStack> context
    ) {

        ServerPlayer buyer = context.getSource().getPlayer();

        if (buyer == null) {
            context.getSource().sendFailure(
                    Component.literal(
                            "Покупать товары могут только игроки."
                    )
            );
            return 0;
        }

        long id = LongArgumentType.getLong(context, "id");

        MarketData market = MarketData.get(
                context.getSource().getServer()
        );

        MarketListing listing = market.getListing(id);

        if (listing == null) {
            buyer.sendSystemMessage(
                    Component.literal("Объявление не найдено.")
            );
            return 0;
        }

        if (listing.sellerId().equals(buyer.getUUID())) {
            buyer.sendSystemMessage(
                    Component.literal(
                            "Нельзя покупать собственный товар."
                    )
            );
            return 0;
        }

        if (!MarketInventoryHelper.canFitCompletely(
        buyer.getInventory(),
        listing.item()
)) {
    buyer.sendSystemMessage(
            Component.literal(
                    "Недостаточно места в инвентаре. Освободите слоты и попробуйте снова."
            )
    );
    return 0;
}

        // Снимаем объявление с рынка.
        // Если оплата не пройдёт, вернём его обратно.
        MarketListing removed = market.removeListing(id);

        if (removed == null) {
            buyer.sendSystemMessage(
                    Component.literal(
                            "Объявление уже недоступно."
                    )
            );
            return 0;
        }

        EconomyData economy = EconomyData.get(
                context.getSource().getServer()
        );

        long sellerPayout;

        try {

            sellerPayout = economy.purchaseMarketItem(
                    buyer.getUUID(),
                    buyer.getName().getString(),
                    removed.sellerId(),
                    removed.sellerName(),
                    removed.id(),
                    removed.item().getHoverName().getString(),
                    removed.price()
            );

        } catch (IllegalArgumentException exception) {

            market.restoreListing(removed);

            buyer.sendSystemMessage(
                    Component.literal(exception.getMessage())
            );

            return 0;
        }

        // Передаём предмет покупателю
buyer.getInventory().add(removed.item());

        long commission = removed.price() / 20L;

        buyer.sendSystemMessage(
                Component.literal(
                        "Вы купили "
                                + removed.item().getHoverName().getString()
                                + " x"
                                + removed.item().getCount()
                                + " за "
                                + formatMoney(removed.price())
                                + " $."
                )
        );

        buyer.sendSystemMessage(
                Component.literal(
                        "Комиссия рынка: "
                                + formatMoney(commission)
                                + " $. Продавцу перечислено: "
                                + formatMoney(sellerPayout)
                                + " $."
                )
        );

        // Если продавец онлайн, уведомляем его
        ServerPlayer onlineSeller = context.getSource()
                .getServer()
                .getPlayerList()
                .getPlayer(removed.sellerId());

        if (onlineSeller != null) {

            onlineSeller.sendSystemMessage(
                    Component.literal(
                            "Ваш товар "
                                    + removed.item().getHoverName().getString()
                                    + " купили за "
                                    + formatMoney(removed.price())
                                    + " $. Вам начислено "
                                    + formatMoney(sellerPayout)
                                    + " $."
                    )
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    private static String formatMoney(long amount) {

        return String.format(
                Locale.US,
                "%,d",
                amount
        ).replace(',', ' ');
    }
}
