package com.pinkyudeer.wthaigd.helper.dataBase;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sqlite.SQLiteConnection;

import com.pinkyudeer.wthaigd.core.Wthaigd;
import com.pinkyudeer.wthaigd.helper.ModFileHelper;
import com.pinkyudeer.wthaigd.task.TaskSqlHelper;

/**
 * SQLite 数据库管理类。
 * 负责连接、执行 SQL 和关闭数据库。
 */
public class SQLiteManager {

    private static final String MEM_DB_URL = "jdbc:sqlite::memory:"; // 内存数据库 URL
    private static Connection inMemoryConnection;
    private static final File DATABASE_FILE = ModFileHelper.getWorldFile("task.db", false)
        .getAbsoluteFile();
    public static boolean isWorldLoaded = false;

    /**
     * 初始化内存数据库。
     * 若文件数据库存在则加载数据，否则创建新数据库。
     */
    public static void initSqlite() {
        try {
            inMemoryConnection = DriverManager.getConnection(MEM_DB_URL);
        } catch (SQLException e) {
            Wthaigd.LOG.error("SQLite 初始化失败", e);
            return;
        }
        if (!DATABASE_FILE.exists()) {
            initNewDataBase();
        } else {
            loadDataFromFileToMemory();
        }
        Wthaigd.LOG.info("SQLite 初始化完成");
        isWorldLoaded = true;
    }

    /**
     * 初始化新数据库并保存到文件。
     */
    private static void initNewDataBase() {
        Wthaigd.LOG.info("初始化新 SQLite 数据库");

        // 在这里添加初始化 SQL 语句
        Map<String, List<Object>> stringListMap = executeAllSqlInMap(TaskSqlHelper.init.generateAllCreateTableSql());
        Wthaigd.LOG.info("初始化结果: {}", stringListMap);// TODO: 这里的日志输出在正式发布前删除

        saveDataFromMemoryToFile();
    }

    /**
     * 从文件加载数据到内存数据库。
     */
    private static void loadDataFromFileToMemory() {
        Wthaigd.LOG.info("加载文件数据到内存");
        try {
            int result;
            try (SQLiteConnection mem = unwrapConnection()) {
                result = mem.getDatabase()
                    .restore("main", DATABASE_FILE.getAbsolutePath(), (remaining, pageCount) -> {
                        int progress = (int) ((1 - (double) remaining / pageCount) * 100);
                        Wthaigd.LOG.info("恢复进度: {}%, 剩余: {}/{}", progress, remaining, pageCount);
                    });
            }
            Wthaigd.LOG.info("恢复结果: {}", result);
        } catch (SQLException e) {
            throw new RuntimeException("加载数据失败", e);
        }
    }

    /**
     * 将内存数据库保存到文件。
     */
    public static void saveDataFromMemoryToFile() {
        if (!isWorldLoaded) return;
        Wthaigd.LOG.info("保存内存数据到文件");
        try {
            int result;
            try (SQLiteConnection mem = unwrapConnection()) {
                ModFileHelper.ensureWorldDirExist();
                result = mem.getDatabase()
                    .backup("main", DATABASE_FILE.getAbsolutePath(), (remaining, pageCount) -> {
                        int progress = (int) ((1 - (double) remaining / pageCount) * 100);
                        Wthaigd.LOG.info("备份进度: {}%, 剩余: {}/{}", progress, remaining, pageCount);
                    });
            }
            Wthaigd.LOG.info("备份结果: {}", result);
        } catch (SQLException | IOException e) {
            throw new RuntimeException("保存数据失败", e);
        }
    }

    /**
     * 关闭数据库连接。
     */
    public static void close() {
        saveDataFromMemoryToFile();
        Wthaigd.LOG.info("关闭 SQLite 连接");
        try {
            if (inMemoryConnection != null && !inMemoryConnection.isClosed()) {
                inMemoryConnection.close();
            }
        } catch (SQLException e) {
            Wthaigd.LOG.error("关闭连接失败", e);
        }
        isWorldLoaded = false;
    }

    /**
     * 执行无参数 SQL。
     *
     * @param sql SQL 语句
     * @return 执行结果, 若为查询则返回 ResultSet, 否则返回影响的行数
     */
    @SuppressWarnings("SqlSourceToSinkFlow")
    public static Object executeSafeSQL(String sql, Object... params) {
        try (PreparedStatement ps = inMemoryConnection.prepareStatement(sql)) {
            setParameters(ps, Collections.singletonList(params));
            Wthaigd.LOG.info("执行 SQL: {}", sql);
            boolean resultIsRs = ps.execute(sql);
            if (resultIsRs) {
                return ps.getResultSet();
            }
            return ps.getUpdateCount();
        } catch (SQLException e) {
            Wthaigd.LOG.error("执行 SQL 失败: {}", sql, e);
        }
        return null;
    }

    /**
     * 执行多个 SQL 语句。
     *
     * @param sqlMap 表名与 SQL 语句列表的映射
     * @return 执行结果, 表名与结果列表的映射
     */
    private static Map<String, List<Object>> executeAllSqlInMap(Map<String, List<String>> sqlMap) {
        Map<String, List<Object>> result = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : sqlMap.entrySet()) {
            String tableName = entry.getKey();
            Wthaigd.LOG.info("表: {}", tableName);
            List<Object> resultList = new ArrayList<>();
            for (String sql : entry.getValue()) {
                resultList.add(executeSafeSQL(sql));
            }
            result.put(entry.getKey(), resultList);
        }
        return result;
    }

    // 工具方法：解包 SQLiteConnection
    private static SQLiteConnection unwrapConnection() {
        try {
            return inMemoryConnection.unwrap(SQLiteConnection.class);
        } catch (SQLException e) {
            throw new RuntimeException("解包连接失败", e);
        }
    }

    // 工具方法：设置 PreparedStatement 参数
    private static void setParameters(PreparedStatement ps, List<Object> params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
        }
    }
}
