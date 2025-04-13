package com.pinkyudeer.wthaigd.helper.render;

import java.lang.reflect.Method;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.pinkyudeer.wthaigd.gui.widget.ICustomAble;

public class MUIHelper {

    public static <T extends IWidget> CustomWidgetBuilder<T> custom(T widget) {
        return new CustomWidgetBuilder<>(widget);
    }

    public static class CustomWidgetBuilder<T extends IWidget> {

        private final T widget;

        private boolean useComplexShader = false;
        private boolean useSimpleShader = false;

        // 位置相关
        private float[] renderOffset = { 0f, 0f };
        private float[] renderSizeMulti = { 1f, 1f };
        private float[] rectSizeMulti = { 1f, 1f };
        private float[] rectCenterOffset = { 0f, 0f };

        // 背景颜色
        private int colorBg = 0x00000000;

        // 矩形参数
        private int colorRect = 0x00000000;
        private float rectEdgeSoftness = 0.5f;
        private float[] cornerRadiuses = { 0f, 0f, 0f, 0f }; // topRight, bottomRight, topLeft, bottomLeft
        private float continuityIndex = 3.0f;

        // 边框参数
        private float borderThickness = 0f;
        private float borderSoftness = 0.5f;
        private float borderPos = 0.0f;
        private float[] borderSelect = { 1f, 1f, 1f, 1f };
        private float borderCutCorners = 1.0f;
        private int colorBorder = 0x00000000;

        // 阴影参数
        private float shadowSoftness = 0f;
        private float[] shadowOffset = { 0f, 0f };
        private int colorShadow = 0x00000000;

        // 阴影2参数
        private float shadow2Softness = 0f;
        private float[] shadow2Offset = { 0f, 0f };
        private int colorShadow2 = 0x00000000;

        // 内阴影参数
        private float innerShadowSoftness = 0f;
        private float[] innerShadowOffset = { 0f, 0f };
        private int colorInnerShadow = 0x00000000;

        // 内阴影2参数
        private float innerShadow2Softness = 0f;
        private float[] innerShadow2Offset = { 0f, -0f };
        private int colorInnerShadow2 = 0x00000000;

        private CustomWidgetBuilder(T widget) {
            this.widget = widget;
        }

        /**
         * 设置渲染偏移量（无需设置其他选项产生的偏移，会自动计算）
         *
         * @param x 偏移量
         * @param y 偏移量
         * @return 构建器实例，用于链式调用
         */
        public CustomWidgetBuilder<T> renderOffset(float x, float y) {
            this.renderOffset = new float[] { x, y };
            return this;
        }

        /**
         * 设置最终渲染缩放乘数（无需设置其他选项产生的形变，会自动计算）
         *
         * @param x 尺寸
         * @param y 尺寸
         * @return 构建器实例，用于链式调用
         */
        public CustomWidgetBuilder<T> renderSizeMulti(float x, float y) {
            this.renderSizeMulti = new float[] { x, y };
            return this;
        }

        /**
         * 设置矩形尺寸乘数（无需设置其他选项产生的形变，会自动计算）
         *
         * @param x 尺寸
         * @param y 尺寸
         * @return 构建器实例，用于链式调用
         */
        public CustomWidgetBuilder<T> rectSizeMulti(float x, float y) {
            this.rectSizeMulti = new float[] { x, y };
            return this;
        }

        /**
         * 设置矩形中心偏移量（无需设置其他选项产生的偏移，会自动计算）
         *
         * @param x 偏移量
         * @param y 偏移量
         * @return 构建器实例，用于链式调用
         */
        public CustomWidgetBuilder<T> rectCenterOffset(float x, float y) {
            this.rectCenterOffset = new float[] { x, y };
            return this;
        }

        /**
         * 设置背景颜色
         *
         * @param color 0xrrggbbaa格式的颜色值
         * @return 构建器实例，用于链式调用
         */
        public CustomWidgetBuilder<T> backgroundColor(int color) {
            this.colorBg = color;
            return this;
        }

        /**
         * 设置矩形颜色
         *
         * @param color 0xrrggbbaa格式的颜色值
         * @return 构建器实例，用于链式调用
         */
        public CustomWidgetBuilder<T> rectColor(int color) {
            this.colorRect = color;
            return this;
        }

        /**
         * 设置连续性指数
         *
         * @param index 连续性指数值
         * @return 构建器实例，用于链式调用
         */
        public CustomWidgetBuilder<T> continuity(float index) {
            this.continuityIndex = index;
            this.useComplexShader = true;
            return this;
        }

        /**
         * 设置矩形边缘柔软度
         *
         * @param softness 边缘柔软度值
         * @return 构建器实例，用于链式调用
         */
        public CustomWidgetBuilder<T> rectEdgeSoftness(float softness) {
            this.rectEdgeSoftness = softness;
            this.useComplexShader = true;
            return this;
        }

        /**
         * 设置四个角的圆角半径
         *
         * @param topRight    右上角半径
         * @param bottomRight 右下角半径
         * @param topLeft     左上角半径
         * @param bottomLeft  左下角半径
         * @return 构建器实例，用于链式调用
         */
        public CustomWidgetBuilder<T> rounds(float topRight, float bottomRight, float topLeft, float bottomLeft) {
            this.cornerRadiuses = new float[] { topRight, bottomRight, topLeft, bottomLeft };
            this.useComplexShader = true;
            return this;
        }

        /**
         * 设置所有角使用相同的圆角半径
         *
         * @param radius 圆角半径
         * @return 构建器实例，用于链式调用
         */
        public CustomWidgetBuilder<T> round(float radius) {
            return rounds(radius, radius, radius, radius);
        }

        /**
         * 设置边框厚度
         *
         * @param thickness 边框厚度
         * @param softness  边框边缘柔软度（mc像素）
         * @param pos       边框位置值，-0.5为外边框，0为中间，0.5为内边框
         * @return 构建器实例，用于链式调用
         */
        public CustomWidgetBuilder<T> border(float thickness, float softness, float pos, int color) {
            this.borderThickness = thickness;
            this.borderSoftness = softness;
            this.borderPos = pos;
            this.colorBorder = color;
            this.useComplexShader = true;
            return this;
        }

        /**
         * 设置边框厚度（简单矩形）
         *
         * @param thickness 边框厚度
         * @param pos       边框位置值，-0.5为外边框，0为中间，0.5为内边框
         * @return 构建器实例，用于链式调用
         */
        public CustomWidgetBuilder<T> border(float thickness, float pos, int color) {
            this.borderThickness = thickness;
            this.borderPos = pos;
            this.colorBorder = color;
            return this;
        }

        /**
         * 设置边框选择
         *
         * @param top    是否显示顶部边框
         * @param bottom 是否显示底部边框
         * @param left   是否显示左侧边框
         * @param right  是否显示右侧边框
         * @return 构建器实例，用于链式调用
         */
        public CustomWidgetBuilder<T> borderSelect(boolean top, boolean bottom, boolean left, boolean right,
            boolean cutCorners) {
            borderSelect[0] = bottom ? 1.0f : 0.0f;
            borderSelect[1] = top ? 1.0f : 0.0f;
            borderSelect[2] = right ? 1.0f : 0.0f;
            borderSelect[3] = left ? 1.0f : 0.0f;
            borderCutCorners = cutCorners ? 1.0f : 0.0f;
            this.useSimpleShader = true;
            return this;
        }

        /**
         * 设置阴影柔软度
         *
         * @param blur    阴影发散距离
         * @param offsetX 阴影偏移量
         * @param offsetY 阴影偏移量
         * @param color   阴影颜色
         * @return 构建器实例，用于链式调用
         */
        public CustomWidgetBuilder<T> shadow(float blur, float offsetX, float offsetY, int color) {
            this.shadowSoftness = blur;
            this.shadowOffset = new float[] { offsetX, offsetY };
            this.colorShadow = color;
            this.useComplexShader = true;
            return this;
        }

        /**
         * 设置第二阴影柔软度
         *
         * @param blur    第二阴影发散距离
         * @param offsetX 第二阴影偏移量
         * @param offsetY 第二阴影偏移量
         * @param color   第二阴影颜色
         * @return 构建器实例，用于链式调用
         */
        public CustomWidgetBuilder<T> shadow2(float blur, float offsetX, float offsetY, int color) {
            this.shadow2Softness = blur;
            this.shadow2Offset = new float[] { offsetX, offsetY };
            this.colorShadow2 = color;
            this.useComplexShader = true;
            return this;
        }

        /**
         * 设置内阴影柔软度
         *
         * @param blur    内阴影发散距离
         * @param offsetX 内阴影偏移量
         * @param offsetY 内阴影偏移量
         * @param color   内阴影颜色
         * @return 构建器实例，用于链式调用
         */
        public CustomWidgetBuilder<T> innerShadow(float blur, float offsetX, float offsetY, int color) {
            this.innerShadowSoftness = blur;
            this.innerShadowOffset = new float[] { offsetX, offsetY };
            this.colorInnerShadow = color;
            this.useComplexShader = true;
            return this;
        }

        /**
         * 设置第二内阴影柔软度
         *
         * @param blur    第二内阴影发散距离
         * @param offsetX 第二内阴影偏移量
         * @param offsetY 第二内阴影偏移量
         * @param color   第二内阴影颜色
         * @return 构建器实例，用于链式调用
         */
        public CustomWidgetBuilder<T> innerShadow2(float blur, float offsetX, float offsetY, int color) {
            this.innerShadow2Softness = blur;
            this.innerShadow2Offset = new float[] { offsetX, offsetY };
            this.colorInnerShadow2 = color;
            this.useComplexShader = true;
            return this;
        }

        /**
         * 完成设置并返回控件实例
         *
         * @return 应用了自定义样式的控件实例
         */
        public T done() {
            GLShaderDrawHelper.CustomRectConfig config;
            if (useComplexShader) {
                if (useSimpleShader) throw new IllegalStateException("复杂矩形不可选择边框。");
                config = new GLShaderDrawHelper.CustomRectConfig(
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
            } else {
                config = new GLShaderDrawHelper.CustomRectConfig(
                    renderOffset,
                    renderSizeMulti,
                    continuityIndex,
                    colorBg,
                    rectSizeMulti,
                    rectCenterOffset,
                    colorRect,
                    borderThickness,
                    borderPos,
                    borderSelect,
                    borderCutCorners,
                    colorBorder);
            }
            return applyCustom(this.widget, config);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends IWidget> T applyCustom(T widget, GLShaderDrawHelper.CustomRectConfig config) {
        if (widget instanceof ICustomAble) {
            return (T) ((ICustomAble<?>) widget).wthaigd$custom(config);
        } else {
            try {
                Method method = widget.getClass()
                    .getMethod("wthaigd$custom", GLShaderDrawHelper.CustomRectConfig.class);
                return (T) method.invoke(widget, config);
            } catch (Exception e) {
                // 方法不存在，吞下异常
            }
        }
        return widget;
    }
}
