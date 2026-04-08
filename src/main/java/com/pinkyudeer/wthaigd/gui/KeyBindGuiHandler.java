package com.pinkyudeer.wthaigd.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import com.cleanroommc.modularui.screen.UISettings;
import com.pinkyudeer.wthaigd.gui.screen.TaskScreen;
import com.pinkyudeer.wthaigd.gui.screen.TransparentScreenWrapper;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
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
            TaskScreen screen = new TaskScreen();
            UISettings settings = new UISettings();
            settings.useTheme("wthaigd:main");
            screen.getContext()
                .setSettings(settings);
            Minecraft.getMinecraft()
                .displayGuiScreen(new TransparentScreenWrapper(screen));
        }
    }
}
