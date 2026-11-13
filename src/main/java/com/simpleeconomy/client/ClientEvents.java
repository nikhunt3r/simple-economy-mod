
package com.simpleeconomy.client;

import com.simpleeconomy.SimpleEconomy;
import com.simpleeconomy.menu.EconomyScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import com.simpleeconomy.network.BalanceSync;

@EventBusSubscriber(
        modid = SimpleEconomy.MODID,
        value = Dist.CLIENT
)
public class ClientEvents {

    // Открытие меню клавишей K
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

        Minecraft minecraft = Minecraft.getInstance();

        while (ModKeyBindings.OPEN_ECONOMY.consumeClick()) {

            if (minecraft.level != null && minecraft.screen == null) {
                minecraft.setScreen(new EconomyScreen());
                BalanceSync.requestBalance();
            }
        }
    }

    // Регистрация команды /economy
    @SubscribeEvent
    public static void onRegisterClientCommands(
            RegisterClientCommandsEvent event
    ) {

        event.getDispatcher().register(
                Commands.literal("economy")
                        .executes(context -> {

                            Minecraft minecraft = Minecraft.getInstance();

                            if (minecraft.level != null) {
                                minecraft.setScreen(new EconomyScreen());
                                BalanceSync.requestBalance();
                                return 1;
                            }

                            return 0;
                        })
        );
    }
}
