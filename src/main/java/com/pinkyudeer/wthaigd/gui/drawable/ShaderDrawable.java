package com.pinkyudeer.wthaigd.gui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.pinkyudeer.wthaigd.render.GLShaderDrawHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ShaderDrawable implements IDrawable {

    private GLShaderDrawHelper.CustomRectConfig config;
    private boolean needsSetup = true;
    private int lastW, lastH;

    private ShaderDrawable(GLShaderDrawHelper.CustomRectConfig config) {
        this.config = config;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        if (config == null) return;
        if (needsSetup || width != lastW || height != lastH) {
            config = config.setup(width, height);
            lastW = width;
            lastH = height;
            needsSetup = false;
        }
        GLShaderDrawHelper.drawComplexRect(config);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ShaderDrawable roundedRect(float radius, int color) {
        return builder().round(radius)
            .rectColor(color)
            .rectEdgeSoftness(1.0f)
            .build();
    }

    public static ShaderDrawable panel(float radius, int color, int borderColor) {
        return builder().round(radius)
            .rectColor(color)
            .rectEdgeSoftness(1.0f)
            .border(1f, 0.6f, 0.0f, borderColor)
            .build();
    }

    public static class Builder {

        private float[] renderOffset = { 0f, 0f };
        private float[] renderSizeMulti = { 1f, 1f };
        private float[] rectSizeMulti = { 1f, 1f };
        private float[] rectCenterOffset = { 0f, 0f };
        private int colorBg = 0x00000000;
        private int colorRect = 0x00000000;
        private float rectEdgeSoftness = 0.5f;
        private float[] cornerRadiuses = { 0f, 0f, 0f, 0f };
        private float continuityIndex = 3.0f;
        private float borderThickness = 0f;
        private float borderSoftness = 0.5f;
        private float borderPos = 0.0f;
        private int colorBorder = 0x00000000;
        private float shadowSoftness = 0f;
        private float[] shadowOffset = { 0f, 0f };
        private int colorShadow = 0x00000000;
        private float shadow2Softness = 0f;
        private float[] shadow2Offset = { 0f, 0f };
        private int colorShadow2 = 0x00000000;
        private float innerShadowSoftness = 0f;
        private float[] innerShadowOffset = { 0f, 0f };
        private int colorInnerShadow = 0x00000000;
        private float innerShadow2Softness = 0f;
        private float[] innerShadow2Offset = { 0f, 0f };
        private int colorInnerShadow2 = 0x00000000;

        public Builder rectColor(int color) {
            this.colorRect = color;
            return this;
        }

        public Builder rectEdgeSoftness(float softness) {
            this.rectEdgeSoftness = softness;
            return this;
        }

        public Builder round(float radius) {
            this.cornerRadiuses = new float[] { radius, radius, radius, radius };
            return this;
        }

        public Builder rounds(float topRight, float bottomRight, float topLeft, float bottomLeft) {
            this.cornerRadiuses = new float[] { topRight, bottomRight, topLeft, bottomLeft };
            return this;
        }

        public Builder border(float thickness, float softness, float pos, int color) {
            this.borderThickness = thickness;
            this.borderSoftness = softness;
            this.borderPos = pos;
            this.colorBorder = color;
            return this;
        }

        public Builder shadow(float blur, float offsetX, float offsetY, int color) {
            this.shadowSoftness = blur;
            this.shadowOffset = new float[] { offsetX, offsetY };
            this.colorShadow = color;
            return this;
        }

        public Builder innerShadow(float blur, float offsetX, float offsetY, int color) {
            this.innerShadowSoftness = blur;
            this.innerShadowOffset = new float[] { offsetX, offsetY };
            this.colorInnerShadow = color;
            return this;
        }

        public ShaderDrawable build() {
            GLShaderDrawHelper.CustomRectConfig config = new GLShaderDrawHelper.CustomRectConfig(
                renderOffset,
                renderSizeMulti,
                continuityIndex,
                colorBg,
                rectSizeMulti,
                rectCenterOffset,
                colorRect,
                rectEdgeSoftness,
                cornerRadiuses,
                borderThickness,
                borderSoftness,
                borderPos,
                colorBorder,
                shadowSoftness,
                shadowOffset,
                colorShadow,
                shadow2Softness,
                shadow2Offset,
                colorShadow2,
                innerShadowSoftness,
                innerShadowOffset,
                colorInnerShadow,
                innerShadow2Softness,
                innerShadow2Offset,
                colorInnerShadow2);
            return new ShaderDrawable(config);
        }
    }
}
