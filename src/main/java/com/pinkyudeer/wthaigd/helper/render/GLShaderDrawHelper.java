package com.pinkyudeer.wthaigd.helper.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;

/**
 * 着色器绘制辅助类
 * 管理所有使用自定义着色器的绘画功能
 */
public class GLShaderDrawHelper {

    private static final ResourceLocation ROUNDED_RECT_VERT = new ResourceLocation(
        "wthaigd",
        "shaders/rounded_rect.vert");
    private static final ResourceLocation ROUNDED_RECT_FRAG = new ResourceLocation(
        "wthaigd",
        "shaders/rounded_rect.frag");
    private static final ResourceLocation BLUR_VERT = new ResourceLocation("wthaigd", "shaders/blur.vert");
    private static final ResourceLocation BLUR_FRAG = new ResourceLocation("wthaigd", "shaders/blur.frag");

    private static int roundedRectShader = 0;
    private static int blurShader = 0;

    // 初始化着色器程序
    public static void initShaders() {
        if (roundedRectShader == 0) {
            roundedRectShader = ShaderHelper.createProgram(ROUNDED_RECT_VERT, ROUNDED_RECT_FRAG);
        }

        if (blurShader == 0) {
            blurShader = ShaderHelper.createProgram(BLUR_VERT, BLUR_FRAG);
        }
    }

    /**
     * 绘制圆角矩形背景
     *
     * @param x               矩形左上角x坐标
     * @param y               矩形左上角y坐标
     * @param width           矩形宽度
     * @param height          矩形高度
     * @param radius          圆角半径
     * @param backgroundColor 背景颜色（包含透明度）
     * @param cornerFlags     圆角位掩码
     */
    public static void drawRoundedRectBackground(int x, int y, int width, int height, int radius, int backgroundColor,
        int cornerFlags) {
        // 如果着色器未初始化或不可用，则跳过
        if (roundedRectShader == 0) {
            initShaders();
            if (roundedRectShader == 0) return;
        }

        // 保存GL状态
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        // 保存当前矩阵
        GL11.glPushMatrix();

        // 移动到正确的位置
        GL11.glTranslatef(x, y, 0);

        // 设置绘制状态
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // 启用着色器
        ARBShaderObjects.glUseProgramObjectARB(roundedRectShader);

        // 设置着色器参数
        ShaderHelper.setUniform2f(roundedRectShader, "resolution", width, height);
        ShaderHelper.setUniform1f(roundedRectShader, "radius", radius);
        ShaderHelper.setUniform1i(roundedRectShader, "cornerFlags", cornerFlags);

        // 设置颜色
        float alpha = ((backgroundColor >> 24) & 0xFF) / 255.0f;
        float red = ((backgroundColor >> 16) & 0xFF) / 255.0f;
        float green = ((backgroundColor >> 8) & 0xFF) / 255.0f;
        float blue = (backgroundColor & 0xFF) / 255.0f;
        ShaderHelper.setUniform4f(roundedRectShader, "color", red, green, blue, alpha);

        // 使用Tessellator绘制矩形，这是Minecraft推荐的渲染方式
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(0, height, 0, 0, 1);
        tessellator.addVertexWithUV(width, height, 0, 1, 1);
        tessellator.addVertexWithUV(width, 0, 0, 1, 0);
        tessellator.addVertexWithUV(0, 0, 0, 0, 0);
        tessellator.draw();

        // 禁用着色器
        ARBShaderObjects.glUseProgramObjectARB(0);

        // 恢复GL状态
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    /**
     * 应用高斯模糊效果
     *
     * @param x      区域左上角x坐标
     * @param y      区域左上角y坐标
     * @param width  区域宽度
     * @param height 区域高度
     * @param radius 模糊半径
     */
    public static void applyBlur(int x, int y, int width, int height, float radius) {
        // 如果着色器未初始化或不可用，则跳过
        if (blurShader == 0) {
            initShaders();
            if (blurShader == 0) return;
        }

        // 保存GL状态
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        // 保存当前矩阵
        GL11.glPushMatrix();

        // 设置绘制状态
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // 启用着色器
        ARBShaderObjects.glUseProgramObjectARB(blurShader);

        // 设置着色器参数
        float pixelSizeX = 1.0f / width;
        float pixelSizeY = 1.0f / height;
        ShaderHelper.setUniform2f(blurShader, "texelSize", pixelSizeX, pixelSizeY);
        ShaderHelper.setUniform1f(blurShader, "radius", radius);

        // 先进行水平模糊
        ShaderHelper.setUniform2f(blurShader, "direction", 1.0f, 0.0f);

        // 绘制区域
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, 0, 0, 1);
        tessellator.addVertexWithUV(x + width, y + height, 0, 1, 1);
        tessellator.addVertexWithUV(x + width, y, 0, 1, 0);
        tessellator.addVertexWithUV(x, y, 0, 0, 0);
        tessellator.draw();

        // 然后进行垂直模糊
        ShaderHelper.setUniform2f(blurShader, "direction", 0.0f, 1.0f);

        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, 0, 0, 1);
        tessellator.addVertexWithUV(x + width, y + height, 0, 1, 1);
        tessellator.addVertexWithUV(x + width, y, 0, 1, 0);
        tessellator.addVertexWithUV(x, y, 0, 0, 0);
        tessellator.draw();

        // 禁用着色器
        ARBShaderObjects.glUseProgramObjectARB(0);

        // 恢复GL状态
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }
}
