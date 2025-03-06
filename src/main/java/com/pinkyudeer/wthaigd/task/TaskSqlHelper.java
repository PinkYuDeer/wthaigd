package com.pinkyudeer.wthaigd.task;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.annotation.Nonnull;

import org.reflections.Reflections;

import com.pinkyudeer.wthaigd.entity.task.Player;
import com.pinkyudeer.wthaigd.entity.task.Tag;
import com.pinkyudeer.wthaigd.entity.task.Task;
import com.pinkyudeer.wthaigd.entity.task.Team;
import com.pinkyudeer.wthaigd.helper.UtilHelper;
import com.pinkyudeer.wthaigd.helper.dataBase.annotation.Column;
import com.pinkyudeer.wthaigd.helper.dataBase.annotation.FieldCheck;
import com.pinkyudeer.wthaigd.helper.dataBase.annotation.Reference;
import com.pinkyudeer.wthaigd.helper.dataBase.annotation.Table;

public class TaskSqlHelper {

    public record SqlParameter(String sql, List<Object> parameters) {

        @Override
        public String toString() {
            return "SqlParameter[" + "sql=" + sql + ", " + "parameters=" + parameters + ']';
        }
    }

    public static class init {

        // 根据注解生成相应的 SQLite CHECK 约束
        public static String generateCheckConstraint(Field field, String columnName) {
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
                        // 对于枚举类型，可以检查是否在指定的值中
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

        // 约束
        private static String generateIdReferences(Field field, Map<String, List<String>> tableRefMap,
            String tableName) {
            Reference reference = field.getAnnotation(Reference.class);
            if (reference == null) return null;

            StringBuilder references = new StringBuilder();
            references.append("FOREIGN KEY(")
                .append(field.getName())
                .append(") REFERENCES ");

            String referencedTableName = getReferencedTableName(reference);
            references.append(referencedTableName);
            references.append("(id) ON DELETE CASCADE ON UPDATE CASCADE;");

            // 更新引用关系映射
            List<String> referencingTables = tableRefMap.computeIfAbsent(referencedTableName, k -> new ArrayList<>());
            if (!referencingTables.contains(tableName)) {
                referencingTables.add(tableName);
            }
            return references.toString();
        }

        // 获取枚举类的值
        private static String getEnumValues(Class<?> enumClass) {
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

        // 生成 CREATE TABLE SQL
        public static Map<String, List<String>> generateCreateTableSql(Class<?> clazz,
            Map<String, List<String>> tableRefMap) {
            Table table = clazz.getAnnotation(Table.class);
            if (table == null) {
                return null;
            }
            StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
            String tableName = table.name();
            sql.append(tableName)
                .append(" (");

            List<String> columnDefinitions = new ArrayList<>(); // 列定义构造器
            List<Field> refer = new ArrayList<>(); // 有外键约束的字段
            List<String> createTableSqlList = new ArrayList<>(); // 创建表语句列表
            // 索引映射，String为索引名称，List<String>为所在索引列表。
            Map<String, List<String>> indexMap = new HashMap<>();

            try {
                for (Field field : UtilHelper.getAllFieldsReverse(clazz)) {
                    Column column = field.getAnnotation(Column.class);
                    if (column == null) continue;
                    String columnName = column.name();
                    StringBuilder columnDefinition = new StringBuilder();
                    columnDefinition.append(columnName)
                        .append(" ");

                    // 根据字段类型生成 SQLite 数据类型
                    switch (field.getType()
                        .getSimpleName()) {
                        case "String":
                        case "UUID":
                            columnDefinition.append("TEXT");
                            break;
                        case "int":
                        case "Integer":
                            columnDefinition.append("INTEGER");
                            break;
                        case "long":
                        case "Long":
                            columnDefinition.append("BIGINT");
                            break;
                        case "double":
                        case "Double":
                        case "Duration":
                            columnDefinition.append("REAL");
                            break;
                        case "boolean":
                        case "Boolean":
                            columnDefinition.append("BOOLEAN");
                            break;
                        case "Date":
                        case "LocalDateTime":
                            columnDefinition.append("TIMESTAMP");
                            break;
                        default:
                            if (field.getType()
                                .isEnum()) {
                                columnDefinition.append("TEXT");
                            } else {
                                throw new IllegalArgumentException("Unsupported field type: " + field.getType());
                            }
                            break;
                    }

                    // 如果column中的defaultValue不为空，则添加默认值
                    if (!"".equals(column.defaultValue())) {
                        columnDefinition.append(" DEFAULT ")
                            .append(column.defaultValue());
                    }

                    // 获取字段的检查约束
                    String checkConstraint = generateCheckConstraint(field, columnName);
                    if (!checkConstraint.isEmpty()) {
                        columnDefinition.append(" ")
                            .append(checkConstraint);
                    }
                    columnDefinitions.add(columnDefinition.toString());

                    // 检查字段是否有外键约束
                    if (generateIdReferences(field, tableRefMap, tableName) != null) {
                        refer.add(field);
                    }

                    // 检查字段是否有唯一约束
                    if (column.isUnique()) {
                        columnDefinitions.add(" UNIQUE");
                    }

                    // 检查字段是否有主键约束
                    if (column.isPrimaryKey()) {
                        columnDefinitions.add(" PRIMARY KEY");
                    }

                    // 检查字段是否有索引
                    if (column.index().length > 0) {
                        indexMap.put(columnName, Arrays.asList(column.index()));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Error generating table SQL for " + clazz.getName(), e);
            }

            sql.append(String.join(", ", columnDefinitions));
            sql.append(");");
            createTableSqlList.add(sql.toString());

            // 添加外键约束
            for (Field field : refer) {
                String foreignKey = generateIdReferences(field, tableRefMap, tableName);
                if (foreignKey != null) {
                    createTableSqlList.add(foreignKey);
                }
            }

            // 添加索引
            Map<String, List<String>> indexMapReverse = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : indexMap.entrySet()) {
                String columnName = entry.getKey();
                List<String> indexNames = entry.getValue();
                for (String indexName : indexNames) {
                    indexMapReverse.computeIfAbsent(indexName, k -> new ArrayList<>())
                        .add(columnName);
                }
            }
            for (Map.Entry<String, List<String>> entry : indexMapReverse.entrySet()) {
                String indexName = entry.getKey();
                List<String> columns = entry.getValue();
                createTableSqlList.add(
                    "CREATE INDEX IF NOT EXISTS " + indexName
                        + " ON "
                        + tableName
                        + "("
                        + String.join(", ", columns)
                        + ");");
            }

            Map<String, List<String>> createTableSql = new HashMap<>();
            createTableSql.put(tableName, createTableSqlList);
            return createTableSql;
        }

        // 扫描task.entity包下的所有类generateCreateTableSql
        public static Map<String, List<String>> generateAllCreateTableSql() {
            // 使用 Reflections 库扫描指定包
            Reflections reflections = new Reflections("com.pinkyudeer.wthaigd.task.entity");
            Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(Table.class);
            Map<String, List<String>> createTableSqlList = new HashMap<>();
            Map<String, List<String>> tableRefMap = new HashMap<>();
            for (Class<?> clazz : annotatedClasses) {
                Map<String, List<String>> createTableSql = generateCreateTableSql(clazz, tableRefMap);
                if (createTableSql != null) {
                    createTableSqlList.putAll(createTableSql);
                }
            }
            createTableSqlList = sortSqlListByRef(tableRefMap, createTableSqlList);
            return createTableSqlList;
        }
    }

    @Nonnull
    private static Map<String, List<String>> sortSqlListByRef(Map<String, List<String>> tableRefMap,
        Map<String, List<String>> createTableSqlList) {
        // 根据依赖关系对建表语句进行排序
        Map<String, Integer> inDegree = new HashMap<>(); // 记录每个表的入度
        Map<String, List<String>> graph = new HashMap<>(); // 邻接表表示依赖图

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

        // 拓扑排序
        Queue<String> queue = new LinkedList<>();
        List<String> sortedTables = new ArrayList<>();

        // 将入度为0的表加入队列
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }

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
        // 检查是否存在循环依赖
        if (sortedTables.size() != inDegree.size()) {
            // 找出循环依赖的具体路径
            List<String> cyclePath = new ArrayList<>();
            Set<String> visited = new HashSet<>();
            Set<String> recursionStack = new HashSet<>();

            // 从未处理的节点中找出循环
            for (String table : inDegree.keySet()) {
                if (!sortedTables.contains(table)) {
                    findCycle(table, graph, visited, recursionStack, cyclePath);
                    if (!cyclePath.isEmpty()) {
                        // 将循环路径格式化为字符串
                        String cycleStr = String.join(" -> ", cyclePath) + " -> " + cyclePath.get(0);
                        throw new IllegalStateException("检测到循环依赖关系,循环路径为: " + cycleStr);
                    }
                }
            }
            throw new IllegalStateException("检测到循环依赖关系,请检查表之间的外键引用");
        }

        // 按照排序后的顺序重新组织建表语句
        Map<String, List<String>> sortedCreateTableSqlList = new LinkedHashMap<>();
        for (String tableName : sortedTables) {
            if (createTableSqlList.containsKey(tableName)) {
                sortedCreateTableSqlList.put(tableName, createTableSqlList.get(tableName));
            }
        }

        // 将不在依赖关系中的表添加到最后
        for (String tableName : createTableSqlList.keySet()) {
            if (!sortedCreateTableSqlList.containsKey(tableName)) {
                sortedCreateTableSqlList.put(tableName, createTableSqlList.get(tableName));
            }
        }
        createTableSqlList = sortedCreateTableSqlList;
        return createTableSqlList;
    }

    private static String getReferencedTableName(Reference reference) {
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

    private static boolean findCycle(String table, Map<String, List<String>> graph, Set<String> visited,
        Set<String> recursionStack, List<String> cyclePath) {
        visited.add(table);
        recursionStack.add(table);

        for (String dependent : graph.get(table)) {
            if (!visited.contains(dependent)) {
                if (findCycle(dependent, graph, visited, recursionStack, cyclePath)) {
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

    private static String getTableName(Class<?> clazz) {
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new IllegalArgumentException("Entity must have @Table annotation.");
        }
        return tableAnnotation.name();
    }

    private static String getColumnName(Column columnAnnotation) {
        if (columnAnnotation.name()
            .isEmpty()) {
            throw new IllegalArgumentException("Try to get empty column name from @Column");
        }
        return columnAnnotation.name();
    }

    public static class add {

        private static boolean shouldSkipField(Column columnAnnotation, Object value) {
            if ("".equals(columnAnnotation.defaultValue())) return false;

            String stringValue = convertToString(value);
            return columnAnnotation.defaultValue()
                .equals(stringValue);
        }

        private static String convertToString(Object value) {
            if (value == null) return "null";
            // 扩展点：可在此处添加日期/数字等特殊类型处理
            return value.toString();
        }

        /**
         * 根据传入的实体类向数据库插入数据
         *
         * @param entity 代表要插入到数据库中的实体类的Class对象
         * @return 如果插入成功返回true，否则返回false
         */
        public static boolean autoInsert(Class<?> entity) {
            return false;
        }
    }

    public static class delete {
    }

    public static class update {
    }

    public static class select {
    }
}
