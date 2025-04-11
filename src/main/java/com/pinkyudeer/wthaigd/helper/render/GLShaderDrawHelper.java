package com.pinkyudeer.wthaigd.helper.render;

import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;

/**
 * 着色器绘制辅助类
 * 管理所有使用自定义着色器的绘画功能
 */
public class GLShaderDrawHelper {

    private static final ResourceLocation COMPLEX_RECT_VERT = new ResourceLocation(
        "wthaigd",
        "shaders/complex_rect.vert");
    private static final ResourceLocation COMPLEX_RECT_FRAG = new ResourceLocation(
        "wthaigd",
        "shaders/complex_rect.frag");

    private static int complexRectShader = 0;

    // 初始化着色器程序
    public static void initShaders() {

        if (complexRectShader == 0) {
            complexRectShader = ShaderHelper.createProgram(COMPLEX_RECT_VERT, COMPLEX_RECT_FRAG);
        }
    }

    public static class CustomRectConfig {

        // 渲染 参数
        public float[] renderOffset = { 0, 0 };
        public float[] renderSize = { 1, 1 };
        public float continuityIndex = 3.0f;
        public int colorBg = 0x00000000;

        // 矩形 参数
        public float[] rectSize = { 1f, 1f };
        public float[] rectCenter = { 0.5f, 0.5f };
        public int colorRect = 0x00000000;
        public float rectEdgeSoftness = 0.5f;
        public float[] cornerRadiuses = { 0.0f, 0.0f, 0.0f, 0.0f }; // 右上, 右下, 左上, 左下

        // 边框 参数
        public float borderThickness = 0.02f;
        public float borderSoftness = 0.5f;
        public float borderPos = 0.0f;
        private float[] borderSelect = { 1.0f, 1.0f, 1.0f, 1.0f };
        public int colorBorder = 0x00000000;

        // 阴影 参数
        public float shadowSoftness = 0.05f;
        public float[] shadowOffset = { 0.02f, 0.02f };
        public int colorShadow = 0x00000000;

        // 阴影2 参数
        public float shadow2Softness = 0.0f;
        public float[] shadow2Offset = { 0.0f, 0.0f };
        public int colorShadow2 = 0x00000000;

        // 内阴影 参数
        public float innerShadowSoftness = 0.0f;
        public float[] innerShadowOffset = { 0.0f, 0.0f };
        public int colorInnerShadow = 0x00000000;

        // 内阴影2 参数
        public float innerShadow2Softness = 0.0f;
        public float[] innerShadow2Offset = { 0.0f, 0.0f };
        public int colorInnerShadow2 = 0x00000000;

        /**
         * 绘制复杂矩形，支持圆角、边框、阴影和内阴影效果
         *
         * @param renderOffset         渲染偏移量 [x, y]
         * @param renderSize           渲染尺寸 [宽, 高]
         * @param continuityIndex      连续性指数，控制圆角平滑度
         * @param colorBg              背景颜色 (0xrrggbbaa格式)
         *
         * @param rectSize             矩形尺寸 [宽, 高]，相对于渲染尺寸的比例
         * @param rectCenter           矩形中心位置 [x, y]，相对于渲染尺寸的比例
         * @param colorRect            矩形颜色 (0xrrggbbaa格式)
         * @param rectEdgeSoftness     矩形边缘柔和度
         * @param cornerRadiuses       四个角的半径 [右上, 右下, 左上, 左下]
         *
         * @param borderThickness      边框厚度
         * @param borderSoftness       边框边缘柔和度
         * @param borderPos            边框位置，相对于矩形边缘：-0.5为外边框，0为中间，0.5为内边框
         * @param borderSelect         边框选择，[上。下。左，右]
         * @param colorBorder          边框颜色 (0xrrggbbaa格式)
         *
         * @param shadowSoftness       阴影发散距离
         * @param shadowOffset         阴影偏移量 [x, y]
         * @param colorShadow          阴影颜色 (0xrrggbbaa格式)
         *
         * @param shadow2Softness      第二阴影发散距离
         * @param shadow2Offset        第二阴影偏移量 [x, y]
         * @param colorShadow2         第二阴影颜色 (0xrrggbbaa格式)
         *
         * @param innerShadowSoftness  内阴影发散距离
         * @param innerShadowOffset    内阴影偏移量 [x, y]
         * @param colorInnerShadow     内阴影颜色 (0xrrggbbaa格式)
         *
         * @param innerShadow2Softness 第二内阴影发散距离
         * @param innerShadow2Offset   第二内阴影偏移量 [x, y]
         * @param colorInnerShadow2    第二内阴影颜色 (0xrrggbbaa格式)
         */
        public CustomRectConfig(float[] renderOffset, float[] renderSize, float continuityIndex, int colorBg,
            float[] rectSize, float[] rectCenter, int colorRect, float rectEdgeSoftness, float[] cornerRadiuses,
            float borderThickness, float borderSoftness, float borderPos, float[] borderSelect, int colorBorder,
            float shadowSoftness, float[] shadowOffset, int colorShadow, float shadow2Softness, float[] shadow2Offset,
            int colorShadow2, float innerShadowSoftness, float[] innerShadowOffset, int colorInnerShadow,
            float innerShadow2Softness, float[] innerShadow2Offset, int colorInnerShadow2) {
            this.renderOffset = renderOffset;
            this.renderSize = renderSize;
            this.continuityIndex = continuityIndex;
            this.colorBg = colorBg;

            this.rectSize = rectSize;
            this.rectCenter = rectCenter;
            this.colorRect = colorRect;
            this.rectEdgeSoftness = rectEdgeSoftness;
            this.cornerRadiuses = cornerRadiuses;

            this.borderThickness = borderThickness;
            this.borderSoftness = borderSoftness;
            this.borderPos = borderPos;
            this.colorBorder = colorBorder;
            this.borderSelect = borderSelect;

            this.shadowSoftness = shadowSoftness;
            this.shadowOffset = shadowOffset;
            this.colorShadow = colorShadow;

            this.shadow2Softness = shadow2Softness;
            this.shadow2Offset = shadow2Offset;
            this.colorShadow2 = colorShadow2;

            this.innerShadowSoftness = innerShadowSoftness;
            this.innerShadowOffset = innerShadowOffset;
            this.colorInnerShadow = colorInnerShadow;

            this.innerShadow2Softness = innerShadow2Softness;
            this.innerShadow2Offset = innerShadow2Offset;
            this.colorInnerShadow2 = colorInnerShadow2;
        }

        public CustomRectConfig() {}

        /**
         * 根据传入的矩形宽高自动调整所有参数。包括：
         * 1、将非归一化参数转换为归一化参数
         * 2、计算渲染区域大小和偏移
         * 3、计算矩形相对大小和中心
         *
         * @param width  矩形宽度
         * @param height 矩形高度
         * @return 调整后的配置
         */
        public CustomRectConfig setup(float width, float height) {
            // setup前，renderOffset、rectCenter为计算的offset后偏移量、renderSize为计算后缩放乘数、rectSize为计算前矩形乘数。
            width *= this.rectSize[0];
            height *= this.rectSize[1];
            float[] renderSizeP = new float[] { width, height };

            // 1、将非归一化参数转换为归一化参数
            float minSize = Math.min(width, height);
            this.borderThickness = adjustPixelRatio(this.borderThickness, 0.5f, minSize);
            this.shadowSoftness = adjustPixelRatio(this.shadowSoftness, 1, minSize);
            this.shadow2Softness = adjustPixelRatio(this.shadow2Softness, 1, minSize);
            this.innerShadowSoftness = adjustPixelRatio(this.innerShadowSoftness, 1, minSize);
            this.innerShadow2Softness = adjustPixelRatio(this.innerShadow2Softness, 1, minSize);
            for (int i = 0; i < 2; i++) {
                this.renderOffset[i] = adjustPixelRatio(this.renderOffset[i], 1, renderSizeP[i]);
                this.rectCenter[i] = adjustPixelRatio(this.rectCenter[i], 1, renderSizeP[i]);
                this.renderSize[i] = adjustPixelRatio(this.renderSize[i], 1, renderSizeP[i]);
                this.shadowOffset[i] = adjustPixelRatio(this.shadowOffset[i], 1, renderSizeP[i]);
                this.shadow2Offset[i] = adjustPixelRatio(this.shadow2Offset[i], 1, renderSizeP[i]);
                this.innerShadowOffset[i] = adjustPixelRatio(this.innerShadowOffset[i], 1, renderSizeP[i]);
                this.innerShadow2Offset[i] = adjustPixelRatio(this.innerShadow2Offset[i], 1, renderSizeP[i]);
            }
            for (int i = 0; i < 4; i++) {
                this.cornerRadiuses[i] = adjustPixelRatio(this.cornerRadiuses[i], 0.5f, minSize);
            }

            // 2 考虑边框和柔软度
            float extraAll;
            if (this.borderPos == 0.5) { // 内边框
                extraAll = Math.max(this.borderSoftness, this.rectEdgeSoftness);
            } else if (this.borderPos == 0) { // 中边框
                extraAll = Math.max(this.borderSoftness + this.borderThickness * minSize, this.rectEdgeSoftness);
            } else { // 外边框
                extraAll = Math.max(this.borderSoftness + this.borderThickness * minSize * 2, this.rectEdgeSoftness);
            }

            // 3 考虑阴影
            float extraTop = Math.min(
                this.shadowOffset[1] * height - this.shadowSoftness,
                this.shadow2Offset[1] * height - this.shadow2Softness);
            float extraBottom = Math.max(
                this.shadowOffset[1] * height + this.shadowSoftness,
                this.shadow2Offset[1] * height + this.shadow2Softness);
            float extraLeft = Math.min(
                this.shadowOffset[0] * width - this.shadowSoftness,
                this.shadow2Offset[0] * width - this.shadow2Softness);
            float extraRight = Math.max(
                this.shadowOffset[0] * width + this.shadowSoftness,
                this.shadow2Offset[0] * width + this.shadow2Softness);

            extraTop += Math.abs(Math.min(0, extraTop)) + extraAll;
            extraBottom += Math.abs(Math.max(0, extraBottom)) + extraAll;
            extraLeft += Math.abs(Math.min(0, extraLeft)) + extraAll;
            extraRight += Math.abs(Math.max(0, extraRight)) + extraAll;

            this.renderSize = new float[] {
                (renderSizeP[0] + extraLeft + extraRight + extraAll * 2) * this.renderSize[0],
                (renderSizeP[1] + extraTop + extraBottom + extraAll * 2) * this.renderSize[1] };
            this.renderOffset = new float[] { this.renderOffset[0] - extraLeft * 2,
                this.renderOffset[1] - extraTop * 2 };
            this.rectSize = new float[] { width / this.renderSize[0], height / this.renderSize[1] };
            this.rectCenter = new float[] { 0.5f + this.rectCenter[0] - (extraLeft - extraRight) / width,
                0.5f + this.rectCenter[1] - (extraTop - extraBottom) / height };

            return this;
        }

        /**
         * 辅助方法：调整像素比例
         * 如果值大于阈值，则除以参照值来缩放
         *
         * @param value     需要调整的值
         * @param threshold 阈值
         * @param reference 参照值（缩放因子）
         * @return 调整后的值
         */
        private static float adjustPixelRatio(float value, float threshold, float reference) {
            if (value > threshold) {
                return value / reference;
            }
            return value;
        }
    }

    /**
     * 绘制复杂矩形，支持圆角、边框、阴影和内阴影效果
     *
     * @param config 复杂矩形的配置参数
     */
    public static void drawComplexRect(CustomRectConfig config) {
        // 如果着色器未初始化或不可用，则跳过
        if (complexRectShader == 0) {
            initShaders();
            if (complexRectShader == 0) return;
        }

        // 保存GL状态
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        // 保存当前矩阵
        GL11.glPushMatrix();

        // 移动到正确的位置
        GL11.glTranslatef(config.renderOffset[0], config.renderOffset[1], 0);

        // 设置绘制状态
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // 启用着色器
        ARBShaderObjects.glUseProgramObjectARB(complexRectShader);

        // 设置着色器参数
        ShaderHelper.setUniform2f(complexRectShader, "iResolution", config.renderSize[0], config.renderSize[1]);
        // 设置基本参数
        ShaderHelper.setUniform1f(complexRectShader, "u_continuityIndex", config.continuityIndex);
        ShaderHelper.setUniformRgba(complexRectShader, "u_colorBg", config.colorBg);

        // 矩形参数
        ShaderHelper.setUniform2f(complexRectShader, "u_rectSize", config.rectSize[0], config.rectSize[1]);
        ShaderHelper.setUniform2f(complexRectShader, "u_rectCenter", config.rectCenter[0], config.rectCenter[1]);
        ShaderHelper.setUniformRgba(complexRectShader, "u_colorRect", config.colorRect);
        ShaderHelper.setUniform1f(complexRectShader, "u_rectEdgeSoftness", config.rectEdgeSoftness);
        ShaderHelper.setUniform4f(
            complexRectShader,
            "u_cornerRadiuses",
            config.cornerRadiuses[0],
            config.cornerRadiuses[1],
            config.cornerRadiuses[2],
            config.cornerRadiuses[3]);

        // 边框参数
        ShaderHelper.setUniform1f(complexRectShader, "u_borderThickness", config.borderThickness);
        ShaderHelper.setUniform1f(complexRectShader, "u_borderSoftness", config.borderSoftness);
        ShaderHelper.setUniform1f(complexRectShader, "u_borderPos", config.borderPos);
        ShaderHelper.setUniform4f(
            complexRectShader,
            "u_borderSelect",
            config.borderSelect[0],
            config.borderSelect[1],
            config.borderSelect[2],
            config.borderSelect[3]);
        ShaderHelper.setUniformRgba(complexRectShader, "u_colorBorder", config.colorBorder);

        // 阴影参数
        ShaderHelper.setUniform1f(complexRectShader, "u_ShadowSoftness", config.shadowSoftness);
        ShaderHelper.setUniform2f(complexRectShader, "u_ShadowOffset", config.shadowOffset[0], config.shadowOffset[1]);
        ShaderHelper.setUniformRgba(complexRectShader, "u_colorShadow", config.colorShadow);

        // 阴影2参数
        ShaderHelper.setUniform1f(complexRectShader, "u_Shadow2Softness", config.shadow2Softness);
        ShaderHelper
            .setUniform2f(complexRectShader, "u_Shadow2Offset", config.shadow2Offset[0], config.shadow2Offset[1]);
        ShaderHelper.setUniformRgba(complexRectShader, "u_colorShadow2", config.colorShadow2);

        // 内阴影参数
        ShaderHelper.setUniform1f(complexRectShader, "u_InnerShadowSoftness", config.innerShadowSoftness);
        ShaderHelper.setUniform2f(
            complexRectShader,
            "u_InnerShadowOffset",
            config.innerShadowOffset[0],
            config.innerShadowOffset[1]);
        ShaderHelper.setUniformRgba(complexRectShader, "u_colorInnerShadow", config.colorInnerShadow);

        // 内阴影2参数
        ShaderHelper.setUniform1f(complexRectShader, "u_InnerShadow2Softness", config.innerShadow2Softness);
        ShaderHelper.setUniform2f(
            complexRectShader,
            "u_InnerShadow2Offset",
            config.innerShadow2Offset[0],
            config.innerShadow2Offset[1]);
        ShaderHelper.setUniformRgba(complexRectShader, "u_colorInnerShadow2", config.colorInnerShadow2);

        // 使用Tessellator绘制矩形
        RenderHelper.drawRelativeRect((int) config.renderSize[0], (int) config.renderSize[1], true);

        // 禁用着色器
        ARBShaderObjects.glUseProgramObjectARB(0);

        // 恢复GL状态
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    public static void drawTestComplexRect() {
        CustomRectConfig config = new CustomRectConfig();

        // 渲染参数
        config.renderOffset = new float[] { 5, 5 };
        config.renderSize = new float[] { 300f, 200f };
        config.continuityIndex = 3.0f;
        config.colorBg = 0xEDEDEDFF;

        // 矩形参数
        config.rectSize = new float[] { 0.75f, 0.75f };
        config.rectCenter = new float[] { 0.5f, 0.5f };
        config.colorRect = 0xBF00BF80;
        config.rectEdgeSoftness = 0.5f;
        config.cornerRadiuses = new float[] { 0.25f, 0.15f, 0.1f, 0.2f }; // 右上, 右下, 左上, 左下

        // 边框参数
        config.borderThickness = 0.025f;
        config.borderSoftness = 0.5f;
        config.borderPos = 0.0f;
        config.borderSelect = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
        config.colorBorder = 0x000000FF;

        // 阴影参数
        config.shadowSoftness = 0.07f;
        config.shadowOffset = new float[] { -0.05f, 0.05f };
        config.colorShadow = 0x00E5E5FF;

        // 阴影2参数
        config.shadow2Softness = 0.07f;
        config.shadow2Offset = new float[] { 0.05f, -0.05f };
        config.colorShadow2 = 0xE5E500FF;

        // 内阴影参数
        config.innerShadowSoftness = 0.07f;
        config.innerShadowOffset = new float[] { -0.05f, 0.05f };
        config.colorInnerShadow = 0x00FF00FF;

        // 内阴影2参数
        config.innerShadow2Softness = 0.07f;
        config.innerShadow2Offset = new float[] { 0.05f, -0.05f };
        config.colorInnerShadow2 = 0xFF0000FF;

        drawComplexRect(config);
    }
}
