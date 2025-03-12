package com.pinkyudeer.wthaigd.helper.dataBase.builder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.pinkyudeer.wthaigd.helper.dataBase.annotation.Reference;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.SQLUtils.SQLColumnUtils;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.SQLUtils.SQLIndexUtils;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.SQLUtils.SQLTableUtils;

/**
 * 表创建构建器。
 * 用于构建数据库表结构，包括创建表、添加外键约束和索引等。
 *
 * @param <T> 实体类型
 */
public class CreateTableBuilder<T> extends BaseDDLBuilder<T, CreateTableBuilder<T>> {

    /**
     * 构造函数。
     *
     * @param entityClass 实体类
     */
    public CreateTableBuilder(Class<T> entityClass) {
        super(entityClass);
    }

    /**
     * 处理构建逻辑。
     * 实现父类的抽象方法，处理表创建的具体逻辑。
     */
    @Override
    protected void processBuild() {
        // 只有一个实体类的简单情况
        if (entityClasses.size() == 1) {
            Class<?> entityClass = entityClasses.get(0);
            String tableName = SQLTableUtils.getTableName(entityClass);
            List<String> tableSqls = buildTableSql(entityClass, tableName);
            sqlMap.put(tableName, tableSqls);
            sqls = tableSqls;
        }
        // 多个实体类需要处理依赖关系
        else {
            // 收集所有表的依赖关系并生成建表SQL
            collectDependenciesAndBuildTableSql();

            // 拓扑排序表
            List<String> sortedTables = sortTablesByDependencies();

            // 根据排序结果合并SQL语句
            sqls = mergeSqlByOrder(sortedTables);
        }
    }

    /**
     * 收集所有表的依赖关系并生成建表SQL。
     */
    private void collectDependenciesAndBuildTableSql() {
        for (Class<?> entityClass : entityClasses) {
            String tableName = SQLTableUtils.getTableName(entityClass);

            // 生成创建表SQL并存储，同时收集依赖关系
            List<String> tableSqls = buildTableSql(entityClass, tableName);
            sqlMap.put(tableName, tableSqls);
        }
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
        Map<String, List<Field>> referFieldsByTable = referFields.stream()
            .collect(
                Collectors
                    .groupingBy(field -> SQLColumnUtils.getReferencedTableName(field.getAnnotation(Reference.class))));

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
