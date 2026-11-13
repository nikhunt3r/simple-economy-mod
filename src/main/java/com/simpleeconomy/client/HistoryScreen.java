package com.simpleeconomy.client;

import com.simpleeconomy.economy.TransactionEntry;
import com.simpleeconomy.network.ClientHistoryCache;
import com.simpleeconomy.network.HistoryRequest;
import com.simpleeconomy.menu.EconomyScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class HistoryScreen extends Screen {

    private static final int BACKGROUND = 0xFF10161B;
    private static final int PANEL = 0xFF182128;
    private static final int ROW = 0xFF202D29;
    private static final int ROW_ALT = 0xFF1C2827;

    private static final int GREEN = 0xFF72E0A0;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFF8A9B91;
    private static final int RED = 0xFFF08A8A;
    private static final int YELLOW = 0xFFF0CF87;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM HH:mm");

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;

    public HistoryScreen() {
        super(Component.literal("История операций"));
    }

    @Override
    protected void init() {

        panelWidth = Math.min(540, this.width - 30);
        panelHeight = Math.min(340, this.height - 30);

        panelX = (this.width - panelWidth) / 2;
        panelY = (this.height - panelHeight) / 2;

        // Запрашиваем свежую историю
        ClientHistoryCache.beginRequest();
        HistoryRequest.request();

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Назад"),
                        button -> this.onClose()
                ).bounds(
                        panelX + panelWidth / 2 - 45,
                        panelY + panelHeight - 30,
                        90,
                        20
                ).build()
        );
    }

    // Отключаем стандартное размытие Minecraft
    @Override
    public void renderBackground(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        // Пустой метод
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        // Фон
        graphics.fill(
                0, 0,
                this.width, this.height,
                BACKGROUND
        );

        // Центральная панель
        graphics.fill(
                panelX,
                panelY,
                panelX + panelWidth,
                panelY + panelHeight,
                PANEL
        );

        // Зелёная полоса слева
        graphics.fill(
                panelX,
                panelY,
                panelX + 3,
                panelY + panelHeight,
                GREEN
        );

        // Заголовок
        graphics.drawString(
                this.font,
                "ИСТОРИЯ ОПЕРАЦИЙ",
                panelX + 18,
                panelY + 17,
                WHITE
        );

        graphics.drawString(
                this.font,
                "Последние движения средств на вашем счёте",
                panelX + 18,
                panelY + 34,
                GRAY
        );

        // Заголовки колонок
        int typeX = panelX + 15;
        int descriptionX = panelX + 135;
        int dateX = panelX + panelWidth - 120;

        graphics.drawString(
                this.font,
                "ОПЕРАЦИЯ",
                typeX,
                panelY + 53,
                GRAY
        );

        graphics.drawString(
                this.font,
                "ОПИСАНИЕ",
                descriptionX,
                panelY + 53,
                GRAY
        );

        graphics.drawString(
                this.font,
                "ДАТА",
                dateX,
                panelY + 53,
                GRAY
        );

        if (!ClientHistoryCache.hasReceived()) {

            drawCentered(
                    graphics,
                    "Загрузка истории...",
                    panelY + 125,
                    GRAY
            );

        } else {

            List<TransactionEntry> entries =
                    ClientHistoryCache.getEntries();

            if (entries.isEmpty()) {

                drawCentered(
                        graphics,
                        "Операций пока нет",
                        panelY + 125,
                        GRAY
                );

            } else {

                int rowHeight = 22;
                int startY = panelY + 65;

                int maxRows = Math.max(
                        1,
                        Math.min(
                                10,
                                (panelHeight - 110) / rowHeight
                        )
                );

                int count = Math.min(
                        entries.size(),
                        maxRows
                );

                for (int i = 0; i < count; i++) {

                    TransactionEntry entry = entries.get(i);

                    int rowY = startY + i * rowHeight;

                    // Фон строки
                    graphics.fill(
                            panelX + 12,
                            rowY,
                            panelX + panelWidth - 12,
                            rowY + 20,
                            i % 2 == 0 ? ROW : ROW_ALT
                    );

                    // Название операции
                    int typeColor = getTypeColor(entry.type());

                    graphics.drawString(
                            this.font,
                            truncate(entry.type(), 112),
                            typeX,
                            rowY + 6,
                            typeColor
                    );

                    // Описание
                    int descriptionMaxWidth =
                            Math.max(20, dateX - descriptionX - 8);

                    graphics.drawString(
                            this.font,
                            truncate(
                                    entry.description(),
                                    descriptionMaxWidth
                            ),
                            descriptionX,
                            rowY + 6,
                            GRAY
                    );

                    // Дата
                    String dateText = Instant.ofEpochMilli(
                            entry.timestamp()
                    ).atZone(ZoneId.systemDefault())
                            .format(DATE_FORMAT);

                    graphics.drawString(
                            this.font,
                            dateText,
                            dateX,
                            rowY + 6,
                            GRAY
                    );

                    // Сумма
                    String amountText = getAmountText(entry);

                    int amountX = panelX + panelWidth
                            - 15 - this.font.width(amountText);

                    graphics.drawString(
                            this.font,
                            amountText,
                            amountX,
                            rowY + 6,
                            getAmountColor(entry)
                    );
                }
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawCentered(
            GuiGraphics graphics,
            String text,
            int y,
            int color
    ) {

        int x = (this.width - this.font.width(text)) / 2;

        graphics.drawString(
                this.font,
                text,
                x,
                y,
                color
        );
    }

    private String truncate(String text, int maxWidth) {

        if (this.font.width(text) <= maxWidth) {
            return text;
        }

        while (!text.isEmpty()
                && this.font.width(text + "...") > maxWidth) {
            text = text.substring(0, text.length() - 1);
        }

        return text + "...";
    }

    private int getTypeColor(String type) {

    return switch (type) {
        case "Перевод отправлен",
             "Списание",
             "Покупка на рынке",
             "Комиссия рынка" -> RED;

        case "Установка баланса" -> YELLOW;

        default -> GREEN;
    };
}

    private int getAmountColor(TransactionEntry entry) {

    return switch (entry.type()) {
        case "Перевод отправлен",
             "Списание",
             "Покупка на рынке",
             "Комиссия рынка" -> RED;

        case "Установка баланса" -> YELLOW;

        default -> GREEN;
    };
}

    private String getAmountText(TransactionEntry entry) {

    String amount = String.format(
            Locale.US,
            "%,d",
            entry.amount()
    ).replace(',', ' ');

    return switch (entry.type()) {

        case "Перевод отправлен",
             "Списание",
             "Покупка на рынке",
             "Комиссия рынка" ->
                "-" + amount + " $";

        case "Установка баланса" ->
                "= " + amount + " $";

        default ->
                "+" + amount + " $";
    };
}

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(new EconomyScreen());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
