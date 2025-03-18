package com.pinkyudeer.wthaigd.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;

public class KeyBindGuiHandler {

    public static final KeyBinding openTaskGui = new KeyBinding(
        "key.openTaskGui",
        Keyboard.KEY_I,
        "key.categories.wthaigd");

    public KeyBindGuiHandler() {
        ClientRegistry.registerKeyBinding(openTaskGui);
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (openTaskGui.isPressed()) {
            Minecraft mc = Minecraft.getMinecraft();
            GuiDisplayTask gui = new GuiDisplayTask(mc.currentScreen);
            mc.displayGuiScreen(gui);
        }
    }
}
