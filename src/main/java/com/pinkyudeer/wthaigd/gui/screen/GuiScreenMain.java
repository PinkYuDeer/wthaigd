package com.pinkyudeer.wthaigd.gui.screen;

import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import com.pinkyudeer.wthaigd.gui.KeyBindGuiHandler;
import com.pinkyudeer.wthaigd.gui.widget.GuiTaskButton;

public class GuiScreenMain extends GuiScreen {

    private final GuiScreen parentScreen;
    private boolean opened = false;

    public GuiScreenMain(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.buttonList.add(new GuiTaskButton(0, width / 2 - 100, height / 2 - 10, "Hello, World!"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        // drawCenteredString(fontRendererObj, "Hello, World!", width / 2, height / 2, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE || keyCode == KeyBindGuiHandler.openTaskGui.getKeyCode()) {
            if (opened) {
                this.mc.displayGuiScreen((GuiScreen) null);
                opened = false;
            } else {
                opened = true;
            }
        }
    }
}
