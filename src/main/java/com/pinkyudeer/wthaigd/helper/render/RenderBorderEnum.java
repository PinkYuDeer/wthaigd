package com.pinkyudeer.wthaigd.helper.render;

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
