package com.pinkyudeer.wthaigd.helper.render;

import org.lwjgl.opengl.GL11;

/**
 * OpenGL绘制辅助类
 * 包含基本的绘制方法，如线条、圆角等
 */
public class GLDrawHelper {

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
        RenderHelper.setGLColor(color);
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
        RenderHelper.setGLColor(backgroundColor);

        // 使用Tessellator渲染矩形
        RenderHelper.drawRect(x, y, width, height, false);

        // 恢复GL状态
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
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
}
