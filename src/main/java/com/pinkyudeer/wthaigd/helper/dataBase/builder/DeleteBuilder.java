package com.pinkyudeer.wthaigd.helper.dataBase.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.pinkyudeer.wthaigd.helper.dataBase.SQLiteManager;

/**
 * 删除操作构建器。
 * 用于构建和执行SQL DELETE语句。
 *
 * @param <T> 实体类型
 */
public class DeleteBuilder<T> extends BaseBuilder<T, DeleteBuilder<T>> {

    /**
     * 基于实体对象的构造函数
     *
     * @param entity 实体对象
     */
    public DeleteBuilder(T entity) {
        super(entity);
    }

    /**
     * 基于实体类的构造函数
     *
     * @param entityClass 实体类
     */
    public DeleteBuilder(Class<T> entityClass) {
        super(entityClass);
        if (entityClass == null) {
            throw new IllegalArgumentException("删除操作的实体类不能为空");
        }
    }

    /**
     * 基于比对模式的构造函数
     *
     * @param entity    当前实体
     * @param oldEntity 旧实体（用于比对）
     */
    public DeleteBuilder(T entity, T oldEntity) {
        super(entity, oldEntity);
        if (entity == null) {
            throw new IllegalArgumentException("删除操作的实体对象不能为空");
        }
    }

    @Override
    public Integer execute() {
        String sql = "DELETE FROM " + getTableName();
        List<Object> executeParams = new ArrayList<>();

        // 如果是比对模式，使用差异值作为条件
        if (compareMode && oldEntity != null) {
            // 获取差异字段
            Map<String, Object> diffValues = getDifferentValues();
            if (!diffValues.isEmpty()) {
                for (Map.Entry<String, Object> entry : diffValues.entrySet()) {
                    where(
                        entry.getKey(),
                        com.pinkyudeer.wthaigd.helper.dataBase.SQLHelper.Operator.EQ,
                        entry.getValue());
                }
            }
        }

        sql = addWhereClause(sql, executeParams, true, "删除");

        return (Integer) SQLiteManager.executeSafeSQL(sql, executeParams.toArray());
    }
}
