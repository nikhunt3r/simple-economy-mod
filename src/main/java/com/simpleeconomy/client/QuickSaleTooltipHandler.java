package com.simpleeconomy.client;

import com.simpleeconomy.util.EconomyText;

import com.simpleeconomy.SimpleEconomy;
import com.simpleeconomy.market.ClientQuickSaleCache;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.Locale;

@EventBusSubscriber(
        modid = SimpleEconomy.MODID,
        value = Dist.CLIENT
)
public class QuickSaleTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {

        // При построении поисковых подсказок игрок может отсутствовать
        if (event.getEntity() == null) {
            return;
        }

        ItemStack stack = event.getItemStack();

        if (stack.isEmpty()) {
            return;
        }

        String itemId = BuiltInRegistries.ITEM
                .getKey(stack.getItem())
                .toString();

        long unitPrice = ClientQuickSaleCache.getPrice(itemId);

        // Если цена не задана, ничего не добавляем
        if (unitPrice <= 0) {
            return;
        }

        String formattedPrice = String.format(
                Locale.US,
                "%,d",
                unitPrice
        ).replace(',', ' ');

        event.getToolTip().add(
                EconomyText.component(
                        "Быстрая продажа: "
                                + formattedPrice
                                + " $/шт."
                ).withStyle(ChatFormatting.GREEN)
        );
    }
}