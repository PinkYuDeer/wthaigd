package com.pinkyudeer.wthaigd.helper;

import java.io.File;
import java.io.IOException;

public class ModFileHelper {

    private static File baseDir;

    // 初始化存档路径
    public static void init() {
        // 获取Minecraft根目录
        File minecraftDir = new File(".");

        // 判断是客户端还是服务端
        File worldDir;
        if (new File(minecraftDir, "saves").exists()) {
            // 客户端
            worldDir = new File(minecraftDir, "saves");
        } else {
            // 服务端
            worldDir = new File(minecraftDir, "world");
        }

        // 创建mod数据目录
        baseDir = new File(worldDir, "wthaigd");
        if (!baseDir.exists() && !baseDir.mkdirs()) {
            throw new RuntimeException("无法创建mod数据目录");
        }
    }

    // 获取特定文件
    public static File getFile(String fileName) {
        if (baseDir == null) {
            throw new IllegalStateException("ModFileManager not initialized.");
        }
        return new File(baseDir, fileName);
    }

    // 获取已有文件或创建新文件
    public static File getOrCreateFile(String fileName) {
        File file = getFile(fileName);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw new RuntimeException("Failed to create file: " + fileName);
                }
            } catch (SecurityException | IOException e) {
                throw new RuntimeException("Failed to create file: " + fileName, e);
            }
        }
        return file;
    }

    // 保存文件
    public static void saveFile(String fileName, String content) {
        File file = getFile(fileName);
        try {
            org.apache.commons.io.FileUtils.writeStringToFile(file, content);
        } catch (SecurityException | IOException e) {
            throw new RuntimeException("Failed to save file: " + fileName, e);
        }
    }

    // 读取文件
    public static String readFile(String fileName) {
        File file = getFile(fileName);
        try {
            return org.apache.commons.io.FileUtils.readFileToString(file);
        } catch (SecurityException | IOException e) {
            throw new RuntimeException("Failed to read file: " + fileName, e);
        }
    }

    // 删除文件
    public static void deleteFile(String fileName) {
        File file = getFile(fileName);
        if (file.exists()) {
            if (!file.delete()) {
                throw new RuntimeException("Failed to delete file: " + fileName);
            }
        }
    }

    // 清空目录
    public static void clearDirectory(String dirName) {
        File dir = getFile(dirName);
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    clearDirectory(file.getName());
                } else {
                    if (!file.delete()) {
                        throw new RuntimeException("Failed to delete file: " + file.getName());
                    }
                }
            }
        }
    }

}
