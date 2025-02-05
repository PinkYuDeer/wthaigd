package com.pinkyudeer.wthaigd.helper;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinkyudeer.wthaigd.core.Config;

public class SQLiteManager {

    private static final Logger log = LogManager.getLogger(SQLiteManager.class);
    private static Connection connection;
    private final static String memoryDbUrl = "jdbc:sqlite::memory:";
    private final static String fileName = "task_data.db";
    private final static String fileDbUrl = "jdbc:sqlite:" + ModFileManager.getFile(fileName)
        .getAbsolutePath();

    // onServerStarting时初始化数据库
    public static void initDatabase() {
        // 控制是否为内存模式
        boolean isMemoryMode = Config.isMemoryMode;
        try {
            String dbUrl = isMemoryMode ? memoryDbUrl : fileDbUrl;
            connection = DriverManager.getConnection(dbUrl);
            if (isMemoryMode) {
                // 如果本地数据库文件存在，则加载到内存
                File dbFile = ModFileManager.getFile("task_data.db");
                if (dbFile.exists()) {

                }
            } else {
                loadFromFile();
            }

            System.out.println("[Wthaigd] SQLite Database initialized successfully. Memory mode: " + isMemoryMode);
        } catch (SQLException e) {
            log.error("Failed to initialize SQLite database.", e);
            throw new RuntimeException("Failed to initialize SQLite database.");
        }
    }

    // 从文件加载到内存数据库
    private static void loadFromFile() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // 附加文件数据库
            stmt.execute("ATTACH DATABASE '" + fileDbUrl + "' AS file_db");

            // 获取所有表结构并复制（需要关闭外键约束）
            stmt.execute("PRAGMA foreign_keys=OFF");
            copySchema(connection, "file_db", "main");
            copyData(connection, "file_db", "main");
            stmt.execute("PRAGMA foreign_keys=ON");

            stmt.execute("DETACH DATABASE file_db");
        }
    }

    // 保存内存数据库到文件
    private static void saveToFile() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // 创建/清空目标文件数据库
            ModFileManager.deleteFile("task_data.db");

            // 附加文件数据库
            stmt.execute("ATTACH DATABASE '" + fileDbUrl + "' AS file_db");

            // 复制内存数据到文件（需要关闭外键约束）
            stmt.execute("PRAGMA foreign_keys=OFF");
            copySchema(connection, "main", "file_db");
            copyData(connection, "main", "file_db");
            stmt.execute("PRAGMA foreign_keys=ON");

            stmt.execute("DETACH DATABASE file_db");
        }
    }

    // 复制表结构
    private static void copySchema(Connection conn, String sourceSchema, String targetSchema) throws SQLException {
        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT sql FROM " + sourceSchema + ".sqlite_master WHERE type='table'")) {

            while (rs.next()) {
                String createTableSQL = rs.getString("sql");
                conn.createStatement()
                    .execute(createTableSQL.replace(sourceSchema, targetSchema));
            }
        }
    }

    // 复制表数据（支持事务批处理）
    private static void copyData(Connection conn, String sourceSchema, String targetSchema) throws SQLException {
        conn.setAutoCommit(false);
        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt
                .executeQuery("SELECT name FROM " + sourceSchema + ".sqlite_master WHERE type='table'")) {

            while (rs.next()) {
                String tableName = rs.getString("name");
                if (tableName.equals("sqlite_sequence")) continue; // 忽略自增序列表

                // 使用批量插入提升性能
                try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO " + targetSchema
                        + "."
                        + tableName
                        + " SELECT * FROM "
                        + sourceSchema
                        + "."
                        + tableName)) {
                    pstmt.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}
