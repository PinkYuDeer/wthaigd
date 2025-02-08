package com.pinkyudeer.wthaigd.task;

import java.util.ArrayList;
import java.util.List;

public class TaskManager {

    private static final TaskManager INSTANCE = new TaskManager();
    private final List<Task> tasks = new ArrayList<>();

    public static TaskManager getInstance() {
        return INSTANCE;
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public boolean removeTask(String taskName) {
        return tasks.removeIf(
            task -> task.getTitle()
                .equals(taskName));
    }

    public boolean updateTask(String taskName, String newDescription) {
        for (Task task : tasks) {
            if (task.getTitle()
                .equals(taskName)) {
                task.setDescription(newDescription);
                return true;
            }
        }
        return false;
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }
}
