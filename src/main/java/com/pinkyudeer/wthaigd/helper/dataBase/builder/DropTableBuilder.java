package com.pinkyudeer.wthaigd.helper.dataBase.builder;

import java.util.ArrayList;
import java.util.List;

import com.pinkyudeer.wthaigd.helper.dataBase.builder.SQLUtils.SQLTableUtils;

/**
 * 表删除构建器。
 * 用于删除数据库表，支持单表和多表删除。
 *
 * @param <T> 实体类型
 */
public class DropTableBuilder<T> extends BaseDDLBuilder<T, DropTableBuilder<T>> {

    /** 表名列表 */
    private final List<String> tableNames = new ArrayList<>();
    /** 是否检查表存在 */
    private boolean ifExists = true;
    /** 是否级联删除 */
    private boolean cascade = false;

    /**
     * 构造函数。
     *
     * @param entityClass 实体类
     */
    public DropTableBuilder(Class<T> entityClass) {
        super(entityClass);
        if (entityClass != null) {
            this.tableNames.add(SQLTableUtils.getTableName(entityClass));
        }
    }

    /**
     * 添加表名。
     *
     * @param tableName 表名
     * @return 当前构建器实例
     */
    public DropTableBuilder<T> table(String tableName) {
        if (tableName != null && !tableName.isEmpty()) {
            this.tableNames.add(tableName);
        }
        return this;
    }

    /**
     * 添加多个表名。
     *
     * @param tableNames 表名列表
     * @return 当前构建器实例
     */
    public DropTableBuilder<T> tables(List<String> tableNames) {
        if (tableNames != null) {
            this.tableNames.addAll(tableNames);
        }
        return this;
    }

    /**
     * 设置是否检查表存在。
     *
     * @param ifExists 是否检查表存在
     * @return 当前构建器实例
     */
    public DropTableBuilder<T> ifExists(boolean ifExists) {
        this.ifExists = ifExists;
        return this;
    }

    /**
     * 设置是否级联删除。
     *
     * @param cascade 是否级联删除
     * @return 当前构建器实例
     */
    public DropTableBuilder<T> cascade(boolean cascade) {
        this.cascade = cascade;
        return this;
    }

    /**
     * 处理构建逻辑。
     * 实现父类的抽象方法，处理表删除的具体逻辑。
     */
    @Override
    protected void processBuild() {
        if (tableNames.isEmpty()) {
            throw new IllegalStateException("没有指定要删除的表");
        }

        List<String> dropSqls = new ArrayList<>();
        for (String tableName : tableNames) {
            StringBuilder sql = new StringBuilder("DROP TABLE ");
            if (ifExists) {
                sql.append("IF EXISTS ");
            }
            sql.append(tableName);
            if (cascade) {
                sql.append(" CASCADE");
            }
            dropSqls.add(sql.toString());
        }
        sqls = dropSqls;
    }
}
