package com.pinkyudeer.wthaigd.helper;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.sqlite.SQLiteConnection;

import com.pinkyudeer.wthaigd.core.Wthaigd;
import com.pinkyudeer.wthaigd.task.TaskSqlHelper;

import lombok.Getter;

/**
 * SQLite数据库管理辅助类。
 * 该类支持内存数据库创建、文件持久化、数据库初始化
 * 以及执行SQL查询或更新操作。
 */
public class SQLiteHelper {

    private static final String MEM_DB_URL = "jdbc:sqlite::memory:"; // 内存数据库
    private static Connection inMemoryConnection;
    private static final File DATABASE_FILE = ModFileHelper.getWorldFile("task.db", false)
        .getAbsoluteFile();
    public static boolean isWorldLoaded = false;

    /**
     * 初始化内存中的SQLite数据库。
     * 如果文件数据库存在，则将数据加载到内存中。
     * 否则，创建一个新的数据库并初始化。
     */
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
        Wthaigd.LOG.info("SQLite数据库初始化完成");
        isWorldLoaded = true;
    }

    /**
     * 通过创建表和添加默认条目来初始化内存数据库。
     * 然后将数据库内容保存到文件中。
     */
    private static void initializeDatabase() {
        Wthaigd.LOG.info("初始化SQLite数据库");

        executeAllSqlInMap(TaskSqlHelper.init.generateAllCreateTableSql());

        saveDataFromMemoryToFile();
    }

    /**
     * 执行Map中的所有SQL
     *
     * @param stringListMap 表名-在对应表中执行的不含参数的sql语句
     */
    private static void executeAllSqlInMap(Map<String, List<String>> stringListMap) {
        for (Map.Entry<String, List<String>> entry : stringListMap.entrySet()) {
            String tableName = entry.getKey();
            Wthaigd.LOG.info("表: {}", tableName);
            List<String> sqlList = entry.getValue();
            for (String sql : sqlList) {
                Wthaigd.LOG.info("执行SQL: {}", sql);
                try {
                    inMemoryConnection.createStatement()
                        .executeUpdate(sql);
                } catch (SQLException e) {
                    Wthaigd.LOG.error("执行SQL失败: {}", sql, e);
                }
            }
        }
    }

    /**
     * 将现有SQLite文件数据库的内容加载到内存数据库中。
     * 记录数据恢复过程的进度。
     */
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

    /**
     * 将内存中SQLite数据库的内容保存到磁盘上的物理文件中。
     * 确保目录结构存在并记录备份过程的进度。
     */
    public static void saveDataFromMemoryToFile() {
        if (!isWorldLoaded) return;
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

    /**
     * 在将数据保存到磁盘后关闭与内存中SQLite数据库的连接。
     * 确保没有资源泄漏并标记数据库为已关闭。
     */
    public static void close() {
        saveDataFromMemoryToFile();
        Wthaigd.LOG.info("关闭SQLite数据库连接");
        try {
            if (inMemoryConnection != null) {
                inMemoryConnection.close();
            }
        } catch (SQLException e) {
            Wthaigd.LOG.error("关闭SQLite数据库连接失败", e);
        }
        isWorldLoaded = false;
    }

    /**
     * Join子句构造器
     * 用于构建SQL查询的JOIN部分
     */
    public static class JoinBuilder {

        private final StringBuilder joinClause;
        @Getter
        private final List<Object> params;

        /**
         * 创建一个新的Join子句构造器
         */
        public JoinBuilder() {
            this.joinClause = new StringBuilder();
            this.params = new ArrayList<>();
        }

        /**
         * 添加INNER JOIN
         *
         * @param table     要连接的表名
         * @param condition 连接条件
         * @return 当前构造器
         */
        public JoinBuilder innerJoin(String table, String condition) {
            joinClause.append(" INNER JOIN ")
                .append(table)
                .append(" ON ")
                .append(condition);
            return this;
        }

        /**
         * 添加LEFT JOIN
         *
         * @param table     要连接的表名
         * @param condition 连接条件
         * @return 当前构造器
         */
        public JoinBuilder leftJoin(String table, String condition) {
            joinClause.append(" LEFT JOIN ")
                .append(table)
                .append(" ON ")
                .append(condition);
            return this;
        }

        /**
         * 添加RIGHT JOIN
         *
         * @param table     要连接的表名
         * @param condition 连接条件
         * @return 当前构造器
         */
        public JoinBuilder rightJoin(String table, String condition) {
            joinClause.append(" RIGHT JOIN ")
                .append(table)
                .append(" ON ")
                .append(condition);
            return this;
        }

        /**
         * 添加FULL JOIN
         *
         * @param table     要连接的表名
         * @param condition 连接条件
         * @return 当前构造器
         */
        public JoinBuilder fullJoin(String table, String condition) {
            joinClause.append(" FULL JOIN ")
                .append(table)
                .append(" ON ")
                .append(condition);
            return this;
        }

        /**
         * 添加带参数的INNER JOIN
         *
         * @param table     要连接的表名
         * @param condition 连接条件（使用?作为参数占位符）
         * @param values    条件参数值
         * @return 当前构造器
         */
        public JoinBuilder innerJoin(String table, String condition, Object... values) {
            joinClause.append(" INNER JOIN ")
                .append(table)
                .append(" ON ")
                .append(condition);
            addParams(values);
            return this;
        }

        /**
         * 添加带参数的LEFT JOIN
         *
         * @param table     要连接的表名
         * @param condition 连接条件（使用?作为参数占位符）
         * @param values    条件参数值
         * @return 当前构造器
         */
        public JoinBuilder leftJoin(String table, String condition, Object... values) {
            joinClause.append(" LEFT JOIN ")
                .append(table)
                .append(" ON ")
                .append(condition);
            addParams(values);
            return this;
        }

        /**
         * 添加带参数的RIGHT JOIN
         *
         * @param table     要连接的表名
         * @param condition 连接条件（使用?作为参数占位符）
         * @param values    条件参数值
         * @return 当前构造器
         */
        public JoinBuilder rightJoin(String table, String condition, Object... values) {
            joinClause.append(" RIGHT JOIN ")
                .append(table)
                .append(" ON ")
                .append(condition);
            addParams(values);
            return this;
        }

        /**
         * 添加带参数的FULL JOIN
         *
         * @param table     要连接的表名
         * @param condition 连接条件（使用?作为参数占位符）
         * @param values    条件参数值
         * @return 当前构造器
         */
        public JoinBuilder fullJoin(String table, String condition, Object... values) {
            joinClause.append(" FULL JOIN ")
                .append(table)
                .append(" ON ")
                .append(condition);
            addParams(values);
            return this;
        }

        /**
         * 添加自定义JOIN
         *
         * @param joinType  JOIN类型
         * @param table     要连接的表名
         * @param condition 连接条件
         * @param values    条件参数值
         * @return 当前构造器
         */
        public JoinBuilder customJoin(String joinType, String table, String condition, Object... values) {
            joinClause.append(" ")
                .append(joinType)
                .append(" JOIN ")
                .append(table)
                .append(" ON ")
                .append(condition);
            addParams(values);
            return this;
        }

        /**
         * 获取构建的JOIN子句
         */
        @Override
        public String toString() {
            return joinClause.toString();
        }

        /**
         * 添加参数的辅助方法
         */
        private void addParams(Object... values) {
            if (values != null) {
                Collections.addAll(params, values);
            }
        }
    }

    /**
     * Having子句构造器
     * 用于构建SQL查询的HAVING条件部分
     */
    public static class HavingBuilder {

        private final StringBuilder havingClause;
        @Getter
        private final List<Object> params;

        /**
         * 创建一个新的Having子句构造器
         */
        public HavingBuilder() {
            this.havingClause = new StringBuilder();
            this.params = new ArrayList<>();
        }

        /**
         * 添加相等条件
         *
         * @param field 字段名或聚合函数
         * @param value 比较值
         * @return 当前构造器
         */
        public HavingBuilder eq(String field, Object value) {
            addCondition(field, "=", value);
            return this;
        }

        /**
         * 添加不等条件
         *
         * @param field 字段名或聚合函数
         * @param value 比较值
         * @return 当前构造器
         */
        public HavingBuilder notEq(String field, Object value) {
            addCondition(field, "<>", value);
            return this;
        }

        /**
         * 添加大于条件
         *
         * @param field 字段名或聚合函数
         * @param value 比较值
         * @return 当前构造器
         */
        public HavingBuilder gt(String field, Object value) {
            addCondition(field, ">", value);
            return this;
        }

        /**
         * 添加大于等于条件
         *
         * @param field 字段名或聚合函数
         * @param value 比较值
         * @return 当前构造器
         */
        public HavingBuilder gte(String field, Object value) {
            addCondition(field, ">=", value);
            return this;
        }

        /**
         * 添加小于条件
         *
         * @param field 字段名或聚合函数
         * @param value 比较值
         * @return 当前构造器
         */
        public HavingBuilder lt(String field, Object value) {
            addCondition(field, "<", value);
            return this;
        }

        /**
         * 添加小于等于条件
         *
         * @param field 字段名或聚合函数
         * @param value 比较值
         * @return 当前构造器
         */
        public HavingBuilder lte(String field, Object value) {
            addCondition(field, "<=", value);
            return this;
        }

        /**
         * 添加LIKE条件
         *
         * @param field   字段名或聚合函数
         * @param pattern 匹配模式
         * @return 当前构造器
         */
        public HavingBuilder like(String field, String pattern) {
            addCondition(field, "LIKE", pattern);
            return this;
        }

        /**
         * 添加IN条件
         *
         * @param field  字段名或聚合函数
         * @param values 值集合
         * @return 当前构造器
         */
        public HavingBuilder in(String field, Collection<?> values) {
            if (values == null || values.isEmpty()) {
                return this;
            }

            if (!havingClause.toString()
                .isEmpty()) {
                havingClause.append(" AND ");
            }

            havingClause.append(field)
                .append(" IN (");
            StringJoiner placeholders = new StringJoiner(", ");
            for (int i = 0; i < values.size(); i++) {
                placeholders.add("?");
            }
            havingClause.append(placeholders)
                .append(")");

            params.addAll(values);
            return this;
        }

        /**
         * 添加AND条件组
         *
         * @param builder 另一个Having构造器
         * @return 当前构造器
         */
        public HavingBuilder and(HavingBuilder builder) {
            if (builder != null && !builder.toString()
                .isEmpty()) {
                if (!havingClause.toString()
                    .isEmpty()) {
                    havingClause.append(" AND ");
                }
                havingClause.append("(")
                    .append(builder)
                    .append(")");
                params.addAll(builder.getParams());
            }
            return this;
        }

        /**
         * 添加OR条件组
         *
         * @param builder 另一个Having构造器
         * @return 当前构造器
         */
        public HavingBuilder or(HavingBuilder builder) {
            if (builder != null && !builder.toString()
                .isEmpty()) {
                if (!havingClause.toString()
                    .isEmpty()) {
                    havingClause.append(" OR ");
                }
                havingClause.append("(")
                    .append(builder)
                    .append(")");
                params.addAll(builder.getParams());
            }
            return this;
        }

        /**
         * 添加自定义条件
         *
         * @param condition 自定义条件字符串
         * @param values    条件参数值
         * @return 当前构造器
         */
        public HavingBuilder custom(String condition, Object... values) {
            if (condition == null || condition.isEmpty()) {
                return this;
            }

            if (!havingClause.toString()
                .isEmpty()) {
                havingClause.append(" AND ");
            }

            havingClause.append(condition);
            if (values != null) {
                Collections.addAll(params, values);
            }

            return this;
        }

        /**
         * 获取构建的HAVING子句
         */
        @Override
        public String toString() {
            return havingClause.toString();
        }

        /**
         * 添加条件的辅助方法
         */
        private void addCondition(String field, String operator, Object value) {
            if (!havingClause.toString()
                .isEmpty()) {
                havingClause.append(" AND ");
            }
            havingClause.append(field)
                .append(" ")
                .append(operator)
                .append(" ?");
            params.add(value);
        }
    }

    /**
     * Where子句构造器
     * 用于构建SQL查询的WHERE条件部分
     */
    public static class WhereBuilder {

        private final StringBuilder whereClause;
        @Getter
        private final List<Object> params;

        /**
         * 创建一个新的Where子句构造器
         */
        public WhereBuilder() {
            this.whereClause = new StringBuilder();
            this.params = new ArrayList<>();
        }

        /**
         * 添加相等条件
         *
         * @param field 字段名
         * @param value 比较值
         * @return 当前构造器
         */
        public WhereBuilder eq(String field, Object value) {
            addCondition(field, "=", value);
            return this;
        }

        /**
         * 添加不等条件
         *
         * @param field 字段名
         * @param value 比较值
         * @return 当前构造器
         */
        public WhereBuilder notEq(String field, Object value) {
            addCondition(field, "<>", value);
            return this;
        }

        /**
         * 添加大于条件
         *
         * @param field 字段名
         * @param value 比较值
         * @return 当前构造器
         */
        public WhereBuilder gt(String field, Object value) {
            addCondition(field, ">", value);
            return this;
        }

        /**
         * 添加小于条件
         *
         * @param field 字段名
         * @param value 比较值
         * @return 当前构造器
         */
        public WhereBuilder lt(String field, Object value) {
            addCondition(field, "<", value);
            return this;
        }

        /**
         * 添加大于等于条件
         *
         * @param field 字段名
         * @param value 比较值
         * @return 当前构造器
         */
        public WhereBuilder gte(String field, Object value) {
            addCondition(field, ">=", value);
            return this;
        }

        /**
         * 添加小于等于条件
         *
         * @param field 字段名
         * @param value 比较值
         * @return 当前构造器
         */
        public WhereBuilder lte(String field, Object value) {
            addCondition(field, "<=", value);
            return this;
        }

        /**
         * 添加LIKE条件
         *
         * @param field   字段名
         * @param pattern 匹配模式
         * @return 当前构造器
         */
        public WhereBuilder like(String field, String pattern) {
            addCondition(field, "LIKE", pattern);
            return this;
        }

        /**
         * 添加IN条件
         *
         * @param field  字段名
         * @param values 值集合
         * @return 当前构造器
         */
        public WhereBuilder in(String field, Object... values) {
            if (values == null || values.length == 0) {
                return this;
            }

            if (!whereClause.toString()
                .isEmpty()) {
                whereClause.append(" AND ");
            }

            whereClause.append(field)
                .append(" IN (");
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    whereClause.append(", ");
                }
                whereClause.append("?");
                params.add(values[i]);
            }
            whereClause.append(")");

            return this;
        }

        /**
         * 添加IS NULL条件
         *
         * @param field 字段名
         * @return 当前构造器
         */
        public WhereBuilder isNull(String field) {
            if (!whereClause.toString()
                .isEmpty()) {
                whereClause.append(" AND ");
            }
            whereClause.append(field)
                .append(" IS NULL");
            return this;
        }

        /**
         * 添加IS NOT NULL条件
         *
         * @param field 字段名
         * @return 当前构造器
         */
        public WhereBuilder isNotNull(String field) {
            if (!whereClause.toString()
                .isEmpty()) {
                whereClause.append(" AND ");
            }
            whereClause.append(field)
                .append(" IS NOT NULL");
            return this;
        }

        /**
         * 添加AND连接的条件组
         *
         * @param builder 嵌套的Where构造器
         * @return 当前构造器
         */
        public WhereBuilder and(WhereBuilder builder) {
            if (builder != null && !builder.toString()
                .isEmpty()) {
                if (!whereClause.toString()
                    .isEmpty()) {
                    whereClause.append(" AND ");
                }
                whereClause.append("(")
                    .append(builder)
                    .append(")");
                params.addAll(builder.getParams());
            }
            return this;
        }

        /**
         * 添加OR连接的条件组
         *
         * @param builder 嵌套的Where构造器
         * @return 当前构造器
         */
        public WhereBuilder or(WhereBuilder builder) {
            if (builder != null && !builder.toString()
                .isEmpty()) {
                if (!whereClause.toString()
                    .isEmpty()) {
                    whereClause.append(" OR ");
                }
                whereClause.append("(")
                    .append(builder)
                    .append(")");
                params.addAll(builder.getParams());
            }
            return this;
        }

        /**
         * 添加自定义条件
         *
         * @param condition 自定义条件字符串
         * @param values    条件参数值
         * @return 当前构造器
         */
        public WhereBuilder custom(String condition, Object... values) {
            if (condition == null || condition.isEmpty()) {
                return this;
            }

            if (!whereClause.toString()
                .isEmpty()) {
                whereClause.append(" AND ");
            }

            whereClause.append(condition);
            if (values != null) {
                Collections.addAll(params, values);
            }

            return this;
        }

        /**
         * 获取构建的WHERE子句
         */
        @Override
        public String toString() {
            return whereClause.toString();
        }

        /**
         * 添加条件的辅助方法
         */
        private void addCondition(String field, String operator, Object value) {
            if (!whereClause.toString()
                .isEmpty()) {
                whereClause.append(" AND ");
            }
            whereClause.append(field)
                .append(" ")
                .append(operator)
                .append(" ?");
            params.add(value);
        }
    }

    /**
     * 构建并完成增删改SQL操作
     */
    public static class SqlBuild {

        private static String sql;
        private static List<Object> params;
        protected String tableName;
        protected Map<String, Object> valueMap;
        protected String whereClause;
        protected String havingClause;
        protected String joinClause;
        protected String[] orderByFields;
        protected boolean isAscending = true;
        protected String[] groupByFields;
        protected int limit = -1;
        protected int offset = 0;

        /**
         * 创建插入操作构建器
         *
         * @return Insert构建器
         */
        public static Insert Insert() {
            return new Insert();
        }

        /**
         * 创建更新操作构建器
         *
         * @return Update构建器
         */
        public static Update Update() {
            return new Update();
        }

        /**
         * 创建删除操作构建器
         *
         * @return Delete构建器
         */
        public static Delete Delete() {
            return new Delete();
        }

        /**
         * 创建查询操作构建器
         *
         * @return Select构建器
         */
        public static Select Select() {
            return new Select();
        }

        /**
         * 执行SQL操作
         *
         * @return 对于查询操作返回结果集，对于更新操作返回影响的行数
         */
        protected Object execute() {
            if (sql == null || sql.isEmpty()) {
                throw new IllegalStateException("SQL语句未构建");
            }

            Wthaigd.LOG.info("执行SQL: {}", sql);
            Wthaigd.LOG.info("参数: {}", params);

            PreparedStatement preparedStatement;
            try {
                preparedStatement = inMemoryConnection.prepareStatement(sql);
                for (int i = 0; i < params.size(); i++) {
                    preparedStatement.setObject(i + 1, params.get(i));
                }
                return preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 插入操作构建器
         */
        public static class Insert extends SqlBuild {

            private Insert() {
                super();
            }

            /**
             * 指定要插入的表名
             *
             * @param tableName 表名
             * @return 当前构建器
             */
            public Insert Into(String tableName) {
                this.tableName = tableName;
                return this;
            }

            /**
             * 设置要插入的字段和值
             *
             * @param values 字段-值映射
             * @return 当前构建器
             */
            public Insert Values(Map<String, Object> values) {
                this.valueMap.putAll(values);
                return this;
            }

            /**
             * 执行插入操作
             *
             * @return 影响的行数
             */
            public int start() {
                if (tableName == null || tableName.isEmpty()) {
                    throw new IllegalStateException("表名未指定");
                }
                if (valueMap.isEmpty()) {
                    throw new IllegalStateException("未指定要插入的值");
                }

                StringJoiner columns = new StringJoiner(", ", "(", ")");
                StringJoiner placeholders = new StringJoiner(", ", "(", ")");

                for (String column : valueMap.keySet()) {
                    columns.add(column);
                    placeholders.add("?");
                    params.add(valueMap.get(column));
                }

                sql = String.format("INSERT INTO %s %s VALUES %s", tableName, columns, placeholders);

                return (int) execute();
            }
        }

        /**
         * 更新操作构建器
         */
        public static class Update extends SqlBuild {

            private Update() {
                super();
            }

            /**
             * 指定要更新的表名
             *
             * @param tableName 表名
             * @return 当前构建器
             */
            public Update Table(String tableName) {
                this.tableName = tableName;
                return this;
            }

            /**
             * 设置要更新的字段和值
             *
             * @param values 字段-值映射
             * @return 当前构建器
             */
            public Update Set(Map<String, Object> values) {
                this.valueMap.putAll(values);
                return this;
            }

            /**
             * 设置更新条件
             *
             * @param whereClause WHERE子句
             * @return 当前构建器
             */
            public Update Where(String whereClause) {
                this.whereClause = whereClause;
                return this;
            }

            /**
             * 执行更新操作
             *
             * @return 影响的行数
             */
            public int start() {
                if (tableName == null || tableName.isEmpty()) {
                    throw new IllegalStateException("表名未指定");
                }
                if (valueMap.isEmpty()) {
                    throw new IllegalStateException("未指定要更新的值");
                }

                StringBuilder setClause = new StringBuilder();
                boolean first = true;

                for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                    if (!first) {
                        setClause.append(", ");
                    }
                    setClause.append(entry.getKey())
                        .append(" = ?");
                    params.add(entry.getValue());
                    first = false;
                }

                sql = String.format("UPDATE %s SET %s", tableName, setClause);

                if (whereClause != null && !whereClause.isEmpty()) {
                    sql += " WHERE " + whereClause;
                }

                return (int) execute();
            }
        }

        /**
         * 删除操作构建器
         */
        public static class Delete extends SqlBuild {

            private Delete() {
                super();
            }

            /**
             * 指定要删除数据的表名
             *
             * @param tableName 表名
             * @return 当前构建器
             */
            public Delete From(String tableName) {
                this.tableName = tableName;
                return this;
            }

            /**
             * 设置删除条件
             *
             * @param whereClause WHERE子句
             * @return 当前构建器
             */
            public Delete Where(String whereClause) {
                this.whereClause = whereClause;
                return this;
            }

            /**
             * 执行删除操作
             *
             * @return 影响的行数
             */
            public int start() {
                if (tableName == null || tableName.isEmpty()) {
                    throw new IllegalStateException("表名未指定");
                }

                sql = "DELETE FROM " + tableName;

                if (whereClause != null && !whereClause.isEmpty()) {
                    sql += " WHERE " + whereClause;
                }

                return (int) execute();
            }
        }

        /**
         * 查询操作构建器
         */
        public static class Select extends SqlBuild {

            private String[] selectColumns;

            private Select() {
                super();
                this.selectColumns = new String[] { "*" };
            }

            /**
             * 指定要查询的列
             *
             * @param columns 列名数组
             * @return 当前构建器
             */
            public Select Columns(String... columns) {
                if (columns != null && columns.length > 0) {
                    this.selectColumns = columns;
                }
                return this;
            }

            /**
             * 指定查询的表名
             *
             * @param tableName 表名
             * @return 当前构建器
             */
            public Select From(String tableName) {
                this.tableName = tableName;
                return this;
            }

            /**
             * 设置查询条件
             *
             * @param whereClause WHERE子句
             * @return 当前构建器
             */
            public Select Where(String whereClause) {
                this.whereClause = whereClause;
                return this;
            }

            /**
             * 设置排序字段
             *
             * @param fields    排序字段数组
             * @param ascending 是否升序排列
             * @return 当前构建器
             */
            public Select OrderBy(String[] fields, boolean ascending) {
                this.orderByFields = fields;
                this.isAscending = ascending;
                return this;
            }

            /**
             * 设置分组字段
             *
             * @param fields 分组字段数组
             * @return 当前构建器
             */
            public Select GroupBy(String[] fields) {
                this.groupByFields = fields;
                return this;
            }

            /**
             * 设置结果限制
             *
             * @param limit  限制数量
             * @param offset 偏移量
             * @return 当前构建器
             */
            public Select Limit(int limit, int offset) {
                this.limit = limit;
                this.offset = offset;
                return this;
            }

            /**
             * 设置HAVING子句
             *
             * @param havingClause HAVING子句
             * @return 当前构建器
             */
            public Select Having(String havingClause) {
                this.havingClause = havingClause;
                return this;
            }

            /**
             * 设置JOIN子句
             *
             * @param joinClause JOIN子句
             * @return 当前构建器
             */
            public Select Join(String joinClause) {
                this.joinClause = joinClause;
                return this;
            }

            /**
             * 执行查询操作
             *
             * @return 查询结果列表
             */
            @SuppressWarnings("unchecked")
            public List<Map<String, Object>> start() {
                if (tableName == null || tableName.isEmpty()) {
                    throw new IllegalStateException("表名未指定");
                }

                StringBuilder query = new StringBuilder("SELECT ");
                query.append(String.join(", ", selectColumns));
                query.append(" FROM ")
                    .append(tableName);

                if (joinClause != null && !joinClause.isEmpty()) {
                    query.append(" ")
                        .append(joinClause);
                }

                if (whereClause != null && !whereClause.isEmpty()) {
                    query.append(" WHERE ")
                        .append(whereClause);
                }

                if (groupByFields != null && groupByFields.length > 0) {
                    query.append(" GROUP BY ")
                        .append(String.join(", ", groupByFields));
                }

                if (havingClause != null && !havingClause.isEmpty()) {
                    query.append(" HAVING ")
                        .append(havingClause);
                }

                if (orderByFields != null && orderByFields.length > 0) {
                    query.append(" ORDER BY ")
                        .append(String.join(", ", orderByFields));
                    query.append(isAscending ? " ASC" : " DESC");
                }

                if (limit > 0) {
                    query.append(" LIMIT ")
                        .append(limit);
                    if (offset > 0) {
                        query.append(" OFFSET ")
                            .append(offset);
                    }
                }

                sql = query.toString();

                return (List<Map<String, Object>>) execute();
            }
        }

    }

    public static void executeSafeSQL(String sql) {
        try {
            // noinspection SqlSourceToSinkFlow
            inMemoryConnection.createStatement()
                .executeUpdate(sql);
        } catch (SQLException e) {
            Wthaigd.LOG.error("执行SQL失败: {}", sql, e);
        }
    }

}
