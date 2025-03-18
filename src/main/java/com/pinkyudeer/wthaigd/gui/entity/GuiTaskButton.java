package com.pinkyudeer.wthaigd.gui.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;

public class GuiTaskButton extends GuiButton {

    public GuiTaskButton(int id, int x, int y, int width, int height, String text) {
        super(id, x, y, width, height, text);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            boolean hovered = mouseX >= xPosition && mouseY >= yPosition
                && mouseX < xPosition + width
                && mouseY < yPosition + height;
            final float state = getHoverState(hovered);
            final float f = state / 2 * 0.9F + 0.1F;
            final int color = (int) (255.0F * f);

            Gui.drawRect(xPosition, yPosition, xPosition + width, yPosition + height, color / 2 << 24);
            drawCenteredString(
                mc.fontRenderer,
                displayString,
                xPosition + width / 2,
                yPosition + (height - 8) / 2,
                0xffffff);
        }
    }

    @Override
    public int getHoverState(boolean mouseOver) {
        int state = 2;
        if (!enabled) {
            state = 5;
        } else if (mouseOver) {
            state = 4;
        }

        return state;
    }
}
