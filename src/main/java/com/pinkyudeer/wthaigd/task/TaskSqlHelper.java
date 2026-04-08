package com.pinkyudeer.wthaigd.task;

import java.sql.ResultSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;

import org.reflections.Reflections;

import com.pinkyudeer.wthaigd.Wthaigd;
import com.pinkyudeer.wthaigd.db.SQLHelper;
import com.pinkyudeer.wthaigd.db.SQLiteManager;
import com.pinkyudeer.wthaigd.db.annotation.Table;
import com.pinkyudeer.wthaigd.task.dao.PlayerDao;

/**
 * 任务系统数据库操作助手类
 * 提供任务相关实体的CRUD操作
 */
public class TaskSqlHelper {

    /**
     * 初始化任务数据库
     * 扫描并创建所有任务相关的表
     */
    public static void initTaskDataBase() {
        Reflections reflections = new Reflections("com.pinkyudeer.wthaigd.task.entity");
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(Table.class);
        try {
            SQLHelper.createTables(annotatedClasses);
        } catch (Exception e) {
            Wthaigd.LOG.error("初始化任务数据库失败", e);
            return;
        }
        Wthaigd.LOG.info("初始化任务数据库，共创建 {} 张表", annotatedClasses.size());

        migrateSchema();
    }

    public static void migrateSchema() {
        addColumnIfNotExists("tasks", "parent_task_id", "TEXT");
    }

    private static void addColumnIfNotExists(String table, String column, String type) {
        try {
            Object result = SQLiteManager.executeSafeSQL("PRAGMA table_info(" + table + ")");
            if (result instanceof ResultSet rs) {
                while (rs.next()) {
                    if (column.equals(rs.getString("name"))) {
                        return;
                    }
                }
                SQLiteManager.executeSafeSQL("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
                Wthaigd.LOG.info("已迁移: {} 表添加列 {}", table, column);
            }
        } catch (Exception e) {
            Wthaigd.LOG.error("数据库迁移失败: {}.{}", table, column, e);
        }
    }

    public static class player {

        public static void login(EntityPlayer player) {
            PlayerDao.updateOrInsert(player);
        }
    }
}
