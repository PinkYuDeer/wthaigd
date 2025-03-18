package com.pinkyudeer.wthaigd.helper;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

public class RenderHelper {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final FontRenderer fontRenderer = mc.fontRenderer;

    public static void drawSplitStringOnHUD(String str, int x, int y, int wrapWidth) {
        renderSplitString(str, x, y, wrapWidth, true);
    }

    public static void renderSplitString(String string, int x, int y, int wrapWidth, boolean addShadow) {
        for (String o : fontRenderer.listFormattedStringToWidth(string, wrapWidth)) {
            fontRenderer.drawString(o, x, y, 0xffffff, addShadow);
            y += fontRenderer.FONT_HEIGHT;
        }
    }

    public static int getSplitStringWidth(String string, int wrapWidth) {
        final List<String> lines = fontRenderer.listFormattedStringToWidth(string, wrapWidth);
        int width = 0;
        for (String line : lines) {
            final int stringWidth = fontRenderer.getStringWidth(line);
            if (stringWidth > width) {
                width = stringWidth;
            }
        }

        return width;
    }

    public static int getSplitStringHeight(String string, int wrapWidth) {
        return fontRenderer.FONT_HEIGHT * fontRenderer.listFormattedStringToWidth(string, wrapWidth)
            .size();
    }

    public static int getRenderWidth(String position, int width, ScaledResolution res) {
        final String positionLower = position.toLowerCase();
        if (positionLower.equals("top_left") || positionLower.equals("center_left")
            || positionLower.equals("bottom_left")) {
            return 10;
        }

        return res.getScaledWidth() - width;
    }

    public static int getRenderHeight(String position, int height, ScaledResolution res) {
        final String positionLower = position.toLowerCase();
        if (positionLower.equals("top_left") || positionLower.equals("top_right")) {
            return 5;
        } else if (positionLower.equals("bottom_left") || positionLower.equals("bottom_right")) {
            return res.getScaledHeight() - height - 5;
        }

        return (res.getScaledHeight() / 2) - (height / 2);
    }

}
