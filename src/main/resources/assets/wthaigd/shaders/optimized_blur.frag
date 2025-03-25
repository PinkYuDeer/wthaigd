#version 120

uniform sampler2D texture;
uniform vec2 texelSize;
uniform vec2 direction;
uniform float radius;

// 安全的纹理采样函数，确保在纹理边界内采样
vec4 safeTexture2D(sampler2D tex, vec2 uv) {
    // 将UV坐标钳制在[0,1]范围内，避免采样超出纹理边界
    uv = clamp(uv, 0.0, 1.0);
    return texture2D(tex, uv);
}

// 高效的高斯模糊片段着色器
// 利用硬件线性过滤优化采样
void main() {
    vec2 uv = gl_TexCoord[0].xy;
    vec4 color = vec4(0.0);

    // 减少采样次数，使用硬件的线性过滤功能
    // 这相当于每次采样获得两个像素的信息

    // 中央采样权重较高
    float central = 0.2270270270;
    color += safeTexture2D(texture, uv) * central;

    // 使用更少的采样点 (13个采样点替代传统的更多采样)
    // 采样偏移和权重经过精心计算
    float weight1 = 0.1945945946;
    float weight2 = 0.1216216216;
    float weight3 = 0.0540540541;
    float weight4 = 0.0162162162;

    // 偏移值被精心选择以在较少采样点的情况下获得最佳效果
    float offset1 = 1.0;
    float offset2 = 2.3333333333;
    float offset3 = 3.5555555556;
    float offset4 = 4.6666666667;

    // 应用四对对称采样，使用安全采样函数
    color += safeTexture2D(texture, uv + direction * texelSize * offset1) * weight1;
    color += safeTexture2D(texture, uv - direction * texelSize * offset1) * weight1;

    color += safeTexture2D(texture, uv + direction * texelSize * offset2) * weight2;
    color += safeTexture2D(texture, uv - direction * texelSize * offset2) * weight2;

    color += safeTexture2D(texture, uv + direction * texelSize * offset3) * weight3;
    color += safeTexture2D(texture, uv - direction * texelSize * offset3) * weight3;

    color += safeTexture2D(texture, uv + direction * texelSize * offset4) * weight4;
    color += safeTexture2D(texture, uv - direction * texelSize * offset4) * weight4;

    gl_FragColor = color;
    gl_FragColor.a = 1.0;
}
