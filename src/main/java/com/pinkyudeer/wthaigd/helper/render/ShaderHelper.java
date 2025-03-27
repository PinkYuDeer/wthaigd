package com.pinkyudeer.wthaigd.helper.render;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GL11;

public class ShaderHelper {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static int createProgram(ResourceLocation vertexLocation, ResourceLocation fragmentLocation) {
        // 确保可以使用OpenGL着色器
        if (!OpenGlHelper.shadersSupported) {
            return 0;
        }

        // 创建着色器程序
        int program = ARBShaderObjects.glCreateProgramObjectARB();

        if (program == 0) {
            return 0;
        }

        try {
            // 加载和编译顶点着色器
            int vertShader = loadShader(vertexLocation, ARBVertexShader.GL_VERTEX_SHADER_ARB);
            // 加载和编译片段着色器
            int fragShader = loadShader(fragmentLocation, ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);

            // 将着色器附加到程序并链接
            ARBShaderObjects.glAttachObjectARB(program, vertShader);
            ARBShaderObjects.glAttachObjectARB(program, fragShader);
            ARBShaderObjects.glLinkProgramARB(program);

            // 验证链接是否成功
            if (ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB)
                == GL11.GL_FALSE) {
                System.err.println(getLogInfo(program));
                return 0;
            }

            // 验证程序是否有效
            ARBShaderObjects.glValidateProgramARB(program);
            if (ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB)
                == GL11.GL_FALSE) {
                System.err.println(getLogInfo(program));
                return 0;
            }
        } catch (Exception e) {
            ARBShaderObjects.glDeleteObjectARB(program);
            throw e;
        }

        return program;
    }

    private static int loadShader(ResourceLocation resourceLocation, int shaderType) {
        int shader = 0;
        try {
            shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);

            if (shader == 0) {
                return 0;
            }

            // 加载着色器源代码
            ARBShaderObjects.glShaderSourceARB(shader, readShaderSource(resourceLocation));
            ARBShaderObjects.glCompileShaderARB(shader);

            // 检查编译是否成功
            if (ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB)
                == GL11.GL_FALSE) {
                throw new RuntimeException("Error compiling render: " + getLogInfo(shader));
            }

            return shader;
        } catch (Exception e) {
            ARBShaderObjects.glDeleteObjectARB(shader);
            throw e;
        }
    }

    private static String readShaderSource(ResourceLocation resourceLocation) {
        try {
            IResourceManager resourceManager = mc.getResourceManager();
            InputStream inputStream = resourceManager.getResource(resourceLocation)
                .getInputStream();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                return reader.lines()
                    .collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read render source", e);
        }
    }

    private static String getLogInfo(int obj) {
        return ARBShaderObjects.glGetInfoLogARB(
            obj,
            ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
    }

    public static void setUniform1i(int program, String name, int value) {
        int location = ARBShaderObjects.glGetUniformLocationARB(program, name);
        ARBShaderObjects.glUniform1iARB(location, value);
    }

    public static void setUniform1f(int program, String name, float value) {
        int location = ARBShaderObjects.glGetUniformLocationARB(program, name);
        ARBShaderObjects.glUniform1fARB(location, value);
    }

    public static void setUniform2f(int program, String name, float x, float y) {
        int location = ARBShaderObjects.glGetUniformLocationARB(program, name);
        ARBShaderObjects.glUniform2fARB(location, x, y);
    }

    public static void setUniform4f(int program, String name, float x, float y, float z, float w) {
        int location = ARBShaderObjects.glGetUniformLocationARB(program, name);
        ARBShaderObjects.glUniform4fARB(location, x, y, z, w);
    }
}
