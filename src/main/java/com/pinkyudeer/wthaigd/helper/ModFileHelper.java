package com.pinkyudeer.wthaigd.helper;

import cpw.mods.fml.common.FMLCommonHandler;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.IOException;

import static com.pinkyudeer.wthaigd.core.Wthaigd.LOG;
import static com.pinkyudeer.wthaigd.core.Wthaigd.MODID;

public class ModFileHelper {

    @Getter
    private static File savesDir; // baseDir/saves
    private static File configDir; // baseDir/config
    private static File currentWorldDir; // 服务器: baseDir/world 客户端: savesDir/<world_name>

    private static File modGlobalDir; // configDir/MODID (跨世界持久化数据)
    private static File modWorldDir; // currentWorldDir/MODID (世界专用数据)

    /**
     * 文件位置类型
     */
    public enum LocationType {
        WORLD, // 当前世界目录下的mod文件夹
        GLOBAL, // 全局config目录下的mod文件夹
        CONFIG // 原始config目录
    }

    /**
     * 初始化文件路径
     */
    public static void init() {
        // 初始化基础路径
        File baseDir = getBaseDir();

        // 初始化派生路径
        savesDir = new File(baseDir, "saves");
        configDir = new File(baseDir, "config");
        modGlobalDir = new File(configDir, MODID);
        updateModWorldDir();

    }

    private static File getBaseDir() {
        boolean isServer = FMLCommonHandler.instance()
            .getSide()
            .isServer();
        File baseDir; // .minecraft或服务器根目录
        if (isServer) {
            MinecraftServer server = FMLCommonHandler.instance()
                .getMinecraftServerInstance();
            try {
                baseDir = server.getFile(".")
                    .getAbsoluteFile()
                    .getCanonicalFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            baseDir = Minecraft.getMinecraft().mcDataDir;
        }
        return baseDir;
    }

    private static void updateModWorldDir() {
        modWorldDir = currentWorldDir != null ? new File(currentWorldDir, MODID)
            : new File(configDir, MODID + "/world_data_fallback");
    }

    /**
     * 更新modWorldDir
     */
    public static void updateModWorldDir(File currentWorldDir) {
        ModFileHelper.currentWorldDir = currentWorldDir;
        updateModWorldDir();
        // TODO: 发布前删除LOG
        LOG.info("更新modWorldDir: {}", modWorldDir);
    }

    /**
     * 确保所有必要目录存在
     */
    public static void ensureBaseDirsExist() throws IOException {
        createDirIfNeeded(modGlobalDir);
        createDirIfNeeded(modWorldDir);
    }

    public static void ensureWorldDirExist() throws IOException {
        createDirIfNeeded(modWorldDir);
    }

    private static void createDirIfNeeded(File dir) throws IOException {
        if (dir.exists() && !dir.isDirectory()) {
            throw new IOException("路径被文件占用: " + dir.getAbsolutePath());
        }
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("目录创建失败: " + dir.getAbsolutePath());
        }
    }

    /**
     * 保存文件到指定位置
     *
     * @param sourceFile   源文件
     * @param locationType 目标位置类型
     * @param overwrite    是否覆盖已存在的文件
     * @return 是否保存成功
     * @throws SecurityException 如果没有足够的权限
     */
    public static boolean saveFile(File sourceFile, LocationType locationType, boolean overwrite) {
        // 参数校验
        if (sourceFile == null) {
            LOG.error("源文件不能为null");
            return false;
        }

        // 获取目标文件路径
        File targetFile = getFile(sourceFile.getName(), locationType, false);
        if (targetFile == null) {
            LOG.error("无法获取目标文件路径");
            return false;
        }

        try {
            // 检查源文件
            if (!sourceFile.exists()) {
                LOG.error("源文件不存在: {}", sourceFile);
                return false;
            }

            if (!sourceFile.isFile()) {
                LOG.error("源文件路径不是一个文件: {}", sourceFile);
                return false;
            }

            // 检查目标文件
            if (targetFile.exists()) {
                if (!targetFile.isFile()) {
                    LOG.error("目标路径已被目录占用: {}", targetFile);
                    return false;
                }
                if (!overwrite) {
                    LOG.error("文件已存在且不允许覆盖: {}", targetFile);
                    return false;
                }
            }

            // 创建目标文件的父目录
            createDirIfNeeded(targetFile.getParentFile());

            // 复制文件
            java.nio.file.Files
                .copy(sourceFile.toPath(), targetFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            LOG.debug("文件保存成功: {} -> {}", sourceFile, targetFile);
            return true;

        } catch (IOException e) {
            LOG.error("保存文件失败: {} -> {}", sourceFile, targetFile, e);
            return false;
        }
    }

    /**
     * 快捷方法：保存文件到世界目录
     */
    public static boolean saveWorldFile(File sourceFile, boolean overwrite) {
        return saveFile(sourceFile, LocationType.WORLD, overwrite);
    }

    /**
     * 快捷方法：保存文件到全局目录
     */
    public static boolean saveGlobalFile(File sourceFile, boolean overwrite) {
        return saveFile(sourceFile, LocationType.GLOBAL, overwrite);
    }

    /**
     * 获取指定位置的文件
     *
     * @param fileName          文件名（支持相对路径）
     * @param locationType      文件位置类型
     * @param createIfNotExists 是否在不存在时创建文件
     * @return 文件对象，如果路径非法则返回null
     * @throws SecurityException 如果没有足够的权限
     */
    public static File getFile(String fileName, LocationType locationType, boolean createIfNotExists) {
        File targetDir = switch (locationType) {
            case WORLD -> modWorldDir;
            case GLOBAL -> modGlobalDir;
            case CONFIG -> configDir;
        };

        try {
            File file = new File(targetDir, fileName).getCanonicalFile();
            if (!isValidPath(file, targetDir)) {
                LOG.error("非法访问路径: {}", file);
                return null;
            }
            if (createIfNotExists && !file.exists()) {
                createDirIfNeeded(file.getParentFile());
                if (!file.createNewFile()) {
                    throw new IOException("创建文件失败: " + file);
                }
            }
            return file;
        } catch (IOException e) {
            LOG.error("路径解析失败: {}", fileName, e);
            return null;
        }
    }

    /**
     * 快捷方法：获取世界目录中的文件
     */
    public static File getWorldFile(String fileName, boolean createIfNotExists) {
        return getFile(fileName, LocationType.WORLD, createIfNotExists);
    }

    /**
     * 快捷方法：获取全局目录中的文件
     */
    public static File getGlobalFile(String fileName, boolean createIfNotExists) {
        return getFile(fileName, LocationType.GLOBAL, createIfNotExists);
    }

    /**
     * 删除指定位置的文件
     *
     * @param fileName     要删除的文件名
     * @param locationType 文件位置类型
     * @return 是否删除成功（文件不存在也返回true）
     * @throws SecurityException 如果没有足够的权限
     */
    public static boolean deleteFile(String fileName, LocationType locationType) {
        File file = getFile(fileName, locationType, false);
        if (file == null) return false;

        try {
            return !file.exists() || file.delete();
        } catch (SecurityException e) {
            LOG.error("删除文件失败: {}", file, e);
            return false;
        }
    }

    /**
     * 快捷方法：删除世界目录中的文件
     */
    public static boolean deleteWorldFile(String fileName) {
        return deleteFile(fileName, LocationType.WORLD);
    }

    /**
     * 快捷方法：删除全局目录中的文件
     */
    public static boolean deleteGlobalFile(String fileName) {
        return deleteFile(fileName, LocationType.GLOBAL);
    }

    /**
     * 验证文件路径是否合法（防止目录遍历攻击）
     *
     * @param file    要验证的文件
     * @param baseDir 基础目录
     * @return 如果路径合法返回true
     * @throws IOException 如果获取规范路径时发生错误
     */
    private static boolean isValidPath(File file, File baseDir) throws IOException {
        return file.getCanonicalPath()
            .startsWith(baseDir.getCanonicalPath());
    }

    /**
     * 获取默认存储位置
     */
    public static LocationType getDefaultLocationType() {
        return FMLCommonHandler.instance()
            .getSide()
            .isServer() ? LocationType.WORLD : LocationType.GLOBAL;
    }

}
