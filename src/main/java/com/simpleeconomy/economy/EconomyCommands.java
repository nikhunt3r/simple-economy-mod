
package com.simpleeconomy.economy;

import com.simpleeconomy.util.EconomyText;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import com.simpleeconomy.SimpleEconomy;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Locale;

@EventBusSubscriber(modid = SimpleEconomy.MODID)
public class EconomyCommands {

    private static final int ADMIN_PERMISSION = 2;

    private enum Action {
        ADD,
        REMOVE,
        SET
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {

        event.getDispatcher().register(
                Commands.literal("eco")

                        // Просмотр баланса
                        .then(Commands.literal("balance")
                                .executes(EconomyCommands::showOwnBalance)

                                .then(Commands.argument(
                                        "player",
                                        EntityArgument.player()
                                )
                                .requires(source ->
                                        source.hasPermission(ADMIN_PERMISSION)
                                )
                                .executes(EconomyCommands::showOtherBalance))
                        )

                        // Начисление денег
                        .then(Commands.literal("add")
                                .requires(source ->
                                        source.hasPermission(ADMIN_PERMISSION)
                                )
                                .then(Commands.argument(
                                        "player",
                                        EntityArgument.player()
                                )
                                .then(Commands.argument(
                                        "amount",
                                        LongArgumentType.longArg(1)
                                )
                                .executes(context ->
                                        changeBalance(context, Action.ADD)
                                )))
                        )

                        // Списание денег
                        .then(Commands.literal("remove")
                                .requires(source ->
                                        source.hasPermission(ADMIN_PERMISSION)
                                )
                                .then(Commands.argument(
                                        "player",
                                        EntityArgument.player()
                                )
                                .then(Commands.argument(
                                        "amount",
                                        LongArgumentType.longArg(1)
                                )
                                .executes(context ->
                                        changeBalance(context, Action.REMOVE)
                                )))
                        )

                        // Установка конкретного баланса
                        .then(Commands.literal("set")
                                .requires(source ->
                                        source.hasPermission(ADMIN_PERMISSION)
                                )
                                .then(Commands.argument(
                                        "player",
                                        EntityArgument.player()
                                )
                                .then(Commands.argument(
                                        "amount",
                                        LongArgumentType.longArg(0)
                                )
                                .executes(context ->
                                        changeBalance(context, Action.SET)
                                )))
                        )
                        .then(Commands.literal("pay")
                                .then(Commands.argument(
                                        "player",
                                        EntityArgument.player()
                                )
                        .then(Commands.argument(
                                        "amount",
                                        LongArgumentType.longArg(1)
                                )
                                .executes(EconomyCommands::payPlayer)))
                        )
        );
    }

    // Просмотр собственного баланса
    private static int showOwnBalance(
            CommandContext<CommandSourceStack> context
    ) {

        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();

        if (player == null) {
            source.sendFailure(
                    EconomyText.component("Укажи игрока: /eco balance <игрок>")
            );
            return 0;
        }

        long balance = EconomyData.get(source.getServer())
                .getOrCreateBalance(player.getUUID());

        source.sendSuccess(
                () -> EconomyText.component(
                        "Твой баланс: " + formatMoney(balance) + " $"
                ),
                false
        );

        return Command.SINGLE_SUCCESS;
    }

    // Просмотр баланса другого игрока
    private static int showOtherBalance(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {

        ServerPlayer target = EntityArgument.getPlayer(context, "player");

        long balance = EconomyData.get(context.getSource().getServer())
                .getOrCreateBalance(target.getUUID());

        context.getSource().sendSuccess(
                () -> EconomyText.component(
                        "Баланс игрока " + target.getName().getString()
                                + ": " + formatMoney(balance) + " $"
                ),
                false
        );

        return Command.SINGLE_SUCCESS;
    }

    // Изменение баланса
    private static int changeBalance(
            CommandContext<CommandSourceStack> context,
            Action action
    ) throws CommandSyntaxException {

        ServerPlayer target = EntityArgument.getPlayer(context, "player");

        long amount = LongArgumentType.getLong(context, "amount");

        EconomyData data = EconomyData.get(
                context.getSource().getServer()
        );

        try {
            long newBalance;

            switch (action) {
                case ADD -> newBalance =
                        data.addBalance(target.getUUID(), amount);

                case REMOVE -> newBalance =
                        data.removeBalance(target.getUUID(), amount);

                case SET -> newBalance =
                        data.setBalance(target.getUUID(), amount);

                default -> throw new IllegalStateException();
            }

            String actionText = switch (action) {
                case ADD -> "Начислено";
                case REMOVE -> "Списано";
                case SET -> "Установлен новый баланс";
            };

            String message = actionText + " игроку "
                    + target.getName().getString()
                    + ". Текущий баланс: "
                    + formatMoney(newBalance) + " $";

            context.getSource().sendSuccess(
                    () -> EconomyText.component(message),
                    true
            );

            return Command.SINGLE_SUCCESS;

        } catch (IllegalArgumentException exception) {

            context.getSource().sendFailure(
                    EconomyText.component(exception.getMessage())
            );

            return 0;
        }
    }
    // Перевод денег другому игроку
private static int payPlayer(
        CommandContext<CommandSourceStack> context
) throws CommandSyntaxException {

    CommandSourceStack source = context.getSource();

    ServerPlayer sender = source.getPlayer();

    if (sender == null) {
        source.sendFailure(
                EconomyText.component(
                        "Переводы доступны только игрокам."
                )
        );
        return 0;
    }

    ServerPlayer target = EntityArgument.getPlayer(
            context,
            "player"
    );

    long amount = LongArgumentType.getLong(
            context,
            "amount"
    );

    EconomyData data = EconomyData.get(source.getServer());

    try {
        data.transferBalance(
        sender.getUUID(),
        sender.getName().getString(),
        target.getUUID(),
        target.getName().getString(),
        amount
);

        long senderBalance = data.getOrCreateBalance(
                sender.getUUID()
        );

        long receiverBalance = data.getOrCreateBalance(
                target.getUUID()
        );

        // Сообщение отправителю
        sender.sendSystemMessage(
                EconomyText.component(
                        "Вы перевели "
                                + formatMoney(amount)
                                + " $ игроку "
                                + target.getName().getString()
                                + ". Остаток: "
                                + formatMoney(senderBalance)
                                + " $"
                )
        );

        // Уведомление получателю
        target.sendSystemMessage(
                EconomyText.component(
                        "Игрок "
                                + sender.getName().getString()
                                + " перевёл вам "
                                + formatMoney(amount)
                                + " $. Ваш баланс: "
                                + formatMoney(receiverBalance)
                                + " $"
                )
        );

        return Command.SINGLE_SUCCESS;

    } catch (IllegalArgumentException exception) {

        source.sendFailure(
                EconomyText.component(exception.getMessage())
        );

        return 0;
    }
}
    // Форматирование чисел
    private static String formatMoney(long amount) {
        return String.format(Locale.US, "%,d", amount)
                .replace(',', ' ');
    }
}
