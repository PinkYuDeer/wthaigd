# TaskSqlHelper 使用指南

## 定位

`TaskSqlHelper` 只负责任务数据库初始化与迁移。业务代码不应再调用旧式 `TaskSqlHelper.entity(...)` 或 `ConvenienceMethods`，这些 API 已不存在。

当前分层如下：

- 建表与迁移：`TaskSqlHelper.initTaskDataBase()`、`TaskSqlHelper.migrateSchema()`。
- SQL 构建：`SQLHelper.createTables/select/insert/update/delete`。
- 连接、查询回调、事务：`SQLiteManager.query(...)`、`SQLiteManager.executeUpdate(...)`、`SQLiteManager.transaction(...)`。
- 任务用例：`TaskService`。
- 团队用例：`TeamService`。
- 标签用例：`TagService`。

## 建库与迁移

世界主库载入时，`SQLiteManager.initSqlite()` 会创建或恢复内存 SQLite。新库调用 `TaskSqlHelper.initTaskDataBase()` 扫描 `com.pinkyudeer.wthaigd.task.entity` 下的 `@Table` 实体并建表；已有库恢复后调用 `migrateSchema()`。

迁移通过 `schema_version` 记录版本，必须保持幂等：

```java
TaskSqlHelper.migrateSchema();
```

现有迁移覆盖：

- `tasks.parent_task_id`
- `teams.sync_source`
- `teams.external_party_id`
- `teams.sync_status`
- `teams.last_sync_time`
- `teams.external_team_key`
- `tags.owner_team_id`

## 查询与事务

读取优先用 `SQLiteManager.query`，使 `PreparedStatement` 与 `ResultSet` 在回调后关闭：

```java
Integer count = SQLiteManager.query(
    "SELECT COUNT(*) AS c FROM tag_links WHERE tag_id = ?",
    rs -> rs.next() ? rs.getInt("c") : 0,
    tagId
);
```

多表写入须放入事务：

```java
SQLiteManager.transaction(() -> {
    Team team = TeamService.createLocalTeam("alpha", ownerId, null);
    TaskService.createTask(TeamService.contextFor(ownerId, team.getId(), false), "task", "", null, null);
    return team;
});
```

## 服务层入口

任务写入应走 `TaskService`，团队写入应走 `TeamService`，标签写入应走 `TagService`。GUI 与网络 handler 只提交操作意图，不直接操作 DAO。

任务标签示例：

```java
TaskService.PermissionContext context = TeamService.contextFor(playerId, teamId, false);
Task task = TaskService.createTask(context, "组装线", "准备材料", Task.Importance.HIGH, Task.Urgency.MEDIUM);

Tag tag = TagService.addTagToTask(
    context,
    task.getId(),
    "物流",
    "物流与管线分类",
    "#336699",
    Tag.TagScope.TEAM
);
```

## 注意

- `Task.id` 目前仍为 `String` 格式 UUID；`TagLink.entityId` 以 `UUID` 存储，任务标签服务会在边界处转换。
- `SQLHelper.select(...).execute()` 仍返回裸 `ResultSet`；新代码宜优先用 `SQLiteManager.query(...)` 或在 DAO/服务层立即消费结果。
- 正式发布前，应继续补 Builder 层单元测试，并把 SQL 日志从 `info` 降为 `debug`。
