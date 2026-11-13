package com.simpleeconomy.market;

import com.simpleeconomy.SimpleEconomy;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(
                    Registries.MENU,
                    SimpleEconomy.MODID
            );

    public static final Supplier<MenuType<MarketMenu>> MARKET_MENU =
            MENUS.register(
                    "market",
                    () -> new MenuType<>(
                            MarketMenu::new,
                            FeatureFlags.DEFAULT_FLAGS
                    )
            );
}