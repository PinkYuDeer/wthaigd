package com.pinkyudeer.wthaigd.helper.render;

import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

import com.pinkyudeer.wthaigd.helper.render.RenderHelper.RenderBorderEnum;
import com.pinkyudeer.wthaigd.helper.render.RenderHelper.RenderCornerEnum;

/**
 * OpenGL绘制辅助类
 * 包含基本的绘制方法，如线条、圆角等
 */
public class GLDrawHelper {

    // 定义边框和圆角常量
    public static final int BORDER_TOP = 1;
    public static final int BORDER_RIGHT = 2;
    public static final int BORDER_BOTTOM = 4;
    public static final int BORDER_LEFT = 8;
    public static final int BORDER_ALL = 15; // 1|2|4|8

    public static final int CORNER_TOP_LEFT = 1;
    public static final int CORNER_TOP_RIGHT = 2;
    public static final int CORNER_BOTTOM_LEFT = 4;
    public static final int CORNER_BOTTOM_RIGHT = 8;
    public static final int CORNER_ALL = 15; // 1|2|4|8

    /**
     * 圆角位置枚举
     */
    public enum CornerPosition {

        TOP_LEFT(0), // 左上角
        TOP_RIGHT(1), // 右上角
        BOTTOM_LEFT(2), // 左下角
        BOTTOM_RIGHT(3);// 右下角

        public final int value;

        CornerPosition(int value) {
            this.value = value;
        }
    }

    /**
     * 设置画线的OpenGL绘制状态
     *
     * @param thickness 线条粗细
     * @param color     颜色
     */
    private static void setupDrawLineGLState(float thickness, int color) {
        // 保存GL状态
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        // 启用混合和线条平滑
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // 设置线条宽度
        GL11.glLineWidth(thickness);

        // 设置颜色
        float alpha = (color >> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }

    /**
     * 恢复OpenGL状态
     */
    private static void restoreDrawLineGLState() {
        // 恢复状态
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glPopAttrib();
    }

    /**
     * 设置正交投影矩阵，用于2D绘制
     *
     * @param x      左边界
     * @param y      上边界
     * @param width  宽度
     * @param height 高度
     */
    private static void setupOrthoMatrix(float x, float y, float width, float height) {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(x, x + width, y + height, y, -1.0D, 1.0D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
    }

    /**
     * 恢复矩阵状态
     */
    private static void restoreMatrix() {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
    }

    /**
     * 绘制单条线
     *
     * @param startX    起点X坐标
     * @param startY    起点Y坐标
     * @param endX      终点X坐标
     * @param endY      终点Y坐标
     * @param color     线条颜色
     * @param thickness 线条粗细
     */
    public static void drawLine(float startX, float startY, float endX, float endY, int color, float thickness) {
        setupDrawLineGLState(thickness, color);

        // 绘制线条
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(startX, startY);
        GL11.glVertex2f(endX, endY);
        GL11.glEnd();

        restoreDrawLineGLState();
    }

    /**
     * 绘制矩形填充背景
     *
     * @param x               矩形左上角x坐标
     * @param y               矩形左上角y坐标
     * @param width           矩形宽度
     * @param height          矩形高度
     * @param backgroundColor 背景颜色（包含透明度）
     */
    public static void drawRect(int x, int y, int width, int height, int backgroundColor) {
        // 保存GL状态
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        // 保存当前矩阵
        GL11.glPushMatrix();

        // 设置绘制状态
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // 设置颜色
        float alpha = ((backgroundColor >> 24) & 0xFF) / 255.0f;
        float red = ((backgroundColor >> 16) & 0xFF) / 255.0f;
        float green = ((backgroundColor >> 8) & 0xFF) / 255.0f;
        float blue = (backgroundColor & 0xFF) / 255.0f;
        GL11.glColor4f(red, green, blue, alpha);

        // 使用Tessellator渲染矩形，这是Minecraft推荐的渲染方式
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertex(x, y + height, 0);
        tessellator.addVertex(x + width, y + height, 0);
        tessellator.addVertex(x + width, y, 0);
        tessellator.addVertex(x, y, 0);
        tessellator.draw();

        // 恢复GL状态
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    /**
     * 使用着色器绘制圆角矩形背景
     *
     * @param x               矩形左上角x坐标
     * @param y               矩形左上角y坐标
     * @param width           矩形宽度
     * @param height          矩形高度
     * @param radius          圆角半径
     * @param backgroundColor 背景颜色（包含透明度）
     * @param cornerFlags     圆角位掩码
     */
    public static void drawRoundedRect(int x, int y, int width, int height, int radius, int backgroundColor,
        int cornerFlags) {
        GLShaderDrawHelper.drawRoundedRectBackground(x, y, width, height, radius, backgroundColor, cornerFlags);
    }

    /**
     * 绘制圆角
     *
     * @param x         圆角位置x坐标
     * @param y         圆角位置y坐标
     * @param radius    圆角半径
     * @param thickness 线条粗细
     * @param color     颜色
     * @param corner    圆角位置
     */
    public static void drawCorner(float x, float y, float radius, float thickness, int color, CornerPosition corner) {
        if (radius <= 0) return; // 如果半径小于等于0，则不绘制

        setupDrawLineGLState(thickness, color);

        // 根据不同角落位置确定圆心和角度范围
        float centerX, centerY;
        float startAngle, endAngle;

        switch (corner) {
            case TOP_LEFT:
                centerX = x + radius;
                centerY = y + radius;
                startAngle = (float) Math.PI; // 180°
                endAngle = (float) (Math.PI * 1.5); // 270°
                break;
            case TOP_RIGHT:
                centerX = x - radius;
                centerY = y + radius;
                startAngle = (float) (Math.PI * 1.5); // 270°
                endAngle = (float) (Math.PI * 2.0); // 360°
                break;
            case BOTTOM_LEFT:
                centerX = x + radius;
                centerY = y - radius;
                startAngle = (float) (Math.PI * 0.5); // 90°
                endAngle = (float) Math.PI; // 180°
                break;
            case BOTTOM_RIGHT:
            default:
                centerX = x - radius;
                centerY = y - radius;
                startAngle = 0.0f; // 0°
                endAngle = (float) (Math.PI * 0.5); // 90°
                break;
        }

        // 绘制圆弧
        GL11.glBegin(GL11.GL_LINE_STRIP);
        // 根据线条粗细动态调整分段数，更细的线需要更多段以保持平滑
        int segments = Math.max(16, Math.min(64, (int) (radius * 0.5f)));
        for (int i = 0; i <= segments; i++) {
            float angle = startAngle + (endAngle - startAngle) * i / segments;
            float px = centerX + (float) Math.cos(angle) * radius;
            float py = centerY + (float) Math.sin(angle) * radius;
            GL11.glVertex2f(px, py);
        }
        GL11.glEnd();

        restoreDrawLineGLState();
    }

    /**
     * 绘制带圆角的矩形边框
     *
     * @param x           矩形左上角x坐标
     * @param y           矩形左上角y坐标
     * @param width       矩形宽度
     * @param height      矩形高度
     * @param radius      圆角半径
     * @param borderColor 边框颜色
     * @param borderSize  边框大小
     * @param borderFlags 边框位掩码
     * @param cornerFlags 圆角位掩码，控制哪些角需要绘制为圆角
     */
    public static void drawRoundedBorder(int x, int y, int width, int height, int radius, int borderColor,
        float borderSize, int borderFlags, int cornerFlags) {
        // 计算实际绘制区域的坐标和尺寸
        float x2 = x + width;
        float y2 = y + height;

        // 绘制直线部分
        if ((borderFlags & RenderBorderEnum.TOP.value) != 0) {
            float startX = ((cornerFlags & RenderCornerEnum.TOP_LEFT.value) != 0) ? (float) x + radius : (float) x;
            float endX = ((cornerFlags & RenderCornerEnum.TOP_RIGHT.value) != 0) ? x2 - radius : x2;
            drawLine(startX, (float) y, endX, (float) y, borderColor, borderSize);
        }

        if ((borderFlags & RenderBorderEnum.BOTTOM.value) != 0) {
            float startX = ((cornerFlags & RenderCornerEnum.BOTTOM_LEFT.value) != 0) ? (float) x + radius : (float) x;
            float endX = ((cornerFlags & RenderCornerEnum.BOTTOM_RIGHT.value) != 0) ? x2 - radius : x2;
            drawLine(startX, y2, endX, y2, borderColor, borderSize);
        }

        if ((borderFlags & RenderBorderEnum.LEFT.value) != 0) {
            float startY = ((cornerFlags & RenderCornerEnum.TOP_LEFT.value) != 0) ? (float) y + radius : (float) y;
            float endY = ((cornerFlags & RenderCornerEnum.BOTTOM_LEFT.value) != 0) ? y2 - radius : y2;
            drawLine((float) x, startY, (float) x, endY, borderColor, borderSize);
        }

        if ((borderFlags & RenderBorderEnum.RIGHT.value) != 0) {
            float startY = ((cornerFlags & RenderCornerEnum.TOP_RIGHT.value) != 0) ? (float) y + radius : (float) y;
            float endY = ((cornerFlags & RenderCornerEnum.BOTTOM_RIGHT.value) != 0) ? y2 - radius : y2;
            drawLine(x2, startY, x2, endY, borderColor, borderSize);
        }

        // 绘制圆角部分
        if ((borderFlags & RenderBorderEnum.TOP.value) != 0 && (borderFlags & RenderBorderEnum.LEFT.value) != 0
            && (cornerFlags & RenderCornerEnum.TOP_LEFT.value) != 0) {
            drawCorner((float) x, (float) y, radius, borderSize, borderColor, CornerPosition.TOP_LEFT);
        }

        if ((borderFlags & RenderBorderEnum.TOP.value) != 0 && (borderFlags & RenderBorderEnum.RIGHT.value) != 0
            && (cornerFlags & RenderCornerEnum.TOP_RIGHT.value) != 0) {
            drawCorner(x2, (float) y, radius, borderSize, borderColor, CornerPosition.TOP_RIGHT);
        }

        if ((borderFlags & RenderBorderEnum.BOTTOM.value) != 0 && (borderFlags & RenderBorderEnum.LEFT.value) != 0
            && (cornerFlags & RenderCornerEnum.BOTTOM_LEFT.value) != 0) {
            drawCorner((float) x, y2, radius, borderSize, borderColor, CornerPosition.BOTTOM_LEFT);
        }

        if ((borderFlags & RenderBorderEnum.BOTTOM.value) != 0 && (borderFlags & RenderBorderEnum.RIGHT.value) != 0
            && (cornerFlags & RenderCornerEnum.BOTTOM_RIGHT.value) != 0) {
            drawCorner(x2, y2, radius, borderSize, borderColor, CornerPosition.BOTTOM_RIGHT);
        }
    }

    /**
     * 绘制带圆角的矩形边框 (所有角都是圆角)
     */
    public static void drawRoundedBorder(int x, int y, int width, int height, int radius, int borderColor,
        float borderSize, int borderFlags) {
        drawRoundedBorder(
            x,
            y,
            width,
            height,
            radius,
            borderColor,
            borderSize,
            borderFlags,
            RenderCornerEnum.ALL.value);
    }

    /**
     * 绘制矩形边框，使用细线绘制
     */
    public static void drawBorder(int x, int y, int width, int height, int borderColor, float borderSize,
        int borderFlags) {
        // 绘制上边框
        if ((borderFlags & RenderBorderEnum.TOP.value) != 0) {
            drawLine(x, y, x + width, y, borderColor, borderSize);
        }
        // 绘制下边框
        if ((borderFlags & RenderBorderEnum.BOTTOM.value) != 0) {
            drawLine(x, y + height, x + width, y + height, borderColor, borderSize);
        }
        // 绘制左边框
        if ((borderFlags & RenderBorderEnum.LEFT.value) != 0) {
            drawLine(x, y, x, y + height, borderColor, borderSize);
        }
        // 绘制右边框
        if ((borderFlags & RenderBorderEnum.RIGHT.value) != 0) {
            drawLine(x + width, y, x + width, y + height, borderColor, borderSize);
        }
    }
}
