package com.pinkyudeer.wthaigd.helper.render;

import java.lang.reflect.Method;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.pinkyudeer.wthaigd.gui.widget.IBorderAble;

public class RenderHelper {

    /**
     * 通过反射应用边框到任何IWidget实例，指定边框位掩码和圆角位掩码
     *
     * @param <T>          IWidget的具体类型
     * @param widget       要应用边框的控件
     * @param borderColor  边框颜色
     * @param borderSize   边框大小
     * @param borderRadius 边框圆角半径
     * @param borderFlags  边框位掩码，指定要绘制的边框部分
     * @param cornerFlags  圆角位掩码，指定哪些角需要绘制为圆角
     * @return 应用了边框的控件实例（通常是同一个实例）
     */
    @SuppressWarnings("unchecked")
    public static <T extends IWidget> T applyBorderWithReflection(T widget, int borderColor, int borderSize,
        int borderRadius, int borderFlags, int cornerFlags) {
        // 先尝试直接instanceof检查（正常启动时应该成功）
        if (widget instanceof IBorderAble) {
            return (T) ((IBorderAble<?>) widget)
                .wthaigd$border(borderColor, borderSize, borderRadius, borderFlags, cornerFlags);
        }

        // 如果上面失败，尝试通过反射直接查找方法（热更新情况下可能有效）
        try {
            if (widget.getClass()
                .getInterfaces().length > 0
                && widget.getClass()
                    .getInterfaces()[0].getName()
                        .contains("IBorderAble")) {
                Method method = widget.getClass()
                    .getMethod("wthaigd$border", int.class, int.class, int.class, int.class, int.class);
                return (T) method.invoke(widget, borderColor, borderSize, borderRadius, borderFlags, cornerFlags);
            }
            try {
                Method method = widget.getClass()
                    .getMethod("wthaigd$border", int.class, int.class, int.class, int.class, int.class);
                return (T) method.invoke(widget, borderColor, borderSize, borderRadius, borderFlags, cornerFlags);
            } catch (NoSuchMethodException e) {
                // 方法不存在，吞下异常
            }

            // 检查是否有字段表明这是一个可边框化的对象
            try {
                // 反射设置字段
                java.lang.reflect.Field shouldDrawBorder = widget.getClass()
                    .getDeclaredField("wthaigd$shouldDrawBorder");
                java.lang.reflect.Field shouldRoundedBorder = widget.getClass()
                    .getDeclaredField("wthaigd$shouldRoundedBorder");
                java.lang.reflect.Field borderColorField = widget.getClass()
                    .getDeclaredField("wthaigd$borderColor");
                java.lang.reflect.Field borderSizeField = widget.getClass()
                    .getDeclaredField("wthaigd$borderSize");
                java.lang.reflect.Field borderRadiusField = widget.getClass()
                    .getDeclaredField("wthaigd$borderRadius");
                java.lang.reflect.Field borderFlagsField = widget.getClass()
                    .getDeclaredField("wthaigd$borderFlags");
                java.lang.reflect.Field cornerFlagsField = widget.getClass()
                    .getDeclaredField("wthaigd$cornerFlags");

                shouldDrawBorder.setAccessible(true);
                shouldRoundedBorder.setAccessible(true);
                borderColorField.setAccessible(true);
                borderSizeField.setAccessible(true);
                borderRadiusField.setAccessible(true);
                borderFlagsField.setAccessible(true);
                cornerFlagsField.setAccessible(true);

                shouldDrawBorder.set(widget, true);
                shouldRoundedBorder.set(widget, borderRadius > 0);
                borderColorField.set(widget, borderColor);
                borderSizeField.set(widget, borderSize);
                borderRadiusField.set(widget, borderRadius);
                borderFlagsField.set(widget, borderFlags);
                cornerFlagsField.set(widget, cornerFlags);
            } catch (NoSuchFieldException e) {
                // 字段不存在，吞下异常
            }
        } catch (Exception e) {
            // 如果出现任何异常，只是简单记录而不中断执行
            System.out.println("反射应用边框失败: " + e.getMessage());
        }

        return widget;
    }

    /**
     * 通过反射应用背景填充到任何IWidget实例
     *
     * @param <T>             IWidget的具体类型
     * @param widget          要应用背景的控件
     * @param backgroundColor 背景颜色（包括透明度）
     * @return 应用了背景的控件实例（通常是同一个实例）
     */
    @SuppressWarnings("unchecked")
    public static <T extends IWidget> T applyBackgroundWithReflection(T widget, int backgroundColor) {
        // 先尝试直接instanceof检查（正常启动时应该成功）
        if (widget instanceof IBorderAble) {
            return (T) ((IBorderAble<?>) widget).wthaigd$fillBackground(backgroundColor);
        }

        // 如果上面失败，尝试通过反射直接查找方法（热更新情况下可能有效）
        try {
            if (widget.getClass()
                .getInterfaces().length > 0
                && widget.getClass()
                    .getInterfaces()[0].getName()
                        .contains("IBorderAble")) {
                Method method = widget.getClass()
                    .getMethod("wthaigd$fillBackground", int.class);
                return (T) method.invoke(widget, backgroundColor);
            }
            try {
                Method method = widget.getClass()
                    .getMethod("wthaigd$fillBackground", int.class);
                return (T) method.invoke(widget, backgroundColor);
            } catch (NoSuchMethodException e) {
                // 方法不存在，吞下异常
            }

            // 检查是否有字段表明这是一个可背景化的对象
            try {
                // 反射设置字段
                java.lang.reflect.Field shouldFillBackground = widget.getClass()
                    .getDeclaredField("wthaigd$shouldFillBackground");
                java.lang.reflect.Field backgroundColorField = widget.getClass()
                    .getDeclaredField("wthaigd$backgroundColor");

                shouldFillBackground.setAccessible(true);
                backgroundColorField.setAccessible(true);

                shouldFillBackground.set(widget, true);
                backgroundColorField.set(widget, backgroundColor);
            } catch (NoSuchFieldException e) {
                // 字段不存在，吞下异常
            }
        } catch (Exception e) {
            // 如果出现任何异常，只是简单记录而不中断执行
            System.out.println("反射应用背景失败: " + e.getMessage());
        }

        return widget;
    }

    /**
     * 应用边框到控件
     *
     * @param <T>    IWidget的具体类型
     * @param widget 要应用边框的控件
     * @return 边框构建器实例，可以链式调用设置边框属性
     */
    public static <T extends IWidget> BorderBuilder<T> applyBorder(T widget) {
        return new BorderBuilder<>(widget);
    }

    /**
     * 应用背景到控件
     *
     * @param <T>    IWidget的具体类型
     * @param widget 要应用背景的控件
     * @param color  背景颜色（包含透明度）
     * @return 已应用背景的控件实例
     */
    public static <T extends IWidget> T applyBackground(T widget, int color) {
        return applyBackgroundWithReflection(widget, color);
    }

    /**
     * 边框构建器，支持链式调用设置边框参数
     */
    public static class BorderBuilder<T extends IWidget> {

        private final T widget;
        private int borderColor = 0xFF808080; // 默认边框颜色：灰色
        private int borderSize = 1; // 默认边框大小：1像素
        private int borderRadius = -1; // 默认无圆角
        private int borderFlags = RenderBorderEnum.ALL.value; // 默认绘制所有边
        private int cornerFlags = RenderCornerEnum.NONE.value; // 默认无圆角
        private boolean withBackground = false; // 是否同时应用背景
        private int backgroundColor = 0; // 背景颜色

        private BorderBuilder(T widget) {
            this.widget = widget;
        }

        /**
         * 设置边框颜色
         *
         * @param color ARGB格式的颜色值
         * @return 构建器本身，用于链式调用
         */
        public BorderBuilder<T> color(int color) {
            this.borderColor = color;
            return this;
        }

        /**
         * 设置边框大小
         *
         * @param size 边框大小（像素）
         * @return 构建器本身，用于链式调用
         */
        public BorderBuilder<T> width(int size) {
            this.borderSize = size;
            return this;
        }

        /**
         * 设置边框圆角半径
         *
         * @param radius 圆角半径（像素）
         * @return 构建器本身，用于链式调用
         */
        public BorderBuilder<T> roundRadius(int radius) {
            this.borderRadius = radius;
            return this;
        }

        /**
         * 设置为圆角边框
         *
         * @return 构建器本身，用于链式调用
         */
        public BorderBuilder<T> round() {
            this.cornerFlags = RenderCornerEnum.ALL.value;
            return this;
        }

        /**
         * 同时设置背景颜色
         *
         * @param color 背景颜色（包含透明度）
         * @return 构建器本身，用于链式调用
         */
        public BorderBuilder<T> withBackground(int color) {
            this.withBackground = true;
            this.backgroundColor = color;
            return this;
        }

        /**
         * 设置边框位掩码
         *
         * @param border 要绘制的边框位置
         * @return 构建器本身，用于链式调用
         */
        public BorderBuilder<T> select(RenderBorderEnum border) {
            this.borderFlags = border.value;
            return this;
        }

        /**
         * 设置边框位掩码（组合多个边框位置）
         *
         * @param borders 要绘制的边框位置数组
         * @return 构建器本身，用于链式调用
         */
        public BorderBuilder<T> selects(RenderBorderEnum... borders) {
            this.borderFlags = RenderBorderEnum.combine(borders);
            return this;
        }

        /**
         * 设置圆角位掩码
         *
         * @param corner 要设置圆角的位置
         * @return 构建器本身，用于链式调用
         */
        public BorderBuilder<T> corner(RenderCornerEnum corner) {
            this.cornerFlags = corner.value;
            return this;
        }

        /**
         * 设置圆角位掩码（组合多个圆角位置）
         *
         * @param corners 要设置圆角的位置数组
         * @return 构建器本身，用于链式调用
         */
        public BorderBuilder<T> corners(RenderCornerEnum... corners) {
            this.cornerFlags = RenderCornerEnum.combine(corners);
            return this;
        }

        /**
         * 完成边框配置并应用
         *
         * @return 应用了边框的控件实例
         */
        public T done() {
            T result = applyBorderWithReflection(
                widget,
                borderColor,
                borderSize,
                borderRadius,
                borderFlags,
                cornerFlags);
            if (withBackground) {
                result = applyBackgroundWithReflection(result, backgroundColor);
            }
            return result;
        }
    }

    /**
     * 圆角枚举，用于控制绘制四个角的圆角效果
     */
    public enum RenderCornerEnum {

        /**
         * 左上角
         */
        TOP_LEFT(1),
        /**
         * 右上角
         */
        TOP_RIGHT(1 << 1),
        /**
         * 左下角
         */
        BOTTOM_LEFT(1 << 2),
        /**
         * 右下角
         */
        BOTTOM_RIGHT(1 << 3),

        // 两角组合
        /**
         * 上方两角
         */
        TOP(TOP_LEFT.value | TOP_RIGHT.value),
        /**
         * 下方两角
         */
        BOTTOM(BOTTOM_LEFT.value | BOTTOM_RIGHT.value),
        /**
         * 左侧两角
         */
        LEFT(TOP_LEFT.value | BOTTOM_LEFT.value),
        /**
         * 右侧两角
         */
        RIGHT(TOP_RIGHT.value | BOTTOM_RIGHT.value),
        /**
         * 对角线（左上 + 右下）
         */
        DIAGONAL_1(TOP_LEFT.value | BOTTOM_RIGHT.value),
        /**
         * 对角线（右上 + 左下）
         */
        DIAGONAL_2(TOP_RIGHT.value | BOTTOM_LEFT.value),

        // 三角组合
        /**
         * 除了左上角
         */
        EXCEPT_TOP_LEFT(TOP_RIGHT.value | BOTTOM_LEFT.value | BOTTOM_RIGHT.value),
        /**
         * 除了右上角
         */
        EXCEPT_TOP_RIGHT(TOP_LEFT.value | BOTTOM_LEFT.value | BOTTOM_RIGHT.value),
        /**
         * 除了左下角
         */
        EXCEPT_BOTTOM_LEFT(TOP_LEFT.value | TOP_RIGHT.value | BOTTOM_RIGHT.value),
        /**
         * 除了右下角
         */
        EXCEPT_BOTTOM_RIGHT(TOP_LEFT.value | TOP_RIGHT.value | BOTTOM_LEFT.value),

        /**
         * 所有角
         */
        ALL(TOP_LEFT.value | TOP_RIGHT.value | BOTTOM_LEFT.value | BOTTOM_RIGHT.value),
        /**
         * 无圆角
         */
        NONE(0);

        public final int value;

        RenderCornerEnum(int value) {
            this.value = value;
        }

        /**
         * 检查是否包含指定角
         */
        public boolean contains(RenderCornerEnum corner) {
            return (this.value & corner.value) != 0;
        }

        /**
         * 组合多个圆角设置
         */
        public static int combine(RenderCornerEnum... corners) {
            int result = 0;
            for (RenderCornerEnum corner : corners) {
                result |= corner.value;
            }
            return result;
        }
    }

    public enum RenderBorderEnum {

        // 基本边框位掩码
        TOP(0x1),
        RIGHT(0x2),
        BOTTOM(0x4),
        LEFT(0x8),

        // 组合边框常量
        ALL(TOP.value | RIGHT.value | BOTTOM.value | LEFT.value),
        NONE(0),

        // "除了X"系列组合
        EXCEPT_TOP(RIGHT.value | BOTTOM.value | LEFT.value),
        EXCEPT_RIGHT(TOP.value | BOTTOM.value | LEFT.value),
        EXCEPT_BOTTOM(TOP.value | RIGHT.value | LEFT.value),
        EXCEPT_LEFT(TOP.value | RIGHT.value | BOTTOM.value),

        // 水平和垂直边框
        HORIZONTAL(TOP.value | BOTTOM.value),
        VERTICAL(LEFT.value | RIGHT.value),

        // 其他常用组合
        TOP_LEFT(TOP.value | LEFT.value),
        TOP_RIGHT(TOP.value | RIGHT.value),
        BOTTOM_LEFT(BOTTOM.value | LEFT.value),
        BOTTOM_RIGHT(BOTTOM.value | RIGHT.value);

        public final int value;

        RenderBorderEnum(int value) {
            this.value = value;
        }

        // 用于检查当前边框设置是否包含指定的边框部分
        public boolean contains(RenderBorderEnum border) {
            return (this.value & border.value) != 0;
        }

        // 用于合并多个边框设置
        public static int combine(RenderBorderEnum... borders) {
            int result = 0;
            for (RenderBorderEnum border : borders) {
                result |= border.value;
            }
            return result;
        }
    }
}
