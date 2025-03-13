package com.pinkyudeer.wthaigd.helper.dataBase.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import com.pinkyudeer.wthaigd.helper.dataBase.SQLiteManager;

/**
 * 插入操作构建器。
 * 用于构建和执行SQL INSERT语句。
 *
 * @param <T> 实体类型
 */
public class InsertBuilder<T> extends BaseBuilder<T, InsertBuilder<T>> {

    /**
     * 构造函数。
     *
     * @param entity 要插入的实体对象
     */
    public InsertBuilder(T entity) {
        super(entity);
        if (entity == null) {
            throw new IllegalArgumentException("插入操作的实体对象不能为空");
        }
    }

    /**
     * 执行插入操作。
     *
     * @return 受影响的行数
     */
    @Override
    public Integer execute() {
        Map<String, Object> values = getColumnValues();
        if (values.isEmpty()) {
            throw new IllegalStateException("没有可以插入的列值");
        }

        StringJoiner columns = new StringJoiner(", ", "(", ")");
        StringJoiner placeholders = new StringJoiner(", ", "(", ")");
        List<Object> params = new ArrayList<>();

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            columns.add(entry.getKey());
            placeholders.add("?");
            params.add(entry.getValue());
        }

        String sql = String.format("INSERT INTO %s %s VALUES %s", getTableName(), columns, placeholders);
        return (Integer) SQLiteManager.executeSafeSQL(sql, params.toArray());
    }
}
