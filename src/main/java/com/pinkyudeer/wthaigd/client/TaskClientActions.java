package com.pinkyudeer.wthaigd.client;

import net.minecraft.nbt.NBTTagCompound;

import com.pinkyudeer.wthaigd.network.handler.NetTaskAction;
import com.pinkyudeer.wthaigd.task.entity.Task;

public final class TaskClientActions {

    private TaskClientActions() {}

    public static void createTask(String title, String description, String parentTaskId, Task.Importance importance,
        Task.Urgency urgency) {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setString("action", parentTaskId == null ? "create" : "create_subtask");
        payload.setString("title", safe(title));
        payload.setString("description", safe(description));
        payload.setString("importance", importance == null ? Task.Importance.UNDEFINED.name() : importance.name());
        payload.setString("urgency", urgency == null ? Task.Urgency.UNDEFINED.name() : urgency.name());
        if (parentTaskId != null) payload.setString("parentTaskId", parentTaskId);
        NetTaskAction.sendAction(payload);
    }

    public static void updateTask(String taskId, int version, String title, String description,
        Task.Importance importance, Task.Urgency urgency, Task.TaskStatus status) {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setString("action", "update");
        payload.setString("taskId", safe(taskId));
        payload.setInteger("version", version);
        payload.setString("title", safe(title));
        payload.setString("description", safe(description));
        payload.setString("importance", importance == null ? Task.Importance.UNDEFINED.name() : importance.name());
        payload.setString("urgency", urgency == null ? Task.Urgency.UNDEFINED.name() : urgency.name());
        if (status != null) payload.setString("status", status.name());
        NetTaskAction.sendAction(payload);
    }

    public static void changeStatus(String taskId, Task.TaskStatus status) {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setString("action", "change_status");
        payload.setString("taskId", safe(taskId));
        payload.setString("status", status == null ? Task.TaskStatus.UnClaimed.name() : status.name());
        NetTaskAction.sendAction(payload);
    }

    public static void completeTask(String taskId) {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setString("action", "complete");
        payload.setString("taskId", safe(taskId));
        NetTaskAction.sendAction(payload);
    }

    public static void deleteTask(String taskId) {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setString("action", "delete");
        payload.setString("taskId", safe(taskId));
        NetTaskAction.sendAction(payload);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
