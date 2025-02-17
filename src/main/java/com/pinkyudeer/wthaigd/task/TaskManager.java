package com.pinkyudeer.wthaigd.task;

import java.util.ArrayList;
import java.util.List;

import com.pinkyudeer.wthaigd.task.entity.Task;

public class TaskManager {
    // TODO 在taskManager中实现增删改查与结果显示，将实体类的属性变为private，以getter和setter方法的形式访问

    private static final TaskManager INSTANCE = new TaskManager();
    private final List<Task> tasks = new ArrayList<>();

    public static TaskManager getInstance() {
        return INSTANCE;
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public boolean removeTask(String taskName) {
        // TODO 实现
        return false;
    }

    public boolean updateTask(String taskName, String newDescription) {
        // TODO 实现
        return false;
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }
}
