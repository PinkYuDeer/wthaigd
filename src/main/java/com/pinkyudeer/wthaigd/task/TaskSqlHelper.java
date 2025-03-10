package com.pinkyudeer.wthaigd.task;

import java.sql.ResultSet;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import com.pinkyudeer.wthaigd.core.Wthaigd;
import com.pinkyudeer.wthaigd.entity.task.Player;
import com.pinkyudeer.wthaigd.entity.task.Tag;
import com.pinkyudeer.wthaigd.entity.task.Task;
import com.pinkyudeer.wthaigd.entity.task.Team;
import com.pinkyudeer.wthaigd.entity.task.record.Notification;
import com.pinkyudeer.wthaigd.entity.task.record.StatusChangeRecord;
import com.pinkyudeer.wthaigd.entity.task.record.TagLink;
import com.pinkyudeer.wthaigd.entity.task.record.TaskInteraction;
import com.pinkyudeer.wthaigd.entity.task.record.TeamMember;
import com.pinkyudeer.wthaigd.helper.dataBase.SQLHelper;
import com.pinkyudeer.wthaigd.helper.dataBase.annotation.Table;

/**
 * 任务系统数据库操作助手类
 * 提供任务相关实体的CRUD操作
 */
public class TaskSqlHelper {

    /**
     * 初始化任务数据库
     * 扫描并创建所有任务相关的表
     */
    public static void initTaskDataBase() {
        Reflections reflections = new Reflections("com.pinkyudeer.wthaigd.entity.task");
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(Table.class);
        try {
            SQLHelper.createTables(annotatedClasses);
        } catch (Exception e) {
            Wthaigd.LOG.error("初始化任务数据库失败", e);
            return;
        }
        Wthaigd.LOG.info("初始化任务数据库，共创建 {} 张表", annotatedClasses.size());
    }

    /**
     * 创建实体操作构建器
     *
     * @param entity 实体对象
     * @param <T>    实体类型
     * @return 操作构建器
     */
    public static <T> EntityBuilder<T> entity(T entity) {
        return new EntityBuilder<>(entity);
    }

    /**
     * 创建批量操作构建器
     *
     * @param entities 实体列表
     * @param <T>      实体类型
     * @return 批量操作构建器
     */
    public static <T> BatchBuilder<T> batch(List<T> entities) {
        return new BatchBuilder<>(entities);
    }

    /**
     * 创建查询构建器
     *
     * @param entityClass 实体类
     * @param <T>         实体类型
     * @return 查询构建器
     */
    public static <T> QueryBuilder<T> query(Class<T> entityClass) {
        return new QueryBuilder<>(entityClass);
    }

    /**
     * 实体操作构建器
     * 支持单个实体的CRUD操作
     */
    public static class EntityBuilder<T> {

        private final T entity;

        private EntityBuilder(T entity) {
            this.entity = entity;
        }

        /**
         * 创建实体
         *
         * @return 受影响的行数
         */
        public Integer create() {
            return SQLHelper.insert(entity)
                .execute();
        }

        /**
         * 根据ID更新实体
         *
         * @return 受影响的行数
         */
        public Integer updateById() {
            return SQLHelper.update(entity)
                .byId()
                .execute();
        }

        /**
         * 根据条件更新实体
         *
         * @param conditions 更新条件
         * @return 受影响的行数
         */
        @SafeVarargs
        public final Integer update(QueryCondition<T>... conditions) {
            if (conditions == null || conditions.length == 0) {
                throw new IllegalArgumentException("更新条件不能为空");
            }
            T updateEntity = entity;
            for (QueryCondition<T> condition : conditions) {
                if (condition == null) {
                    throw new IllegalArgumentException("更新条件不能为null");
                }
                condition.apply(updateEntity);
            }
            return SQLHelper.update(updateEntity)
                .execute();
        }

        /**
         * 根据ID删除实体
         *
         * @return 受影响的行数
         */
        public Integer deleteById() {
            return SQLHelper.delete(entity)
                .byId()
                .execute();
        }

        /**
         * 根据条件删除实体
         *
         * @param conditions 删除条件
         * @return 受影响的行数
         */
        @SafeVarargs
        public final Integer delete(QueryCondition<T>... conditions) {
            if (conditions == null || conditions.length == 0) {
                throw new IllegalArgumentException("删除条件不能为空");
            }
            T deleteEntity = entity;
            for (QueryCondition<T> condition : conditions) {
                if (condition == null) {
                    throw new IllegalArgumentException("删除条件不能为null");
                }
                condition.apply(deleteEntity);
            }
            return SQLHelper.delete(deleteEntity)
                .execute();
        }

        /**
         * 查询实体
         *
         * @return 查询结果
         */
        public ResultSet query() {
            return SQLHelper.select(entity)
                .execute();
        }
    }

    /**
     * 批量操作构建器
     * 支持多个实体的批量操作
     */
    public static class BatchBuilder<T> {

        private final List<T> entities;

        private BatchBuilder(List<T> entities) {
            this.entities = entities;
        }

        /**
         * 批量创建实体
         *
         * @return 受影响的总行数
         */
        public Integer create() {
            return entities.stream()
                .mapToInt(
                    entity -> SQLHelper.insert(entity)
                        .execute())
                .sum();
        }

        /**
         * 批量根据ID更新实体
         *
         * @return 受影响的总行数
         */
        public Integer updateById() {
            return entities.stream()
                .mapToInt(
                    entity -> SQLHelper.update(entity)
                        .byId()
                        .execute())
                .sum();
        }

        /**
         * 批量根据条件更新实体
         *
         * @param conditions 更新条件
         * @return 受影响的总行数
         */
        @SafeVarargs
        public final Integer update(QueryCondition<T>... conditions) {
            if (conditions == null || conditions.length == 0) {
                throw new IllegalArgumentException("更新条件不能为空");
            }
            return entities.stream()
                .mapToInt(entity -> {
                    for (QueryCondition<T> condition : conditions) {
                        if (condition == null) {
                            throw new IllegalArgumentException("更新条件不能为null");
                        }
                        condition.apply(entity);
                    }
                    return SQLHelper.update(entity)
                        .execute();
                })
                .sum();
        }

        /**
         * 批量根据ID删除实体
         *
         * @return 受影响的总行数
         */
        public Integer deleteById() {
            return entities.stream()
                .mapToInt(
                    entity -> SQLHelper.delete(entity)
                        .byId()
                        .execute())
                .sum();
        }

        /**
         * 批量根据条件删除实体
         *
         * @param conditions 删除条件
         * @return 受影响的总行数
         */
        @SafeVarargs
        public final Integer delete(QueryCondition<T>... conditions) {
            if (conditions == null || conditions.length == 0) {
                throw new IllegalArgumentException("删除条件不能为空");
            }
            return entities.stream()
                .mapToInt(entity -> {
                    for (QueryCondition<T> condition : conditions) {
                        if (condition == null) {
                            throw new IllegalArgumentException("删除条件不能为null");
                        }
                        condition.apply(entity);
                    }
                    return SQLHelper.delete(entity)
                        .execute();
                })
                .sum();
        }
    }

    /**
     * 查询构建器
     * 支持复杂的查询条件构建
     */
    public static class QueryBuilder<T> {

        private final T queryEntity;

        private QueryBuilder(Class<T> entityClass) {
            try {
                this.queryEntity = entityClass.getDeclaredConstructor()
                    .newInstance();
            } catch (Exception e) {
                throw new RuntimeException("无法创建查询实体实例", e);
            }
        }

        /**
         * 设置查询条件
         *
         * @param condition 查询条件
         * @return 当前构建器
         */
        public QueryBuilder<T> where(QueryCondition<T> condition) {
            condition.apply(queryEntity);
            return this;
        }

        /**
         * 执行查询
         *
         * @return 查询结果数量
         */
        public ResultSet execute() {
            return SQLHelper.select(queryEntity)
                .execute();
        }
    }

    /**
     * 查询条件接口
     */
    @FunctionalInterface
    public interface QueryCondition<T> {

        void apply(T entity);
    }

    /**
     * 便捷方法集合
     * 提供各种实体的便捷操作方法
     */
    public static class ConvenienceMethods {

        public static class Tasks {

            public static EntityBuilder<Task> of(Task task) {
                return entity(task);
            }

            public static BatchBuilder<Task> batch(List<Task> tasks) {
                return TaskSqlHelper.batch(tasks);
            }

            public static QueryBuilder<Task> query() {
                return TaskSqlHelper.query(Task.class);
            }
        }

        public static class Players {

            public static EntityBuilder<Player> of(Player player) {
                return entity(player);
            }

            public static BatchBuilder<Player> batch(List<Player> players) {
                return TaskSqlHelper.batch(players);
            }

            public static QueryBuilder<Player> query() {
                return TaskSqlHelper.query(Player.class);
            }
        }

        public static class Teams {

            public static EntityBuilder<Team> of(Team team) {
                return entity(team);
            }

            public static BatchBuilder<Team> batch(List<Team> teams) {
                return TaskSqlHelper.batch(teams);
            }

            public static QueryBuilder<Team> query() {
                return TaskSqlHelper.query(Team.class);
            }
        }

        public static class Tags {

            public static EntityBuilder<Tag> of(Tag tag) {
                return entity(tag);
            }

            public static BatchBuilder<Tag> batch(List<Tag> tags) {
                return TaskSqlHelper.batch(tags);
            }

            public static QueryBuilder<Tag> query() {
                return TaskSqlHelper.query(Tag.class);
            }
        }

        public static class TaskInteractions {

            public static EntityBuilder<TaskInteraction> of(TaskInteraction interaction) {
                return entity(interaction);
            }

            public static BatchBuilder<TaskInteraction> batch(List<TaskInteraction> interactions) {
                return TaskSqlHelper.batch(interactions);
            }

            public static QueryBuilder<TaskInteraction> query() {
                return TaskSqlHelper.query(TaskInteraction.class);
            }
        }

        public static class StatusChangeRecords {

            public static EntityBuilder<StatusChangeRecord> of(StatusChangeRecord record) {
                return entity(record);
            }

            public static BatchBuilder<StatusChangeRecord> batch(List<StatusChangeRecord> records) {
                return TaskSqlHelper.batch(records);
            }

            public static QueryBuilder<StatusChangeRecord> query() {
                return TaskSqlHelper.query(StatusChangeRecord.class);
            }
        }

        public static class Notifications {

            public static EntityBuilder<Notification> of(Notification notification) {
                return entity(notification);
            }

            public static BatchBuilder<Notification> batch(List<Notification> notifications) {
                return TaskSqlHelper.batch(notifications);
            }

            public static QueryBuilder<Notification> query() {
                return TaskSqlHelper.query(Notification.class);
            }
        }

        public static class TagLinks {

            public static EntityBuilder<TagLink> of(TagLink tagLink) {
                return entity(tagLink);
            }

            public static BatchBuilder<TagLink> batch(List<TagLink> tagLinks) {
                return TaskSqlHelper.batch(tagLinks);
            }

            public static QueryBuilder<TagLink> query() {
                return TaskSqlHelper.query(TagLink.class);
            }
        }

        public static class TeamMembers {

            public static EntityBuilder<TeamMember> of(TeamMember teamMember) {
                return entity(teamMember);
            }

            public static BatchBuilder<TeamMember> batch(List<TeamMember> teamMembers) {
                return TaskSqlHelper.batch(teamMembers);
            }

            public static QueryBuilder<TeamMember> query() {
                return TaskSqlHelper.query(TeamMember.class);
            }
        }
    }
}
