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
float roundedBoxSDF(vec2 CenterPosition, vec2 Size, vec4 Radius, float n)
{
    Radius.xy = (CenterPosition.x > 0.0) ? Radius.xy : Radius.zw;
    Radius.x  = (CenterPosition.y > 0.0) ? Radius.x  : Radius.y;

    vec2 q = abs(CenterPosition) - Size + Radius.x;

    vec2 q_clamped = max(q, 0.0);
    float exterior_dist = pow(pow(q_clamped.x, n) + pow(q_clamped.y, n), 1.0 / n);
    float interior_dist = min(max(q.x, q.y), 0.0);
    return interior_dist + exterior_dist - Radius.x;
}

void main() {
    float minSize = min(iResolution.x, iResolution.y);
    vec2 position = (gl_TexCoord[0].xy- u_rectCenter) * iResolution.xy;

    vec2 rectSize = u_rectSize * iResolution.xy;
    vec4 radius = u_cornerRadiuses * minSize;
    float borderThickness = u_borderThickness * minSize;

    vec2 shadowOffset = u_ShadowOffset * iResolution.xy;
    float shadowSoftness = u_ShadowSoftness * minSize ;
    vec2 shadow2Offset = u_Shadow2Offset * iResolution.xy;
    float shadow2Softness = u_Shadow2Softness * minSize;

    vec2 innerShadowOffset = u_InnerShadowOffset * iResolution.xy;
    float innerShadowSoftness = u_InnerShadowSoftness * minSize;
    vec2 innerShadow2Offset = u_InnerShadow2Offset * iResolution.xy;
    float innerShadow2Softness = u_InnerShadow2Softness * minSize;

    // -------------------------------------------------------------------------

    // base caculate
    vec2 halfSize = rectSize / 2.0;
    position.x = -position.x;
    float distance = roundedBoxSDF(position, halfSize, radius,  u_continuityIndex);
    float smoothedAlpha = 1.0 - smoothstep(0.0, u_rectEdgeSoftness, distance);

    // first shadow
    float shadowDistance = roundedBoxSDF(position + shadowOffset, halfSize, radius,  u_continuityIndex);
    float shadowAlpha = min(1.0 - smoothstep(-shadowSoftness, shadowSoftness, shadowDistance), u_colorShadow.a);
    shadowAlpha *= step(0.0, distance - 2.0);
    vec4 color_1_bs = mix(u_colorBg, vec4(u_colorShadow.rgb, shadowAlpha), shadowAlpha);

    // second shadow
    float shadow2Distance = roundedBoxSDF(position + shadow2Offset, halfSize, radius,  u_continuityIndex);
    float shadow2Alpha = min(1.0 - smoothstep(-shadow2Softness, shadow2Softness, shadow2Distance), u_colorShadow2.a);
    shadow2Alpha *= step(0.0, distance - 2.0);
    vec4 color_2_bss = mix(color_1_bs, vec4(u_colorShadow2.rgb, shadow2Alpha), shadow2Alpha);

    // rectangle
    vec4 color_3_bssr = mix(color_2_bss, u_colorRect, min(u_colorRect.a, smoothedAlpha));

    // first inner shadow
    float innerShadowDistance = roundedBoxSDF(position - innerShadowOffset, halfSize, radius,  u_continuityIndex);
    float innerShadowAlpha = min(1.0 - smoothstep(-innerShadowSoftness, innerShadowSoftness, -innerShadowDistance), u_colorInnerShadow.a);
    innerShadowAlpha *= step(distance - 2.0, 0.0);
    vec4 color_4_bssri = mix(color_3_bssr, vec4(u_colorInnerShadow.rgb, innerShadowAlpha), innerShadowAlpha);

    // second inner shadow
    float innerShadow2Distance = roundedBoxSDF(position - innerShadow2Offset, halfSize, radius,  u_continuityIndex);
    float innerShadow2Alpha = min(1.0 - smoothstep(-innerShadow2Softness, innerShadow2Softness, -innerShadow2Distance), u_colorInnerShadow2.a);
    innerShadow2Alpha *= step(distance - 2.0, 0.0);
    vec4 color_5_bssrii = mix(color_4_bssri, vec4(u_colorInnerShadow2.rgb, innerShadow2Alpha), innerShadow2Alpha);

    // border
    float borderDistance = abs(distance + u_borderPos * (borderThickness - u_borderSoftness));
    float borderAlpha = 1.0 - smoothstep(borderThickness / 2.0 - u_borderSoftness, borderThickness / 2.0, borderDistance);
    vec4 color_6_bssriib = mix(color_5_bssrii, u_colorBorder, min(u_colorBorder.a, borderAlpha));

    // output
    gl_FragColor = color_6_bssriib;
}
