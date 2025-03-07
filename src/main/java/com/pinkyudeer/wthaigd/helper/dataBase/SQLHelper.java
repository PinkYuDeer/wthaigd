package com.pinkyudeer.wthaigd.helper.dataBase;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.pinkyudeer.wthaigd.entity.task.Player;
import com.pinkyudeer.wthaigd.entity.task.Tag;
import com.pinkyudeer.wthaigd.entity.task.Task;
import com.pinkyudeer.wthaigd.entity.task.Team;
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
     * 创建批量DDL构建器。
     *
     * @return DDL构建器实例
     */
    public static DDLBuilder<?> createTables() {
        return new DDLBuilder<>(null);
    }

    /**
     * 创建多个表的DDL构建器。
     *
     * @param entityClasses 实体类集合
     * @return DDL构建器实例
     */
    public static DDLBuilder<?> createTables(Set<Class<?>> entityClasses) {
        return createTables().addAll(entityClasses);
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

            if (usePrimaryKey) {
                // 使用主键删除
                Pair<String, Object> primaryKeyCondition = buildPrimaryKeyCondition();
                sql += " WHERE " + primaryKeyCondition.getLeft();
                executeParams.add(primaryKeyCondition.getRight());
            } else if (!whereClause.isEmpty()) {
                // 使用自定义条件删除
                sql += " WHERE " + whereClause;
                executeParams.addAll(params);
            } else {
                throw new IllegalStateException("删除操作必须指定条件（使用byId()或where条件）");
            }

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

            if (usePrimaryKey) {
                // 使用主键更新
                Pair<String, Object> primaryKeyCondition = buildPrimaryKeyCondition();
                sql += " WHERE " + primaryKeyCondition.getLeft();
                executeParams.add(primaryKeyCondition.getRight());
            } else if (!whereClause.isEmpty()) {
                // 使用自定义条件更新
                sql += " WHERE " + whereClause;
                executeParams.addAll(params);
            } else {
                throw new IllegalStateException("更新操作必须指定条件（使用byId()或where条件）");
            }

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
            String whereClause = buildWhereClause();
            if (!whereClause.isEmpty()) {
                query.append(" WHERE ")
                    .append(whereClause);
            }
            if (groupByFields != null && groupByFields.length > 0) query.append(" GROUP BY ")
                .append(String.join(", ", groupByFields));
            if (!havingClause.isEmpty()) query.append(" HAVING ")
                .append(havingClause);
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

            List<Object> executeParams = new ArrayList<>(params);
            executeParams.addAll(havingParams);

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

            public String buildClause() {
                StringBuilder result = new StringBuilder();

                // 添加当前组的条件
                if (!clause.isEmpty()) {
                    result.append(clause);
                }

                // 添加子组
                for (WhereGroup subGroup : subGroups) {
                    if (result.length() > 0) {
                        result.append(subGroup.isOr ? " OR " : " AND ");
                    }
                    result.append("(")
                        .append(subGroup.buildClause())
                        .append(")");
                }

                return result.toString();
            }

            public List<Object> getParams() {
                List<Object> allParams = new ArrayList<>(groupParams);
                for (WhereGroup subGroup : subGroups) {
                    allParams.addAll(subGroup.getParams());
                }
                return allParams;
            }
        }

        protected WhereGroup currentGroup = new WhereGroup();

        /**
         * 开始一个新的AND条件组。
         *
         * @return 当前构建器实例
         */
        public B beginGroup() {
            WhereGroup newGroup = new WhereGroup();
            currentGroup.addSubGroup(newGroup);
            currentGroup = newGroup;
            return self();
        }

        /**
         * 开始一个新的OR条件组。
         *
         * @return 当前构建器实例
         */
        public B orBeginGroup() {
            WhereGroup newGroup = new WhereGroup();
            newGroup.isOr = true;
            currentGroup.addSubGroup(newGroup);
            currentGroup = newGroup;
            return self();
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

        // 获取字段对应的列名
        protected String getColumnName(Field field) {
            Column column = field.getAnnotation(Column.class);
            if (column == null) {
                throw new IllegalArgumentException("字段必须使用@Column注解: " + field.getName());
            }
            return column.name();
        }

        // 获取类中所有带Column注解的字段
        protected List<Field> getColumnFields(Class<?> clazz) {
            return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Column.class))
                .collect(Collectors.toList());
        }

        // 根据字段获取表名.列名的完整引用
        protected String getFullColumnName(Field field) {
            String tableName = getTableName();
            String columnName = getColumnName(field);
            return tableName + "." + columnName;
        }

        /**
         * 添加WHERE条件。
         *
         * @param field    字段
         * @param operator 操作符
         * @param value    值
         * @return 当前构建器实例
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

        /**
         * 添加IN条件。
         *
         * @param field  字段
         * @param values 值列表
         * @return 当前构建器实例
         */
        public B whereIn(Field field, List<?> values) {
            return where(field, Operator.IN, values);
        }

        /**
         * 添加NOT IN条件。
         *
         * @param field  字段
         * @param values 值列表
         * @return 当前构建器实例
         */
        public B whereNotIn(Field field, List<?> values) {
            return where(field, Operator.NOT_IN, values);
        }

        /**
         * 添加LIKE条件。
         *
         * @param field   字段
         * @param pattern 匹配模式
         * @return 当前构建器实例
         */
        public B whereLike(Field field, String pattern) {
            return where(field, Operator.LIKE, pattern);
        }

        /**
         * 添加IS NULL条件。
         *
         * @param field 字段
         * @return 当前构建器实例
         */
        public B whereNull(Field field) {
            return where(field, Operator.IS_NULL, null);
        }

        /**
         * 添加IS NOT NULL条件。
         *
         * @param field 字段
         * @return 当前构建器实例
         */
        public B whereNotNull(Field field) {
            return where(field, Operator.IS_NOT_NULL, null);
        }

        /**
         * 构建WHERE子句。
         *
         * @return WHERE子句
         */
        protected String buildWhereClause() {
            String whereClause = currentGroup.buildClause();
            params = currentGroup.getParams();
            return whereClause;
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

        /** 实体类 */
        private final Class<T> entityClass;
        /** 表引用映射 */
        private final Map<String, List<String>> tableRefMap = new HashMap<>();
        /** 创建表SQL列表 */
        private final List<String> createTableSqlList = new ArrayList<>();
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
            this.entityClass = entityClass;
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

        public DDLBuilder<T> addAll(List<Class<?>> entityClasses) {
            this.entityClasses.addAll(entityClasses);
            return this;
        }

        public DDLBuilder<T> addAll(Set<Class<?>> entityClasses) {
            this.entityClasses.addAll(entityClasses);
            return this;
        }

        public List<String> build() {
            if (entityClasses.isEmpty()) {
                throw new IllegalStateException("没有要创建的表");
            }

            if (entityClasses.size() == 1) {
                return buildSingleTable();
            } else {
                return buildMultipleTables();
            }
        }

        public Integer execute() {
            List<String> sqls = build();
            return sqls.stream()
                .mapToInt(sql -> (Integer) SQLiteManager.executeSafeSQL(sql, Collections.emptyList()))
                .sum();
        }

        /**
         * 构建单个表的DDL语句。
         *
         * @return DDL语句列表
         */
        private List<String> buildSingleTable() {
            String tableName = SQLTableUtils.getTableName(entityClass);
            List<String> sqls = buildTableSql(entityClass, tableName);
            createTableSqlList.addAll(sqls);
            return createTableSqlList;
        }

        /**
         * 构建多个表的DDL语句。
         *
         * @return DDL语句列表
         */
        private List<String> buildMultipleTables() {
            collectAllTableDependencies();
            List<String> sortedTables = sortTablesByDependencies();
            generateAllCreateTableSql(sortedTables);
            return mergeAllSql();
        }

        private void collectAllTableDependencies() {
            for (Class<?> entityClass : entityClasses) {
                collectTableDependencies(entityClass);
            }
        }

        private void collectTableDependencies(Class<?> entityClass) {
            String tableName = SQLTableUtils.getTableName(entityClass);

            // 收集外键引用
            for (Field field : UtilHelper.getAllFieldsReverse(entityClass)) {
                if (field.isAnnotationPresent(Reference.class)) {
                    Reference reference = field.getAnnotation(Reference.class);
                    String referencedTable = SQLColumnUtils.getReferencedTableName(reference);

                    tableRefMap.computeIfAbsent(referencedTable, k -> new ArrayList<>())
                        .add(tableName);
                }
            }
        }

        private List<String> sortTablesByDependencies() {
            Map<String, Integer> inDegree = new HashMap<>();
            Map<String, List<String>> graph = buildDependencyGraph(inDegree);
            return performTopologicalSort(graph, inDegree);
        }

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

        private List<String> findCycle(Map<String, List<String>> graph) {
            Set<String> visited = new HashSet<>();
            Set<String> recursionStack = new HashSet<>();
            List<String> cyclePath = new ArrayList<>();

            for (String table : graph.keySet()) {
                if (!visited.contains(table) && findCycleUtil(table, graph, visited, recursionStack, cyclePath)) {
                    return cyclePath;
                }
            }
            return Collections.emptyList();
        }

        private boolean findCycleUtil(String table, Map<String, List<String>> graph, Set<String> visited,
            Set<String> recursionStack, List<String> cyclePath) {
            visited.add(table);
            recursionStack.add(table);

            for (String dependent : graph.get(table)) {
                if (!visited.contains(dependent)) {
                    if (findCycleUtil(dependent, graph, visited, recursionStack, cyclePath)) {
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

        private void generateAllCreateTableSql(List<String> sortedTables) {
            for (String tableName : sortedTables) {
                Class<?> entityClass = findEntityClassByTableName(tableName);
                if (entityClass != null) {
                    generateCreateTableSql(entityClass);
                }
            }
        }

        private Class<?> findEntityClassByTableName(String tableName) {
            return entityClasses.stream()
                .filter(clazz -> {
                    Table table = clazz.getAnnotation(Table.class);
                    return table != null && table.name()
                        .equals(tableName);
                })
                .findFirst()
                .orElse(null);
        }

        private void addForeignKeyConstraints(List<Field> referFields, String tableName, List<String> sqls) {
            for (Field field : referFields) {
                Reference reference = field.getAnnotation(Reference.class);
                String referencedTable = SQLColumnUtils.getReferencedTableName(reference);
                sqls.add(SQLTableUtils.generateForeignKeySql(field, referencedTable));
            }
        }

        private void addIndexes(Map<String, List<String>> indexMap, String tableName, List<String> sqls) {
            Map<String, List<String>> indexMapReverse = SQLIndexUtils.reverseIndexMap(indexMap);

            for (Map.Entry<String, List<String>> entry : indexMapReverse.entrySet()) {
                sqls.add(SQLIndexUtils.generateIndexSql(entry.getKey(), tableName, entry.getValue()));
            }
        }

        private List<String> mergeAllSql() {
            return createTableSqlMap.values()
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
        }

        /**
         * 生成创建表SQL。
         *
         * @param entityClass 实体类
         */
        private void generateCreateTableSql(Class<?> entityClass) {
            String tableName = SQLTableUtils.getTableName(entityClass);
            List<String> sqls = buildTableSql(entityClass, tableName);
            createTableSqlMap.put(tableName, sqls);
        }

        /**
         * 构建表的SQL语句。
         * 包括创建表、添加外键约束和索引。
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

            // 生成建表语句
            sqls.add(SQLTableUtils.generateCreateTableSql(tableName, columnDefinitions));

            // 添加外键约束
            addForeignKeyConstraints(referFields, tableName, sqls);

            // 添加索引
            addIndexes(indexMap, tableName, sqls);

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
         *
         * @param field 字段
         * @return SQLite数据类型
         */
        static String getSqliteType(Field field) {
            return switch (field.getType()
                .getSimpleName()) {
                case "String", "UUID" -> "TEXT";
                case "int", "Integer" -> "INTEGER";
                case "long", "Long" -> "BIGINT";
                case "double", "Double", "Duration" -> "REAL";
                case "boolean", "Boolean" -> "BOOLEAN";
                case "Date", "LocalDateTime" -> "TIMESTAMP";
                default -> {
                    if (field.getType()
                        .isEnum()) {
                        yield "TEXT";
                    } else {
                        throw new IllegalArgumentException("Unsupported field type: " + field.getType());
                    }
                }
            };
        }

        /**
         * 构建列定义。
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

            // 添加检查约束
            String checkConstraint = generateCheckConstraint(field, columnName);
            if (!checkConstraint.isEmpty()) {
                columnDef.append(" ")
                    .append(checkConstraint);
            }

            // 添加唯一约束
            if (column.isUnique()) {
                columnDef.append(" UNIQUE");
            }

            // 添加主键约束
            if (column.isPrimaryKey()) {
                columnDef.append(" PRIMARY KEY");
            }

            return columnDef.toString();
        }

        /**
         * 生成检查约束。
         *
         * @param field      字段
         * @param columnName 列名
         * @return 检查约束SQL
         */
        static String generateCheckConstraint(Field field, String columnName) {
            StringBuilder checkConstraint = new StringBuilder();

            // 获取javax.annotation.Nonnull注解
            if (field.getAnnotation(javax.annotation.Nonnull.class) != null) {
                checkConstraint.append("NOT NULL ");
            }

            // 获取 FieldCheck 注解
            FieldCheck fieldCheck = field.getAnnotation(FieldCheck.class);
            if (fieldCheck == null) return "";

            // 针对不同类型的校验
            switch (fieldCheck.type()) {
                case NOT_VALUE:
                    checkConstraint.append("CHECK(")
                        .append(columnName)
                        .append(" != ")
                        .append(fieldCheck.notValue())
                        .append(")");
                    break;
                case MIN:
                    checkConstraint.append("CHECK(")
                        .append(columnName)
                        .append(" >= ")
                        .append(fieldCheck.min())
                        .append(")");
                    break;
                case MAX:
                    checkConstraint.append("CHECK(")
                        .append(columnName)
                        .append(" <= ")
                        .append(fieldCheck.max())
                        .append(")");
                    break;
                case RANGE:
                    checkConstraint.append("CHECK(")
                        .append(columnName)
                        .append(" BETWEEN ")
                        .append(fieldCheck.min())
                        .append(" AND ")
                        .append(fieldCheck.max())
                        .append(")");
                    break;
                case GLOB:
                    checkConstraint.append("CHECK(")
                        .append(columnName)
                        .append(" GLOB '")
                        .append(fieldCheck.glob())
                        .append("')");
                    break;
                case LENGTH:
                    checkConstraint.append("CHECK(LENGTH(")
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
                    checkConstraint.append("CHECK(LENGTH(")
                        .append(columnName)
                        .append(") = 36)");
                    break;
                case ENUM:
                    if (fieldCheck.dataType()
                        .isEnum()) {
                        checkConstraint.append("CHECK(")
                            .append(columnName)
                            .append(" IN (")
                            .append(getEnumValues(fieldCheck.dataType()))
                            .append("))");
                    }
                    break;
            }

            return checkConstraint.toString();
        }

        /**
         * 获取枚举值列表。
         *
         * @param enumClass 枚举类
         * @return 枚举值列表SQL
         */
        static String getEnumValues(Class<?> enumClass) {
            if (!enumClass.isEnum()) {
                throw new IllegalArgumentException("Provided class is not an Enum");
            }

            StringBuilder values = new StringBuilder();
            for (Object enumConstant : enumClass.getEnumConstants()) {
                values.append("'")
                    .append(((Enum<?>) enumConstant).name())
                    .append("', ");
            }

            // 去掉最后的逗号和空格
            return !(values.length() == 0) ? values.substring(0, values.length() - 2) : "";
        }

        /**
         * 获取引用表名。
         *
         * @param reference 引用注解
         * @return 引用表名
         */
        static String getReferencedTableName(Reference reference) {
            Reference.Type ref_type = reference.referenceType();
            return switch (ref_type) {
                case PLAYER -> Player.class.getAnnotation(Table.class)
                    .name();
                case TEAM -> Team.class.getAnnotation(Table.class)
                    .name();
                case TASK -> Task.class.getAnnotation(Table.class)
                    .name();
                case TAG -> Tag.class.getAnnotation(Table.class)
                    .name();
            };
        }
    }

    /**
     * SQL索引处理的工具类。
     * 提供索引信息的收集和SQL生成功能。
     */
    private static class SQLIndexUtils {

        /**
         * 反转索引映射。
         *
         * @param indexMap 原始索引映射
         * @return 反转后的索引映射
         */
        static Map<String, List<String>> reverseIndexMap(Map<String, List<String>> indexMap) {
            Map<String, List<String>> indexMapReverse = new HashMap<>();

            for (Map.Entry<String, List<String>> entry : indexMap.entrySet()) {
                String columnName = entry.getKey();
                for (String indexName : entry.getValue()) {
                    indexMapReverse.computeIfAbsent(indexName, k -> new ArrayList<>())
                        .add(columnName);
                }
            }

            return indexMapReverse;
        }

        /**
         * 收集索引信息。
         *
         * @param field    字段
         * @param column   列注解
         * @param indexMap 索引映射
         */
        static void collectIndexInfo(Field field, Column column, Map<String, List<String>> indexMap) {
            if (column.index().length > 0) {
                indexMap.put(column.name(), Arrays.asList(column.index()));
            }
        }

        /**
         * 生成索引SQL。
         *
         * @param indexName 索引名
         * @param tableName 表名
         * @param columns   列名列表
         * @return 索引SQL
         */
        static String generateIndexSql(String indexName, String tableName, List<String> columns) {
            return String
                .format("CREATE INDEX IF NOT EXISTS %s ON %s(%s);", indexName, tableName, String.join(", ", columns));
        }
    }

    /**
     * SQL表结构处理的工具类。
     * 提供表结构信息的收集和SQL生成功能。
     */
    private static class SQLTableUtils {

        /**
         * 处理字段。
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
         * @return 创建表SQL
         */
        static String generateCreateTableSql(String tableName, List<String> columnDefinitions) {
            return String
                .format("CREATE TABLE IF NOT EXISTS %s (%s);", tableName, String.join(", ", columnDefinitions));
        }

        /**
         * 生成外键SQL。
         *
         * @param field           字段
         * @param referencedTable 引用表名
         * @return 外键SQL
         */
        static String generateForeignKeySql(Field field, String referencedTable) {
            return String.format(
                "FOREIGN KEY(%s) REFERENCES %s(id) ON DELETE CASCADE ON UPDATE CASCADE;",
                field.getName(),
                referencedTable);
        }

        /**
         * 获取表名。
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
    }
}
