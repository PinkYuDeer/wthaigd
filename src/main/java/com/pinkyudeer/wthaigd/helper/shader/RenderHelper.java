package com.pinkyudeer.wthaigd.helper.shader;

import java.awt.Color;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

import org.lwjgl.opengl.GL11;

public class RenderHelper {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final FontRenderer fontRenderer = mc.fontRenderer;

    // 默认滚动条颜色
    public static final int DEFAULT_SCROLLBAR_COLOR = 0xAA777777;
    public static final int DEFAULT_SCROLLBAR_BACKGROUND_COLOR = 0x44000000;
    public static final int DEFAULT_SCROLLBAR_SIZE = 6;

    // 滚动条最小阈值
    public static final int MIN_HEIGHT_FOR_SCROLLBAR = 40;
    public static final int MIN_WIDTH_FOR_SCROLLBAR = 150;

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

    /**
     * 绘制垂直滚动条
     *
     * @param x                滚动条x坐标
     * @param y                滚动条y坐标
     * @param width            滚动条宽度
     * @param height           滚动条高度
     * @param scrollPos        当前滚动位置
     * @param scrollMax        最大滚动位置
     * @param contentSize      内容总大小
     * @param viewportSize     可视区域大小
     * @param scrollBarColor   滚动块颜色
     * @param scrollBarBgColor 滚动条背景颜色
     */
    public static void drawVerticalScrollBar(int x, int y, int width, int height, int scrollPos, int scrollMax,
        int contentSize, int viewportSize, int scrollBarColor, int scrollBarBgColor) {
        // 绘制滚动条背景
        Gui.drawRect(x, y, x + width, y + height, scrollBarBgColor);

        // 计算滚动块的位置和大小
        float thumbRatio = (float) viewportSize / contentSize;
        int thumbHeight = Math.max(20, (int) (height * thumbRatio));
        int thumbY = y;

        if (scrollMax > 0) {
            thumbY = y + (int) ((height - thumbHeight) * ((float) scrollPos / scrollMax));
        }

        // 绘制滚动块
        Gui.drawRect(x, thumbY, x + width, thumbY + thumbHeight, scrollBarColor);
    }

    /**
     * 绘制水平滚动条
     *
     * @param x                滚动条x坐标
     * @param y                滚动条y坐标
     * @param width            滚动条宽度
     * @param height           滚动条高度
     * @param scrollPos        当前滚动位置
     * @param scrollMax        最大滚动位置
     * @param contentSize      内容总大小
     * @param viewportSize     可视区域大小
     * @param scrollBarColor   滚动块颜色
     * @param scrollBarBgColor 滚动条背景颜色
     */
    public static void drawHorizontalScrollBar(int x, int y, int width, int height, int scrollPos, int scrollMax,
        int contentSize, int viewportSize, int scrollBarColor, int scrollBarBgColor) {
        // 绘制滚动条背景
        Gui.drawRect(x, y, x + width, y + height, scrollBarBgColor);

        // 计算滚动块的位置和大小
        float thumbRatio = (float) viewportSize / contentSize;
        int thumbWidth = Math.max(20, (int) (width * thumbRatio));
        int thumbX = x;

        if (scrollMax > 0) {
            thumbX = x + (int) ((width - thumbWidth) * ((float) scrollPos / scrollMax));
        }

        // 绘制滚动块
        Gui.drawRect(thumbX, y, thumbX + thumbWidth, y + height, scrollBarColor);
    }

    /**
     * 设置OpenGL剪裁区域
     *
     * @param x      剪裁区x坐标
     * @param y      剪裁区y坐标
     * @param width  剪裁区宽度
     * @param height 剪裁区高度
     */
    public static void setScissorArea(int x, int y, int width, int height) {
        Minecraft minecraft = Minecraft.getMinecraft();
        ScaledResolution scaledResolution = new ScaledResolution(
            minecraft,
            minecraft.displayWidth,
            minecraft.displayHeight);
        int scaleFactor = scaledResolution.getScaleFactor();

        // 确保宽度和高度为正数
        if (width < 0) width = 0;
        if (height < 0) height = 0;

        // 保存当前矩阵状态
        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT);

        // 启用剪裁测试
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        // 设置剪裁区域，注意OpenGL坐标系是从左下角开始的，而Minecraft是从左上角
        GL11.glScissor(
            x * scaleFactor,
            minecraft.displayHeight - (y + height) * scaleFactor,
            Math.max(0, width * scaleFactor),
            Math.max(0, height * scaleFactor));
    }

    /**
     * 禁用剪裁
     */
    public static void disableScissor() {
        // 恢复之前的矩阵状态
        GL11.glPopAttrib();
    }

    /**
     * 计算垂直滚动拖动量
     *
     * @param deltaY          鼠标Y轴移动距离
     * @param scrollBarHeight 滚动条高度
     * @param contentSize     内容总大小
     * @param viewportSize    可视区域大小
     * @return 滚动位置变化量
     */
    public static int calculateVerticalScrollAmount(int deltaY, int scrollBarHeight, int contentSize,
        int viewportSize) {
        float thumbRatio = (float) viewportSize / contentSize;
        int thumbHeight = Math.max(20, (int) (scrollBarHeight * thumbRatio));

        // 根据拖动距离计算滚动量
        float scrollFactor = (float) Math.max(0, contentSize - viewportSize) / (scrollBarHeight - thumbHeight);
        return (int) (deltaY * scrollFactor);
    }

    /**
     * 计算水平滚动拖动量
     *
     * @param deltaX         鼠标X轴移动距离
     * @param scrollBarWidth 滚动条宽度
     * @param contentSize    内容总大小
     * @param viewportSize   可视区域大小
     * @return 滚动位置变化量
     */
    public static int calculateHorizontalScrollAmount(int deltaX, int scrollBarWidth, int contentSize,
        int viewportSize) {
        float thumbRatio = (float) viewportSize / contentSize;
        int thumbWidth = Math.max(20, (int) (scrollBarWidth * thumbRatio));

        // 根据拖动距离计算滚动量
        float scrollFactor = (float) Math.max(0, contentSize - viewportSize) / (scrollBarWidth - thumbWidth);
        return (int) (deltaX * scrollFactor);
    }

    /**
     * 检查鼠标是否在垂直滚动条上
     *
     * @param mouseX          鼠标X坐标
     * @param mouseY          鼠标Y坐标
     * @param scrollBarX      滚动条X坐标
     * @param scrollBarY      滚动条Y坐标
     * @param scrollBarWidth  滚动条宽度
     * @param scrollBarHeight 滚动条高度
     * @return 是否在滚动条上
     */
    public static boolean isMouseOverVerticalScrollBar(int mouseX, int mouseY, int scrollBarX, int scrollBarY,
        int scrollBarWidth, int scrollBarHeight) {
        return mouseX >= scrollBarX && mouseX < scrollBarX + scrollBarWidth
            && mouseY >= scrollBarY
            && mouseY < scrollBarY + scrollBarHeight;
    }

    /**
     * 检查鼠标是否在水平滚动条上
     *
     * @param mouseX          鼠标X坐标
     * @param mouseY          鼠标Y坐标
     * @param scrollBarX      滚动条X坐标
     * @param scrollBarY      滚动条Y坐标
     * @param scrollBarWidth  滚动条宽度
     * @param scrollBarHeight 滚动条高度
     * @return 是否在滚动条上
     */
    public static boolean isMouseOverHorizontalScrollBar(int mouseX, int mouseY, int scrollBarX, int scrollBarY,
        int scrollBarWidth, int scrollBarHeight) {
        return mouseX >= scrollBarX && mouseX < scrollBarX + scrollBarWidth
            && mouseY >= scrollBarY
            && mouseY < scrollBarY + scrollBarHeight;
    }

    /**
     * 检查鼠标是否在垂直滚动块上
     *
     * @param mouseX      鼠标X坐标
     * @param mouseY      鼠标Y坐标
     * @param thumbX      滚动块X坐标
     * @param thumbY      滚动块Y坐标
     * @param thumbWidth  滚动块宽度
     * @param thumbHeight 滚动块高度
     * @return 是否在滚动块上
     */
    public static boolean isMouseOverVerticalThumb(int mouseX, int mouseY, int thumbX, int thumbY, int thumbWidth,
        int thumbHeight) {
        return mouseX >= thumbX && mouseX < thumbX + thumbWidth && mouseY >= thumbY && mouseY < thumbY + thumbHeight;
    }

    /**
     * 计算垂直滚动块位置
     *
     * @param scrollBarY      滚动条Y坐标
     * @param scrollBarHeight 滚动条高度
     * @param thumbHeight     滚动块高度
     * @param scrollPos       当前滚动位置
     * @param scrollMax       最大滚动位置
     * @return 滚动块Y坐标
     */
    public static int calculateVerticalThumbY(int scrollBarY, int scrollBarHeight, int thumbHeight, int scrollPos,
        int scrollMax) {
        if (scrollMax <= 0) return scrollBarY;
        return scrollBarY + (int) ((scrollBarHeight - thumbHeight) * ((float) scrollPos / scrollMax));
    }

    /**
     * 计算水平滚动块位置
     *
     * @param scrollBarX     滚动条X坐标
     * @param scrollBarWidth 滚动条宽度
     * @param thumbWidth     滚动块宽度
     * @param scrollPos      当前滚动位置
     * @param scrollMax      最大滚动位置
     * @return 滚动块X坐标
     */
    public static int calculateHorizontalThumbX(int scrollBarX, int scrollBarWidth, int thumbWidth, int scrollPos,
        int scrollMax) {
        if (scrollMax <= 0) return scrollBarX;
        return scrollBarX + (int) ((scrollBarWidth - thumbWidth) * ((float) scrollPos / scrollMax));
    }

    /**
     * 计算从鼠标点击位置得到的滚动位置
     *
     * @param mousePos      鼠标坐标（垂直滚动时为Y，水平滚动时为X）
     * @param scrollBarPos  滚动条位置（垂直滚动时为Y，水平滚动时为X）
     * @param scrollBarSize 滚动条大小（垂直滚动时为高度，水平滚动时为宽度）
     * @param scrollMax     最大滚动位置
     * @return 新的滚动位置
     */
    public static int calculateScrollPosFromClick(int mousePos, int scrollBarPos, int scrollBarSize, int scrollMax) {
        float clickPos = (mousePos - scrollBarPos) / (float) scrollBarSize;
        int newScrollPos = (int) (scrollMax * clickPos);
        return Math.max(0, Math.min(newScrollPos, scrollMax));
    }

    /**
     * 绘制矩形边框
     *
     * @param x           边框左上角x坐标
     * @param y           边框左上角y坐标
     * @param width       边框宽度
     * @param height      边框高度
     * @param borderColor 边框颜色
     * @param borderSize  边框大小（边框粗细）
     */
    public static void drawBorder(int x, int y, int width, int height, int borderColor, int borderSize) {
        // 绘制上边框
        Gui.drawRect(x - borderSize, y - borderSize, x + width + borderSize, y, borderColor);
        // 绘制下边框
        Gui.drawRect(x - borderSize, y + height, x + width + borderSize, y + height + borderSize, borderColor);
        // 绘制左边框
        Gui.drawRect(x - borderSize, y, x, y + height, borderColor);
        // 绘制右边框
        Gui.drawRect(x + width, y, x + width + borderSize, y + height, borderColor);
    }

    /**
     * 绘制矩形边框（默认边框大小为1像素）
     *
     * @param x           边框左上角x坐标
     * @param y           边框左上角y坐标
     * @param width       边框宽度
     * @param height      边框高度
     * @param borderColor 边框颜色
     */
    public static void drawBorder(int x, int y, int width, int height, int borderColor) {
        drawBorder(x, y, width, height, borderColor, 1);
    }

    /**
     * 实现简化版的模糊效果
     * 使用多次半透明黑色叠加实现视觉上的模糊效果
     *
     * @param radius 模糊强度（0-10）
     */
    public static void applyGaussianBlur(int radius) {
        if (radius <= 0) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        ScaledResolution scaledResolution = new ScaledResolution(
            minecraft,
            minecraft.displayWidth,
            minecraft.displayHeight);

        int width = scaledResolution.getScaledWidth();
        int height = scaledResolution.getScaledHeight();

        // 保存OpenGL状态
        GL11.glPushMatrix();

        // 禁用纹理和启用混合
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // 计算基于半径的透明度
        // 半径越大，每层的透明度越低，需要更多层
        float alphaPerLayer = Math.min(0.2f, 0.7f / radius);
        int layers = Math.min(5, radius);

        // 清除深度缓冲区，确保我们绘制在最上层
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        // 绘制多层半透明黑色矩形来模拟模糊
        for (int i = 0; i < layers; i++) {
            // 每层略微偏移位置，创建"模糊"效果
            int offset = (i + 1) * 2;

            // 上偏移
            drawRect(0, 0 - offset, width, height, new Color(0f, 0f, 0f, alphaPerLayer).getRGB());

            // 下偏移
            drawRect(0, 0 + offset, width, height, new Color(0f, 0f, 0f, alphaPerLayer).getRGB());

            // 左偏移
            drawRect(0 - offset, 0, width, height, new Color(0f, 0f, 0f, alphaPerLayer).getRGB());

            // 右偏移
            drawRect(0 + offset, 0, width, height, new Color(0f, 0f, 0f, alphaPerLayer).getRGB());

            // 对角偏移
            drawRect(0 - offset, 0 - offset, width, height, new Color(0f, 0f, 0f, alphaPerLayer / 2).getRGB());
            drawRect(0 + offset, 0 - offset, width, height, new Color(0f, 0f, 0f, alphaPerLayer / 2).getRGB());
            drawRect(0 - offset, 0 + offset, width, height, new Color(0f, 0f, 0f, alphaPerLayer / 2).getRGB());
            drawRect(0 + offset, 0 + offset, width, height, new Color(0f, 0f, 0f, alphaPerLayer / 2).getRGB());
        }

        // 绘制整个屏幕的半透明覆盖层
        drawRect(0, 0, width, height, new Color(0f, 0f, 0f, 0.3f).getRGB());

        // 恢复OpenGL状态
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    /**
     * 绘制一个矩形
     */
    public static void drawRect(int left, int top, int right, int bottom, int color) {
        if (left < right) {
            int i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            int j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;

        GL11.glColor4f(f, f1, f2, f3);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(left, bottom);
        GL11.glVertex2f(right, bottom);
        GL11.glVertex2f(right, top);
        GL11.glVertex2f(left, top);
        GL11.glEnd();
    }
}
