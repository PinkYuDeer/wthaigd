#version 120

#extension GL_EXT_gpu_shader4 : enable

// 接收的统一变量
uniform vec2 resolution;      // 矩形尺寸（宽度和高度）
uniform float radius;         // 圆角半径
uniform int cornerFlags;      // 哪些角需要圆角（位掩码）
uniform vec4 color;           // 填充颜色（包括透明度）

// 常量定义，与Java代码中一致
const int CORNER_TOP_LEFT = 1;
const int CORNER_TOP_RIGHT = 2;
const int CORNER_BOTTOM_LEFT = 4;
const int CORNER_BOTTOM_RIGHT = 8;

// 判断一个点是否在圆角区域内
bool isInRoundedCorner(vec2 pos, vec2 cornerCenter, float radius) {
    float dist = distance(pos, cornerCenter);
    return dist <= radius;
}

// 判断一个点是否应该被渲染
bool shouldRender(vec2 pos) {
    // 获取矩形的宽高
    float width = resolution.x;
    float height = resolution.y;

    // 如果半径无效，则当作普通矩形处理
    if (radius <= 0.0) {
        return true;
    }

    // 有效半径不应超过矩形尺寸的一半
    float effectiveRadius = min(radius, min(width, height) * 0.5);

    // 中央区域 - 非圆角区域，总是渲染
    if (pos.x >= effectiveRadius && pos.x <= width - effectiveRadius &&
        pos.y >= effectiveRadius && pos.y <= height - effectiveRadius) {
        return true;
    }

    // 四个角的处理 - 只有在圆角区域外才会丢弃片段

    // 左上角
    if ((cornerFlags & CORNER_TOP_LEFT) != 0) {
        if (pos.x < effectiveRadius && pos.y < effectiveRadius) {
            vec2 cornerCenter = vec2(effectiveRadius, effectiveRadius);
            return isInRoundedCorner(pos, cornerCenter, effectiveRadius);
        }
    }

    // 右上角
    if ((cornerFlags & CORNER_TOP_RIGHT) != 0) {
        if (pos.x > width - effectiveRadius && pos.y < effectiveRadius) {
            vec2 cornerCenter = vec2(width - effectiveRadius, effectiveRadius);
            return isInRoundedCorner(pos, cornerCenter, effectiveRadius);
        }
    }

    // 左下角
    if ((cornerFlags & CORNER_BOTTOM_LEFT) != 0) {
        if (pos.x < effectiveRadius && pos.y > height - effectiveRadius) {
            vec2 cornerCenter = vec2(effectiveRadius, height - effectiveRadius);
            return isInRoundedCorner(pos, cornerCenter, effectiveRadius);
        }
    }

    // 右下角
    if ((cornerFlags & CORNER_BOTTOM_RIGHT) != 0) {
        if (pos.x > width - effectiveRadius && pos.y > height - effectiveRadius) {
            vec2 cornerCenter = vec2(width - effectiveRadius, height - effectiveRadius);
            return isInRoundedCorner(pos, cornerCenter, effectiveRadius);
        }
    }

    // 如果不在任何需要特殊处理的区域，则渲染
    return true;
}

void main() {
    // 获取当前片段的坐标
    vec2 pos = gl_TexCoord[0].xy * resolution;

    // 判断是否应该渲染此片段
    if (shouldRender(pos)) {
        gl_FragColor = color;
    } else {
        // 丢弃片段（完全透明）
        discard;
    }
}
