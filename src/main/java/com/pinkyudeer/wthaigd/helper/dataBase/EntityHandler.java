package com.pinkyudeer.wthaigd.helper.dataBase;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.pinkyudeer.wthaigd.helper.dataBase.annotation.Column;

public class EntityHandler<T> {

    /**
     * 将当前ResultSet行映射为实体对象。调用方需要确保ResultSet已定位到一行数据。
     *
     * @param rs   当前行的ResultSet对象
     * @param type 目标实体类
     * @param <T>  泛型类型
     * @return 映射后的实体对象
     */
    public static <T> T mapRow(ResultSet rs, Class<T> type) throws SQLException {
        try {
            T entity = type.getDeclaredConstructor()
                .newInstance();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // 遍历实体类中所有字段，检查是否有@Column注解
            for (Field field : type.getDeclaredFields()) {
                field.setAccessible(true);
                String columnName = field.getName(); // 默认使用字段名
                Column colAnno = field.getAnnotation(Column.class);
                if (colAnno != null) {
                    columnName = colAnno.name();
                }
                // 检查ResultSet中是否存在对应的列（忽略大小写）
                boolean columnExists = false;
                for (int i = 1; i <= columnCount; i++) {
                    String rsColumn = metaData.getColumnLabel(i);
                    if (rsColumn == null || rsColumn.isEmpty()) {
                        rsColumn = metaData.getColumnName(i);
                    }
                    if (rsColumn.equalsIgnoreCase(columnName)) {
                        columnExists = true;
                        break;
                    }
                }
                // 如果存在，则取值并赋给实体
                if (columnExists) {
                    Object value = rs.getObject(columnName);
                    field.set(entity, value);
                }
            }
            return entity;
        } catch (Exception e) {
            throw new SQLException("Error mapping ResultSet to " + type.getName(), e);
        }
    }

    /**
     * 处理单行数据并返回实体对象。如果ResultSet中没有数据，则返回null。
     *
     * @param rs   ResultSet对象
     * @param type 目标实体类
     * @param <T>  泛型类型
     * @return 映射后的实体对象或null
     */
    public static <T> T handleSingle(ResultSet rs, Class<T> type) throws SQLException {
        if (rs.next()) {
            return mapRow(rs, type);
        }
        return null;
    }

    /**
     * 遍历ResultSet中所有行，将每行数据映射为实体对象并返回实体列表。
     *
     * @param rs   ResultSet对象
     * @param type 目标实体类
     * @param <T>  泛型类型
     * @return 实体对象的列表
     */
    public static <T> List<T> handleList(ResultSet rs, Class<T> type) throws SQLException {
        List<T> list = new ArrayList<>();
        while (rs.next()) {
            // 每次循环直接使用EntityHandler.mapRow将当前行映射为实体
            T entity = EntityHandler.mapRow(rs, type);
            list.add(entity);
        }
        return list;
    }
}
