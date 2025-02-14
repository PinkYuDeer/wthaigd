package com.pinkyudeer.wthaigd.helper;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import com.pinkyudeer.wthaigd.core.Wthaigd;

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
        Wthaigd.LOG.info("初始化SQLite数据库");
        try (Statement stmt = fileConnection.createStatement()) {
            // 创建数据库表结构
            String createTableSQL = "CREATE TABLE IF NOT EXISTS your_table (" + "id INTEGER PRIMARY KEY, "
                + "name TEXT NOT NULL, "
                + "value INTEGER NOT NULL);";
            stmt.execute(createTableSQL);
        }
    }

    private void loadDataFromFileToMemory() {
        Wthaigd.LOG.info("加载数据到内存数据库");
    }

    public void saveDataFromMemoryToFile() {
        Wthaigd.LOG.info("将数据从内存数据库保存到文件数据库");
    }

    // 下面是你在内存数据库中增删改查的操作

    public void close() {
        Wthaigd.LOG.info("关闭SQLite数据库连接");
    }
}
