package com.pinkyudeer.wthaigd.helper.dataBase.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.pinkyudeer.wthaigd.helper.dataBase.SQLiteManager;

/**
 * 更新操作构建器。
 * 用于构建和执行SQL UPDATE语句。
 *
 * @param <T> 实体类型
 */
public class UpdateBuilder<T> extends BaseBuilder<T, UpdateBuilder<T>> {

    /**
     * 基于实体对象的构造函数
     *
     * @param entity 实体对象
     */
    public UpdateBuilder(T entity) {
        super(entity);
        if (entity == null) {
            throw new IllegalArgumentException("更新操作的实体对象不能为空");
        }
    }

    /**
     * 基于实体类的构造函数
     *
     * @param entityClass 实体类
     */
    public UpdateBuilder(Class<T> entityClass) {
        super(entityClass);
        if (entityClass == null) {
            throw new IllegalArgumentException("更新操作的实体类不能为空");
        }
    }

    /**
     * 基于比对模式的构造函数
     *
     * @param entity    当前实体
     * @param oldEntity 旧实体（用于比对）
     */
    public UpdateBuilder(T entity, T oldEntity) {
        super(entity, oldEntity);
        if (entity == null) {
            throw new IllegalArgumentException("更新操作的实体对象不能为空");
        }
    }

    /**
     * 设置旧实体对象，用于比较并只更新变更的字段
     *
     * @param oldEntity 用于比较的实体对象
     * @return this
     */
    public UpdateBuilder<T> onlyChangesFrom(T oldEntity) {
        // 使用父类中的oldEntity，启用比较模式
        this.compareMode = true;
        return new UpdateBuilder<>(this.entity, oldEntity);
    }

    @Override
    public Integer execute() {
        Map<String, Object> columnValues;

        // 检查是否启用了比较模式
        if (compareMode && oldEntity != null) {
            // 使用基类的getDifferentValues方法获取差异字段
            columnValues = getDifferentValues();
            if (columnValues.isEmpty()) {
                return 0; // 没有变化，不执行更新
            }
        } else {
            // 使用所有字段（原始行为）
            columnValues = getColumnValues();

            // 确保有字段被更新
            if (columnValues.isEmpty()) {
                throw new IllegalStateException("没有可以更新的列值");
            }
        }

        StringBuilder setClause = new StringBuilder();
        List<Object> executeParams = new ArrayList<>();
        boolean first = true;

        for (Map.Entry<String, Object> entry : columnValues.entrySet()) {
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
