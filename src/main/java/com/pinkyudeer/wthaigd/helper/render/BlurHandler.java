package com.pinkyudeer.wthaigd.helper.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.pinkyudeer.wthaigd.helper.config.ConfigHelper;

import lombok.Setter;

public class BlurHandler {

    private static final Minecraft mc = Minecraft.getMinecraft();

    // 着色器程序ID
    private static int blurShaderProgram = 0;

    // 主帧缓冲区
    private static Framebuffer mainBuffer = null;

    // 用于降采样的帧缓冲区数组
    private static Framebuffer[] downscaleBuffers = null;

    // 用于模糊处理的帧缓冲区
    private static Framebuffer blurBufferH = null;
    private static Framebuffer blurBufferV = null;

    // 降采样级别
    private static final int MAX_DOWNSCALE_LEVELS = 3;
    private static int downscaleLevels = 2; // 默认降采样2级

    // 模糊设置
    @Setter
    private static float radius = 12.0f;
    // 默认模糊通道数
    @Setter
    private static int blurPasses = 2;
    // 模糊效果的透明度，用于淡入淡出效果
    @Setter
    private static float alpha = 1.0f;

    public static void init() {
        if (!OpenGlHelper.isFramebufferEnabled()) {
            return;
        }

        // 初始化着色器程序
        if (blurShaderProgram == 0) {
            // 使用ShaderHelper初始化着色器
            blurShaderProgram = ShaderHelper.createProgram(
                new ResourceLocation("wthaigd", "shaders/normal.vert"),
                new ResourceLocation("wthaigd", "shaders/blur.frag"));
        }

        // 创建帧缓冲区
        checkFrameBuffers();

        setDownscaleLevels(ConfigHelper.getInt("blur.downscaleLevels"));
        setRadius(ConfigHelper.getFloat("blur.radius"));
        setBlurPasses(ConfigHelper.getInt("blur.blurPasses"));
    }

    private static void checkFrameBuffers() {
        boolean needsRecreate = mainBuffer == null || mc.displayWidth != mainBuffer.framebufferWidth
            || mc.displayHeight != mainBuffer.framebufferHeight;

        // 检查主帧缓冲区
        if (needsRecreate) {
            // 释放旧的帧缓冲区
            cleanFrameBuffers();

            // 创建主帧缓冲区
            mainBuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, false);
            mainBuffer.setFramebufferFilter(GL11.GL_LINEAR);

            // 设置主帧缓冲区的纹理环绕模式
            configureFramebufferTexture(mainBuffer);

            // 创建降采样帧缓冲区
            downscaleBuffers = new Framebuffer[MAX_DOWNSCALE_LEVELS];
            int width = mc.displayWidth;
            int height = mc.displayHeight;

            for (int i = 0; i < MAX_DOWNSCALE_LEVELS; i++) {
                width /= 2;
                height /= 2;
                downscaleBuffers[i] = new Framebuffer(width, height, false);
                downscaleBuffers[i].setFramebufferFilter(GL11.GL_LINEAR);
                configureFramebufferTexture(downscaleBuffers[i]);
            }

            // 创建模糊处理帧缓冲区
            // 使用最低分辨率作为模糊处理的缓冲区尺寸
            blurBufferH = new Framebuffer(width, height, false);
            blurBufferV = new Framebuffer(width, height, false);
            blurBufferH.setFramebufferFilter(GL11.GL_LINEAR);
            blurBufferV.setFramebufferFilter(GL11.GL_LINEAR);
            configureFramebufferTexture(blurBufferH);
            configureFramebufferTexture(blurBufferV);
        }
    }

    private static void cleanFrameBuffers() {
        if (mainBuffer != null) {
            mainBuffer.deleteFramebuffer();
            mainBuffer = null;
        }

        if (downscaleBuffers != null) {
            for (int i = 0; i < downscaleBuffers.length; i++) {
                if (downscaleBuffers[i] != null) {
                    downscaleBuffers[i].deleteFramebuffer();
                    downscaleBuffers[i] = null;
                }
            }
        }

        if (blurBufferH != null) {
            blurBufferH.deleteFramebuffer();
            blurBufferH = null;
        }

        if (blurBufferV != null) {
            blurBufferV.deleteFramebuffer();
            blurBufferV = null;
        }
    }

    /**
     * 配置帧缓冲区纹理的包装模式
     */
    private static void configureFramebufferTexture(Framebuffer framebuffer) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, framebuffer.framebufferTexture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    /**
     * 设置降采样级别
     */
    public static void setDownscaleLevels(int levels) {
        downscaleLevels = Math.min(MAX_DOWNSCALE_LEVELS, Math.max(1, levels));
    }

    /**
     * 渲染模糊背景
     */
    public static void renderBlurredBackground() {
        renderBlurredBackground(alpha);
    }

    /**
     * 渲染模糊背景，带透明度控制
     *
     * @param alpha 透明度值 (0.0f - 1.0f)
     */
    public static void renderBlurredBackground(float alpha) {
        if (!OpenGlHelper.isFramebufferEnabled() || blurShaderProgram == 0) {
            return;
        }

        // 添加检查：如果游戏窗口最小化，则跳过渲染
        if (!Display.isVisible()) {
            return;
        }

        // 检查Minecraft的帧缓冲是否有效
        if (isFramebufferValid(mc.getFramebuffer())) {
            return;
        }

        // 确保alpha在合法范围内
        alpha = Math.max(0.0f, Math.min(1.0f, alpha));

        // 如果完全透明，则跳过渲染
        if (alpha <= 0.0f) {
            return;
        }

        checkFrameBuffers();

        // 检查我们自己的帧缓冲是否有效
        if (isFramebufferValid(mainBuffer)) {
            return;
        }

        // 保存当前状态
        GL11.glPushMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        try {
            // 复制主屏幕到我们的缓冲区
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);

            // 从MC主帧缓冲复制到我们的主缓冲
            mc.getFramebuffer()
                .bindFramebuffer(false);
            mainBuffer.framebufferClear();
            mainBuffer.bindFramebuffer(false);
            drawFramebuffer(mc.getFramebuffer().framebufferTexture);

            // 执行降采样
            performDownSampling();

            // 应用模糊效果
            applyBlur();

            // 执行上采样
            performUpSampling();

            // 绑定回主帧缓冲
            mc.getFramebuffer()
                .bindFramebuffer(false);

            // 渲染最终的模糊结果到屏幕，使用指定的透明度
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
            drawFramebuffer(mainBuffer.framebufferTexture);

        } finally {
            // 恢复状态
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPopMatrix();
        }
    }

    private static boolean isFramebufferValid(Framebuffer fb) {
        return fb == null || fb.framebufferObject <= 0;
    }

    /**
     * 执行降采样操作
     */
    private static void performDownSampling() {
        Framebuffer input = mainBuffer;

        if (isFramebufferValid(input)) {
            return;
        }

        for (int i = 0; i < downscaleLevels; i++) {
            Framebuffer output = downscaleBuffers[i];

            if (isFramebufferValid(output)) {
                return;
            }

            output.framebufferClear();
            output.bindFramebuffer(false);

            // 简单地绘制上一级缓冲区内容到当前尺寸
            drawFramebuffer(input.framebufferTexture);

            input = output;
        }
    }

    /**
     * 应用模糊效果
     */
    private static void applyBlur() {
        // 获取最小级别的降采样缓冲区作为输入
        Framebuffer input = downscaleBuffers[downscaleLevels - 1];
        Framebuffer output = blurBufferV;

        if (isFramebufferValid(input) || isFramebufferValid(blurBufferH) || isFramebufferValid(output)) {
            return;
        }

        // 应用多个模糊通道
        for (int pass = 0; pass < blurPasses; pass++) {
            // 第一步：水平方向模糊
            blurBufferH.framebufferClear();
            blurBufferH.bindFramebuffer(false);
            ARBShaderObjects.glUseProgramObjectARB(blurShaderProgram);

            // 设置着色器参数 - 水平方向
            ShaderHelper.setUniform1i(blurShaderProgram, "texture", 0);
            ShaderHelper.setUniform2f(
                blurShaderProgram,
                "texelSize",
                1.0f / (float) input.framebufferWidth,
                1.0f / (float) input.framebufferHeight);
            ShaderHelper.setUniform2f(blurShaderProgram, "direction", 1.0f, 0.0f);
            ShaderHelper.setUniform1f(blurShaderProgram, "radius", radius);

            drawFramebuffer(input.framebufferTexture);

            // 第二步：垂直方向模糊
            output.framebufferClear();
            output.bindFramebuffer(false);

            // 设置着色器参数 - 垂直方向
            ShaderHelper.setUniform2f(blurShaderProgram, "direction", 0.0f, 1.0f);

            drawFramebuffer(blurBufferH.framebufferTexture);

            // 为下一个通道准备输入
            // 最后一个通道的结果应该在blurBufferV中
            input = output;
        }

        // 禁用着色器
        ARBShaderObjects.glUseProgramObjectARB(0);
    }

    /**
     * 执行上采样操作
     */
    private static void performUpSampling() {
        Framebuffer input = blurBufferV;

        if (isFramebufferValid(input)) {
            return;
        }

        // 从最小分辨率开始，逐步上采样回主分辨率
        for (int i = downscaleLevels - 1; i >= 0; i--) {
            Framebuffer output = i > 0 ? downscaleBuffers[i - 1] : mainBuffer;

            if (isFramebufferValid(output)) {
                return;
            }

            output.framebufferClear();
            output.bindFramebuffer(false);

            // 简单地绘制上一级缓冲区内容到当前尺寸
            drawFramebuffer(input.framebufferTexture);

            input = output;
        }
    }

    /**
     * 绘制帧缓冲区纹理
     */
    private static void drawFramebuffer(int texture) {
        // 绑定纹理
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);

        // 设置纹理环绕模式为边缘钳制，这将改善边缘处理
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        // 设置正交投影
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, 1.0D, 1.0D, 0.0D, -1.0D, 1.0D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        // 渲染一个全屏四边形
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(0, 1, 0, 0, 0);
        tessellator.addVertexWithUV(1, 1, 0, 1, 0);
        tessellator.addVertexWithUV(1, 0, 0, 1, 1);
        tessellator.addVertexWithUV(0, 0, 0, 0, 1);
        tessellator.draw();
    }
}
