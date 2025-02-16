package com.pinkyudeer.wthaigd.helper;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sqlite.SQLiteConnection;

import com.pinkyudeer.wthaigd.core.Wthaigd;

public class SQLiteHelper {

    private static final String MEM_DB_URL = "jdbc:sqlite::memory:"; // 内存数据库
    private static Connection inMemoryConnection;
    private static final File DATABASE_FILE = ModFileHelper.getWorldFile("task.db", false)
        .getAbsoluteFile();

    private static final String CREATE_TASKS_TABLE = "CREATE TABLE IF NOT EXISTS tasks ("
        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
        + "title TEXT NOT NULL,"
        + "description TEXT,"
        + "status INTEGER DEFAULT 0,"
        + // 0:未完成 1:已完成
        "create_time INTEGER NOT NULL"
        + ")";

    public static void initializeDatabases() {
        try {
            // 初始化内存数据库连接
            inMemoryConnection = DriverManager.getConnection(MEM_DB_URL);
            // 如果文件数据库不存在，创建表并初始化，否则加载数据到内存数据库
            if (!DATABASE_FILE.exists()) {
                initializeDatabase();
            } else {
                loadDataFromFileToMemory();
            }
        } catch (SQLException e) {
            Wthaigd.LOG.error("SQLite数据库初始化失败", e);
        }
    }

    private static void initializeDatabase() {
        Wthaigd.LOG.info("初始化SQLite数据库");
        // 创建数据库表结构
        try (Statement memStmt = inMemoryConnection.createStatement()) {
            memStmt.executeUpdate(CREATE_TASKS_TABLE);

            // 插入测试数据
            memStmt.executeUpdate(
                "INSERT INTO tasks (title, description, status, create_time) VALUES" + "('收集钻石', '挖矿获得10个钻石', 0, "
                    + System.currentTimeMillis()
                    + "),"
                    + "('建造房屋', '使用木头建造一个小木屋', 0, "
                    + System.currentTimeMillis()
                    + "),"
                    + "('种植小麦', '种植一片小麦田', 1, "
                    + System.currentTimeMillis()
                    + ")");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        saveDataFromMemoryToFile();
    }

    private static void loadDataFromFileToMemory() {
        Wthaigd.LOG.info("加载数据到内存数据库");
        SQLiteConnection mem;
        try {
            mem = inMemoryConnection.unwrap(SQLiteConnection.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        int result;
        try {
            result = mem.getDatabase()
                .restore("main", DATABASE_FILE.getAbsolutePath(), (int remaining, int pageCount) -> {
                    int progress = (int) ((1 - (double) remaining / pageCount) * 100);
                    Wthaigd.LOG.info("数据库恢复进度: {}%, 剩余页数: {}/{}", progress, remaining, pageCount);
                });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Wthaigd.LOG.info("数据库恢复结果: {}", result);

    }

    public static void saveDataFromMemoryToFile() {
        Wthaigd.LOG.info("将数据从内存数据库保存到文件数据库");
        SQLiteConnection mem;
        try {
            mem = inMemoryConnection.unwrap(SQLiteConnection.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Wthaigd.LOG.info("数据库文件路径: {}", DATABASE_FILE.getAbsolutePath());
        try {
            ModFileHelper.ensureWorldDirExist();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int result;
        try {
            result = mem.getDatabase()
                .backup("main", DATABASE_FILE.getAbsolutePath(), (int remaining, int pageCount) -> {
                    int progress = (int) ((1 - (double) remaining / pageCount) * 100);
                    Wthaigd.LOG.info("数据库备份进度: {}%, 剩余页数: {}/{}", progress, remaining, pageCount);
                });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Wthaigd.LOG.info("数据库备份结果: {}", result);
    }

    // 下面是你在内存数据库中增删改查的操作

    public static void close() {
        Wthaigd.LOG.info("关闭SQLite数据库连接");
        try {
            if (inMemoryConnection != null) {
                inMemoryConnection.close();
            }
        } catch (SQLException e) {
            Wthaigd.LOG.error("关闭SQLite数据库连接失败", e);
        }
    }

    public static List<Map<String, Object>> executeSQL(String sql) {
        Wthaigd.LOG.info("执行SQL: {}", sql);
        if (sql == null || sql.trim()
            .isEmpty()) {
            throw new IllegalArgumentException("SQL语句不能为空");
        }
        List<Map<String, Object>> results = new ArrayList<>();
        try {
            Statement statement = inMemoryConnection.createStatement();
            boolean hasResultSet = statement.execute(sql);
            if (hasResultSet) {
                ResultSet rs = statement.getResultSet();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(metaData.getColumnName(i), rs.getObject(i));
                    }
                    results.add(row);
                }
            }
            return results;
        } catch (SQLException e) {
            Wthaigd.LOG.error("执行SQL失败", e);
            throw new RuntimeException(e);
        }
    }
}
