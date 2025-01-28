package com.pinkyudeer.wthaigd.helper;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SQLiteManager {

    private static final Logger log = LogManager.getLogger(SQLiteManager.class);
    private static Connection connection;
    private static boolean isMemoryMode = true; // 控制是否为内存模式
    private final static String memoryDbUrl = "jdbc:sqlite::memory:";
    private final static String fileDbUrl = "jdbc:sqlite:" + ModFileManager.getFile("task_data.db")
        .getAbsolutePath();

    // onServerStarting时初始化数据库
    public static void initDatabase() {
        try {
            String dbUrl = isMemoryMode ? memoryDbUrl : fileDbUrl;
            connection = DriverManager.getConnection(dbUrl);
            // 判断是否有task_data.db，没有则新建并初始化表

            System.out.println("[Wthaigd] SQLite Database initialized successfully. Memory mode: " + isMemoryMode);
        } catch (SQLException e) {
            log.error("Failed to initialize SQLite database.", e);
            throw new RuntimeException("Failed to initialize SQLite database.");
        }
    }

    private static void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS tasks (" + "id TEXT PRIMARY KEY,"
                    + "title TEXT NOT NULL,"
                    + "description TEXT NOT NULL,"
                    + "assignee TEXT,"
                    + "followers TEXT,"
                    + "likes TEXT,"
                    + "comments TEXT,"
                    + "priority INTEGER NOT NULL,"
                    + "importance INTEGER NOT NULL,"
                    + "urgency INTEGER NOT NULL,"
                    + "status INTEGER NOT NULL,"
                    + "create_time INTEGER NOT NULL,"
                    + "deadline INTEGER NOT NULL,"
                    + "subtasks TEXT,"
                    + "tags TEXT,"
                    + "update_time INTEGER NOT NULL"
                    + ")");
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS task_users (" + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT NOT NULL,"
                    + "group_id INTEGER NOT NULL,"
                    + "status INTEGER NOT NULL,"
                    + "create_time INTEGER NOT NULL,"
                    + "update_time INTEGER NOT NULL"
                    + ")");
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS task_user_groups (" + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT NOT NULL,"
                    + "create_time INTEGER NOT NULL,"
                    + "update_time INTEGER NOT NULL"
                    + ")");
        }
    }

    private static void loadTables() throws SQLException {
        // 从本地文件task_data.db中加载数据到内存中
        if (isMemoryMode) {
            File dbFile = ModFileManager.getFile("task_data.db");
            try (Connection fileConnection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath())) {
                try (Statement stmt = fileConnection.createStatement()) {
                    stmt.execute("ATTACH DATABASE '" + dbFile.getAbsolutePath() + "' AS backup");
                    stmt.execute("INSERT INTO tasks SELECT * FROM backup.tasks");
                }
            }
        } else {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("ATTACH DATABASE '" + fileDbUrl + "' AS backup");
                stmt.execute("INSERT INTO tasks SELECT * FROM backup.tasks");
            }
        }
    }

    // 获取数据库连接
    public static Connection getConnection() {
        return connection;
    }

    // 保存内存模式数据到磁盘
    public static void saveDatabase() throws SQLException {
        if (isMemoryMode) {
            File dbFile = ModFileManager.getFile("task_data.db");
            try (Connection fileConnection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath())) {
                // 使用 SQLite 提供的 backup API 将内存数据库的内容保存到文件中
                try (Statement stmt = fileConnection.createStatement()) {
                    stmt.execute("ATTACH DATABASE '" + dbFile.getAbsolutePath() + "' AS backup");
                    stmt.execute("INSERT INTO backup.tasks SELECT * FROM tasks");
                }
                System.out.println("[Wthaigd] Memory data saved to file: " + dbFile.getAbsolutePath());
            }
        }
    }
}
