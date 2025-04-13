#version 120

uniform vec2 iResolution;             // 渲染区域分辨率 (renderWidth, renderHeight)
uniform vec4 u_colorBg;               // The color of background

uniform vec2 u_rectSize;              // The scale of the rectangle. (WH)
uniform vec2 u_rectCenter;            // The pixel-space rectangle center location (WH)
uniform vec4 u_colorRect;             // The color of rectangle

uniform float u_borderThickness;      // The border size (mWH)
uniform float u_borderPos;            // Relative to the edge of a rectangle：-0.5outer:, 0.5:inner, 0:middle
uniform vec4 u_borderSelect;          // Select border by [Top, Bottom, Left, Right]
uniform float u_borderCutCorners;     // Whether to not render corners when unilateral
uniform vec4 u_colorBorder;           // The color of (internal) border


float sdBox( in vec2 p, in vec2 b ) {
    vec2 d = abs(p)-b;
    return length(max(d,0.0)) + min(max(d.x,d.y),0.0);
}

void main() {
    vec2 position = (gl_TexCoord[0].xy- u_rectCenter) * iResolution.xy;
    vec2 rectCenter = u_rectCenter * iResolution.xy;
    vec2 rectSize = u_rectSize * iResolution.xy;
    float scaleR = min(rectSize.x, rectSize.y);
    float borderThickness = u_borderThickness * scaleR;

    // -------------------------------------------------------------------------

    // base calculate
    vec2 halfSize = rectSize / 2.0;
    float distance = sdBox(position, halfSize);

    // rectangle
    vec4  color_br = mix(u_colorBg, u_colorRect, min(u_colorRect.a, step(distance, 0.0)));

    // border
    float outerT = borderThickness * (-u_borderPos + 0.5);
    float distanceBorder = sdBox(position, halfSize + outerT);
    float borderAlpha = step(distanceBorder, 0.0) - step(distanceBorder, -borderThickness);
    // borderSelect
    vec2 innerRect = halfSize - borderThickness + outerT;
    vec4 pos = vec4(step(innerRect.y, position.y), step(position.y, -innerRect.y),
                    step(innerRect.x, position.x), step(position.x, -innerRect.x))
               * u_borderSelect;
    vec4 posCut = vec4(step(halfSize.y, position.y), step(position.y, -halfSize.y),
                     step(halfSize.x, position.x), step(position.x, -halfSize.x))
                * (1.0-u_borderSelect) * u_borderCutCorners;
    borderAlpha *= max(max(pos.x, pos.y) - (posCut.z + posCut.w), max(pos.z, pos.w) - (posCut.x + posCut.y));

    gl_FragColor = mix(color_br, u_colorBorder, min(u_colorBorder.a, borderAlpha));
}
