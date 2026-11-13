package com.simpleeconomy.client;

import com.simpleeconomy.SimpleEconomy;
import com.simpleeconomy.market.ModMenus;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(
        modid = SimpleEconomy.MODID,
        bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class MarketScreenRegistration {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {

        event.register(
                ModMenus.MARKET_MENU.get(),
                MarketScreen::new
        );
    }
}