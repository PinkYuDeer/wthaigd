package com.pinkyudeer.wthaigd.helper.dataBase.builder;

import java.util.ArrayList;
import java.util.List;

import com.pinkyudeer.wthaigd.helper.dataBase.builder.SQLUtils.SQLTableUtils;

/**
 * 表结构修改构建器。
 * 用于修改数据库表结构，包括添加列、删除列、修改列等操作。
 *
 * @param <T> 实体类型
 */
public class AlterTableBuilder<T> extends BaseDDLBuilder<T, AlterTableBuilder<T>> {

    /** 表名 */
    private String tableName;
    /** 是否需要重建表 */
    private boolean needRebuild = false;
    /** 列操作列表 */
    private final List<ColumnOperation> columnOperations = new ArrayList<>();

    /**
     * 列操作类型枚举
     */
    private enum OperationType {
        /** 添加列 */
        ADD,
        /** 删除列 */
        DROP,
        /** 重命名列 */
        RENAME,
        /** 修改列类型 */
        MODIFY
    }

    /**
     * 列操作类
     */
    private static class ColumnOperation {

        private final OperationType type;
        private final String columnName;
        private final String newColumnName;
        private final String columnDefinition;

        /**
         * 构造函数
         *
         * @param type             操作类型
         * @param columnName       列名
         * @param newColumnName    新列名（仅用于重命名操作）
         * @param columnDefinition 列定义（仅用于添加和修改操作）
         */
        public ColumnOperation(OperationType type, String columnName, String newColumnName, String columnDefinition) {
            this.type = type;
            this.columnName = columnName;
            this.newColumnName = newColumnName;
            this.columnDefinition = columnDefinition;
        }

        /**
         * 生成SQL语句
         *
         * @param tableName 表名
         * @return SQL语句
         */
        public String toSql(String tableName) {
            return switch (type) {
                case ADD -> String.format("ALTER TABLE %s ADD COLUMN %s %s", tableName, columnName, columnDefinition);
                case DROP -> String.format("ALTER TABLE %s DROP COLUMN %s", tableName, columnName);
                case RENAME -> String
                    .format("ALTER TABLE %s RENAME COLUMN %s TO %s", tableName, columnName, newColumnName);
                case MODIFY -> String
                    .format("ALTER TABLE %s MODIFY COLUMN %s %s", tableName, columnName, columnDefinition);
            };
        }
    }

    /**
     * 构造函数。
     *
     * @param entityClass 实体类
     */
    public AlterTableBuilder(Class<T> entityClass) {
        super(entityClass);
        if (entityClass != null) {
            this.tableName = SQLTableUtils.getTableName(entityClass);
        }
    }

    /**
     * 设置表名。
     *
     * @param tableName 表名
     * @return 当前构建器实例
     */
    public AlterTableBuilder<T> table(String tableName) {
        this.tableName = tableName;
        return this;
    }

    /**
     * 添加列。
     *
     * @param columnName       列名
     * @param columnDefinition 列定义（类型、约束等）
     * @return 当前构建器实例
     */
    public AlterTableBuilder<T> addColumn(String columnName, String columnDefinition) {
        columnOperations.add(new ColumnOperation(OperationType.ADD, columnName, null, columnDefinition));
        return this;
    }

    /**
     * 删除列。
     *
     * @param columnName 列名
     * @return 当前构建器实例
     */
    public AlterTableBuilder<T> dropColumn(String columnName) {
        columnOperations.add(new ColumnOperation(OperationType.DROP, columnName, null, null));
        needRebuild = true; // SQLite需要重建表来删除列
        return this;
    }

    /**
     * 重命名列。
     *
     * @param oldColumnName 旧列名
     * @param newColumnName 新列名
     * @return 当前构建器实例
     */
    public AlterTableBuilder<T> renameColumn(String oldColumnName, String newColumnName) {
        columnOperations.add(new ColumnOperation(OperationType.RENAME, oldColumnName, newColumnName, null));
        needRebuild = true; // SQLite需要重建表来重命名列
        return this;
    }

    /**
     * 修改列定义。
     *
     * @param columnName    列名
     * @param newDefinition 新的列定义
     * @return 当前构建器实例
     */
    public AlterTableBuilder<T> modifyColumn(String columnName, String newDefinition) {
        columnOperations.add(new ColumnOperation(OperationType.MODIFY, columnName, null, newDefinition));
        needRebuild = true; // SQLite需要重建表来修改列
        return this;
    }

    /**
     * 处理构建逻辑。
     * 实现父类的抽象方法，处理表修改的具体逻辑。
     */
    @Override
    protected void processBuild() {
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalStateException("表名不能为空");
        }

        if (columnOperations.isEmpty()) {
            throw new IllegalStateException("没有指定任何列操作");
        }

        // SQLite不支持直接修改表结构的某些操作，需要通过创建新表并复制数据来实现
        if (needRebuild) {
            buildRebuildTableSql();
        } else {
            buildSimpleAlterTableSql();
        }
    }

    /**
     * 构建简单的ALTER TABLE语句。
     * 适用于SQLite支持的简单操作，如添加列。
     */
    private void buildSimpleAlterTableSql() {
        List<String> alterSqls = new ArrayList<>();
        for (ColumnOperation operation : columnOperations) {
            if (operation.type == OperationType.ADD) {
                alterSqls.add(operation.toSql(tableName));
            } else {
                throw new IllegalStateException("SQLite不支持直接" + operation.type + "操作，请使用表重建方式");
            }
        }
        sqls = alterSqls;
    }

    /**
     * 构建重建表的SQL语句。
     * 适用于SQLite不支持直接修改的操作，如删除列、修改列类型等。
     */
    private void buildRebuildTableSql() {
        // 这里需要实现表的重建逻辑
        // 1. 获取当前表结构
        // 2. 应用修改操作
        // 3. 创建临时表
        // 4. 复制数据
        // 5. 删除旧表
        // 6. 重命名临时表

        // 由于实现复杂，这里仅提供一个简化版本
        List<String> rebuildSqls = new ArrayList<>();

        // 获取表信息的SQL
        String getTableInfoSql = String.format("PRAGMA table_info(%s)", tableName);
        rebuildSqls.add("-- 需要执行以下SQL获取表结构: " + getTableInfoSql);

        // 创建临时表
        String tempTableName = tableName + "_temp";
        rebuildSqls.add(String.format("CREATE TABLE %s AS SELECT * FROM %s", tempTableName, tableName));

        // 应用修改
        for (ColumnOperation operation : columnOperations) {
            rebuildSqls.add("-- 应用操作: " + operation.type + " " + operation.columnName);
        }

        // 删除旧表并重命名
        rebuildSqls.add(String.format("DROP TABLE %s", tableName));
        rebuildSqls.add(String.format("ALTER TABLE %s RENAME TO %s", tempTableName, tableName));

        // 注意：这只是一个示例，实际实现需要更复杂的逻辑
        rebuildSqls.add("-- 注意：表重建操作需要更复杂的实现，这里只是示例");

        sqls = rebuildSqls;
    }
}
