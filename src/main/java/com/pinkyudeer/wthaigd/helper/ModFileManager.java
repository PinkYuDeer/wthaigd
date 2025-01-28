package com.pinkyudeer.wthaigd.helper;

import java.io.File;

import net.minecraft.server.MinecraftServer;

public class ModFileManager {

    private static File baseDir;

    // 初始化存档路径
    public static void init() {
        MinecraftServer server = MinecraftServer.getServer();
        if (server != null) {
            baseDir = new File(server.getFile(""), "wthaigd"); // 存储在存档目录的 wthaigd 文件夹下
            if (!baseDir.exists() && !baseDir.mkdirs()) {
                throw new IllegalStateException(
                    "Failed to initialize ModFileManager: Failed to create base directory.");
            }
        } else {
            throw new IllegalStateException("Failed to initialize ModFileManager: Server is null.");
        }
    }

    // 获取特定文件
    public static File getFile(String fileName) {
        if (baseDir == null) {
            throw new IllegalStateException("ModFileManager not initialized.");
        }
        return new File(baseDir, fileName);
    }

    // 保存文件
    public static void saveFile(String fileName, String content) {
        File file = getFile(fileName);
        try {
            org.apache.commons.io.FileUtils.writeStringToFile(file, content);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save file: " + fileName, e);
        }
    }

    // 读取文件
    public static String readFile(String fileName) {
        File file = getFile(fileName);
        try {
            return org.apache.commons.io.FileUtils.readFileToString(file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file: " + fileName, e);
        }
    }
}
