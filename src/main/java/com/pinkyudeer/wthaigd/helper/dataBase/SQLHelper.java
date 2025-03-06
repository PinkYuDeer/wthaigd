package com.pinkyudeer.wthaigd.helper.dataBase;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.pinkyudeer.wthaigd.helper.dataBase.annotation.Column;
import com.pinkyudeer.wthaigd.helper.dataBase.annotation.Table;

import lombok.Getter;

/**
 * SQL 语句构建器。
 * 提供增删改查 SQL 构建并执行功能。
 */
public class SQLHelper {

    // 添加操作符枚举
    @Getter
    public enum Operator {

        EQ("="),
        NE("<>"),
        GT(">"),
        GE(">="),
        LT("<"),
        LE("<="),
        LIKE("LIKE"),
        IN("IN"),
        NOT_IN("NOT IN"),
        IS_NULL("IS NULL"),
        IS_NOT_NULL("IS NOT NULL");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

    }

    // 基础抽象构建器
    private abstract static class BaseBuilder<T, B extends BaseBuilder<T, B>> {

        protected final T entity;
        protected String whereClause = "";
        protected List<Object> params = new ArrayList<>();

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

        // 开始一个新的AND条件组
        public B beginGroup() {
            WhereGroup newGroup = new WhereGroup();
            currentGroup.addSubGroup(newGroup);
            currentGroup = newGroup;
            return self();
        }

        // 开始一个新的OR条件组
        public B orBeginGroup() {
            WhereGroup newGroup = new WhereGroup();
            newGroup.isOr = true;
            currentGroup.addSubGroup(newGroup);
            currentGroup = newGroup;
            return self();
        }

        // 结束当前条件组
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

        // 修改where方法，使用Operator枚举
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

        // 添加OR条件的方法
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

        // 添加IN条件的便捷方法
        public B whereIn(Field field, List<?> values) {
            return where(field, Operator.IN, values);
        }

        // 添加NOT IN条件的便捷方法
        public B whereNotIn(Field field, List<?> values) {
            return where(field, Operator.NOT_IN, values);
        }

        // 添加LIKE条件的便捷方法
        public B whereLike(Field field, String pattern) {
            return where(field, Operator.LIKE, pattern);
        }

        // 添加IS NULL条件的便捷方法
        public B whereNull(Field field) {
            return where(field, Operator.IS_NULL, null);
        }

        // 添加IS NOT NULL条件的便捷方法
        public B whereNotNull(Field field) {
            return where(field, Operator.IS_NOT_NULL, null);
        }

        // 修改execute方法中的where子句构建
        protected String buildWhereClause() {
            String whereClause = currentGroup.buildClause();
            params = currentGroup.getParams();
            return whereClause;
        }

        public abstract Integer execute();
    }

    // 插入构建器
    public static class InsertBuilder<T> extends BaseBuilder<T, InsertBuilder<T>> {

        private InsertBuilder(T entity) {
            super(entity);
        }

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

    // 更新构建器
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
            if (!whereClause.isEmpty()) {
                sql += " WHERE " + whereClause;
                executeParams.addAll(params);
            }

            return (Integer) SQLiteManager.executeSafeSQL(sql, executeParams);
        }
    }

    // 删除构建器
    public static class DeleteBuilder<T> extends BaseBuilder<T, DeleteBuilder<T>> {

        private DeleteBuilder(T entity) {
            super(entity);
        }

        @Override
        public Integer execute() {
            String sql = "DELETE FROM " + getTableName();
            if (!whereClause.isEmpty()) {
                sql += " WHERE " + whereClause;
            }
            return (Integer) SQLiteManager.executeSafeSQL(sql, params);
        }
    }

    // 查询构建器
    public static class SelectBuilder<T> extends BaseBuilder<T, SelectBuilder<T>> {

        private String[] selectColumns;
        private String joins = "";
        private String[] groupByFields;
        private String havingClause = "";
        private final List<Object> havingParams = new ArrayList<>();
        private String[] orderByFields;
        private boolean isAscending = true;
        private int limit = 0;
        private int offset = 0;

        private SelectBuilder(T entity) {
            super(entity);
        }

        // 修改columns方法，接收Field数组
        public SelectBuilder<T> columns(Field... fields) {
            this.selectColumns = Arrays.stream(fields)
                .map(this::getFullColumnName)
                .toArray(String[]::new);
            return this;
        }

        // 修改join方法，使用Field而不是字符串
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

        // 修改groupBy方法，使用Field数组
        public SelectBuilder<T> groupBy(Field... fields) {
            this.groupByFields = Arrays.stream(fields)
                .map(this::getFullColumnName)
                .toArray(String[]::new);
            return this;
        }

        // 修改orderBy方法，使用Field数组
        public SelectBuilder<T> orderBy(boolean ascending, Field... fields) {
            this.orderByFields = Arrays.stream(fields)
                .map(this::getFullColumnName)
                .toArray(String[]::new);
            this.isAscending = ascending;
            return this;
        }

        public SelectBuilder<T> having(String clause, Object... params) {
            this.havingClause = clause;
            if (params != null) {
                this.havingParams.addAll(Arrays.asList(params));
            }
            return this;
        }

        public SelectBuilder<T> limit(int limit) {
            this.limit = limit;
            return this;
        }

        public SelectBuilder<T> offset(int offset) {
            this.offset = offset;
            return this;
        }

        @Override
        public Integer execute() {
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

            return (Integer) SQLiteManager.executeSafeSQL(query.toString(), executeParams);
        }
    }

    // 静态工厂方法
    public static <T> InsertBuilder<T> insert(T entity) {
        return new InsertBuilder<>(entity);
    }

    public static <T> UpdateBuilder<T> update(T entity) {
        return new UpdateBuilder<>(entity);
    }

    public static <T> DeleteBuilder<T> delete(T entity) {
        return new DeleteBuilder<>(entity);
    }

    public static <T> SelectBuilder<T> select(T entity) {
        return new SelectBuilder<>(entity);
    }
}
