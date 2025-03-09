package com.pinkyudeer.wthaigd.helper.dataBase;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.pinkyudeer.wthaigd.helper.UtilHelper;
import com.pinkyudeer.wthaigd.helper.dataBase.annotation.Column;
import com.pinkyudeer.wthaigd.helper.dataBase.annotation.FieldCheck;
import com.pinkyudeer.wthaigd.helper.dataBase.annotation.Reference;
import com.pinkyudeer.wthaigd.helper.dataBase.annotation.Table;

import lombok.Getter;

/**
 * SQL语句构建器。
 * 提供增删改查SQL构建并执行功能。
 * 支持单表和多表操作，包括：
 * <ul>
 * <li>表的创建和管理</li>
 * <li>数据的插入、更新、删除和查询</li>
 * <li>复杂条件查询</li>
 * <li>表关联查询</li>
 * <li>索引管理</li>
 * <li>外键约束</li>
 * </ul>
 *
 * @author pinkyudeer
 * @version 1.0
 */
public class SQLHelper {

    /**
     * 创建单个表的DDL构建器。
     *
     * @param entityClass 实体类
     * @param <T>         实体类型
     * @return DDL构建器实例
     */
    public static <T> DDLBuilder<T> createTable(Class<T> entityClass) {
        return new DDLBuilder<>(entityClass);
    }

    /**
     * 创建多个表的DDL构建器。
     *
     * @param entityClasses 实体类集合
     * @return DDL构建器实例
     */
    public static DDLBuilder<?> createTables(Collection<Class<?>> entityClasses) {
        return new DDLBuilder<>(null).addAll(entityClasses);
    }

    /**
     * 创建插入操作构建器。
     *
     * @param entity 要插入的实体对象
     * @param <T>    实体类型
     * @return 插入构建器实例
     */
    public static <T> InsertBuilder<T> insert(T entity) {
        return new InsertBuilder<>(entity);
    }

    /**
     * 创建删除操作构建器。
     *
     * @param entity 要删除的实体对象
     * @param <T>    实体类型
     * @return 删除构建器实例
     */
    public static <T> DeleteBuilder<T> delete(T entity) {
        return new DeleteBuilder<>(entity);
    }

    /**
     * 创建更新操作构建器。
     *
     * @param entity 要更新的实体对象
     * @param <T>    实体类型
     * @return 更新构建器实例
     */
    public static <T> UpdateBuilder<T> update(T entity) {
        return new UpdateBuilder<>(entity);
    }

    /**
     * 创建查询操作构建器。
     *
     * @param entity 查询的实体对象
     * @param <T>    实体类型
     * @return 查询构建器实例
     */
    public static <T> SelectBuilder<T> select(T entity) {
        return new SelectBuilder<>(entity);
    }

    /**
     * SQL操作符枚举。
     * 定义了常用的SQL比较操作符。
     */
    @Getter
    public enum Operator {

        /** 等于 */
        EQ("="),
        /** 不等于 */
        NE("<>"),
        /** 大于 */
        GT(">"),
        /** 大于等于 */
        GE(">="),
        /** 小于 */
        LT("<"),
        /** 小于等于 */
        LE("<="),
        /** 模糊匹配 */
        LIKE("LIKE"),
        /** 包含于 */
        IN("IN"),
        /** 不包含于 */
        NOT_IN("NOT IN"),
        /** 为空 */
        IS_NULL("IS NULL"),
        /** 不为空 */
        IS_NOT_NULL("IS NOT NULL");

        /** 操作符符号 */
        private final String symbol;

        /**
         * 构造函数。
         *
         * @param symbol 操作符符号
         */
        Operator(String symbol) {
            this.symbol = symbol;
        }
    }

    /**
     * 插入操作构建器。
     * 用于构建和执行SQL INSERT语句。
     *
     * @param <T> 实体类型
     */
    public static class InsertBuilder<T> extends BaseBuilder<T, InsertBuilder<T>> {

        /**
         * 构造函数。
         *
         * @param entity 要插入的实体对象
         */
        private InsertBuilder(T entity) {
            super(entity);
        }

        /**
         * 执行插入操作。
         *
         * @return 受影响的行数
         */
        @Override
        public Integer execute() {
            Map<String, Object> values = getColumnValues();
            StringJoiner columns = new StringJoiner(", ", "(", ")");
            StringJoiner placeholders = new StringJoiner(", ", "(", ")");
            List<Object> params = new ArrayList<>();

            for (Map.Entry<String, Object> entry : values.entrySet()) {
                columns.add(entry.getKey());
                placeholders.add("?");
                params.add(entry.getValue());
            }

            String sql = String.format("INSERT INTO %s %s VALUES %s", getTableName(), columns, placeholders);
            return (Integer) SQLiteManager.executeSafeSQL(sql, params);
        }
    }

    /**
     * 删除操作构建器。
     * 用于构建和执行SQL DELETE语句。
     *
     * @param <T> 实体类型
     */
    public static class DeleteBuilder<T> extends BaseBuilder<T, DeleteBuilder<T>> {

        private DeleteBuilder(T entity) {
            super(entity);
        }

        @Override
        public Integer execute() {
            String sql = "DELETE FROM " + getTableName();
            List<Object> executeParams = new ArrayList<>();

            sql = addWhereClause(sql, executeParams, true, "删除");

            return (Integer) SQLiteManager.executeSafeSQL(sql, executeParams);
        }
    }

    /**
     * 更新操作构建器。
     * 用于构建和执行SQL UPDATE语句。
     *
     * @param <T> 实体类型
     */
    public static class UpdateBuilder<T> extends BaseBuilder<T, UpdateBuilder<T>> {

        private UpdateBuilder(T entity) {
            super(entity);
        }

        @Override
        public Integer execute() {
            Map<String, Object> values = getColumnValues();
            StringBuilder setClause = new StringBuilder();
            List<Object> executeParams = new ArrayList<>();
            boolean first = true;

            for (Map.Entry<String, Object> entry : values.entrySet()) {
                if (!first) setClause.append(", ");
                setClause.append(entry.getKey())
                    .append(" = ?");
                executeParams.add(entry.getValue());
                first = false;
            }

            String sql = String.format("UPDATE %s SET %s", getTableName(), setClause);

            sql = addWhereClause(sql, executeParams, true, "更新");

            return (Integer) SQLiteManager.executeSafeSQL(sql, executeParams);
        }
    }

    /**
     * 查询操作构建器。
     * 用于构建和执行SQL SELECT语句，支持复杂的查询条件、连接、分组和排序。
     *
     * @param <T> 实体类型
     */
    public static class SelectBuilder<T> extends BaseBuilder<T, SelectBuilder<T>> {

        /**
         * 构造函数。
         *
         * @param entity 查询的实体对象
         */
        private SelectBuilder(T entity) {
            super(entity);
        }

        private String[] selectColumns;
        private String joins = "";
        private String[] groupByFields;
        private String havingClause = "";
        private final List<Object> havingParams = new ArrayList<>();
        private String[] orderByFields;
        private boolean isAscending = true;
        private int limit = 0;
        private int offset = 0;

        /**
         * 设置要查询的列。
         *
         * @param fields 要查询的字段数组
         * @return 当前构建器实例
         */
        public SelectBuilder<T> columns(Field... fields) {
            this.selectColumns = Arrays.stream(fields)
                .map(this::getFullColumnName)
                .toArray(String[]::new);
            return this;
        }

        /**
         * 添加表连接。
         *
         * @param joinTable 连接的表类
         * @param fromField 当前表的字段
         * @param toField   连接表的字段
         * @return 当前构建器实例
         */
        public SelectBuilder<T> join(Class<?> joinTable, Field fromField, Field toField) {
            Table joinTableAnno = joinTable.getAnnotation(Table.class);
            if (joinTableAnno == null) {
                throw new IllegalArgumentException("连接表必须使用@Table注解");
            }

            String fromColumnName = getFullColumnName(fromField);
            String toColumnName = joinTableAnno.name() + "." + getColumnName(toField);

            this.joins += String.format(" JOIN %s ON %s = %s", joinTableAnno.name(), fromColumnName, toColumnName);
            return this;
        }

        /**
         * 设置分组字段。
         *
         * @param fields 分组字段数组
         * @return 当前构建器实例
         */
        public SelectBuilder<T> groupBy(Field... fields) {
            this.groupByFields = Arrays.stream(fields)
                .map(this::getFullColumnName)
                .toArray(String[]::new);
            return this;
        }

        /**
         * 设置排序字段和方向。
         *
         * @param ascending 是否升序
         * @param fields    排序字段数组
         * @return 当前构建器实例
         */
        public SelectBuilder<T> orderBy(boolean ascending, Field... fields) {
            this.orderByFields = Arrays.stream(fields)
                .map(this::getFullColumnName)
                .toArray(String[]::new);
            this.isAscending = ascending;
            return this;
        }

        /**
         * 设置HAVING子句。
         *
         * @param clause HAVING条件
         * @param params 条件参数
         * @return 当前构建器实例
         */
        public SelectBuilder<T> having(String clause, Object... params) {
            this.havingClause = clause;
            if (params != null) {
                this.havingParams.addAll(Arrays.asList(params));
            }
            return this;
        }

        /**
         * 设置结果集限制。
         *
         * @param limit 限制数量
         * @return 当前构建器实例
         */
        public SelectBuilder<T> limit(int limit) {
            this.limit = limit;
            return this;
        }

        /**
         * 设置结果集偏移量。
         *
         * @param offset 偏移量
         * @return 当前构建器实例
         */
        public SelectBuilder<T> offset(int offset) {
            this.offset = offset;
            return this;
        }

        /**
         * 执行查询操作。
         *
         * @return 查询结果数量
         */
        @Override
        public ResultSet execute() {
            StringBuilder query = new StringBuilder("SELECT ");
            query.append(selectColumns == null || selectColumns.length == 0 ? "*" : String.join(", ", selectColumns));
            query.append(" FROM ")
                .append(getTableName());

            if (!joins.isEmpty()) query.append(joins);

            List<Object> executeParams = new ArrayList<>();
            query = new StringBuilder(addWhereClause(query.toString(), executeParams, false, "查询"));

            if (groupByFields != null && groupByFields.length > 0) query.append(" GROUP BY ")
                .append(String.join(", ", groupByFields));
            if (!havingClause.isEmpty()) {
                query.append(" HAVING ")
                    .append(havingClause);
                executeParams.addAll(havingParams);
            }
            if (orderByFields != null && orderByFields.length > 0) {
                query.append(" ORDER BY ")
                    .append(String.join(", ", orderByFields))
                    .append(isAscending ? " ASC" : " DESC");
            }
            if (limit > 0) {
                query.append(" LIMIT ")
                    .append(limit);
                if (offset > 0) query.append(" OFFSET ")
                    .append(offset);
            }

            return (ResultSet) SQLiteManager.executeSafeSQL(query.toString(), executeParams);
        }
    }

    /**
     * 基础构建器抽象类。
     * 提供通用的SQL构建功能，包括条件构建、参数管理等。
     *
     * @param <T> 实体类型
     * @param <B> 构建器类型
     */
    private abstract static class BaseBuilder<T, B extends BaseBuilder<T, B>> {

        protected final T entity;
        protected String whereClause = "";
        protected List<Object> params = new ArrayList<>();
        protected boolean usePrimaryKey = false;

        // 条件组类
        protected static class WhereGroup {

            private String clause = "";
            private final List<Object> groupParams = new ArrayList<>();
            private final List<WhereGroup> subGroups = new ArrayList<>();
            private boolean isOr = false; // 标识是否为OR连接

            public void addCondition(String condition, Object... params) {
                if (clause.isEmpty()) {
                    clause = condition;
                } else {
                    clause += " AND " + condition;
                }
                if (params != null) {
                    groupParams.addAll(Arrays.asList(params));
                }
            }

            public void addSubGroup(WhereGroup group) {
                subGroups.add(group);
            }

            /**
             * 构建WHERE子句并收集所有参数
             *
             * @param clauseResult 存储WHERE子句的StringBuilder
             * @param paramsResult 存储参数的列表
             */
            public void buildClauseAndCollectParams(StringBuilder clauseResult, List<Object> paramsResult) {
                // 添加当前组的条件
                if (!clause.isEmpty()) {
                    clauseResult.append(clause);
                }

                // 添加当前组的参数
                paramsResult.addAll(groupParams);

                // 添加子组
                for (WhereGroup subGroup : subGroups) {
                    if (clauseResult.length() > 0) {
                        clauseResult.append(subGroup.isOr ? " OR " : " AND ");
                    }

                    clauseResult.append("(");
                    StringBuilder subClause = new StringBuilder();
                    subGroup.buildClauseAndCollectParams(subClause, paramsResult);
                    clauseResult.append(subClause);
                    clauseResult.append(")");
                }
            }
        }

        protected WhereGroup currentGroup = new WhereGroup();

        /**
         * 开始一个新的条件组。
         *
         * @param isOr 是否为OR连接（true为OR，false为AND）
         * @return 当前构建器实例
         */
        public B beginGroup(boolean isOr) {
            WhereGroup newGroup = new WhereGroup();
            newGroup.isOr = isOr;
            currentGroup.addSubGroup(newGroup);
            currentGroup = newGroup;
            return self();
        }

        /**
         * 开始一个新的AND条件组。
         *
         * @return 当前构建器实例
         */
        public B beginGroup() {
            return beginGroup(false);
        }

        /**
         * 开始一个新的OR条件组。
         *
         * @return 当前构建器实例
         */
        public B orBeginGroup() {
            return beginGroup(true);
        }

        /**
         * 结束当前条件组。
         *
         * @return 当前构建器实例
         */
        public B endGroup() {
            // 返回到父组
            WhereGroup parent = findParentGroup(this.currentGroup);
            if (parent != null) {
                currentGroup = parent;
            }
            return self();
        }

        // 查找父组
        private WhereGroup findParentGroup(WhereGroup current) {
            for (WhereGroup subGroup : currentGroup.subGroups) {
                if (subGroup == current) {
                    return currentGroup;
                }
            }
            return null;
        }

        protected BaseBuilder(T entity) {
            this.entity = entity;
        }

        @SuppressWarnings("unchecked")
        protected B self() {
            return (B) this;
        }

        protected String getTableName() {
            Table table = entity.getClass()
                .getAnnotation(Table.class);
            if (table == null) {
                throw new IllegalArgumentException("类必须使用@Table注解");
            }
            return table.name();
        }

        protected Map<String, Object> getColumnValues() {
            Map<String, Object> values = new HashMap<>();
            for (Field field : entity.getClass()
                .getDeclaredFields()) {
                Column column = field.getAnnotation(Column.class);
                if (column != null) {
                    field.setAccessible(true);
                    try {
                        Object value = field.get(entity);
                        if (value != null) {
                            values.put(column.name(), value);
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return values;
        }

        /**
         * 获取字段对应的列名，根据needFullName参数决定是否返回完整列名（表名.列名）
         *
         * @param field        字段
         * @param needFullName 是否需要完整列名
         * @return 列名或完整列名
         */
        protected String getColumnName(Field field, boolean needFullName) {
            Column column = field.getAnnotation(Column.class);
            if (column == null) {
                throw new IllegalArgumentException("字段必须使用@Column注解: " + field.getName());
            }
            String columnName = column.name();

            if (needFullName) {
                String tableName = getTableName();
                return tableName + "." + columnName;
            }

            return columnName;
        }

        /**
         * 获取字段对应的列名
         */
        protected String getColumnName(Field field) {
            return getColumnName(field, false);
        }

        /**
         * 获取字段对应的完整列名（表名.列名）
         */
        protected String getFullColumnName(Field field) {
            return getColumnName(field, true);
        }

        // 获取类中所有带Column注解的字段
        protected List<Field> getColumnFields(Class<?> clazz) {
            return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Column.class))
                .collect(Collectors.toList());
        }

        /**
         * 添加条件查询方法，包括IN、NOT IN、LIKE等各种操作符的便捷方法
         */
        public B where(Field field, Operator operator, Object value) {
            String columnName = getFullColumnName(field);
            String whereCondition;
            List<Object> conditionParams = new ArrayList<>();

            // 特殊处理 IS NULL 和 IS NOT NULL
            if (operator == Operator.IS_NULL || operator == Operator.IS_NOT_NULL) {
                if (value != null) {
                    throw new IllegalArgumentException(operator.name() + " 操作符不需要值参数");
                }
                whereCondition = columnName + " " + operator.getSymbol();
            }
            // 特殊处理 IN 和 NOT IN
            else if (operator == Operator.IN || operator == Operator.NOT_IN) {
                if (!(value instanceof List<?>values)) {
                    throw new IllegalArgumentException(operator.name() + " 操作符需要List类型的参数");
                }
                if (values.isEmpty()) {
                    throw new IllegalArgumentException(operator.name() + " 操作符的值列表不能为空");
                }
                String placeholders = String.join(", ", Collections.nCopies(values.size(), "?"));
                whereCondition = columnName + " " + operator.getSymbol() + " (" + placeholders + ")";
                conditionParams.addAll(values);
            }
            // 处理普通操作符
            else {
                if (value == null) {
                    throw new IllegalArgumentException("值参数不能为null，请使用IS NULL或IS NOT NULL");
                }
                whereCondition = columnName + " " + operator.getSymbol() + " ?";
                conditionParams.add(value);
            }

            currentGroup.addCondition(whereCondition, conditionParams.toArray());
            return self();
        }

        /**
         * 添加OR WHERE条件。
         *
         * @param field    字段
         * @param operator 操作符
         * @param value    值
         * @return 当前构建器实例
         */
        public B orWhere(Field field, Operator operator, Object value) {
            // 保存当前的where子句和参数
            String currentWhereClause = whereClause;
            List<Object> currentParams = new ArrayList<>(params);

            // 清空where子句和参数，以便构建新条件
            whereClause = "";
            params.clear();

            // 构建新条件
            where(field, operator, value);

            // 合并条件
            if (!currentWhereClause.isEmpty()) {
                whereClause = "(" + currentWhereClause + ") OR (" + whereClause + ")";
                params.addAll(0, currentParams); // 在开头添加之前的参数
            }

            return self();
        }

        // 简化的条件方法，全部使用where方法实现
        public B whereIn(Field field, List<?> values) {
            return where(field, Operator.IN, values);
        }

        public B whereNotIn(Field field, List<?> values) {
            return where(field, Operator.NOT_IN, values);
        }

        public B whereLike(Field field, String pattern) {
            return where(field, Operator.LIKE, pattern);
        }

        public B whereNull(Field field) {
            return where(field, Operator.IS_NULL, null);
        }

        public B whereNotNull(Field field) {
            return where(field, Operator.IS_NOT_NULL, null);
        }

        /**
         * 构建WHERE子句。
         *
         * @return WHERE子句
         */
        protected String buildWhereClause() {
            StringBuilder whereClauseBuilder = new StringBuilder();
            params = new ArrayList<>();
            currentGroup.buildClauseAndCollectParams(whereClauseBuilder, params);
            whereClause = whereClauseBuilder.toString();
            return whereClause;
        }

        /**
         * 处理WHERE条件并添加到SQL语句和参数中
         *
         * @param sql           当前的SQL语句
         * @param executeParams 执行参数列表，供函数添加参数
         * @param requireWhere  是否要求必须有条件（例如UPDATE和DELETE通常需要条件）
         * @param operationName 操作名称，仅在抛出异常时使用
         * @return 添加了WHERE子句的SQL语句
         */
        protected String addWhereClause(String sql, List<Object> executeParams, boolean requireWhere,
            String operationName) {
            if (usePrimaryKey) {
                // 使用主键条件
                Pair<String, Object> primaryKeyCondition = buildPrimaryKeyCondition();
                sql += " WHERE " + primaryKeyCondition.getLeft();
                executeParams.add(primaryKeyCondition.getRight());
            } else if (!whereClause.isEmpty()) {
                // 使用自定义条件
                sql += " WHERE " + whereClause;
                executeParams.addAll(params);
            } else if (requireWhere) {
                throw new IllegalStateException(operationName + "操作必须指定条件（使用byId()或where条件）");
            }
            return sql;
        }

        /**
         * 执行SQL操作。
         *
         * @return 执行结果
         */
        public abstract Object execute();

        /**
         * 使用主键操作
         *
         * @return 当前构建器实例
         */
        @SuppressWarnings("unchecked")
        public B byId() {
            this.usePrimaryKey = true;
            return (B) this;
        }

        /**
         * 获取主键字段
         *
         * @return 主键字段
         */
        protected Field getPrimaryKeyField() {
            return Arrays.stream(
                entity.getClass()
                    .getDeclaredFields())
                .filter(field -> {
                    Column column = field.getAnnotation(Column.class);
                    return column != null && column.isPrimaryKey();
                })
                .findFirst()
                .orElse(null);
        }

        /**
         * 构建主键条件
         *
         * @return 主键条件SQL和参数
         */
        protected Pair<String, Object> buildPrimaryKeyCondition() {
            Field primaryKeyField = getPrimaryKeyField();
            if (primaryKeyField == null) {
                throw new IllegalStateException("实体类没有主键字段");
            }
            String primaryKeyColumn = getColumnName(primaryKeyField);
            primaryKeyField.setAccessible(true);
            try {
                Object primaryKeyValue = primaryKeyField.get(entity);
                if (primaryKeyValue == null) {
                    throw new IllegalStateException("主键值不能为空");
                }
                return Pair.of(primaryKeyColumn + " = ?", primaryKeyValue);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * DDL构建器。
     * 用于构建数据库表结构，包括创建表、添加外键约束和索引等。
     *
     * @param <T> 实体类型
     */
    public static class DDLBuilder<T> {

        /** 表引用映射 */
        private final Map<String, List<String>> tableRefMap = new HashMap<>();
        /** 实体类列表 */
        private final List<Class<?>> entityClasses = new ArrayList<>();
        /** 创建表SQL映射 */
        private final Map<String, List<String>> createTableSqlMap = new HashMap<>();

        /**
         * 构造函数。
         *
         * @param entityClass 实体类
         */
        private DDLBuilder(Class<T> entityClass) {
            if (entityClass != null) {
                this.entityClasses.add(entityClass);
            }
        }

        /**
         * 添加单个实体类。
         *
         * @param entityClass 实体类
         * @return 当前构建器实例
         */
        public DDLBuilder<T> add(Class<?> entityClass) {
            this.entityClasses.add(entityClass);
            return this;
        }

        public DDLBuilder<T> addAll(Collection<Class<?>> entityClasses) {
            this.entityClasses.addAll(entityClasses);
            return this;
        }

        /**
         * 构建SQL语句。
         *
         * @return SQL语句列表
         */
        public List<String> build() {
            // 初始化创建表SQL映射
            createTableSqlMap.clear();
            tableRefMap.clear();

            // 只有一个实体类的简单情况
            if (entityClasses.size() == 1) {
                Class<?> entityClass = entityClasses.get(0);
                String tableName = SQLTableUtils.getTableName(entityClass);
                List<String> sqls = buildTableSql(entityClass, tableName);
                createTableSqlMap.put(tableName, sqls);
                return sqls;
            }
            // 多个实体类需要处理依赖关系
            else {
                // 收集所有表的依赖关系并生成建表SQL
                collectDependenciesAndBuildTableSql();

                // 拓扑排序表
                List<String> sortedTables = sortTablesByDependencies();

                // 根据排序结果合并SQL语句
                return mergeSqlByOrder(sortedTables);
            }
        }

        /**
         * 收集所有表的依赖关系并生成建表SQL。
         * 这个方法将原来分开的两次遍历合并为一次，同时完成依赖收集和SQL生成。
         */
        private void collectDependenciesAndBuildTableSql() {
            for (Class<?> entityClass : entityClasses) {
                String tableName = SQLTableUtils.getTableName(entityClass);

                // 生成创建表SQL并存储，同时收集依赖关系
                List<String> sqls = buildTableSql(entityClass, tableName);
                createTableSqlMap.put(tableName, sqls);
            }
        }

        /**
         * 根据表排序结果合并SQL语句。
         *
         * @param sortedTables 排序后的表名列表
         * @return 合并后的SQL语句列表
         */
        private List<String> mergeSqlByOrder(List<String> sortedTables) {
            List<String> result = new ArrayList<>();
            for (String tableName : sortedTables) {
                List<String> sqls = createTableSqlMap.get(tableName);
                if (sqls != null) {
                    result.addAll(sqls);
                }
            }
            return result;
        }

        public Integer execute() {
            List<String> sqls = build();
            return sqls.stream()
                .mapToInt(sql -> (Integer) SQLiteManager.executeSafeSQL(sql))
                .sum();
        }

        private List<String> sortTablesByDependencies() {
            Map<String, Integer> inDegree = new HashMap<>();
            Map<String, List<String>> graph = buildDependencyGraph(inDegree);
            return performTopologicalSort(graph, inDegree);
        }

        /**
         * 构建依赖图
         *
         * @param inDegree 用于存储各节点入度的映射
         * @return 依赖图
         */
        private Map<String, List<String>> buildDependencyGraph(Map<String, Integer> inDegree) {
            Map<String, List<String>> graph = new HashMap<>();

            // 初始化入度和邻接表
            for (Map.Entry<String, List<String>> entry : tableRefMap.entrySet()) {
                String table = entry.getKey();
                inDegree.putIfAbsent(table, 0);
                graph.putIfAbsent(table, new ArrayList<>());

                for (String dependent : entry.getValue()) {
                    inDegree.putIfAbsent(dependent, 0);
                    graph.putIfAbsent(dependent, new ArrayList<>());
                    graph.get(table)
                        .add(dependent);
                    inDegree.put(dependent, inDegree.get(dependent) + 1);
                }
            }

            return graph;
        }

        /**
         * 执行拓扑排序
         *
         * @param graph    依赖图
         * @param inDegree 节点入度映射
         * @return 排序后的表名列表
         */
        private List<String> performTopologicalSort(Map<String, List<String>> graph, Map<String, Integer> inDegree) {
            List<String> sortedTables = new ArrayList<>();
            Queue<String> queue = new LinkedList<>();

            // 将入度为0的节点加入队列
            inDegree.entrySet()
                .stream()
                .filter(e -> e.getValue() == 0)
                .map(Map.Entry::getKey)
                .forEach(queue::offer);

            while (!queue.isEmpty()) {
                String table = queue.poll();
                sortedTables.add(table);

                for (String dependent : graph.get(table)) {
                    inDegree.put(dependent, inDegree.get(dependent) - 1);
                    if (inDegree.get(dependent) == 0) {
                        queue.offer(dependent);
                    }
                }
            }

            // 检查循环依赖
            if (sortedTables.size() != inDegree.size()) {
                List<String> cyclePath = findCycle(graph);
                String errorMessage = cyclePath.isEmpty() ? "检测到循环依赖关系,请检查表之间的外键引用"
                    : "检测到循环依赖关系,循环路径为: " + String.join(" -> ", cyclePath) + " -> " + cyclePath.get(0);
                throw new IllegalStateException(errorMessage);
            }

            return sortedTables;
        }

        /**
         * 查找依赖图中的循环
         *
         * @param graph 依赖图
         * @return 循环路径，如果没有循环则返回空列表
         */
        private List<String> findCycle(Map<String, List<String>> graph) {
            Set<String> visited = new HashSet<>();
            Set<String> recursionStack = new HashSet<>();

            for (String table : graph.keySet()) {
                if (!visited.contains(table)) {
                    List<String> cyclePath = new ArrayList<>();
                    if (detectCycle(table, graph, visited, recursionStack, cyclePath)) {
                        return cyclePath;
                    }
                }
            }
            return Collections.emptyList();
        }

        /**
         * 使用DFS检测循环
         *
         * @param table          当前表
         * @param graph          依赖图
         * @param visited        已访问节点集合
         * @param recursionStack 当前递归栈
         * @param cyclePath      用于存储检测到的循环路径
         * @return 是否检测到循环
         */
        private boolean detectCycle(String table, Map<String, List<String>> graph, Set<String> visited,
            Set<String> recursionStack, List<String> cyclePath) {
            visited.add(table);
            recursionStack.add(table);

            for (String dependent : graph.get(table)) {
                if (!visited.contains(dependent)) {
                    if (detectCycle(dependent, graph, visited, recursionStack, cyclePath)) {
                        cyclePath.add(0, table);
                        return true;
                    }
                } else if (recursionStack.contains(dependent)) {
                    cyclePath.add(0, table);
                    return true;
                }
            }

            recursionStack.remove(table);
            return false;
        }

        /**
         * 构建表的SQL语句。
         * 包括创建表、添加外键约束和索引。
         * 同时构建表依赖关系。
         *
         * @param entityClass 实体类
         * @param tableName   表名
         * @return SQL语句列表
         */
        private List<String> buildTableSql(Class<?> entityClass, String tableName) {
            List<String> sqls = new ArrayList<>();
            List<String> columnDefinitions = new ArrayList<>();
            List<Field> referFields = new ArrayList<>();
            Map<String, List<String>> indexMap = new HashMap<>();

            // 处理所有字段
            SQLTableUtils.processFields(entityClass, columnDefinitions, referFields, indexMap);

            // 收集外键约束并按引用表分组
            // 这里对外键进行分组优化，使相同表的外键引用可以合并为一个FOREIGN KEY语句
            // SQLite支持多列外键: FOREIGN KEY(A, B) REFERENCES tasks(id) ON DELETE CASCADE ON UPDATE CASCADE
            Map<String, List<Field>> referFieldsByTable = referFields.stream()
                .collect(
                    Collectors.groupingBy(
                        field -> SQLColumnUtils.getReferencedTableName(field.getAnnotation(Reference.class))));

            // 构建依赖关系映射
            for (Map.Entry<String, List<Field>> entry : referFieldsByTable.entrySet()) {
                String referencedTable = entry.getKey();
                tableRefMap.computeIfAbsent(referencedTable, k -> new ArrayList<>())
                    .add(tableName);
            }

            // 生成合并后的外键约束
            List<String> foreignKeys = referFieldsByTable.entrySet()
                .stream()
                .map(entry -> SQLTableUtils.generateForeignKeySql(entry.getValue(), entry.getKey()))
                .collect(Collectors.toList());

            // 生成建表语句
            sqls.add(SQLTableUtils.generateCreateTableSql(tableName, columnDefinitions, foreignKeys));

            // 添加索引
            SQLIndexUtils.processAndAddIndexes(indexMap, tableName, sqls);

            return sqls;
        }
    }

    /**
     * SQL列定义和约束的工具类。
     * 提供列类型转换、约束生成等功能。
     */
    private static class SQLColumnUtils {

        /**
         * 获取SQLite数据类型。
         * 根据Java字段类型转换为对应的SQLite数据类型。
         *
         * @param field 字段
         * @return SQLite数据类型
         */
        static String getSqliteType(Field field) {
            Class<?> type = field.getType();
            String typeName = type.getSimpleName();

            // 字符串类型
            if (typeName.equals("String") || typeName.equals("UUID") || type.isEnum()) {
                return "TEXT";
            } else {
                return switch (typeName) {
                    // 整数类型
                    case "int", "Integer" -> "INTEGER";
                    // 长整数类型
                    case "long", "Long" -> "BIGINT";
                    // 浮点类型
                    case "double", "Double", "Duration" -> "REAL";
                    // 布尔类型
                    case "boolean", "Boolean" -> "BOOLEAN";
                    // 时间类型
                    case "Date", "LocalDateTime" -> "TIMESTAMP";
                    default -> throw new IllegalArgumentException("不支持的字段类型: " + type);
                };
            }
        }

        /**
         * 构建列定义。
         * 根据字段和注解生成完整的列定义SQL，包括数据类型和约束。
         *
         * @param field  字段
         * @param column 列注解
         * @return 列定义SQL
         */
        static String buildColumnDefinition(Field field, Column column) {
            StringBuilder columnDef = new StringBuilder();
            String columnName = column.name();

            columnDef.append(columnName)
                .append(" ");

            // 添加数据类型
            columnDef.append(getSqliteType(field));

            // 添加默认值
            if (!"".equals(column.defaultValue())) {
                columnDef.append(" DEFAULT ")
                    .append(column.defaultValue());
            }

            // 添加NOT NULL约束
            if (field.getAnnotation(javax.annotation.Nonnull.class) != null) {
                columnDef.append(" NOT NULL");
            }

            // 添加主键约束
            if (column.isPrimaryKey()) {
                columnDef.append(" PRIMARY KEY");
            }

            // 添加唯一约束
            if (column.isUnique()) {
                columnDef.append(" UNIQUE");
            }

            // 添加字段检查约束
            FieldCheck fieldCheck = field.getAnnotation(FieldCheck.class);
            if (fieldCheck != null) {
                // 特殊处理枚举类型，添加CHECK约束确保值在枚举范围内
                if (field.getType()
                    .isEnum()) {
                    String enumValues = getEnumValues(field.getType());
                    if (!enumValues.isEmpty()) {
                        columnDef.append(" CHECK(")
                            .append(columnName)
                            .append(" IN (")
                            .append(enumValues)
                            .append("))");
                    }
                } else {
                    // 根据检查类型生成约束
                    switch (fieldCheck.type()) {
                        case RANGE:
                            columnDef.append(" CHECK(")
                                .append(columnName)
                                .append(" >= ")
                                .append(fieldCheck.min())
                                .append(" AND ")
                                .append(columnName)
                                .append(" <= ")
                                .append(fieldCheck.max())
                                .append(")");
                            break;
                        case GLOB:
                            columnDef.append(" CHECK(")
                                .append(columnName)
                                .append(" GLOB '")
                                .append(fieldCheck.glob())
                                .append("')");
                            break;
                        case LENGTH:
                            columnDef.append(" CHECK(LENGTH(")
                                .append(columnName)
                                .append(") >= ")
                                .append(fieldCheck.min())
                                .append(" AND LENGTH(")
                                .append(columnName)
                                .append(") <= ")
                                .append(fieldCheck.max())
                                .append(")");
                            break;
                        case UUID:
                            columnDef.append(" CHECK(LENGTH(")
                                .append(columnName)
                                .append(") = 36)");
                            break;
                        case ENUM:
                            if (fieldCheck.dataType()
                                .isEnum()) {
                                columnDef.append(" CHECK(")
                                    .append(columnName)
                                    .append(" IN (")
                                    .append(getEnumValues(fieldCheck.dataType()))
                                    .append("))");
                            }
                            break;
                        case NOT_VALUE:
                            columnDef.append(" CHECK(")
                                .append(columnName)
                                .append(" != ")
                                .append(fieldCheck.notValue())
                                .append(")");
                            break;
                        case MIN:
                            columnDef.append(" CHECK(")
                                .append(columnName)
                                .append(" >= ")
                                .append(fieldCheck.min())
                                .append(")");
                            break;
                        case MAX:
                            columnDef.append(" CHECK(")
                                .append(columnName)
                                .append(" <= ")
                                .append(fieldCheck.max())
                                .append(")");
                            break;
                    }
                }
            }
            return columnDef.toString();
        }

        /**
         * 获取枚举类的所有值，用于构建CHECK约束。
         *
         * @param enumClass 枚举类
         * @return 格式化的枚举值列表
         */
        static String getEnumValues(Class<?> enumClass) {
            if (!enumClass.isEnum()) {
                return "";
            }

            Object[] constants = enumClass.getEnumConstants();
            return Arrays.stream(constants)
                .map(constant -> "'" + constant.toString() + "'")
                .collect(Collectors.joining(", "));
        }

        /**
         * 获取引用的表名。
         * 根据Reference注解获取引用的表名。
         *
         * @param reference 引用注解
         * @return 引用的表名
         */
        static String getReferencedTableName(Reference reference) {
            if (reference == null) {
                throw new IllegalArgumentException("引用注解不能为null");
            }

            Reference.Type referenceType = reference.referenceType();
            return referenceType.name()
                .toLowerCase() + "s";
        }
    }

    /**
     * SQL索引处理的工具类。
     * 提供索引信息的收集和SQL生成功能。
     */
    private static class SQLIndexUtils {

        /**
         * 处理并生成索引SQL语句。
         *
         * @param indexMap  索引映射（列名到索引名的映射）
         * @param tableName 表名
         * @param sqls      SQL语句列表，生成的索引SQL会添加到这个列表中
         */
        static void processAndAddIndexes(Map<String, List<String>> indexMap, String tableName, List<String> sqls) {
            // 反转索引映射（索引名到列名的映射）
            Map<String, List<String>> indexMapReverse = new HashMap<>();

            for (Map.Entry<String, List<String>> entry : indexMap.entrySet()) {
                String columnName = entry.getKey();
                for (String indexName : entry.getValue()) {
                    indexMapReverse.computeIfAbsent(indexName, k -> new ArrayList<>())
                        .add(columnName);
                }
            }

            // 生成并添加索引SQL
            for (Map.Entry<String, List<String>> entry : indexMapReverse.entrySet()) {
                String indexSql = String.format(
                    "CREATE INDEX IF NOT EXISTS %s ON %s(%s);",
                    entry.getKey(),
                    tableName,
                    String.join(", ", entry.getValue()));
                sqls.add(indexSql);
            }
        }

        /**
         * 收集字段的索引信息。
         *
         * @param field    字段
         * @param column   列注解
         * @param indexMap 索引映射，用于存储收集结果
         */
        static void collectIndexInfo(Field field, Column column, Map<String, List<String>> indexMap) {
            if (column.index().length > 0) {
                indexMap.put(column.name(), Arrays.asList(column.index()));
            }
        }
    }

    /**
     * SQL表结构处理的工具类。
     * 提供表结构信息的收集和SQL生成功能。
     */
    private static class SQLTableUtils {

        /**
         * 处理实体类的所有字段。
         *
         * @param entityClass       实体类
         * @param columnDefinitions 列定义列表
         * @param referFields       引用字段列表
         * @param indexMap          索引映射
         */
        static void processFields(Class<?> entityClass, List<String> columnDefinitions, List<Field> referFields,
            Map<String, List<String>> indexMap) {
            for (Field field : UtilHelper.getAllFieldsReverse(entityClass)) {
                Column column = field.getAnnotation(Column.class);
                if (column == null) continue;

                columnDefinitions.add(SQLColumnUtils.buildColumnDefinition(field, column));

                if (field.isAnnotationPresent(Reference.class)) {
                    referFields.add(field);
                }

                SQLIndexUtils.collectIndexInfo(field, column, indexMap);
            }
        }

        /**
         * 生成创建表SQL。
         *
         * @param tableName         表名
         * @param columnDefinitions 列定义列表
         * @param foreignKeys       外键约束列表
         * @return 创建表SQL
         */
        static String generateCreateTableSql(String tableName, List<String> columnDefinitions,
            List<String> foreignKeys) {
            List<String> allDefinitions = new ArrayList<>(columnDefinitions);
            if (foreignKeys != null && !foreignKeys.isEmpty()) {
                allDefinitions.addAll(foreignKeys);
            }
            return String.format("CREATE TABLE IF NOT EXISTS %s (%s);", tableName, String.join(", ", allDefinitions));
        }

        /**
         * 获取实体类对应的表名。
         *
         * @param entityClass 实体类
         * @return 表名
         */
        static String getTableName(Class<?> entityClass) {
            Table table = entityClass.getAnnotation(Table.class);
            if (table == null) {
                throw new IllegalArgumentException("类必须使用@Table注解");
            }
            return table.name();
        }

        /**
         * 生成外键SQL。
         *
         * @param fields          引用字段列表
         * @param referencedTable 引用表名
         * @return 外键SQL
         */
        static String generateForeignKeySql(List<Field> fields, String referencedTable) {
            // 构建字段名列表
            String fieldNames = fields.stream()
                .map(field -> {
                    Column column = field.getAnnotation(Column.class);
                    return column != null ? column.name() : field.getName();
                })
                .collect(Collectors.joining(", "));

            // 生成外键约束SQL，使用CASCADE作为默认操作
            return String.format(
                "FOREIGN KEY(%s) REFERENCES %s(id) ON DELETE CASCADE ON UPDATE CASCADE",
                fieldNames,
                referencedTable);
        }
    }
}
