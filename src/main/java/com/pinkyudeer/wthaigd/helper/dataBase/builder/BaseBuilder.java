package com.pinkyudeer.wthaigd.helper.dataBase.builder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.pinkyudeer.wthaigd.helper.dataBase.SQLHelper;
import com.pinkyudeer.wthaigd.helper.dataBase.annotation.Column;
import com.pinkyudeer.wthaigd.helper.dataBase.annotation.Table;

/**
 * 基础构建器抽象类。
 * 提供通用的SQL构建功能，包括条件构建、参数管理等。
 *
 * @param <T> 实体类型
 * @param <B> 构建器类型
 */
public abstract class BaseBuilder<T, B extends BaseBuilder<T, B>> {

    protected final T entity;
    protected final T oldEntity;
    protected final Class<T> entityClass;
    protected String whereClause = "";
    protected List<Object> params = new ArrayList<>();
    protected boolean usePrimaryKey = false;
    protected boolean compareMode = false;

    // 条件组类
    protected static class WhereGroup {

        private String clause = "";
        private final List<Object> groupParams = new ArrayList<>();
        private final List<WhereGroup> subGroups = new ArrayList<>();
        private boolean isOr = false; // 标识是否为OR连接

        public void addCondition(String condition, Object... params) {
            if (condition == null || condition.isEmpty()) {
                return; // 忽略空条件
            }

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
            if (group != null) {
                subGroups.add(group);
            }
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
    protected WhereGroup rootGroup = currentGroup;

    /**
     * 基于实体对象的构造函数
     *
     * @param entity 实体对象
     */
    @SuppressWarnings("unchecked")
    protected BaseBuilder(T entity) {
        this.entity = entity;
        this.oldEntity = null;
        this.entityClass = entity != null ? (Class<T>) entity.getClass() : null;
    }

    /**
     * 基于实体类的构造函数
     *
     * @param entityClass 实体类
     */
    protected BaseBuilder(Class<T> entityClass) {
        this.entity = null;
        this.oldEntity = null;
        this.entityClass = entityClass;
        this.compareMode = false;
    }

    /**
     * 基于比对模式的构造函数
     *
     * @param entity    当前实体
     * @param oldEntity 旧实体（用于比对）
     */
    @SuppressWarnings("unchecked")
    protected BaseBuilder(T entity, T oldEntity) {
        this.entity = entity;
        this.oldEntity = oldEntity;
        this.entityClass = entity != null ? (Class<T>) entity.getClass() : null;
        this.compareMode = true;
    }

    @SuppressWarnings("unchecked")
    protected B self() {
        return (B) this;
    }

    /**
     * 获取表名
     *
     * @return 表名
     */
    protected String getTableName() {
        Class<?> clazz = entityClass;
        if (clazz == null && entity != null) {
            clazz = entity.getClass();
        }

        if (clazz == null) {
            throw new IllegalStateException("无法确定表名：entityClass和entity均为null");
        }

        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            throw new IllegalArgumentException("类必须使用@Table注解: " + clazz.getName());
        }
        return table.name();
    }

    /**
     * 获取指定实体对象的列名和值映射
     *
     * @param targetEntity 实体对象
     * @return 列名和值的映射
     */
    protected Map<String, Object> getColumnValues(T targetEntity) {
        if (targetEntity == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> values = new HashMap<>();
        for (Field field : targetEntity.getClass()
            .getDeclaredFields()) {
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                field.setAccessible(true);
                try {
                    Object value = field.get(targetEntity);
                    if (value != null) {
                        values.put(column.name(), value);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("无法访问字段: " + field.getName(), e);
                }
            }
        }
        return values;
    }

    /**
     * 获取当前实体对象的列名和值映射
     *
     * @return 列名和值的映射
     */
    protected Map<String, Object> getColumnValues() {
        return getColumnValues(entity);
    }

    /**
     * 获取实体与旧实体的差异字段和值
     *
     * @return 差异的字段名和新值的映射
     */
    protected Map<String, Object> getDifferentValues() {
        if (entity == null || oldEntity == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> newValues = getColumnValues(entity);
        Map<String, Object> oldValues = getColumnValues(oldEntity);
        Map<String, Object> differentValues = new HashMap<>();

        for (Map.Entry<String, Object> entry : newValues.entrySet()) {
            String column = entry.getKey();
            Object newValue = entry.getValue();
            Object oldValue = oldValues.get(column);

            // 比较新旧值，不同则添加到差异列表
            if (!Objects.equals(newValue, oldValue)) {
                differentValues.put(column, newValue);
            }
        }

        return differentValues;
    }

    /**
     * 获取字段对应的列名，根据needFullName参数决定是否返回完整列名（表名.列名）
     *
     * @param field        字段
     * @param needFullName 是否需要完整列名
     * @return 列名或完整列名
     */
    protected String getColumnName(Field field, boolean needFullName) {
        if (field == null) {
            throw new IllegalArgumentException("字段不能为null");
        }

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
        if (clazz == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(clazz.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(Column.class))
            .collect(Collectors.toList());
    }

    /**
     * 添加条件查询方法，包括IN、NOT IN、LIKE等各种操作符的便捷方法
     */
    public B where(Field field, SQLHelper.Operator operator, Object value) {
        if (field == null) {
            throw new IllegalArgumentException("字段不能为null");
        }

        String columnName = getFullColumnName(field);
        addWhereCondition(columnName, operator, value);
        return self();
    }

    /**
     * 添加条件查询方法（使用列名字符串）
     *
     * @param columnName 列名
     * @param operator   操作符
     * @param value      值
     * @return 当前构建器实例
     */
    public B where(String columnName, SQLHelper.Operator operator, Object value) {
        if (columnName == null || columnName.isEmpty()) {
            throw new IllegalArgumentException("列名不能为空");
        }

        addWhereCondition(columnName, operator, value);
        return self();
    }

    /**
     * 添加WHERE条件
     *
     * @param columnName 列名
     * @param operator   操作符
     * @param value      值
     */
    private void addWhereCondition(String columnName, SQLHelper.Operator operator, Object value) {
        String whereCondition;
        List<Object> conditionParams = new ArrayList<>();

        // 特殊处理 IS NULL 和 IS NOT NULL
        if (operator == SQLHelper.Operator.IS_NULL || operator == SQLHelper.Operator.IS_NOT_NULL) {
            if (value != null) {
                throw new IllegalArgumentException(operator.name() + " 操作符不需要值参数");
            }
            whereCondition = columnName + " " + operator.getSymbol();
        }
        // 特殊处理 IN 和 NOT IN
        else if (operator == SQLHelper.Operator.IN || operator == SQLHelper.Operator.NOT_IN) {
            if (!(value instanceof Collection<?>values)) {
                throw new IllegalArgumentException(operator.name() + " 操作符需要Collection类型的参数");
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
    }

    /**
     * 使用主键操作
     *
     * @return 当前构建器实例
     */
    public B byId() {
        this.usePrimaryKey = true;
        return self();
    }

    /**
     * 获取主键字段
     *
     * @return 主键字段
     */
    protected Field getPrimaryKeyField() {
        final Class<?> clazz = entityClass != null ? entityClass : (entity != null ? entity.getClass() : null);

        if (clazz == null) {
            throw new IllegalStateException("无法确定主键字段：entityClass和entity均为null");
        }

        return Arrays.stream(clazz.getDeclaredFields())
            .filter(field -> {
                Column column = field.getAnnotation(Column.class);
                return column != null && column.isPrimaryKey();
            })
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("实体类没有主键字段: " + clazz.getName()));
    }

    /**
     * 构建主键条件
     *
     * @return 主键条件SQL和参数
     */
    protected Pair<String, Object> buildPrimaryKeyCondition() {
        Field primaryKeyField = getPrimaryKeyField();
        String primaryKeyColumn = getColumnName(primaryKeyField);
        primaryKeyField.setAccessible(true);

        if (entity == null) {
            throw new IllegalStateException("使用byId()时实体对象不能为null");
        }

        try {
            Object primaryKeyValue = primaryKeyField.get(entity);
            if (primaryKeyValue == null) {
                throw new IllegalStateException("主键值不能为空");
            }
            return Pair.of(primaryKeyColumn + " = ?", primaryKeyValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("无法访问主键字段: " + primaryKeyField.getName(), e);
        }
    }

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
        return findParentGroupRecursive(rootGroup, current);
    }

    // 递归查找父组
    private WhereGroup findParentGroupRecursive(WhereGroup parent, WhereGroup target) {
        for (WhereGroup subGroup : parent.subGroups) {
            if (subGroup == target) {
                return parent;
            }
            WhereGroup found = findParentGroupRecursive(subGroup, target);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * 构建WHERE子句。
     *
     * @return WHERE子句
     */
    protected String buildWhereClause() {
        StringBuilder whereClauseBuilder = new StringBuilder();
        params = new ArrayList<>();
        rootGroup.buildClauseAndCollectParams(whereClauseBuilder, params);
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
        } else {
            // 确保WHERE子句已构建
            buildWhereClause();

            if (!whereClause.isEmpty()) {
                // 使用自定义条件
                sql += " WHERE " + whereClause;
                executeParams.addAll(params);
            } else if (requireWhere) {
                throw new IllegalStateException(operationName + "操作必须指定条件（使用byId()或where条件）");
            }
        }
        return sql;
    }

    /**
     * 执行SQL操作。
     *
     * @return 执行结果
     */
    public abstract Object execute();

    // 简化的条件方法，全部使用where方法实现
    public B whereIn(Field field, List<?> values) {
        return where(field, SQLHelper.Operator.IN, values);
    }

    public B whereNotIn(Field field, List<?> values) {
        return where(field, SQLHelper.Operator.NOT_IN, values);
    }

    public B whereLike(Field field, String pattern) {
        return where(field, SQLHelper.Operator.LIKE, pattern);
    }

    public B whereNull(Field field) {
        return where(field, SQLHelper.Operator.IS_NULL, null);
    }

    public B whereNotNull(Field field) {
        return where(field, SQLHelper.Operator.IS_NOT_NULL, null);
    }
}
