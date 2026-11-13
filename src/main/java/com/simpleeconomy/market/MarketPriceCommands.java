package com.simpleeconomy.market;

import com.simpleeconomy.util.EconomyText;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import com.simpleeconomy.SimpleEconomy;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import com.simpleeconomy.network.QuickSalePricesSync;

import java.util.Map;

@EventBusSubscriber(modid = SimpleEconomy.MODID)
public class MarketPriceCommands {

    private static final int ADMIN_PERMISSION = 2;

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {

        event.getDispatcher().register(
                Commands.literal("eco")
                        .then(Commands.literal("price")
                                .requires(source ->
                                        source.hasPermission(ADMIN_PERMISSION)
                                )

                                // Установка цены
                                .then(Commands.literal("set")

                                        // По предмету в руке
                                        .then(Commands.argument(
                                                "price",
                                                LongArgumentType.longArg(1)
                                        )
                                        .executes(
                                                MarketPriceCommands::setHeldPrice
                                        ))

                                        // По ID предмета из консоли
                                        .then(Commands.argument(
                                                "item",
                                                StringArgumentType.word()
                                        )
                                        .then(Commands.argument(
                                                "price",
                                                LongArgumentType.longArg(1)
                                        )
                                        .executes(
                                                MarketPriceCommands::setNamedPrice
                                        )))
                                )

                                // Удаление цены
                                .then(Commands.literal("remove")
                                        .executes(
                                                MarketPriceCommands::removeHeldPrice
                                        )
                                        .then(Commands.argument(
                                                "item",
                                                StringArgumentType.word()
                                        )
                                        .executes(
                                                MarketPriceCommands::removeNamedPrice
                                        ))
                                )

                                // Просмотр цены
                                .then(Commands.literal("get")
                                        .executes(
                                                MarketPriceCommands::getHeldPrice
                                        )
                                        .then(Commands.argument(
                                                "item",
                                                StringArgumentType.word()
                                        )
                                        .executes(
                                                MarketPriceCommands::getNamedPrice
                                        ))
                                )

                                // Список всех цен
                                .then(Commands.literal("list")
                                        .executes(
                                                MarketPriceCommands::listPrices
                                        )
                                )
                        )
        );
    }

    private static int setHeldPrice(
            CommandContext<CommandSourceStack> context
    ) {

        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(
                    EconomyText.component(
                            "Для этой команды возьми предмет в основную руку."
                    )
            );
            return 0;
        }

        ItemStack stack = player.getMainHandItem();

        if (stack.isEmpty()) {
            player.sendSystemMessage(
                    EconomyText.component("В руке нет предмета.")
            );
            return 0;
        }

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(
                stack.getItem()
        );

        long price = LongArgumentType.getLong(context, "price");

        return setPrice(context, id, price);
    }

    private static int setNamedPrice(
            CommandContext<CommandSourceStack> context
    ) {

        String itemName = StringArgumentType.getString(
                context,
                "item"
        );

        ResourceLocation id = parseItemId(context, itemName);

        if (id == null) {
            return 0;
        }

        long price = LongArgumentType.getLong(context, "price");

        return setPrice(context, id, price);
    }

    private static int setPrice(
            CommandContext<CommandSourceStack> context,
            ResourceLocation id,
            long price
    ) {

        MinecraftServer server = context.getSource().getServer();

        MarketPriceData data = MarketPriceData.get(server);

        data.setPrice(id, price);

        saveAndSync(server);

        String itemName = id.toString();

        context.getSource().sendSuccess(
                () -> EconomyText.component(
                        "Цена быстрой продажи установлена: "
                                + itemName
                                + " = "
                                + formatMoney(price)
                                + " $ за 1 штуку."
                ),
                true
        );

        return 1;
    }

    private static int removeHeldPrice(
            CommandContext<CommandSourceStack> context
    ) {

        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(
                    EconomyText.component(
                            "Укажи ID предмета при использовании из консоли."
                    )
            );
            return 0;
        }

        ItemStack stack = player.getMainHandItem();

        if (stack.isEmpty()) {
            player.sendSystemMessage(
                    EconomyText.component("В руке нет предмета.")
            );
            return 0;
        }

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(
                stack.getItem()
        );

        return removePrice(context, id);
    }

    private static int removeNamedPrice(
            CommandContext<CommandSourceStack> context
    ) {

        ResourceLocation id = parseItemId(
                context,
                StringArgumentType.getString(context, "item")
        );

        if (id == null) {
            return 0;
        }

        return removePrice(context, id);
    }

    private static int removePrice(
            CommandContext<CommandSourceStack> context,
            ResourceLocation id
    ) {

        MinecraftServer server = context.getSource().getServer();

        boolean removed = MarketPriceData.get(server).removePrice(id);

        if (!removed) {
            context.getSource().sendFailure(
                    EconomyText.component("Для этого предмета цена не установлена.")
            );
            return 0;
        }

        saveAndSync(server);

        context.getSource().sendSuccess(
                () -> EconomyText.component(
                        "Цена быстрой продажи удалена: " + id
                ),
                true
        );

        return 1;
    }

    private static int getHeldPrice(
            CommandContext<CommandSourceStack> context
    ) {

        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(
                    EconomyText.component(
                            "Укажи ID предмета при использовании из консоли."
                    )
            );
            return 0;
        }

        ItemStack stack = player.getMainHandItem();

        if (stack.isEmpty()) {
            player.sendSystemMessage(
                    EconomyText.component("В руке нет предмета.")
            );
            return 0;
        }

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(
                stack.getItem()
        );

        return showPrice(context, id);
    }

    private static int getNamedPrice(
            CommandContext<CommandSourceStack> context
    ) {

        ResourceLocation id = parseItemId(
                context,
                StringArgumentType.getString(context, "item")
        );

        if (id == null) {
            return 0;
        }

        return showPrice(context, id);
    }

    private static int showPrice(
            CommandContext<CommandSourceStack> context,
            ResourceLocation id
    ) {

        long price = MarketPriceData.get(
                context.getSource().getServer()
        ).getPrice(id);

        String message = price > 0
                ? id + ": " + formatMoney(price) + " $ за штуку."
                : "Цена для " + id + " не установлена.";

        context.getSource().sendSuccess(
                () -> EconomyText.component(message),
                false
        );

        return 1;
    }

    private static int listPrices(
            CommandContext<CommandSourceStack> context
    ) {

        Map<String, Long> prices = MarketPriceData.get(
                context.getSource().getServer()
        ).getAllPrices();

        context.getSource().sendSuccess(
                () -> EconomyText.component(
                        "Установлено цен: " + prices.size()
                ),
                false
        );

        for (Map.Entry<String, Long> entry : prices.entrySet()) {

            context.getSource().sendSuccess(
                    () -> EconomyText.component(
                            entry.getKey()
                                    + " = "
                                    + formatMoney(entry.getValue())
                                    + " $/шт."
                    ),
                    false
            );
        }

        return 1;
    }

    private static ResourceLocation parseItemId(
            CommandContext<CommandSourceStack> context,
            String value
    ) {

        ResourceLocation id = ResourceLocation.tryParse(value);

        if (id == null || BuiltInRegistries.ITEM.getOptional(id).isEmpty()) {

            context.getSource().sendFailure(
                    EconomyText.component("Неизвестный предмет: " + value)
            );

            return null;
        }

        return id;
    }

    private static void saveAndSync(MinecraftServer server) {

        server.overworld()
                .getDataStorage()
                .save();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            QuickSalePricesSync.sendToPlayer(player);
        }
    }

    private static String formatMoney(long amount) {

        return String.format(
                java.util.Locale.US,
                "%,d",
                amount
        ).replace(',', ' ');
    }
}
