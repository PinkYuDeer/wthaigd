package com.pinkyudeer.wthaigd.helper.dataBase.builder;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.pinkyudeer.wthaigd.helper.dataBase.SQLiteManager;
import com.pinkyudeer.wthaigd.helper.dataBase.annotation.Table;

/**
 * 查询操作构建器。
 * 用于构建和执行SQL SELECT语句，支持复杂的查询条件、连接、分组和排序。
 *
 * @param <T> 实体类型
 */
public class SelectBuilder<T> extends BaseBuilder<T, SelectBuilder<T>> {

    /**
     * 构造函数。
     *
     * @param entity 查询的实体对象
     */
    public SelectBuilder(T entity) {
        super(entity);
    }

    /**
     * 基于实体类的构造函数
     *
     * @param entityClass 实体类
     */
    public SelectBuilder(Class<T> entityClass) {
        super(entityClass);
        if (entityClass == null) {
            throw new IllegalArgumentException("查询操作的实体类不能为空");
        }
    }

    /**
     * 基于比对模式的构造函数
     *
     * @param entity    当前实体
     * @param oldEntity 旧实体（用于比对）
     */
    public SelectBuilder(T entity, T oldEntity) {
        super(entity, oldEntity);
        if (entity == null) {
            throw new IllegalArgumentException("查询操作的实体对象不能为空");
        }
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
        if (fields == null || fields.length == 0) {
            throw new IllegalArgumentException("分组字段不能为空");
        }
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
        if (fields == null || fields.length == 0) {
            throw new IllegalArgumentException("排序字段不能为空");
        }
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
        if (clause == null || clause.isEmpty()) {
            throw new IllegalArgumentException("HAVING子句不能为空");
        }
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
        if (limit <= 0) {
            throw new IllegalArgumentException("LIMIT值必须大于0");
        }
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
        if (offset < 0) {
            throw new IllegalArgumentException("OFFSET值不能为负数");
        }
        this.offset = offset;
        return this;
    }

    /**
     * 执行查询操作。
     *
     * @return 查询结果集
     */
    @Override
    public ResultSet execute() {
        StringBuilder query = new StringBuilder("SELECT ");
        query.append(selectColumns == null || selectColumns.length == 0 ? "*" : String.join(", ", selectColumns));
        query.append(" FROM ")
            .append(getTableName());

        if (!joins.isEmpty()) query.append(joins);

        // 如果是比对模式，使用差异值作为条件
        if (compareMode && oldEntity != null) {
            // 获取差异字段
            Map<String, Object> diffValues = getDifferentValues();
            if (!diffValues.isEmpty()) {
                for (Map.Entry<String, Object> entry : diffValues.entrySet()) {
                    String columnName = entry.getKey();
                    Object value = entry.getValue();
                    currentGroup.addCondition(columnName + " = ?", value);
                }
            }
        }

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
