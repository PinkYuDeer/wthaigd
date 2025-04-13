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

// Calculate the distance using the hyperellipse formula
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

    vec4 radius4 = u_cornerRadiuses * minSize;
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
    vec2  halfSize = rectSize / 2.0;
    float radius = getEffectRadius(position, radius4);
    float distance = roundedBoxSDF(position, halfSize, radius,  u_continuityIndex);
    float rectEdgeAlpha = 1.0 - smoothstep(0.0, u_rectEdgeSoftness, distance);

    // shadow
    vec2  positionS1 = position + shadowOffset;
    vec2  positionS2 = position + shadow2Offset;
    float radiusS1 = getEffectRadius(positionS1, radius4);
    float radiusS2 = getEffectRadius(positionS2, radius4);
    float outerMulti = step(u_rectEdgeSoftness / 2.0, distance);
    float shadowDistance = roundedBoxSDF(positionS1, halfSize, radiusS1,  u_continuityIndex);
    float shadow2Distance = roundedBoxSDF(positionS2, halfSize, radiusS2,  u_continuityIndex);
    float shadowAlpha = min(1.0 - smoothstep(-shadowSoftness, shadowSoftness, shadowDistance), u_colorShadow.a) * outerMulti;
    float shadow2Alpha = min(1.0 - smoothstep(-shadow2Softness, shadow2Softness, shadow2Distance), u_colorShadow2.a) * outerMulti;
    vec3 colorShadow = mix(u_colorShadow2.rgb, u_colorShadow.rgb, shadowAlpha/max((shadowAlpha + shadow2Alpha), 1e-10));
    vec3 colorShadow2 = mix(u_colorShadow.rgb, u_colorShadow2.rgb, shadow2Alpha/max((shadowAlpha + shadow2Alpha), 1e-10));
    vec4  color_1_BS = mix(u_colorBg, vec4(colorShadow, shadowAlpha), shadowAlpha);
    vec4  color_2_BSS = mix(color_1_BS, vec4(colorShadow2, shadow2Alpha), shadow2Alpha);

    // rectangle
    vec4  color_3_BSSR = mix(color_2_BSS, u_colorRect, min(u_colorRect.a, rectEdgeAlpha));

    // inner shadow
    vec2  position1 = position - innerShadowOffset;
    vec2  position2 = position - innerShadow2Offset;
    float radius1 = getEffectRadius(position1, radius4);
    float radius2 = getEffectRadius(position2, radius4);
    float innerShadowDistance = roundedBoxSDF(position1, halfSize, radius1,  u_continuityIndex);
    float innerShadow2Distance = roundedBoxSDF(position2, halfSize, radius2,  u_continuityIndex);
    float innerShadowAlpha = min(1.0 - smoothstep(-innerShadowSoftness, innerShadowSoftness, -innerShadowDistance), u_colorInnerShadow.a);
    float innerShadow2Alpha = min(1.0 - smoothstep(-innerShadow2Softness, innerShadow2Softness, -innerShadow2Distance), u_colorInnerShadow2.a);
    vec3 colorInnerShadow = mix(u_colorInnerShadow2.rgb, u_colorInnerShadow.rgb, innerShadowAlpha/max((innerShadowAlpha + innerShadow2Alpha), 1e-10));
    vec3 colorInnerShadow2 = mix(u_colorInnerShadow.rgb, u_colorInnerShadow2.rgb, innerShadow2Alpha/max((innerShadowAlpha + innerShadow2Alpha), 1e-10));
    vec4  color_4_BSSRI = mix(color_3_BSSR, vec4(colorInnerShadow, innerShadowAlpha), min(innerShadowAlpha, rectEdgeAlpha));
    vec4  color_5_BSSRII = mix(color_4_BSSRI, vec4(colorInnerShadow2, innerShadow2Alpha), min(innerShadow2Alpha, rectEdgeAlpha));

    // border
    float innerT = -borderThickness * (u_borderPos + 0.5) + u_rectEdgeSoftness / (1.25 + (-u_borderPos + 0.5) * 0.5);
    float outerT = borderThickness * (-u_borderPos + 0.5) + u_rectEdgeSoftness / (1.25 + (-u_borderPos + 0.5) * 0.5);
    float outerEdgeS = step(u_borderPos, 0.1) * step(outerT, u_rectEdgeSoftness) * (u_rectEdgeSoftness - outerT);
    float borderAlpha = smoothstep(innerT - u_borderSoftness - 1e-5, innerT, distance) - smoothstep(outerT, outerT + u_borderSoftness + 1e-5 + outerEdgeS, distance);
    vec4 color_6_BSSRIIB = mix(color_5_BSSRII, u_colorBorder, min(u_colorBorder.a, borderAlpha));

    // output
    gl_FragColor = color_6_BSSRIIB;
}
