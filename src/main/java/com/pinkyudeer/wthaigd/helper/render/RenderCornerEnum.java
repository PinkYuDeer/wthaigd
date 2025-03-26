package com.pinkyudeer.wthaigd.helper.render;

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
