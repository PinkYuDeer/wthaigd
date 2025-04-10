package com.pinkyudeer.wthaigd.gui.screen;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import com.cleanroommc.modularui.factory.ClientGUI;
import com.cleanroommc.modularui.screen.CustomModularScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.pinkyudeer.wthaigd.Wthaigd;
import com.pinkyudeer.wthaigd.gui.screen.panel.MainPanel;
import com.pinkyudeer.wthaigd.helper.config.ConfigHelper;
import com.pinkyudeer.wthaigd.helper.render.BlurHandler;
import com.pinkyudeer.wthaigd.helper.render.GLShaderDrawHelper;

public class MainModularScreen extends CustomModularScreen {

    int width;
    int height;
    int panelWidth;
    int panelHeight;
    int panelX;
    int panelY;

    boolean inited = false;

    // 动画状态
    private long animationStartTime;
    private final int fadeDuration; // 淡入淡出持续时间（毫秒）
    private final boolean animationEnabled; // 是否启用动画
    private boolean isFadingIn; // 是否正在淡入
    private boolean isFadingOut = false; // 是否正在淡出
    private boolean isClosing = false; // 是否正在淡出
    private float currentAlpha; // 当前透明度

    /**
     * 创建并初始化动画状态
     */
    public MainModularScreen() {
        // 从配置中读取动画设置
        this.animationEnabled = ConfigHelper.getBoolean("ui.animation.enabled", true);
        this.fadeDuration = ConfigHelper.getInt("ui.animation.fadeDuration", 300);

        // 初始化动画状态
        this.animationStartTime = System.currentTimeMillis();

        // 如果禁用了动画，则直接设置为完成状态
        if (!animationEnabled) {
            this.isFadingIn = false;
            this.currentAlpha = 1.0f;
        } else {
            this.isFadingIn = true;
            this.currentAlpha = 0.0f;
        }
    }

    @Override
    public @Nonnull ModularPanel buildUI(ModularGuiContext context) {
        if (!inited) {
            Minecraft mc = Minecraft.getMinecraft();
            ScaledResolution scaledResolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            width = scaledResolution.getScaledWidth();
            height = scaledResolution.getScaledHeight();
        }

        // 动态计算面板尺寸和位置
        panelWidth = width / 10 * 9;
        panelHeight = height / 8 * 7;
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;

        // 创建主面板
        return new MainPanel(panelWidth, panelHeight);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // 如果动画已禁用，则直接正常渲染
        if (animationEnabled) {
            // 更新淡入淡出动画
            updateFadeAnimation();

            // 在绘制GUI元素前应用模糊效果，并传入当前的透明度
            BlurHandler.renderBlurredBackground(currentAlpha);

            // 只有当动画完成时才绘制UI元素
            if (isFadingIn || isFadingOut || isClosing) {
                return;
            }
        } else {
            BlurHandler.renderBlurredBackground(1.0f);
        }

        GLShaderDrawHelper.drawTestComplexRect();

        // 调用父类方法绘制基本UI
        // super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * 更新淡入淡出动画的状态
     */
    private void updateFadeAnimation() {
        // 如果动画被禁用，则直接设置为完成状态
        if (!animationEnabled) {
            isFadingIn = false;
            isFadingOut = false;
            currentAlpha = 1.0f;
            return;
        }

        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - animationStartTime;

        if (isFadingIn) {
            // 淡入动画
            if (elapsedTime >= fadeDuration) {
                // 动画完成，设置状态为完成
                currentAlpha = 1.0f;
                isFadingIn = false;
                // 此时应该绘制UI
            } else {
                // 动画进行中
                currentAlpha = (float) elapsedTime / fadeDuration;
            }
        } else if (isFadingOut) {
            // 淡出动画
            if (elapsedTime >= fadeDuration) {
                // 动画完成，关闭界面
                currentAlpha = 0.0f;
                isFadingOut = false;
                super.close(true);
            } else {
                // 动画进行中
                currentAlpha = 1.0f - (float) elapsedTime / fadeDuration;
            }
        }
    }

    /**
     * 重写关闭方法，添加淡出效果
     */
    @Override
    public void close(boolean force) {
        // 如果已经在淡出，则不再处理
        if (isFadingOut) return;

        // 开始淡出动画
        isFadingOut = true;
        isFadingIn = false;
        animationStartTime = System.currentTimeMillis();
        isClosing = true;
    }

    @Override
    public void onResize(int width, int height) {
        super.onResize(width, height);
        if (width == this.width && height == this.height) {
            return;
        }
        Wthaigd.LOG.info("onResize: width={}-{}, height={}-{}", this.width, width, this.height, height);

        // 使用淡出效果关闭当前界面，然后在回调中打开新界面
        if (!isFadingOut) {
            this.close(false);

            // 等待时间根据动画是否启用决定
            int waitTime = animationEnabled ? fadeDuration + 50 : 0;

            // 在淡出完成后打开新界面
            new Thread(() -> {
                try {
                    // 等待淡出动画完成
                    Thread.sleep(waitTime);

                    // 在主线程中打开新界面
                    Minecraft.getMinecraft()
                        .func_152344_a(() -> ClientGUI.open(new MainModularScreen().useTheme("wthaigd:main")));
                } catch (InterruptedException e) {
                    Thread.currentThread()
                        .interrupt();
                }
            }).start();
        }
    }
}
