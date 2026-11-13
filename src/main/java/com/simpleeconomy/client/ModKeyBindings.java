
package com.simpleeconomy.client;

import com.simpleeconomy.SimpleEconomy;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(
        modid = SimpleEconomy.MODID,
        bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ModKeyBindings {

    public static final KeyMapping OPEN_ECONOMY = new KeyMapping(
            "key.simpleeconomy.open_economy",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.categories.misc"
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_ECONOMY);
    }
}
