package com.pinkyudeer.wthaigd.task;

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
        ensureSchemaVersionTable();
        applyMigration(1, "tasks.parent_task_id", () -> addColumnIfNotExists("tasks", "parent_task_id", "TEXT"));
        applyMigration(2, "teams.sync_source", () -> addColumnIfNotExists(
            "teams",
            "sync_source",
            "TEXT DEFAULT 'LOCAL'"));
        applyMigration(3, "teams.external_party_id", () -> addColumnIfNotExists(
            "teams",
            "external_party_id",
            "INTEGER DEFAULT -1"));
        applyMigration(4, "teams.sync_status", () -> addColumnIfNotExists(
            "teams",
            "sync_status",
            "TEXT DEFAULT 'ACTIVE'"));
        applyMigration(5, "teams.last_sync_time", () -> addColumnIfNotExists("teams", "last_sync_time", "TIMESTAMP"));
        applyMigration(6, "teams.external_team_key", () -> addColumnIfNotExists("teams", "external_team_key", "TEXT"));
    }

    private static void addColumnIfNotExists(String table, String column, String type) {
        try {
            if (columnExists(table, column)) {
                return;
            }
            SQLiteManager.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
            Wthaigd.LOG.info("已迁移: {} 表添加列 {}", table, column);
        } catch (Exception e) {
            Wthaigd.LOG.error("数据库迁移失败: {}.{}", table, column, e);
            throw new RuntimeException(e);
        }
    }

    private static void ensureSchemaVersionTable() {
        SQLiteManager.executeUpdate(
            "CREATE TABLE IF NOT EXISTS schema_version ("
                + "id INTEGER PRIMARY KEY, "
                + "description TEXT NOT NULL, "
                + "applied_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")");
    }

    private static void applyMigration(int id, String description, Runnable migration) {
        SQLiteManager.transaction(() -> {
            if (isMigrationApplied(id)) return null;
            migration.run();
            SQLiteManager.executeUpdate(
                "INSERT INTO schema_version (id, description) VALUES (?, ?)",
                id,
                description);
            Wthaigd.LOG.info("数据库迁移完成: {} {}", id, description);
            return null;
        });
    }

    private static boolean isMigrationApplied(int id) {
        Boolean applied = SQLiteManager.query(
            "SELECT 1 FROM schema_version WHERE id = ? LIMIT 1",
            rs -> rs.next(),
            id);
        return Boolean.TRUE.equals(applied);
    }

    private static boolean columnExists(String table, String column) {
        Boolean exists = SQLiteManager.query("PRAGMA table_info(" + table + ")", rs -> {
            while (rs.next()) {
                if (column.equals(rs.getString("name"))) return true;
            }
            return false;
        });
        return Boolean.TRUE.equals(exists);
    }

    public static class player {

        public static void login(EntityPlayer player) {
            PlayerDao.updateOrInsert(player);
        }
    }
}
