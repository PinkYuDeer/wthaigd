# TaskSqlHelper 使用指南

## 简介

TaskSqlHelper 是一个强大的数据库操作工具类，它提供了简单而优雅的API来进行数据库的增删改查操作。这个工具类采用了建造者模式，使得数据库操作变得更加直观和易于使用。

## 基础用法

### 1. 单个实体的操作

#### 创建实体

```java
Task task = new Task();
// 设置任务属性
task.setTitle("新任务");
task.setDescription("任务描述");

// 创建任务
TaskSqlHelper.entity(task).create();
```

#### 更新实体

```java
// 根据ID更新
Task task = new Task();
task.setId(1L);
task.setTitle("更新后的标题");
TaskSqlHelper.entity(task).updateById();

// 根据条件更新
Task task = new Task();
TaskSqlHelper.entity(task)
    .update(entity -> {
        entity.setStatus("已完成");
        entity.setCompletedAt(new Date());
    });
```

#### 删除实体

```java
// 根据ID删除
Task task = new Task();
task.setId(1L);
TaskSqlHelper.entity(task).deleteById();

// 根据条件删除
Task task = new Task();
TaskSqlHelper.entity(task)
    .delete(entity -> {
        entity.setStatus("已删除");
    });
```

#### 查询实体

```java
// 查询单个实体
Task task = new Task();
ResultSet resultSet = TaskSqlHelper.entity(task).query();
```

### 2. 批量操作

#### 批量创建

```java
List<Task> tasks = Arrays.asList(
    new Task("任务1"),
    new Task("任务2"),
    new Task("任务3")
);
TaskSqlHelper.batch(tasks).create();
```

#### 批量更新

```java
List<Task> tasks = getTasks();
TaskSqlHelper.batch(tasks)
    .updateById();  // 根据ID批量更新
```

#### 批量删除

```java
List<Task> tasks = getTasks();
TaskSqlHelper.batch(tasks)
    .deleteById();  // 根据ID批量删除
```

### 3. 便捷方法的使用

TaskSqlHelper 为每种实体类型都提供了便捷方法，使用起来更加简洁：

```java
// 任务相关操作
TaskSqlHelper.ConvenienceMethods.Tasks.of(task).create();
TaskSqlHelper.ConvenienceMethods.Tasks.batch(tasks).create();
TaskSqlHelper.ConvenienceMethods.Tasks.query();

// 玩家相关操作
TaskSqlHelper.ConvenienceMethods.Players.of(player).create();
TaskSqlHelper.ConvenienceMethods.Players.batch(players).create();
TaskSqlHelper.ConvenienceMethods.Players.query();

// 团队相关操作
TaskSqlHelper.ConvenienceMethods.Teams.of(team).create();
TaskSqlHelper.ConvenienceMethods.Teams.batch(teams).create();
TaskSqlHelper.ConvenienceMethods.Teams.query();
```

## 实际使用示例

### 1. 创建一个新任务并分配标签

```java
// 创建任务
Task task = new Task();
task.setTitle("新任务");
task.setDescription("这是一个新任务");
TaskSqlHelper.ConvenienceMethods.Tasks.of(task).create();

// 创建标签
Tag tag = new Tag();
tag.setName("重要");
TaskSqlHelper.ConvenienceMethods.Tags.of(tag).create();

// 关联任务和标签
TagLink tagLink = new TagLink();
tagLink.setTaskId(task.getId());
tagLink.setTagId(tag.getId());
TaskSqlHelper.ConvenienceMethods.TagLinks.of(tagLink).create();
```

### 2. 更新任务状态并记录变更

```java
// 更新任务状态
Task task = new Task();
task.setId(1L);
task.setStatus("进行中");
TaskSqlHelper.ConvenienceMethods.Tasks.of(task).updateById();

// 记录状态变更
StatusChangeRecord record = new StatusChangeRecord();
record.setTaskId(task.getId());
record.setOldStatus("待处理");
record.setNewStatus("进行中");
TaskSqlHelper.ConvenienceMethods.StatusChangeRecords.of(record).create();
```

### 3. 查询特定状态的任务

```java
// 查询所有进行中的任务
ResultSet resultSet = TaskSqlHelper.ConvenienceMethods.Tasks.query()
    .where(task -> task.setStatus("进行中"))
    .execute();
```

## 注意事项

1. 在使用前确保数据库已经正确初始化：

    ```java
    TaskSqlHelper.initTaskDataBase();
    ```

2. 所有的实体类都需要使用 `@Table` 注解进行标记。

3. 在进行批量操作时，建议控制每批次的数据量，避免一次性处理过多数据。

4. 使用条件更新或删除时，确保条件足够明确，避免误操作。

## 常见问题

1. **Q: 为什么我的实体创建失败了？**

   A: 检查实体类是否正确使用了 `@Table` 注解，以及所有必填字段是否都已设置。

2. **Q: 批量操作时出现异常怎么办？**

   A: 建议将批量操作拆分成较小的批次，并添加适当的错误处理机制。

3. **Q: 如何确保数据库操作的原子性？**

   A: 对于需要保证原子性的操作，建议使用事务进行包装。
