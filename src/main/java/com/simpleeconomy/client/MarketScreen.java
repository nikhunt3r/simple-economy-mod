package com.simpleeconomy.client;

import com.simpleeconomy.util.EconomyText;

import com.simpleeconomy.market.ClientMarketCache;
import com.simpleeconomy.market.MarketListingView;
import com.simpleeconomy.market.MarketMenu;
import com.simpleeconomy.menu.EconomyScreen;
import com.simpleeconomy.network.MarketActionRequest;
import com.simpleeconomy.network.MarketListRequest;
import com.simpleeconomy.network.MarketPublishRequest;
import com.simpleeconomy.network.MarketTabRequest;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import com.simpleeconomy.network.MarketTabRequest;
import com.simpleeconomy.market.ClientQuickSaleCache;
import com.simpleeconomy.network.QuickSaleRequest;
import com.simpleeconomy.network.MarketTabRequest;

import net.minecraft.world.inventory.Slot;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

public class MarketScreen extends AbstractContainerScreen<MarketMenu> {

    private static final int BACKGROUND = 0xFF10161B;
    private static final int PANEL = 0xFF182128;
    private static final int SIDEBAR = 0xFF141C22;
    private static final int ROW = 0xFF202D29;
    private static final int ROW_ALT = 0xFF1C2827;

    private static final int GREEN = 0xFF72E0A0;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFF8A9B91;
    private static final int RED = 0xFFF08A8A;

    private static final int MAX_ROWS = 7;

    private int selectedTab = 0;

    private Button[] actionButtons = new Button[MAX_ROWS];

    private Button marketTab;
    private Button myListingsTab;
    private Button sellTab;
    private Button quickSaleTab;
    private Button quickSaleButton;
    private Button publishButton;
    private Button closeButton;

    private EditBox priceField;

    private EditBox searchField;
    private Button searchButton;
    private Button sortButton;
    private Button previousPageButton;
    private Button nextPageButton;

        private int currentPage = 0;
        private int sortMode = 0;

    public MarketScreen(
            MarketMenu menu,
            Inventory inventory,
            Component title
    ) {

        super(menu, inventory, title);

        this.imageWidth = 500;
        this.imageHeight = 320;

        this.titleLabelX = 16;
        this.titleLabelY = 12;

        this.inventoryLabelX = 169;
        this.inventoryLabelY = 171;
    }

    @Override
    protected void init() {

        super.init();

        // Общий рынок
marketTab = this.addRenderableWidget(
        Button.builder(
                EconomyText.component("Общий рынок"),
                button -> setTab(0)
        ).bounds(
                leftPos + 10,
                topPos + 38,
                112,
                20
        ).build()
);

// Мои объявления
myListingsTab = this.addRenderableWidget(
        Button.builder(
                EconomyText.component("Мои объявления"),
                button -> setTab(1)
        ).bounds(
                leftPos + 126,
                topPos + 38,
                112,
                20
        ).build()
);

// Обычная продажа на рынке
sellTab = this.addRenderableWidget(
        Button.builder(
                EconomyText.component("Продать"),
                button -> setTab(2)
        ).bounds(
                leftPos + 242,
                topPos + 38,
                100,
                20
        ).build()
);

// Быстрая продажа по прайсу
quickSaleTab = this.addRenderableWidget(
        Button.builder(
                EconomyText.component("Быстрая продажа"),
                button -> setTab(3)
        ).bounds(
                leftPos + 346,
                topPos + 38,
                144,
                20
        ).build()
);
            
        // Кнопки покупки и снятия объявлений
for (int i = 0; i < MAX_ROWS; i++) {

    final int index = i;

    actionButtons[i] = this.addRenderableWidget(
            Button.builder(
                    EconomyText.component("Купить"),
                    button -> performListingAction(index)
            ).bounds(
                    leftPos + 418,
                    topPos + 98 + i * 20,
                    70,
                    19
            ).build()
    );
}            
        // Поле поиска
searchField = new EditBox(
        this.font,
        leftPos + 14,
        topPos + 65,
        190,
        20,
        EconomyText.component("Поиск")
);

searchField.setMaxLength(32);
searchField.setHint(
        EconomyText.component("Предмет или продавец")
);

this.addRenderableWidget(searchField);

// Кнопка поиска
searchButton = this.addRenderableWidget(
        Button.builder(
                EconomyText.component("Найти"),
                button -> {
                    currentPage = 0;
                    requestListings();
                }
        ).bounds(
                leftPos + 210,
                topPos + 65,
                55,
                20
        ).build()
);

// Сортировка
sortButton = this.addRenderableWidget(
        Button.builder(
                EconomyText.component("Сначала новые"),
                button -> cycleSort()
        ).bounds(
                leftPos + 270,
                topPos + 65,
                110,
                20
        ).build()
);

// Предыдущая страница
previousPageButton = this.addRenderableWidget(
        Button.builder(
                EconomyText.component("<"),
                button -> changePage(-1)
        ).bounds(
                leftPos + 385,
                topPos + 65,
                35,
                20
        ).build()
);

// Следующая страница
nextPageButton = this.addRenderableWidget(
        Button.builder(
                EconomyText.component(">"),
                button -> changePage(1)
        ).bounds(
                leftPos + 430,
                topPos + 65,
                35,
                20
        ).build()
);

        // Поле цены
        priceField = new EditBox(
                this.font,
                leftPos + 105,
                topPos + 96,
                190,
                20,
                EconomyText.component("Цена")
        );

        priceField.setMaxLength(19);
        priceField.setFilter(
                value -> value.matches("[0-9]*")
        );
        priceField.setHint(
                EconomyText.component("Цена за весь лот")
        );

        this.addRenderableWidget(priceField);

        // Публикация товара
        publishButton = this.addRenderableWidget(
                Button.builder(
                        EconomyText.component("Выставить на рынок"),
                        button -> publishListing()
                ).bounds(
                        leftPos + 105,
                        topPos + 127,
                        145,
                        22
                ).build()
        );

        
quickSaleButton = this.addRenderableWidget(
        Button.builder(
                EconomyText.component("Продать предметы"),
                button -> performQuickSale()
        ).bounds(
                leftPos + 326,
                topPos + 105,
                150,
                22
        ).build()
);


        // Возврат в банковское меню
        closeButton = this.addRenderableWidget(
                Button.builder(
                        EconomyText.component("Назад"),
                        button -> onClose()
                ).bounds(
                        leftPos + 411,
                        topPos + 10,
                        76,
                        20
                ).build()
        );

        setTab(0);
    }

    // Переключение вкладок
    
private void setTab(int tab) {

    selectedTab = Math.max(0, Math.min(3, tab));
    ClientMarketCache.clearStatus();
    currentPage = 0;

    boolean listTab = selectedTab == 0 || selectedTab == 1;
    boolean itemTab = selectedTab == 2 || selectedTab == 3;

    // Локально переключаем слоты
    menu.setActiveTab(selectedTab);
    menu.setOfferingVisible(itemTab);
    menu.setInventoryVisible(itemTab);

    // Отправляем выбранную вкладку серверу
    MarketTabRequest.send(selectedTab);

    // Поиск и сортировка нужны только для списка
    searchField.visible = listTab;
    searchField.active = listTab;

    searchButton.visible = listTab;
    searchButton.active = listTab;

    sortButton.visible = listTab;
    sortButton.active = listTab;

    previousPageButton.visible = listTab;
    nextPageButton.visible = listTab;

    // Обычная продажа
    priceField.visible = selectedTab == 2;
    priceField.active = selectedTab == 2;

    publishButton.visible = selectedTab == 2;
    publishButton.active = selectedTab == 2;

    // Быстрая продажа
    quickSaleButton.visible = selectedTab == 3;
    quickSaleButton.active = false;

    updateSortButtonLabel();
    updateActionButtons();

    if (listTab) {
        requestListings();
    }
}

        // Отправка запроса серверу
private void requestListings() {

    MarketListRequest.request(
            selectedTab == 1,
            searchField.getValue(),
            sortMode,
            currentPage
    );
}

// Переключение режима сортировки
private void cycleSort() {

    sortMode = (sortMode + 1) % 3;
    currentPage = 0;

    updateSortButtonLabel();
    requestListings();
}

// Переключение страниц
private void changePage(int direction) {

    if (ClientMarketCache.isPending()) {
        return;
    }

    int newPage = ClientMarketCache.getPage() + direction;

    if (newPage < 0
            || newPage >= ClientMarketCache.getTotalPages()) {
        return;
    }

    currentPage = newPage;

    requestListings();
}

// Обновление надписи сортировки
private void updateSortButtonLabel() {

    if (sortButton == null) {
        return;
    }

    String label = switch (sortMode) {
        case 1 -> "Дешевле";
        case 2 -> "Дороже";
        default -> "Сначала новые";
    };

    sortButton.setMessage(EconomyText.component(label));
}

    // Обновляем видимость кнопок объявлений
    private void updateActionButtons() {

    List<MarketListingView> entries =
            ClientMarketCache.getListings();

    boolean listTab = selectedTab == 0 || selectedTab == 1;

    for (int i = 0; i < MAX_ROWS; i++) {

        if (actionButtons[i] == null) {
            continue;
        }

        boolean visible =
                listTab && i < entries.size();

        actionButtons[i].visible = visible;

        actionButtons[i].active =
        visible
        && ClientMarketCache.hasReceived()
        && !ClientMarketCache.isActionPending();

        actionButtons[i].setMessage(
                EconomyText.component(
                        selectedTab == 1 ? "Снять" : "Купить"
                )
        );
    }

    if (marketTab != null) {
        marketTab.active = selectedTab != 0;
    }

    if (myListingsTab != null) {
        myListingsTab.active = selectedTab != 1;
    }

    if (sellTab != null) {
        sellTab.active = selectedTab != 2;
    }

    if (previousPageButton != null) {

        previousPageButton.active =
                listTab
                && !ClientMarketCache.isPending()
                && ClientMarketCache.getPage() > 0;
    }

    if (nextPageButton != null) {

        nextPageButton.active =
                listTab
                && !ClientMarketCache.isPending()
                && ClientMarketCache.getPage() + 1
                        < ClientMarketCache.getTotalPages();
    }
    if (quickSaleTab != null) {
    quickSaleTab.active = selectedTab != 3;
}
}

    // Покупка или отмена объявления
    private void performListingAction(int index) {

        List<MarketListingView> entries =
                ClientMarketCache.getListings();

        if (index < 0 || index >= entries.size()) {
            return;
        }

        if (ClientMarketCache.isActionPending()
        || !ClientMarketCache.hasReceived()) {
    return;
}

        MarketListingView listing = entries.get(index);

        if (selectedTab == 1) {

            MarketActionRequest.send(
                    MarketActionRequest.CANCEL,
                    listing.id(),
                    true
            );

        } else {

            MarketActionRequest.send(
                    MarketActionRequest.BUY,
                    listing.id(),
                    false
            );
        }
    }

    // Выставление товара
    private void publishListing() {

        String value = priceField.getValue().trim();

        long price;

        try {
            price = Long.parseLong(value);
        } catch (NumberFormatException exception) {
            ClientMarketCache.setStatus(
                    "Введите корректную цену."
            );
            return;
        }

        if (price <= 0) {
            ClientMarketCache.setStatus(
                    "Цена должна быть больше нуля."
            );
            return;
        }

        if (menu.getOfferingStack().isEmpty()) {
            ClientMarketCache.setStatus(
                    "Положите предмет в слот."
            );
            return;
        }

        if (ClientMarketCache.isActionPending()
        || !ClientMarketCache.hasReceived()) {
    return;
}

        MarketPublishRequest.send(price);
    }

    // Проверяем цену выставленного предмета
private long getOfferingUnitPrice() {

    ItemStack stack = menu.getOfferingStack();

    if (stack.isEmpty()) {
        return 0;
    }

    String itemId = BuiltInRegistries.ITEM
            .getKey(stack.getItem())
            .toString();

    return ClientQuickSaleCache.getPrice(itemId);
}

// Обработка нажатия кнопки быстрой продажи
private void performQuickSale() {

    ItemStack stack = menu.getOfferingStack();

    if (stack.isEmpty()) {
        ClientMarketCache.setStatus(
                "Поместите предмет в слот."
        );
        return;
    }

    if (getOfferingUnitPrice() <= 0) {
        ClientMarketCache.setStatus(
                "Для этого предмета быстрая продажа недоступна."
        );
        return;
    }

    if (ClientMarketCache.isActionPending()
        || !ClientMarketCache.hasReceived()) {
    return;
}

    QuickSaleRequest.send();
}

    // Собственный фон без размытия
    @Override
    public void renderBackground(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        // Стандартное размытие отключено
    }

        @Override
public boolean keyPressed(
        int keyCode,
        int scanCode,
        int modifiers
) {

    // Поиск
    if (searchField != null && searchField.isFocused()) {

        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER
                || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER) {

            currentPage = 0;
            requestListings();
            return true;
        }

        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            searchField.setFocused(false);
            return true;
        }

        searchField.keyPressed(
                keyCode,
                scanCode,
                modifiers
        );

        // Не передаём клавишу AbstractContainerScreen.
        // Иначе он может обработать её как игровой биндинг.
        return true;
    }

    // Поле цены
    if (priceField != null && priceField.isFocused()) {

        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            priceField.setFocused(false);
            return true;
        }

        priceField.keyPressed(
                keyCode,
                scanCode,
                modifiers
        );

        return true;
    }

    return super.keyPressed(
            keyCode,
            scanCode,
            modifiers
    );
}
    
@Override
protected void renderBg(
        GuiGraphics graphics,
        float partialTick,
        int mouseX,
        int mouseY
) {

    // Координаты здесь локальные, без leftPos и topPos.
    graphics.fill(
            0, 0,
            imageWidth, imageHeight,
            PANEL
    );

    // Зелёная полоса
    graphics.fill(
            0, 0,
            3, imageHeight,
            GREEN
    );

    // Панель вкладок
    graphics.fill(
            10, 34,
            imageWidth - 10, 62,
            SIDEBAR
    );

    if (selectedTab == 0 || selectedTab == 1) {

        // Фоны объявлений
        for (int i = 0; i < MAX_ROWS; i++) {

            int rowY = 98 + i * 20;

            graphics.fill(
                    12,
                    rowY,
                    imageWidth - 12,
                    rowY + 19,
                    i % 2 == 0 ? ROW : ROW_ALT
            );
        }

    } else {

        // Панель выставления предмета
        graphics.fill(
                14, 70,
                imageWidth - 14, 163,
                SIDEBAR
        );
    }

    // Разделитель инвентаря
    graphics.fill(
            12, 169,
            imageWidth - 12, 170,
            0xFF303B40
    );
        
}

        @Override
protected void renderSlot(
        GuiGraphics graphics,
        net.minecraft.world.inventory.Slot slot
) {
    if (selectedTab == 2 || selectedTab == 3) {
        drawSlotBackground(
                graphics,
                slot.x,
                slot.y
        );
    }

    super.renderSlot(graphics, slot);
}


@Override
protected void renderLabels(
        GuiGraphics graphics,
        int mouseX,
        int mouseY
) {

    graphics.drawString(
            this.font,
            "SIMPLE ECONOMY / РЫНОК",
            16,
            12,
            WHITE
    );

    // ОБЩИЙ РЫНОК И МОИ ОБЪЯВЛЕНИЯ
    if (selectedTab == 0 || selectedTab == 1) {

        graphics.drawString(
                this.font,
                selectedTab == 0
                        ? "ДОСТУПНЫЕ ОБЪЯВЛЕНИЯ"
                        : "ВАШИ ОБЪЯВЛЕНИЯ",
                16,
                89,
                GRAY
        );

        graphics.drawString(
                this.font,
                "Найдено: " + ClientMarketCache.getTotalListings(),
                175,
                89,
                GRAY
        );

        graphics.drawString(
                this.font,
                "Страница "
                        + (ClientMarketCache.getPage() + 1)
                        + " / "
                        + ClientMarketCache.getTotalPages(),
                365,
                89,
                GREEN
        );

        List<MarketListingView> entries =
                ClientMarketCache.getListings();

        if (!ClientMarketCache.hasReceived()) {

            graphics.drawString(
                    this.font,
                    "Загрузка объявлений...",
                    18,
                    112,
                    GRAY
            );

        } else if (entries.isEmpty()) {

            graphics.drawString(
                    this.font,
                    searchField.getValue().isBlank()
                            ? "Объявлений пока нет."
                            : "По вашему запросу ничего не найдено.",
                    18,
                    112,
                    GRAY
            );

        } else {

            for (int i = 0;
                    i < Math.min(entries.size(), MAX_ROWS);
                    i++) {

                MarketListingView entry = entries.get(i);

                int rowY = 101 + i * 20;

                String itemText = "#"
                        + entry.id()
                        + " "
                        + entry.itemName()
                        + " x"
                        + entry.count()
                        + " · "
                        + entry.sellerName();

                itemText = truncate(itemText, 300);

                graphics.drawString(
                        this.font,
                        itemText,
                        18,
                        rowY,
                        WHITE
                );

                String priceText =
                        formatMoney(entry.price()) + " $";

                graphics.drawString(
                        this.font,
                        priceText,
                        345,
                        rowY,
                        GREEN
                );
            }
        }

    // ОБЫЧНАЯ ПРОДАЖА НА РЫНКЕ
    } else if (selectedTab == 2) {

        graphics.drawString(
                this.font,
                "ВЫСТАВЛЕНИЕ ТОВАРА",
                18,
                76,
                WHITE
        );

        graphics.drawString(
                this.font,
                "ПОМЕСТИТЕ ПРЕДМЕТ В СЛОТ",
                18,
                121,
                GRAY
        );

        graphics.drawString(
                this.font,
                "Цена указывается за весь лот.",
                18,
                157,
                GRAY
        );

    // БЫСТРАЯ ПРОДАЖА
    } else if (selectedTab == 3) {

        graphics.drawString(
                this.font,
                "БЫСТРАЯ ПРОДАЖА",
                18,
                76,
                WHITE
        );

        ItemStack stack = menu.getOfferingStack();

        if (stack.isEmpty()) {

            graphics.drawString(
                    this.font,
                    "Положите предмет в слот.",
                    65,
                    94,
                    GRAY
            );

        } else {

            long unitPrice = getOfferingUnitPrice();

            graphics.drawString(
                    this.font,
                    truncate(
                            stack.getHoverName().getString(),
                            260
                    ),
                    65,
                    85,
                    WHITE
            );

            graphics.drawString(
                    this.font,
                    "Количество: " + stack.getCount(),
                    65,
                    103,
                    GRAY
            );

            if (unitPrice <= 0) {

                graphics.drawString(
                        this.font,
                        "Цена не установлена.",
                        65,
                        122,
                        RED
                );

                graphics.drawString(
                        this.font,
                        "Продать этот предмет нельзя.",
                        65,
                        138,
                        GRAY
                );

            } else {

                long totalPrice;

                try {

                    totalPrice = Math.multiplyExact(
                            unitPrice,
                            (long) stack.getCount()
                    );

                    graphics.drawString(
                            this.font,
                            "Цена за 1 штуку: "
                                    + formatMoney(unitPrice) + " $",
                            65,
                            119,
                            GREEN
                    );

                    graphics.drawString(
                            this.font,
                            "Всего за стак: "
                                    + formatMoney(totalPrice) + " $",
                            65,
                            137,
                            GREEN
                    );

                } catch (ArithmeticException exception) {

                    graphics.drawString(
                            this.font,
                            "Слишком большая сумма.",
                            65,
                            122,
                            RED
                    );
                }
            }
        }
    }

    // Временное сообщение о результате операции
    if (!ClientMarketCache.getStatus().isEmpty()) {

        graphics.drawString(
                this.font,
                truncate(ClientMarketCache.getStatus(), 430),
                16,
                160,
                GREEN
        );
    }

    // Подпись инвентаря только на двух вкладках продажи
    if (selectedTab == 2 || selectedTab == 3) {

        graphics.drawString(
                this.font,
                "ИНВЕНТАРЬ",
                169,
                171,
                GRAY
        );
    }
}


private String formatMoney(long amount) {

    return String.format(
            Locale.US,
            "%,d",
            amount
    ).replace(',', ' ');
}

    private String truncate(String text, int maxWidth) {

        if (this.font.width(text) <= maxWidth) {
            return text;
        }

        while (
                !text.isEmpty()
                && this.font.width(text + "...") > maxWidth
        ) {
            text = text.substring(0, text.length() - 1);
        }

        return text + "...";
    }

    @Override
protected void containerTick() {
    super.containerTick();

    ClientMarketCache.tickStatus();
}

    @Override
public void render(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float partialTick
) {

    // Полностью непрозрачный фон.
    graphics.fill(
            0, 0,
            this.width, this.height,
            BACKGROUND
    );

    updateActionButtons();

    if (selectedTab == 3 && quickSaleButton != null) {

    ItemStack stack = menu.getOfferingStack();

    boolean canSell =
            !stack.isEmpty()
            && getOfferingUnitPrice() > 0
            && !ClientMarketCache.isActionPending();

    quickSaleButton.active = canSell;
}

    super.render(
            graphics,
            mouseX,
            mouseY,
            partialTick
    );
}

    @Override
    public void onClose() {

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player != null) {
            minecraft.player.closeContainer();
        }

        minecraft.setScreen(new EconomyScreen());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
    private void drawSlotBackground(
        GuiGraphics graphics,
        int x,
        int y
) {

    // Внешняя тёмная рамка
    graphics.fill(
            x, y,
            x + 18, y + 18,
            0xFF080D10
    );

    // Основная область слота
    graphics.fill(
            x + 1, y + 1,
            x + 17, y + 17,
            0xFF222E32
    );

    // Светлая верхняя и левая граница
    graphics.fill(
            x + 1, y + 1,
            x + 17, y + 2,
            0xFF52645D
    );

    graphics.fill(
            x + 1, y + 1,
            x + 2, y + 17,
            0xFF52645D
    );

    // Тёмная нижняя и правая граница
    graphics.fill(
            x + 1, y + 16,
            x + 17, y + 17,
            0xFF11191C
    );

    graphics.fill(
            x + 16, y + 1,
            x + 17, y + 17,
            0xFF11191C
    );
}
}