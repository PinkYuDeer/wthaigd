package com.pinkyudeer.wthaigd.gui;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import com.cleanroommc.modularui.factory.ClientGUI;
import com.pinkyudeer.wthaigd.gui.screen.MainModularScreen;

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
            // Minecraft mc = Minecraft.getMinecraft();
            // GuiScreenMain gui = new GuiScreenMain(mc.currentScreen);
            // mc.displayGuiScreen(gui);
            ClientGUI.open(new MainModularScreen().useTheme("wthaigd:main"));

        }
    }
}
