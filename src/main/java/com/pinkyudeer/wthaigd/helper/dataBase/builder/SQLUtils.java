package com.pinkyudeer.wthaigd.helper.dataBase.builder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.pinkyudeer.wthaigd.helper.UtilHelper;
import com.pinkyudeer.wthaigd.helper.dataBase.annotation.Column;
import com.pinkyudeer.wthaigd.helper.dataBase.annotation.FieldCheck;
import com.pinkyudeer.wthaigd.helper.dataBase.annotation.Reference;
import com.pinkyudeer.wthaigd.helper.dataBase.annotation.Table;

/**
 * SQL工具类集合。
 * 包含列定义、索引处理和表结构处理的工具方法。
 */
public class SQLUtils {

    /**
     * SQL列定义和约束的工具类。
     * 提供列类型转换、约束生成等功能。
     */
    public static class SQLColumnUtils {

        /**
         * 获取SQLite数据类型。
         * 根据Java字段类型转换为对应的SQLite数据类型。
         *
         * @param field 字段
         * @return SQLite数据类型
         */
        public static String getSqliteType(Field field) {
            Class<?> type = field.getType();
            String typeName = type.getSimpleName();

            if (typeName.equals("String") || typeName.equals("UUID") || type.isEnum()) {
                return "TEXT";
            } else {
                return switch (typeName) {
                    // 整数类型
                    case "int", "Integer" -> "INTEGER";
                    // 长整数类型
                    case "long", "Long" -> "BIGINT";
                    // 浮点类型
                    case "double", "Double", "Duration" -> "REAL";
                    // 布尔类型
                    case "boolean", "Boolean" -> "BOOLEAN";
                    // 时间类型
                    case "Date", "LocalDateTime" -> "TIMESTAMP";
                    default -> throw new IllegalArgumentException("不支持的字段类型: " + type);
                };
            }
        }

        /**
         * 构建列定义。
         * 根据字段和注解生成完整的列定义SQL，包括数据类型和约束。
         *
         * @param field  字段
         * @param column 列注解
         * @return 列定义SQL
         */
        public static String buildColumnDefinition(Field field, Column column) {
            StringBuilder columnDef = new StringBuilder();
            String columnName = column.name();

            columnDef.append(columnName)
                .append(" ");

            // 添加数据类型
            columnDef.append(getSqliteType(field));

            // 添加默认值
            if (!"".equals(column.defaultValue())) {
                columnDef.append(" DEFAULT ")
                    .append(column.defaultValue());
            }

            // 添加NOT NULL约束
            if (field.getAnnotation(javax.annotation.Nonnull.class) != null) {
                columnDef.append(" NOT NULL");
            }

            // 添加主键约束
            if (column.isPrimaryKey()) {
                columnDef.append(" PRIMARY KEY");
            }

            // 添加唯一约束
            if (column.isUnique()) {
                columnDef.append(" UNIQUE");
            }

            // 添加字段检查约束
            FieldCheck fieldCheck = field.getAnnotation(FieldCheck.class);
            if (fieldCheck != null) {
                // 特殊处理枚举类型，添加CHECK约束确保值在枚举范围内
                if (field.getType()
                    .isEnum()) {
                    String enumValues = getEnumValues(field.getType());
                    if (!enumValues.isEmpty()) {
                        columnDef.append(" CHECK(")
                            .append(columnName)
                            .append(" IN (")
                            .append(enumValues)
                            .append("))");
                    }
                } else {
                    // 根据检查类型生成约束
                    switch (fieldCheck.type()) {
                        case RANGE:
                            columnDef.append(" CHECK(")
                                .append(columnName)
                                .append(" >= ")
                                .append(fieldCheck.min())
                                .append(" AND ")
                                .append(columnName)
                                .append(" <= ")
                                .append(fieldCheck.max())
                                .append(")");
                            break;
                        case GLOB:
                            columnDef.append(" CHECK(")
                                .append(columnName)
                                .append(" GLOB '")
                                .append(fieldCheck.glob())
                                .append("')");
                            break;
                        case LENGTH:
                            columnDef.append(" CHECK(LENGTH(")
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
                            columnDef.append(" CHECK(LENGTH(")
                                .append(columnName)
                                .append(") = 36)");
                            break;
                        case ENUM:
                            if (fieldCheck.dataType()
                                .isEnum()) {
                                columnDef.append(" CHECK(")
                                    .append(columnName)
                                    .append(" IN (")
                                    .append(getEnumValues(fieldCheck.dataType()))
                                    .append("))");
                            }
                            break;
                        case NOT_VALUE:
                            columnDef.append(" CHECK(")
                                .append(columnName)
                                .append(" != ")
                                .append(fieldCheck.notValue())
                                .append(")");
                            break;
                        case MIN:
                            columnDef.append(" CHECK(")
                                .append(columnName)
                                .append(" >= ")
                                .append(fieldCheck.min())
                                .append(")");
                            break;
                        case MAX:
                            columnDef.append(" CHECK(")
                                .append(columnName)
                                .append(" <= ")
                                .append(fieldCheck.max())
                                .append(")");
                            break;
                    }
                }
            }
            return columnDef.toString();
        }

        /**
         * 获取枚举类的所有值，用于构建CHECK约束。
         *
         * @param enumClass 枚举类
         * @return 格式化的枚举值列表
         */
        public static String getEnumValues(Class<?> enumClass) {
            if (!enumClass.isEnum()) {
                return "";
            }

            Object[] constants = enumClass.getEnumConstants();
            return Arrays.stream(constants)
                .map(constant -> "'" + constant.toString() + "'")
                .collect(Collectors.joining(", "));
        }

        /**
         * 获取引用的表名。
         * 根据Reference注解获取引用的表名。
         *
         * @param reference 引用注解
         * @return 引用的表名
         */
        public static String getReferencedTableName(Reference reference) {
            if (reference == null) {
                throw new IllegalArgumentException("引用注解不能为null");
            }

            // 从实体类派生表名
            Table table = reference.entity()
                .getAnnotation(Table.class);
            if (table != null) {
                return table.name();
            } else {
                throw new IllegalArgumentException("实体类必须使用@Table注解");
            }
        }
    }

    /**
     * SQL索引处理的工具类。
     * 提供索引信息的收集和SQL生成功能。
     */
    public static class SQLIndexUtils {

        /**
         * 处理并生成索引SQL语句。
         *
         * @param indexMap  索引映射（列名到索引名的映射）
         * @param tableName 表名
         * @param sqls      SQL语句列表，生成的索引SQL会添加到这个列表中
         */
        public static void processAndAddIndexes(Map<String, List<String>> indexMap, String tableName,
            List<String> sqls) {
            // 反转索引映射（索引名到列名的映射）
            Map<String, List<String>> indexMapReverse = new HashMap<>();

            for (Map.Entry<String, List<String>> entry : indexMap.entrySet()) {
                String columnName = entry.getKey();
                for (String indexName : entry.getValue()) {
                    indexMapReverse.computeIfAbsent(indexName, k -> new ArrayList<>())
                        .add(columnName);
                }
            }

            // 生成并添加索引SQL
            for (Map.Entry<String, List<String>> entry : indexMapReverse.entrySet()) {
                String indexSql = String.format(
                    "CREATE INDEX IF NOT EXISTS %s ON %s(%s);",
                    entry.getKey(),
                    tableName,
                    String.join(", ", entry.getValue()));
                sqls.add(indexSql);
            }
        }

        /**
         * 收集字段的索引信息。
         *
         * @param field    字段
         * @param column   列注解
         * @param indexMap 索引映射，用于存储收集结果
         */
        public static void collectIndexInfo(Field field, Column column, Map<String, List<String>> indexMap) {
            if (column.index().length > 0) {
                indexMap.put(column.name(), Arrays.asList(column.index()));
            }
        }
    }

    /**
     * SQL表结构处理的工具类。
     * 提供表结构信息的收集和SQL生成功能。
     */
    public static class SQLTableUtils {

        /**
         * 处理实体类的所有字段。
         *
         * @param entityClass       实体类
         * @param columnDefinitions 列定义列表
         * @param referFields       引用字段列表
         * @param indexMap          索引映射
         */
        public static void processFields(Class<?> entityClass, List<String> columnDefinitions, List<Field> referFields,
            Map<String, List<String>> indexMap) {
            for (Field field : UtilHelper.getAllFieldsReverse(entityClass)) {
                Column column = field.getAnnotation(Column.class);
                if (column == null) continue;

                columnDefinitions.add(SQLColumnUtils.buildColumnDefinition(field, column));

                if (field.isAnnotationPresent(Reference.class)) {
                    referFields.add(field);
                }

                SQLIndexUtils.collectIndexInfo(field, column, indexMap);
            }
        }

        /**
         * 生成创建表SQL。
         *
         * @param tableName         表名
         * @param columnDefinitions 列定义列表
         * @param foreignKeys       外键约束列表
         * @return 创建表SQL
         */
        public static String generateCreateTableSql(String tableName, List<String> columnDefinitions,
            List<String> foreignKeys) {
            List<String> allDefinitions = new ArrayList<>(columnDefinitions);
            if (foreignKeys != null && !foreignKeys.isEmpty()) {
                allDefinitions.addAll(foreignKeys);
            }
            return String.format("CREATE TABLE IF NOT EXISTS %s (%s);", tableName, String.join(", ", allDefinitions));
        }

        /**
         * 获取实体类对应的表名。
         *
         * @param entityClass 实体类
         * @return 表名
         */
        public static String getTableName(Class<?> entityClass) {
            Table table = entityClass.getAnnotation(Table.class);
            if (table == null) {
                throw new IllegalArgumentException("类必须使用@Table注解");
            }
            return table.name();
        }

        /**
         * 生成外键SQL。
         *
         * @param fields          引用字段列表
         * @param referencedTable 引用表名
         * @return 外键SQL
         */
        public static String generateForeignKeySql(List<Field> fields, String referencedTable) {
            // 构建字段名列表
            String fieldNames = fields.stream()
                .map(field -> {
                    Column column = field.getAnnotation(Column.class);
                    return column != null ? column.name() : field.getName();
                })
                .collect(Collectors.joining(", "));

            // 构建引用字段名列表
            String referencedFields = fields.stream()
                .map(field -> {
                    Reference reference = field.getAnnotation(Reference.class);
                    return reference != null ? reference.fieldName() : "id";
                })
                .collect(Collectors.joining(", "));

            // 生成外键约束SQL，使用CASCADE作为默认操作
            return String.format(
                "FOREIGN KEY(%s) REFERENCES %s(%s) ON DELETE CASCADE ON UPDATE CASCADE",
                fieldNames,
                referencedTable,
                referencedFields);
        }
    }
}
