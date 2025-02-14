package com.pinkyudeer.wthaigd.helper;

import com.pinkyudeer.wthaigd.core.Wthaigd;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteHelper {

    private static final String DB_URL = "jdbc:sqlite::memory:"; // 内存数据库
    private Connection inMemoryConnection;
    private Connection fileConnection;

    public SQLiteHelper() {
        initializeDatabases();
    }

    private void initializeDatabases() {
        try {
            // 初始化内存数据库连接
            inMemoryConnection = DriverManager.getConnection(DB_URL);
            // 初始化文件数据库连接
            String fileName = "task.db";
            File dbFile = ModFileHelper.getWorldFile(fileName, false);
            fileConnection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            // 如果文件数据库不存在，创建表并初始化，否则加载数据到内存数据库
            if (!dbFile.exists()) {
                initializeDatabase();
            } else {
                loadDataFromFileToMemory();
            }
        } catch (SQLException e) {
            Wthaigd.LOG.error("SQLite数据库初始化失败", e);
        }
    }

    private void initializeDatabase() throws SQLException {
        try (Statement stmt = fileConnection.createStatement()) {
            // 创建数据库表结构
            String createTableSQL = "CREATE TABLE IF NOT EXISTS your_table (" + "id INTEGER PRIMARY KEY, "
                + "name TEXT NOT NULL, "
                + "value INTEGER NOT NULL);";
            stmt.execute(createTableSQL);
        }
    }

    private void loadDataFromFileToMemory() {}

    public void saveDataFromMemoryToFile() {}

    // 下面是你在内存数据库中增删改查的操作

    public void close() {}
}
