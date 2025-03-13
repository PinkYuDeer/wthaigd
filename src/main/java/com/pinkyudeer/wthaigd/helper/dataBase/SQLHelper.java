package com.pinkyudeer.wthaigd.helper.dataBase;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.pinkyudeer.wthaigd.helper.dataBase.builder.AlterTableBuilder;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.CreateTableBuilder;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.DeleteBuilder;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.DropTableBuilder;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.InsertBuilder;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.SelectBuilder;
import com.pinkyudeer.wthaigd.helper.dataBase.builder.UpdateBuilder;

import lombok.Getter;

/**
 * SQL语句构建器。
 * 提供增删改查SQL构建并执行功能。
 * 支持单表和多表操作，包括：
 * <ul>
 * <li>表的创建和管理</li>
 * <li>数据的插入、更新、删除和查询</li>
 * <li>复杂条件查询</li>
 * <li>表关联查询</li>
 * <li>索引管理</li>
 * <li>外键约束</li>
 * </ul>
 *
 * @author pinkyudeer
 * @version 1.0
 */
public class SQLHelper {

    /**
     * 创建单个表的DDL构建器。
     *
     * @param entityClass 实体类
     * @param <T>         实体类型
     * @return 创建数量
     */
    public static <T> Integer createTable(Class<T> entityClass) {
        return new CreateTableBuilder<>(entityClass).build()
            .execute();
    }

    /**
     * 创建多个表的DDL构建器。
     *
     * @param entityClasses 实体类集合
     * @return 创建数量
     */
    public static Integer createTables(Collection<Class<?>> entityClasses) {
        return new CreateTableBuilder<>(null).addAll(entityClasses)
            .build()
            .execute();
    }

    /**
     * 创建表结构修改构建器。
     *
     * @param entityClass 实体类
     * @param <T>         实体类型
     * @return 表结构修改构建器实例
     */
    public static <T> AlterTableBuilder<T> alterTable(Class<T> entityClass) {
        return new AlterTableBuilder<>(entityClass);
    }

    /**
     * 创建表结构修改构建器。
     *
     * @param tableName 表名
     * @return 表结构修改构建器实例
     */
    public static AlterTableBuilder<?> alterTable(String tableName) {
        return new AlterTableBuilder<>(null).table(tableName);
    }

    /**
     * 创建表删除构建器。
     *
     * @param entityClass 实体类
     * @param <T>         实体类型
     * @return 表删除构建器实例
     */
    public static <T> DropTableBuilder<T> dropTable(Class<T> entityClass) {
        return new DropTableBuilder<>(entityClass);
    }

    /**
     * 创建表删除构建器。
     *
     * @param tableName 表名
     * @return 表删除构建器实例
     */
    public static DropTableBuilder<?> dropTable(String tableName) {
        return new DropTableBuilder<>(null).table(tableName);
    }

    /**
     * 删除单个表。
     *
     * @param entityClass 实体类
     * @param <T>         实体类型
     * @return 删除结果
     */
    public static <T> Integer dropTableIfExists(Class<T> entityClass) {
        return new DropTableBuilder<>(entityClass).ifExists(true)
            .build()
            .execute();
    }

    /**
     * 删除单个表。
     *
     * @param tableName 表名
     * @return 删除结果
     */
    public static Integer dropTableIfExists(String tableName) {
        return new DropTableBuilder<>(null).table(tableName)
            .ifExists(true)
            .build()
            .execute();
    }

    /**
     * 插入单个实体对象。
     *
     * @param entity 要插入的实体对象
     * @param <T>    实体类型
     * @return 受影响的行数
     */
    public static <T> Integer insert(T entity) {
        return new InsertBuilder<>(entity).execute();
    }

    /**
     * 批量插入实体对象列表。
     *
     * @param entities 要插入的实体对象列表
     * @param <T>      实体类型
     * @return 受影响的行数总和
     */
    public static <T> Integer insertAll(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return 0;
        }

        int total = 0;
        for (T entity : entities) {
            total += insert(entity);
        }
        return total;
    }

    /**
     * 创建删除操作构建器。
     *
     * @param entityClass 实体类
     * @param <T>         实体类型
     * @return 删除构建器实例
     */
    public static <T> DeleteBuilder<T> delete(Class<T> entityClass) {
        return new DeleteBuilder<>(entityClass);
    }

    /**
     * 基于实体对象创建删除操作构建器。
     *
     * @param entity 实体对象
     * @param <T>    实体类型
     * @return 删除构建器实例
     */
    public static <T> DeleteBuilder<T> deleteById(T entity) {
        return new DeleteBuilder<>(entity).byId();
    }

    /**
     * 创建删除操作构建器，并使用比对模式。
     *
     * @param entity    当前实体对象
     * @param oldEntity 旧实体对象（用于比对差异）
     * @param <T>       实体类型
     * @return 删除构建器实例
     */
    public static <T> DeleteBuilder<T> deleteByCompare(T entity, T oldEntity) {
        return new DeleteBuilder<>(entity, oldEntity);
    }

    /**
     * 创建更新操作构建器。
     *
     * @param entityClass 实体类
     * @param <T>         实体类型
     * @return 更新构建器实例
     */
    public static <T> UpdateBuilder<T> update(Class<T> entityClass) {
        return new UpdateBuilder<>(entityClass);
    }

    /**
     * 基于实体对象创建更新操作构建器。
     *
     * @param entity 实体对象
     * @param <T>    实体类型
     * @return 更新构建器实例
     */
    public static <T> UpdateBuilder<T> updateById(T entity) {
        return new UpdateBuilder<>(entity).byId();
    }

    /**
     * 创建更新操作构建器，并使用比对模式。
     *
     * @param entity    当前实体对象
     * @param oldEntity 旧实体对象（用于比对差异）
     * @param <T>       实体类型
     * @return 更新构建器实例
     */
    public static <T> UpdateBuilder<T> updateByCompare(T entity, T oldEntity) {
        return new UpdateBuilder<>(entity, oldEntity);
    }

    /**
     * 创建查询操作构建器。
     *
     * @param entityClass 实体类
     * @param <T>         实体类型
     * @return 查询构建器实例
     */
    public static <T> SelectBuilder<T> select(Class<T> entityClass) {
        return new SelectBuilder<>(entityClass);
    }

    /**
     * 创建查询操作构建器，并使用比对模式。
     *
     * @param entity    当前实体对象
     * @param oldEntity 旧实体对象（用于比对差异）
     * @param <T>       实体类型
     * @return 查询构建器实例
     */
    public static <T> SelectBuilder<T> selectByCompare(T entity, T oldEntity) {
        return new SelectBuilder<>(entity, oldEntity);
    }

    /**
     * 查询全部数据，直接返回实体对象列表。
     *
     * @param entityClass 实体类
     * @param <T>         实体类型
     * @return 实体对象列表
     */
    public static <T> List<T> selectAllFrom(Class<T> entityClass) {
        return EntityHandler.handleList(select(entityClass).execute(), entityClass);
    }

    /**
     * 根据主键查询单个实体对象。
     *
     * @param entityClass 实体类
     * @param id          实体主键，本项目中为UUID
     * @param <T>         实体类型
     * @return 实体对象
     */
    public static <T> T selectByPremiereKey(Class<T> entityClass, UUID id) {
        return EntityHandler.handleSingle(
            select(entityClass).where("id", SQLHelper.Operator.EQ, id)
                .limit(1)
                .execute(),
            entityClass);
    }

    /**
     * SQL操作符枚举。
     * 定义了常用的SQL比较操作符。
     */
    @Getter
    public enum Operator {

        /** 等于 */
        EQ("="),
        /** 不等于 */
        NE("<>"),
        /** 大于 */
        GT(">"),
        /** 大于等于 */
        GE(">="),
        /** 小于 */
        LT("<"),
        /** 小于等于 */
        LE("<="),
        /** 模糊匹配 */
        LIKE("LIKE"),
        /** 包含于 */
        IN("IN"),
        /** 不包含于 */
        NOT_IN("NOT IN"),
        /** 为空 */
        IS_NULL("IS NULL"),
        /** 不为空 */
        IS_NOT_NULL("IS NOT NULL");

        /** 操作符符号 */
        private final String symbol;

        /**
         * 构造函数。
         *
         * @param symbol 操作符符号
         */
        Operator(String symbol) {
            this.symbol = symbol;
        }
    }
}
