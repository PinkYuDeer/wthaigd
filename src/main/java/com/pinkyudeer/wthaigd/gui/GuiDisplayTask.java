package com.pinkyudeer.wthaigd.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import com.pinkyudeer.wthaigd.Wthaigd;
import com.pinkyudeer.wthaigd.gui.entity.GuiTaskButton;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiDisplayTask extends GuiScreen {

    private final GuiScreen parentScreen;
    private GuiButton testButton;
    private boolean opened = false;

    public GuiDisplayTask(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        setUpButtons();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == testButton) {
            // Do something
            Wthaigd.LOG.info("Test Button pressed!");
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawString(fontRendererObj, "Hello, World!", width / 2, height / 2, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == KeyBindGuiHandler.openTaskGui.getKeyCode()) {
            if (opened) {
                this.mc.displayGuiScreen(null);
                opened = false;
            } else {
                opened = true;
            }
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    private void setUpButtons() {
        testButton = addButton(new GuiTaskButton(0, width / 2 - 100, height / 2 + 20, 100, 20, "Test Button"));
    }

    private <T extends GuiButton> T addButton(T button) {
        buttonList.add(button);
        return button;
    }
}
