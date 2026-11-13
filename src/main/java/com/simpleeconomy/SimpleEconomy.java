
package com.simpleeconomy;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import com.simpleeconomy.network.BalanceSync;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import com.simpleeconomy.network.TransferRequest;
import com.simpleeconomy.network.TransferResult;
import com.simpleeconomy.network.HistoryRequest;
import com.simpleeconomy.network.HistoryResult;
import com.simpleeconomy.market.ModMenus;
import com.simpleeconomy.network.QuickSalePricesSync;
import com.simpleeconomy.network.MarketOpenRequest;
import com.simpleeconomy.network.MarketListRequest;
import com.simpleeconomy.network.MarketListResult;
import com.simpleeconomy.network.MarketPublishRequest;
import com.simpleeconomy.network.MarketActionRequest;
import com.simpleeconomy.network.MarketActionResult;
import com.simpleeconomy.network.MarketTabRequest;
import com.simpleeconomy.network.QuickSaleRequest;
import com.simpleeconomy.network.QuickSaleResult;

@Mod(SimpleEconomy.MODID)
public class SimpleEconomy {

    public static final String MODID = "simpleeconomy";

    public static final Logger LOGGER = LogUtils.getLogger();

    public SimpleEconomy(IEventBus modEventBus, ModContainer modContainer) {

        // Регистрируем обработчики игровых событий
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::registerPayloads);
        ModMenus.MENUS.register(modEventBus);
        LOGGER.info("SimpleEconomy успешно загружен!");
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {

    var registrar = event.registrar("1");

    registrar.playBidirectional(
            BalanceSync.TYPE,
            BalanceSync.STREAM_CODEC,
            BalanceSync::handle
    );

    registrar.playToServer(
            TransferRequest.TYPE,
            TransferRequest.STREAM_CODEC,
            TransferRequest::handle
    );

    registrar.playToClient(
            TransferResult.TYPE,
            TransferResult.STREAM_CODEC,
            TransferResult::handle
    );

    registrar.playToServer(
            HistoryRequest.TYPE,
            HistoryRequest.STREAM_CODEC,
            HistoryRequest::handle
    );

    registrar.playToClient(
            HistoryResult.TYPE,
            HistoryResult.STREAM_CODEC,
            HistoryResult::handle
    );

    // Открытие рынка
    registrar.playToServer(
            MarketOpenRequest.TYPE,
            MarketOpenRequest.STREAM_CODEC,
            MarketOpenRequest::handle
    );

    // Запрос объявлений
    registrar.playToServer(
            MarketListRequest.TYPE,
            MarketListRequest.STREAM_CODEC,
            MarketListRequest::handle
    );

    registrar.playToClient(
            MarketListResult.TYPE,
            MarketListResult.STREAM_CODEC,
            MarketListResult::handle
    );

    // Выставление товара
    registrar.playToServer(
            MarketPublishRequest.TYPE,
            MarketPublishRequest.STREAM_CODEC,
            MarketPublishRequest::handle
    );

    // Покупка и отмена объявлений
    registrar.playToServer(
            MarketActionRequest.TYPE,
            MarketActionRequest.STREAM_CODEC,
            MarketActionRequest::handle
    );

    registrar.playToClient(
            MarketActionResult.TYPE,
            MarketActionResult.STREAM_CODEC,
            MarketActionResult::handle
    );
    registrar.playToServer(
        MarketTabRequest.TYPE,
        MarketTabRequest.STREAM_CODEC,
        MarketTabRequest::handle);
        // Синхронизация стандартных цен сервера с клиентом
    registrar.playToClient(
        QuickSalePricesSync.TYPE,
        QuickSalePricesSync.STREAM_CODEC,
        QuickSalePricesSync::handle
    );
    // Запрос быстрой продажи: клиент -> сервер
registrar.playToServer(
        QuickSaleRequest.TYPE,
        QuickSaleRequest.STREAM_CODEC,
        QuickSaleRequest::handle
);

// Результат быстрой продажи: сервер -> клиент
registrar.playToClient(
        QuickSaleResult.TYPE,
        QuickSaleResult.STREAM_CODEC,
        QuickSaleResult::handle
);
    }
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Экономическая система запущена!");
    }
}
