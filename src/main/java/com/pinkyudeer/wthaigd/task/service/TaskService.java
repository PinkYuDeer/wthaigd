package com.pinkyudeer.wthaigd.task.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.pinkyudeer.wthaigd.Wthaigd;
import com.pinkyudeer.wthaigd.db.EntityHandler;
import com.pinkyudeer.wthaigd.db.SQLHelper;
import com.pinkyudeer.wthaigd.task.dao.TaskDao;
import com.pinkyudeer.wthaigd.task.entity.Task;
import com.pinkyudeer.wthaigd.task.entity.Task.TaskStatus;
import com.pinkyudeer.wthaigd.task.entity.record.StatusChangeRecord;

public class TaskService {

    public static Task createTask(String title, String description, UUID creatorId) {
        Task task = new Task(title, description, creatorId);
        Integer result = TaskDao.insert(task);
        if (result == null || result <= 0) {
            Wthaigd.LOG.error("Failed to create task: {}", title);
            return null;
        }
        Wthaigd.LOG.info("Task created: {} ({})", title, task.getId());
        return task;
    }

    public static Task createTask(String title, String description, UUID creatorId, Task.Importance importance,
        Task.Urgency urgency) {
        Task task = new Task(title, description, creatorId, importance, urgency);
        Integer result = TaskDao.insert(task);
        if (result == null || result <= 0) {
            Wthaigd.LOG.error("Failed to create task: {}", title);
            return null;
        }
        return task;
    }

    public static boolean updateTask(Task task, Task oldTask) {
        task.setUpdateTime(LocalDateTime.now());
        task.setVersion(task.getVersion() + 1);
        Integer result = TaskDao.updateByIdByCompare(task, oldTask);
        if (result == null || result <= 0) {
            Wthaigd.LOG.error("Failed to update task: {}", task.getId());
            return false;
        }
        return true;
    }

    public static boolean changeStatus(String taskId, TaskStatus newStatus, UUID operatorId) {
        try {
            Task task = EntityHandler.handleSingle(
                SQLHelper.select(Task.class)
                    .where("id", SQLHelper.Operator.EQ, taskId)
                    .limit(1)
                    .execute(),
                Task.class);
            if (task == null) {
                Wthaigd.LOG.error("Task not found: {}", taskId);
                return false;
            }
            TaskStatus oldStatus = task.getStatus();
            if (oldStatus == newStatus) return true;

            task.setStatus(newStatus);
            task.setUpdateTime(LocalDateTime.now());
            task.setLastOperator(operatorId);

            if (newStatus == TaskStatus.Completed || newStatus == TaskStatus.Closed) {
                task.setEndTime(LocalDateTime.now());
            }
            if (newStatus == TaskStatus.InProgress && task.getStartTime() == null) {
                task.setStartTime(LocalDateTime.now());
            }

            Task oldTask = new Task(task.getTitle(), task.getDescription(), task.getCreator());
            oldTask.setId(task.getId());
            oldTask.setStatus(oldStatus);
            Integer result = TaskDao.updateByIdByCompare(task, oldTask);

            if (result == null || result <= 0) return false;

            StatusChangeRecord record = new StatusChangeRecord(
                operatorId,
                UUID.fromString(taskId),
                oldStatus,
                newStatus);
            SQLHelper.insert(record);

            Wthaigd.LOG.info("Task {} status changed: {} -> {}", taskId, oldStatus, newStatus);
            return true;
        } catch (Exception e) {
            Wthaigd.LOG.error("Failed to change task status", e);
            return false;
        }
    }

    public static boolean completeTask(String taskId, UUID operatorId) {
        return changeStatus(taskId, TaskStatus.Completed, operatorId);
    }

    public static boolean archiveTask(String taskId, UUID operatorId) {
        return changeStatus(taskId, TaskStatus.Closed, operatorId);
    }

    public static List<Task> getActiveTasks() {
        try {
            List<String> excluded = java.util.Arrays
                .asList(TaskStatus.Completed.name(), TaskStatus.Closed.name(), TaskStatus.Canceled.name());
            return EntityHandler.handleList(
                SQLHelper.select(Task.class)
                    .where("status", SQLHelper.Operator.NOT_IN, excluded)
                    .execute(),
                Task.class);
        } catch (Exception e) {
            Wthaigd.LOG.error("Failed to fetch active tasks", e);
            return Collections.emptyList();
        }
    }

    public static List<Task> getAllTasks() {
        try {
            return TaskDao.selectAll();
        } catch (Exception e) {
            Wthaigd.LOG.error("Failed to fetch all tasks", e);
            return Collections.emptyList();
        }
    }

    public static boolean deleteTask(String taskId) {
        try {
            SQLHelper.delete(Task.class)
                .where("parent_task_id", SQLHelper.Operator.EQ, taskId)
                .execute();
            Integer result = SQLHelper.delete(Task.class)
                .where("id", SQLHelper.Operator.EQ, taskId)
                .execute();
            return result != null && result > 0;
        } catch (Exception e) {
            Wthaigd.LOG.error("Failed to delete task: {}", taskId, e);
            return false;
        }
    }

    public static Task createSubtask(String title, String description, UUID creatorId, String parentTaskId) {
        Task task = new Task(title, description, creatorId);
        task.setParentTaskId(parentTaskId);
        Integer result = TaskDao.insert(task);
        if (result == null || result <= 0) {
            Wthaigd.LOG.error("Failed to create subtask: {}", title);
            return null;
        }
        return task;
    }

    public static Task createSubtask(String title, String description, UUID creatorId, String parentTaskId,
        Task.Importance importance, Task.Urgency urgency) {
        Task task = new Task(title, description, creatorId, importance, urgency);
        task.setParentTaskId(parentTaskId);
        Integer result = TaskDao.insert(task);
        if (result == null || result <= 0) {
            Wthaigd.LOG.error("Failed to create subtask: {}", title);
            return null;
        }
        return task;
    }

    public static List<Task> getSubtasks(String parentTaskId) {
        try {
            return EntityHandler.handleList(
                SQLHelper.select(Task.class)
                    .where("parent_task_id", SQLHelper.Operator.EQ, parentTaskId)
                    .execute(),
                Task.class);
        } catch (Exception e) {
            Wthaigd.LOG.error("Failed to fetch subtasks for: {}", parentTaskId, e);
            return Collections.emptyList();
        }
    }

    public static int getSubtaskCount(String parentTaskId) {
        try {
            List<Task> subs = getSubtasks(parentTaskId);
            return subs.size();
        } catch (Exception e) {
            return 0;
        }
    }
}
