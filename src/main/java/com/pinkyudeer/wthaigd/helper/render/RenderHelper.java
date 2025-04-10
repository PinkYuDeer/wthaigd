package com.pinkyudeer.wthaigd.helper.render;

import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

/**
 * 渲染工具类
 * 提供各种渲染通用方法
 */
public class RenderHelper {

    /**
     * 从一个整数颜色值中解析出RGBA分量
     *
     * @param color 整数颜色值
     * @return 包含r, g, b, a分量的浮点数组
     */
    public static float[] parseRGBA(int color) {
        float[] rgba = new float[4];
        rgba[0] = ((color >> 24) & 0xFF) / 255.0f; // red
        rgba[1] = ((color >> 16) & 0xFF) / 255.0f; // green
        rgba[2] = ((color >> 8) & 0xFF) / 255.0f; // blue
        rgba[3] = (color & 0xFF) / 255.0f; // alpha
        return rgba;
    }

    /**
     * 设置OpenGL渲染颜色
     *
     * @param color RGBA整数颜色值
     */
    public static void setGLColor(int color) {
        float[] rgba = parseRGBA(color);
        GL11.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    /**
     * 使用Tessellator绘制矩形
     *
     * @param x      左上角X坐标
     * @param y      左上角Y坐标
     * @param width  宽度
     * @param height 高度
     * @param withUV 是否使用UV坐标
     */
    public static void drawRect(int x, int y, int width, int height, boolean withUV) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();

        if (withUV) {
            tessellator.addVertexWithUV(x, y + height, 0, 0, 1);
            tessellator.addVertexWithUV(x + width, y + height, 0, 1, 1);
            tessellator.addVertexWithUV(x + width, y, 0, 1, 0);
            tessellator.addVertexWithUV(x, y, 0, 0, 0);
        } else {
            tessellator.addVertex(x, y + height, 0);
            tessellator.addVertex(x + width, y + height, 0);
            tessellator.addVertex(x + width, y, 0);
            tessellator.addVertex(x, y, 0);
        }

        tessellator.draw();
    }

    /**
     * 使用Tessellator绘制相对于当前变换的矩形（适用于已经进行了平移的情况）
     *
     * @param width  宽度
     * @param height 高度
     * @param withUV 是否使用UV坐标
     */
    public static void drawRelativeRect(int width, int height, boolean withUV) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();

        if (withUV) {
            tessellator.addVertexWithUV(0, height, 0, 0, 1);
            tessellator.addVertexWithUV(width, height, 0, 1, 1);
            tessellator.addVertexWithUV(width, 0, 0, 1, 0);
            tessellator.addVertexWithUV(0, 0, 0, 0, 0);
        } else {
            tessellator.addVertex(0, height, 0);
            tessellator.addVertex(width, height, 0);
            tessellator.addVertex(width, 0, 0);
            tessellator.addVertex(0, 0, 0);
        }

        tessellator.draw();
    }
}
