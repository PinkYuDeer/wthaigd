#version 120

uniform vec2 iResolution;             // 渲染区域分辨率 (renderWidth, renderHeight)
uniform float u_continuityIndex;      // A value higher than 2.0 means closer to a rectangle than a period. Smaller Size need higher value.
uniform vec4 u_colorBg;               // The color of background

// Rectangle
uniform vec2 u_rectSize;              // The scale of the rectangle. (WH)
uniform vec2 u_rectCenter;            // The pixel-space rectangle center location (WH)
uniform vec4 u_colorRect;             // The color of rectangle
uniform float u_rectEdgeSoftness;     // How soft the edges should be. Higher values could be used to simulate a drop shadow. (pixels)
uniform vec4 u_cornerRadiuses;        // The radiuses of the corners: [topRight, bottomRight, topLeft, bottomLeft] (mWH)

// Border
uniform float u_borderThickness;      // The border size (mWH)
uniform float u_borderSoftness;       // How soft the (internal) border edge should be (in pixels)
uniform float u_borderPos;            // Relative to the edge of a rectangle：-0.5outer:, 0.5:inner, 0:middle
uniform vec4 u_borderSelect;          // Select border by [Top, Bottom, Left, Right]
uniform vec4 u_colorBorder;           // The color of (internal) border

// Shadow
uniform float u_ShadowSoftness;       // The shadow distance (2 x mWH)
uniform vec2 u_ShadowOffset;          // The shadow offset from rectangle center (WH)
uniform vec4 u_colorShadow;           // The color of shadow
// Shadow2
uniform float u_Shadow2Softness;      // The shadow distance (2 x mWH)
uniform vec2 u_Shadow2Offset;         // The shadow offset from rectangle center (WH)
uniform vec4 u_colorShadow2;          // The color of shadow

// innerShadow
uniform float u_InnerShadowSoftness;  // The shadow distance (2 x mWH)
uniform vec2 u_InnerShadowOffset;     // The shadow offset from rectangle center (WH)
uniform vec4 u_colorInnerShadow;      // The color of shadow
// innerShadow2
uniform float u_InnerShadow2Softness; // The shadow distance (2 x mWH)
uniform vec2 u_InnerShadow2Offset;    // The shadow offset from rectangle center (WH)
uniform vec4 u_colorInnerShadow2;     // The color of shadow


// 矩形SDF（使用比例单位）
float roundedBoxSDF(vec2 p, vec2 a, float r, float n){
    vec2 q = abs(p) - a + r;
    vec2 q_clamped = max(q, 0.0);
    float exterior_dist = pow(pow(q_clamped.x, n) + pow(q_clamped.y, n), 1.0 / n);
    float interior_dist = min(max(q.x, q.y), 0.0);
    return interior_dist + exterior_dist - r;
}

float getEffectRadius(vec2 p, vec4 r){
    r.xy = (p.x > 0.0) ? r.xy : r.zw;
    return (p.y > 0.0) ? r.x  : r.y;
}

void main() {
    vec2 position = (gl_TexCoord[0].xy- u_rectCenter) * iResolution.xy;
    position.x = -position.x;
    vec2 rectSize = u_rectSize * iResolution.xy;
    float minSize = min(rectSize.x, rectSize.y);

    float radius = getEffectRadius(position, u_cornerRadiuses * minSize);
    float borderThickness = u_borderThickness * minSize;

    vec2 shadowOffset = u_ShadowOffset * rectSize.xy;
    float shadowSoftness = u_ShadowSoftness * minSize ;
    vec2 shadow2Offset = u_Shadow2Offset * rectSize.xy;
    float shadow2Softness = u_Shadow2Softness * minSize;

    vec2 innerShadowOffset = u_InnerShadowOffset * rectSize.xy;
    float innerShadowSoftness = u_InnerShadowSoftness * minSize;
    vec2 innerShadow2Offset = u_InnerShadow2Offset * rectSize.xy;
    float innerShadow2Softness = u_InnerShadow2Softness * minSize;

    // -------------------------------------------------------------------------

    // base calculate
    vec2 halfSize = rectSize / 2.0;
    float distance = roundedBoxSDF(position, halfSize, radius,  u_continuityIndex);
    float smoothedAlpha = 1.0 - smoothstep(0.0, u_rectEdgeSoftness, distance);

    // first shadow
    float shadowDistance = roundedBoxSDF(position + shadowOffset, halfSize, radius,  u_continuityIndex);
    float shadowAlpha = min(1.0 - smoothstep(-shadowSoftness, shadowSoftness, shadowDistance), u_colorShadow.a);
    shadowAlpha *= step(0.0, distance - u_rectEdgeSoftness);
    vec4 color_1_bs = mix(u_colorBg, vec4(u_colorShadow.rgb, shadowAlpha), shadowAlpha);

    // second shadow
    float shadow2Distance = roundedBoxSDF(position + shadow2Offset, halfSize, radius,  u_continuityIndex);
    float shadow2Alpha = min(1.0 - smoothstep(-shadow2Softness, shadow2Softness, shadow2Distance), u_colorShadow2.a);
    shadow2Alpha *= step(0.0, distance - u_rectEdgeSoftness);
    vec4 color_2_bss = mix(color_1_bs, vec4(u_colorShadow2.rgb, shadow2Alpha), shadow2Alpha);

    // rectangle
    vec4 color_3_bssr = mix(color_2_bss, u_colorRect, min(u_colorRect.a, smoothedAlpha));
    float innerMuilti = smoothstep(u_rectEdgeSoftness, -0.000001, distance - u_rectEdgeSoftness);

    // first inner shadow
    float innerShadowDistance = roundedBoxSDF(position - innerShadowOffset, halfSize, radius,  u_continuityIndex);
    float innerShadowAlpha = min(1.0 - smoothstep(-innerShadowSoftness, innerShadowSoftness, -innerShadowDistance), u_colorInnerShadow.a) * innerMuilti;
    vec4 color_4_bssri = mix(color_3_bssr, vec4(u_colorInnerShadow.rgb, innerShadowAlpha), innerShadowAlpha);

    // second inner shadow
    float innerShadow2Distance = roundedBoxSDF(position - innerShadow2Offset, halfSize, radius,  u_continuityIndex);
    float innerShadow2Alpha = min(1.0 - smoothstep(-innerShadow2Softness, innerShadow2Softness, -innerShadow2Distance), u_colorInnerShadow2.a) * innerMuilti;
    vec4 color_5_bssrii = mix(color_4_bssri, vec4(u_colorInnerShadow2.rgb, innerShadow2Alpha), innerShadow2Alpha);

    // border
    float borderDistance = abs(distance + u_borderPos * (borderThickness - u_borderSoftness) - u_rectEdgeSoftness * 1.5);
    float borderAlpha = 1.0 - smoothstep(borderThickness / 2.0 - u_borderSoftness, borderThickness / 2.0, borderDistance);
    vec2 innerRectHalfSize = halfSize - borderThickness * (u_borderPos+0.5)  + 1.5 * (u_rectEdgeSoftness + u_borderSoftness);
    float difHalf = radius / 2.0;
    float borderTop    = step(innerRectHalfSize.y - difHalf, position.y) * step(0.5, u_borderSelect.x);
    float borderBottom = step(position.y,-innerRectHalfSize.y + difHalf) * step(0.5, u_borderSelect.y);
    float borderLeft   = step(position.x,-innerRectHalfSize.x + difHalf) * step(0.5, u_borderSelect.z);
    float borderRight  = step(innerRectHalfSize.x - difHalf, position.x) * step(0.5, u_borderSelect.w);
    borderAlpha *= max(max(max(borderTop, borderBottom), borderLeft), borderRight);
    vec4 color_6_bssriib = mix(color_5_bssrii, u_colorBorder, min(u_colorBorder.a, borderAlpha));

    // output
    gl_FragColor = color_6_bssriib;
}
