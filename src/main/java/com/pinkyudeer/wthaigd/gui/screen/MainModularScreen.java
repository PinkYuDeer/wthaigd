package com.pinkyudeer.wthaigd.gui.screen;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.CustomModularScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.pinkyudeer.wthaigd.Wthaigd;
import com.pinkyudeer.wthaigd.helper.shader.OptimizedBlurHandler;
import com.pinkyudeer.wthaigd.helper.shader.RenderHelper;

public class MainModularScreen extends CustomModularScreen {

    int width;
    int height;
    int panelWidth;
    int panelHeight;
    int panelX;
    int panelY;

    @Override
    public @Nonnull ModularPanel buildUI(ModularGuiContext context) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledResolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        width = scaledResolution.getScaledWidth();
        height = scaledResolution.getScaledHeight();

        // 计算面板尺寸和位置
        panelWidth = width / 5 * 4;
        panelHeight = height / 5 * 4;
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;

        ModularPanel panel = ModularPanel.defaultPanel("main", panelWidth, panelHeight)
            .padding(0, 0, 0, 0)
            .margin(0, 0, 0, 0)
            .background(IDrawable.EMPTY); // 使用完全透明的背景

        panel.child(
            IKey.lang("wthaigd.gui.main.title")
                .asWidget()
                .top(7)
                .left(7))
            .child(
                new ButtonWidget<>().center()
                    .size(60, 16)
                    .background(GuiTextures.BUTTON_CLEAN)
                    .overlay(IKey.lang("wthaigd.gui.main.button.test"))
                    .onMousePressed(mouseButton -> {
                        Wthaigd.LOG.info("Button clicked!"); // TODO: Replace with actual action
                        return true;
                    }));
        return panel;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // 在绘制GUI元素前应用模糊效果
        OptimizedBlurHandler.renderBlurredBackground();

        // 先调用父类方法绘制基本UI
        super.drawScreen(mouseX, mouseY, partialTicks);

        // 使用RenderHelper绘制边框
        RenderHelper.drawBorder(panelX, panelY, panelWidth, panelHeight, 0xFF808080);
    }
}
