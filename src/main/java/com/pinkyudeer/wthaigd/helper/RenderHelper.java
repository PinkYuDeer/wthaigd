package com.pinkyudeer.wthaigd.helper;

import java.lang.reflect.Method;

import net.minecraft.client.gui.Gui;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.pinkyudeer.wthaigd.gui.widget.IBorderAble;

public class RenderHelper {

    /**
     * 绘制矩形边框
     *
     * @param x             边框左上角x坐标
     * @param y             边框左上角y坐标
     * @param width         边框宽度
     * @param height        边框高度
     * @param borderColor   边框颜色
     * @param borderSize    边框大小（边框粗细）
     * @param isInnerBorder 是否为内框（true为内框，false为外框）
     */
    public static void drawBorder(int x, int y, int width, int height, int borderColor, int borderSize,
        boolean isInnerBorder) {
        if (isInnerBorder) {
            // 绘制内框
            // 绘制上边框
            Gui.drawRect(x, y, x + width, y + borderSize, borderColor);
            // 绘制下边框
            Gui.drawRect(x, y + height - borderSize, x + width, y + height, borderColor);
            // 绘制左边框
            Gui.drawRect(x, y + borderSize, x + borderSize, y + height - borderSize, borderColor);
            // 绘制右边框
            Gui.drawRect(x + width - borderSize, y + borderSize, x + width, y + height - borderSize, borderColor);
        } else {
            // 绘制外框
            // 绘制上边框
            Gui.drawRect(x - borderSize, y - borderSize, x + width + borderSize, y, borderColor);
            // 绘制下边框
            Gui.drawRect(x - borderSize, y + height, x + width + borderSize, y + height + borderSize, borderColor);
            // 绘制左边框
            Gui.drawRect(x - borderSize, y, x, y + height, borderColor);
            // 绘制右边框
            Gui.drawRect(x + width, y, x + width + borderSize, y + height, borderColor);
        }
    }

    /**
     * 绘制矩形边框（默认边框大小为1像素，默认为外框）
     *
     * @param x           边框左上角x坐标
     * @param y           边框左上角y坐标
     * @param width       边框宽度
     * @param height      边框高度
     * @param borderColor 边框颜色
     */
    public static void drawBorder(int x, int y, int width, int height, int borderColor) {
        drawBorder(x, y, width, height, borderColor, 1, false);
    }

    /**
     * 通过反射应用边框到任何IWidget实例，即使在热更新后接口缺失的情况下也能工作
     *
     * @param <T>           IWidget的具体类型
     * @param widget        要应用边框的控件
     * @param borderColor   边框颜色
     * @param borderSize    边框大小
     * @param isInnerBorder 是否为内边框
     * @return 应用了边框的控件实例（通常是同一个实例）
     */
    @SuppressWarnings("unchecked")
    public static <T extends IWidget> T applyBorderWithReflection(T widget, int borderColor, int borderSize,
        boolean isInnerBorder) {
        if (widget instanceof IBorderAble) {
            return (T) ((IBorderAble<?>) widget).wthaigd$border(borderColor, borderSize, isInnerBorder);
        }
        try {
            // 先尝试直接instanceof检查（正常启动时应该成功）
            if (widget.getClass()
                .getInterfaces().length > 0
                && widget.getClass()
                    .getInterfaces()[0].getName()
                        .contains("IBorderAble")) {
                Method method = widget.getClass()
                    .getMethod("wthaigd$border", int.class, int.class, boolean.class);
                return (T) method.invoke(widget, borderColor, borderSize, isInnerBorder);
            }

            // 如果上面失败，尝试通过反射直接查找方法（热更新情况下可能有效）
            try {
                Method method = widget.getClass()
                    .getMethod("wthaigd$border", int.class, int.class, boolean.class);
                return (T) method.invoke(widget, borderColor, borderSize, isInnerBorder);
            } catch (NoSuchMethodException e) {
                // 方法不存在，吞下异常
            }

            // 检查是否有字段表明这是一个可边框化的对象
            try {
                // 反射设置字段
                java.lang.reflect.Field shouldDrawBorder = widget.getClass()
                    .getDeclaredField("wthaigd$shouldDrawBorder");
                java.lang.reflect.Field borderColorField = widget.getClass()
                    .getDeclaredField("wthaigd$borderColor");
                java.lang.reflect.Field borderSizeField = widget.getClass()
                    .getDeclaredField("wthaigd$borderSize");
                java.lang.reflect.Field isInnerBorderField = widget.getClass()
                    .getDeclaredField("wthaigd$isInnerBorder");

                shouldDrawBorder.setAccessible(true);
                borderColorField.setAccessible(true);
                borderSizeField.setAccessible(true);
                isInnerBorderField.setAccessible(true);

                shouldDrawBorder.set(widget, true);
                borderColorField.set(widget, borderColor);
                borderSizeField.set(widget, borderSize);
                isInnerBorderField.set(widget, isInnerBorder);
            } catch (NoSuchFieldException e) {
                // 字段不存在，吞下异常
            }
        } catch (Exception e) {
            // 如果出现任何异常，只是简单记录而不中断执行
            System.out.println("反射应用边框失败: " + e.getMessage());
        }

        return widget;
    }
}
