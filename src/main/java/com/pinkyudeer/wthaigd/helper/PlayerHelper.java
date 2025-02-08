package com.pinkyudeer.wthaigd.helper;

import java.io.IOException;
import java.lang.reflect.Field;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraftforge.common.util.FakePlayer;

import com.pinkyudeer.wthaigd.core.Wthaigd;

public class PlayerHelper {

    // 反射字段缓存（针对1.7.10的混淆名称）
    private static final Field opPermissionLevelField = initOpPermissionLevelField();

    private static Field initOpPermissionLevelField() {
        try {
            Field field = UserListOpsEntry.class.getDeclaredField("field_152644_b");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            try {
                // 备用字段名称（不同核心可能不同）
                Field field = UserListOpsEntry.class.getDeclaredField("permissionLevel");
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ex) {
                Wthaigd.LOG.error("Failed to initialize OP permission level field", ex);
                return null;
            }
        }
    }

    /**
     * 判断玩家是否是OP
     */
    public static boolean isOp(EntityPlayer player) {
        if (isInvalidPlayer(player)) return false;
        return getServer().getConfigurationManager()
            .func_152596_g(player.getGameProfile());
    }

    /**
     * 获取玩家OP等级（非OP返回0）
     */
    public static int getOpLevel(EntityPlayer player) {
        if (isInvalidPlayer(player)) return 0;

        UserListOpsEntry entry = (UserListOpsEntry) getServer().getConfigurationManager()
            .func_152603_m() // 这是1.7.10中getOppedPlayers()的混淆名
            .func_152683_b(player.getGameProfile()); // 这是getEntry()的混淆名

        return entry != null ? getOpLevelFromEntry(entry) : 0;
    }

    /**
     * 设置玩家OP等级（需要服务器权限）
     */
    public static boolean setOpLevel(EntityPlayer player, int level) {
        if (isInvalidPlayer(player)) return false;
        if (level < 0 || level > 4) return false;

        try {
            ServerConfigurationManager configManager = getServer().getConfigurationManager();
            configManager.func_152605_a(player.getGameProfile());

            UserListOpsEntry entry = (UserListOpsEntry) configManager.func_152603_m()
                .func_152683_b(player.getGameProfile());

            if (entry != null && opPermissionLevelField != null) {
                opPermissionLevelField.setInt(entry, level);
                configManager.func_152603_m()
                    .func_152678_f();
                return true;
            }
        } catch (IllegalAccessException | IOException e) {
            Wthaigd.LOG.error("Failed to set OP level", e);
        }
        return false;
    }

    /**
     * 增加玩家经验等级
     */
    public static void addPlayerLevel(EntityPlayer player, int levels) {
        if (isInvalidPlayer(player)) return;
        player.addExperienceLevel(levels);
    }

    /**
     * 减少玩家经验等级（强制不低于0级）
     */
    public static void removePlayerLevel(EntityPlayer player, int levels) {
        if (isInvalidPlayer(player)) return;
        player.experienceLevel = Math.max(player.experienceLevel - levels, 0); // 直接设置等级确保不出现负数
    }

    // 辅助方法
    private static MinecraftServer getServer() {
        return MinecraftServer.getServer();
    }

    private static int getOpLevelFromEntry(UserListOpsEntry entry) {
        try {
            return opPermissionLevelField != null ? opPermissionLevelField.getInt(entry) : 0;
        } catch (IllegalAccessException e) {
            return 0;
        }
    }

    private static boolean isInvalidPlayer(EntityPlayer player) {
        return player == null || player instanceof FakePlayer
            || player.worldObj.isRemote
            || !getServer().isServerRunning();
    }
}
