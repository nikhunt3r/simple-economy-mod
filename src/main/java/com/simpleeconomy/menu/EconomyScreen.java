
package com.simpleeconomy.menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.simpleeconomy.network.ClientBalanceCache;
import java.util.Locale;
import com.simpleeconomy.client.TransferScreen;
import net.minecraft.client.Minecraft;
import com.simpleeconomy.client.HistoryScreen;
import com.simpleeconomy.economy.TransactionEntry;
import com.simpleeconomy.network.ClientHistoryCache;
import com.simpleeconomy.network.HistoryRequest;
import com.simpleeconomy.network.MarketOpenRequest;

import java.util.List;
import java.util.Locale;

public class EconomyScreen extends Screen {

    private static final int BACKGROUND = 0xFF10161B;
    private static final int PANEL = 0xFF182128;
    private static final int SIDEBAR = 0xFF141C22;
    private static final int CARD = 0xFF202D29;
    private static final int CARD_SECONDARY = 0xFF253D32;
    private static final int GREEN = 0xFF72E0A0;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFF8A9B91;

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;

    public EconomyScreen() {
        super(Component.literal("Simple Economy"));
    }

    @Override
public void renderBackground(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float partialTick
) {
    // Ничего не делаем, чтобы отключить стандартное размытие
}
    @Override
    protected void init() {

        panelWidth = Math.min(540, this.width - 30);
        panelHeight = Math.min(340, this.height - 30);

        panelX = (this.width - panelWidth) / 2;
        panelY = (this.height - panelHeight) / 2;

        // Кнопка закрытия
        this.addRenderableWidget(
                Button.builder(
                        Component.literal("X"),
                        button -> this.onClose()
                ).bounds(
                        panelX + panelWidth - 34,
                        panelY + 12,
                        22,
                        20
                ).build()
        );
        // Загружаем свежую историю операций
ClientHistoryCache.beginRequest();
HistoryRequest.request();
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        // Собственный фон вместо стандартного размытия
        graphics.fill(
                0, 0, this.width, this.height,
                BACKGROUND
        );

        // Основная панель
        graphics.fill(
                panelX, panelY,
                panelX + panelWidth,
                panelY + panelHeight,
                PANEL
        );

        int sidebarWidth = 125;
        int contentX = panelX + sidebarWidth + 20;
        int contentWidth = panelWidth - sidebarWidth - 35;

        // Боковая панель
        graphics.fill(
                panelX,
                panelY,
                panelX + sidebarWidth,
                panelY + panelHeight,
                SIDEBAR
        );

        // Название мода
        graphics.drawString(
                this.font,
                "SIMPLE",
                panelX + 15,
                panelY + 22,
                GREEN
        );

        graphics.drawString(
                this.font,
                "ECONOMY",
                panelX + 15,
                panelY + 34,
                WHITE
        );

        // Навигация
        graphics.drawString(
                this.font,
                "ОБЗОР",
                panelX + 15,
                panelY + 75,
                GREEN
        );

        graphics.drawString(
                this.font,
                "Переводы",
                panelX + 15,
                panelY + 105,
                GRAY
        );

        graphics.drawString(
                this.font,
                "Магазин",
                panelX + 15,
                panelY + 130,
                GRAY
        );

        graphics.drawString(
                this.font,
                "История",
                panelX + 15,
                panelY + 155,
                GRAY
        );

        // Разделитель
        graphics.fill(
                panelX + sidebarWidth - 1,
                panelY,
                panelX + sidebarWidth,
                panelY + panelHeight,
                0xFF303B40
        );

        // Заголовок главной страницы
        graphics.drawString(
                this.font,
                "Главная",
                contentX,
                panelY + 20,
                WHITE
        );

        graphics.drawString(
                this.font,
                "Ваш финансовый кабинет",
                contentX,
                panelY + 35,
                GRAY
        );

        // Карточка баланса
        int balanceY = panelY + 65;
        int balanceHeight = 85;

        graphics.fill(
                contentX,
                balanceY,
                contentX + contentWidth,
                balanceY + balanceHeight,
                CARD
        );

        graphics.drawString(
                this.font,
                "ОБЩИЙ БАЛАНС",
                contentX + 14,
                balanceY + 13,
                GRAY
        );

        String balanceText = ClientBalanceCache.hasBalance()
        ? String.format(
                Locale.US,
                "%,d $",
                ClientBalanceCache.getBalance()
        ).replace(',', ' ')
        : "Загрузка...";

graphics.drawString(
        this.font,
        balanceText,
        contentX + 14,
        balanceY + 34,
        GREEN
);

        graphics.drawString(
                this.font,
                "Ваши игровые средства",
                contentX + 14,
                balanceY + 60,
                GRAY
        );

        // Карточки действий
        int actionY = balanceY + balanceHeight + 12;
        int actionWidth = (contentWidth - 8) / 2;

        graphics.fill(
                contentX,
                actionY,
                contentX + actionWidth,
                actionY + 55,
                CARD_SECONDARY
        );

        graphics.fill(
                contentX + actionWidth + 8,
                actionY,
                contentX + contentWidth,
                actionY + 55,
                CARD_SECONDARY
        );

        graphics.drawString(
                this.font,
                "Переводы",
                contentX + 10,
                actionY + 13,
                WHITE
        );

        graphics.drawString(
                this.font,
                "Отправить деньги",
                contentX + 10,
                actionY + 29,
                GRAY
        );

        graphics.drawString(
                this.font,
                "Магазин",
                contentX + actionWidth + 18,
                actionY + 13,
                WHITE
        );

        graphics.drawString(
                this.font,
                "Покупка товаров",
                contentX + actionWidth + 18,
                actionY + 29,
                GRAY
        );

        // Последние операции
        int historyY = actionY + 68;

        
graphics.drawString(
        this.font,
        "Последние операции",
        contentX,
        historyY,
        WHITE
);

List<TransactionEntry> recentEntries =
        ClientHistoryCache.getEntries();

if (!ClientHistoryCache.hasReceived()) {

    graphics.drawString(
            this.font,
            "Загрузка истории...",
            contentX,
            historyY + 19,
            GRAY
    );

} else if (recentEntries.isEmpty()) {

    graphics.drawString(
            this.font,
            "Операций пока нет",
            contentX,
            historyY + 19,
            GRAY
    );

} else {

    int count = Math.min(2, recentEntries.size());

    for (int i = 0; i < count; i++) {

        TransactionEntry entry = recentEntries.get(i);

        int rowY = historyY + 19 + i * 20;

        // Название операции и описание
        String label = entry.type();

        if (!entry.description().isBlank()) {
            label += " · " + entry.description();
        }

        // Форматируем сумму
        String formattedAmount = String.format(
                Locale.US,
                "%,d",
                entry.amount()
        ).replace(',', ' ');

        String amountText;
        int amountColor;

        switch (entry.type()) {

            case "Перевод отправлен",
                "Списание",
                "Покупка на рынке",
                "Комиссия рынка":
                amountText = "-" + formattedAmount + " $";
                amountColor = 0xFFF08A8A;
                break;

            case "Установка баланса":
                amountText = "= " + formattedAmount + " $";
                amountColor = 0xFFF0CF87;
                break;

            default:
                amountText = "+" + formattedAmount + " $";
                amountColor = GREEN;
                break;
        }

        // Определяем доступную ширину для названия
        int amountX = contentX + contentWidth
                - this.font.width(amountText);

        int maxLabelWidth = amountX - contentX - 12;

        // Сокращаем длинные названия
        while (
                !label.isEmpty()
                && this.font.width(label) > maxLabelWidth
        ) {
            label = label.substring(0, label.length() - 1);
        }

        if (!label.equals(
                entry.type() + (
                        entry.description().isBlank()
                                ? ""
                                : " · " + entry.description()
                )
        )) {
            while (
                    !label.isEmpty()
                    && this.font.width(label + "...") > maxLabelWidth
            ) {
                label = label.substring(0, label.length() - 1);
            }

            label += "...";
        }

        // Выводим операцию
        graphics.drawString(
                this.font,
                label,
                contentX,
                rowY,
                GRAY
        );

        // Выводим сумму
        graphics.drawString(
                this.font,
                amountText,
                amountX,
                rowY,
                amountColor
        );
    }
}


        // Отрисовка кнопок поверх интерфейса
        super.render(graphics, mouseX, mouseY, partialTick);
    }
    @Override
public boolean mouseClicked(
        double mouseX,
        double mouseY,
        int button
) {

    if (super.mouseClicked(mouseX, mouseY, button)) {
        return true;
    }

    if (button == 0) {

        // Переводы в боковом меню
        boolean sidebarTransfer =
                mouseX >= panelX + 10
                && mouseX <= panelX + 115
                && mouseY >= panelY + 97
                && mouseY <= panelY + 119;

        // Магазин в боковом меню
        boolean sidebarMarket =
                mouseX >= panelX + 10
                && mouseX <= panelX + 115
                && mouseY >= panelY + 122
                && mouseY <= panelY + 145;

        // История в боковом меню
        boolean sidebarHistory =
                mouseX >= panelX + 10
                && mouseX <= panelX + 115
                && mouseY >= panelY + 147
                && mouseY <= panelY + 170;

        int contentX = panelX + 145;
        int contentWidth = panelWidth - 160;

        int balanceY = panelY + 65;
        int balanceHeight = 85;

        int actionY = balanceY + balanceHeight + 12;
        int actionWidth = (contentWidth - 8) / 2;

        // Карточка переводов
        boolean cardTransfer =
                mouseX >= contentX
                && mouseX <= contentX + actionWidth
                && mouseY >= actionY
                && mouseY <= actionY + 55;

        // Карточка магазина
        boolean cardMarket =
                mouseX >= contentX + actionWidth + 8
                && mouseX <= contentX + contentWidth
                && mouseY >= actionY
                && mouseY <= actionY + 55;

        if (sidebarTransfer || cardTransfer) {
            Minecraft.getInstance().setScreen(
                    new com.simpleeconomy.client.TransferScreen()
            );
            return true;
        }

        if (sidebarHistory) {
            Minecraft.getInstance().setScreen(
                    new com.simpleeconomy.client.HistoryScreen()
            );
            return true;
        }

        if (sidebarMarket || cardMarket) {

            MarketOpenRequest.send();

            return true;
        }
    }

    return false;
}

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
