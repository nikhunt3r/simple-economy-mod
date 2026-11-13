package com.simpleeconomy.client;

import com.simpleeconomy.util.EconomyText;

import com.simpleeconomy.menu.EconomyScreen;
import com.simpleeconomy.network.BalanceSync;
import com.simpleeconomy.network.ClientBalanceCache;
import com.simpleeconomy.network.TransferRequest;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public class TransferScreen extends Screen {

    private static final int BACKGROUND = 0xFF10161B;
    private static final int PANEL = 0xFF182128;
    private static final int CARD = 0xFF202D29;
    private static final int GREEN = 0xFF72E0A0;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFF8A9B91;
    private static final int RED = 0xFFF08A8A;

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;

    private EditBox playerField;
    private EditBox amountField;

    private Button sendButton;

    public TransferScreen() {
        super(EconomyText.component("Перевод денег"));
    }

    @Override
    protected void init() {

        panelWidth = Math.min(420, this.width - 30);
        panelHeight = Math.min(290, this.height - 30);

        panelX = (this.width - panelWidth) / 2;
        panelY = (this.height - panelHeight) / 2;

        ClientBalanceCache.clearTransferResult();

        // Запрашиваем актуальный баланс
        BalanceSync.requestBalance();

        // Поле игрока
        playerField = new EditBox(
                this.font,
                panelX + 20,
                panelY + 91,
                panelWidth - 40,
                20,
                EconomyText.component("Ник игрока")
        );

        playerField.setMaxLength(16);
        playerField.setFilter(
                value -> value.matches("[A-Za-z0-9_]{0,16}")
        );
        playerField.setHint(EconomyText.component("Ник получателя"));

        this.addRenderableWidget(playerField);

        // Поле суммы
        amountField = new EditBox(
                this.font,
                panelX + 20,
                panelY + 140,
                panelWidth - 40,
                20,
                EconomyText.component("Сумма")
        );

        amountField.setMaxLength(19);
        amountField.setFilter(
                value -> value.matches("[0-9]*")
        );
        amountField.setHint(EconomyText.component("Например, 500"));

        this.addRenderableWidget(amountField);

        // Кнопка отправки
        sendButton = Button.builder(
                EconomyText.component("Отправить деньги"),
                button -> sendTransfer()
        ).bounds(
                panelX + panelWidth / 2 - 75,
                panelY + 181,
                150,
                22
        ).build();

        this.addRenderableWidget(sendButton);

        // Возврат в главное меню
        this.addRenderableWidget(
                Button.builder(
                        EconomyText.component("Назад"),
                        button -> this.onClose()
                ).bounds(
                        panelX + panelWidth / 2 - 45,
                        panelY + 251,
                        90,
                        20
                ).build()
        );
    }

    private void sendTransfer() {

        String targetName = playerField.getValue().trim();
        String amountText = amountField.getValue().trim();

        if (!targetName.matches("[A-Za-z0-9_]{1,16}")) {
            ClientBalanceCache.setTransferResult(
                    false,
                    "Введите корректный ник игрока."
            );
            return;
        }

        long amount;

        try {
            amount = Long.parseLong(amountText);
        } catch (NumberFormatException exception) {
            ClientBalanceCache.setTransferResult(
                    false,
                    "Введите корректную сумму."
            );
            return;
        }

        if (amount <= 0) {
            ClientBalanceCache.setTransferResult(
                    false,
                    "Сумма должна быть больше нуля."
            );
            return;
        }

        ClientBalanceCache.beginTransfer();

        TransferRequest.send(targetName, amount);
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        graphics.fill(
                0, 0,
                this.width, this.height,
                BACKGROUND
        );

        // Основная панель
        graphics.fill(
                panelX, panelY,
                panelX + panelWidth,
                panelY + panelHeight,
                PANEL
        );

        // Зелёная полоса
        graphics.fill(
                panelX,
                panelY,
                panelX + 3,
                panelY + panelHeight,
                GREEN
        );

        graphics.drawString(
                this.font,
                "ПЕРЕВОД МЕЖДУ ИГРОКАМИ",
                panelX + 20,
                panelY + 19,
                WHITE
        );

        // Баланс
        String balanceText;

        if (ClientBalanceCache.hasBalance()) {
            balanceText = "Ваш баланс: "
                    + String.format(
                            Locale.US,
                            "%,d",
                            ClientBalanceCache.getBalance()
                    ).replace(',', ' ')
                    + " $";
        } else {
            balanceText = "Загрузка баланса...";
        }

        graphics.drawString(
                this.font,
                balanceText,
                panelX + 20,
                panelY + 46,
                GREEN
        );

        // Поля
        graphics.drawString(
                this.font,
                "ПОЛУЧАТЕЛЬ",
                panelX + 20,
                panelY + 75,
                GRAY
        );

        graphics.drawString(
                this.font,
                "СУММА ПЕРЕВОДА",
                panelX + 20,
                panelY + 124,
                GRAY
        );

        // Карточка подсказки
        graphics.fill(
                panelX + 20,
                panelY + 217,
                panelX + panelWidth - 20,
                panelY + 241,
                CARD
        );

        // Статус операции
        String status = "";

        int statusColor = GRAY;

        if (ClientBalanceCache.isTransferPending()) {

            status = "Отправка перевода...";

        } else if (ClientBalanceCache.hasTransferResult()) {

            status = ClientBalanceCache.getTransferMessage();

            statusColor = ClientBalanceCache.wasTransferSuccessful()
                    ? GREEN
                    : RED;
        }

        if (!status.isEmpty()) {

            int textWidth = this.font.width(status);

            graphics.drawString(
                    this.font,
                    status,
                    panelX + (panelWidth - textWidth) / 2,
                    panelY + 225,
                    statusColor
            );
        }

        sendButton.active = !ClientBalanceCache.isTransferPending();

        super.render(graphics, mouseX, mouseY, partialTick);
    }
    
    @Override
public void renderBackground(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float partialTick
) {
    // Отключаем стандартное размытие Minecraft
}
    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(new EconomyScreen());
        BalanceSync.requestBalance();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
