package com.pinkyudeer.wthaigd.client;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.pinkyudeer.wthaigd.task.entity.Task;

public final class TaskClientStore {

    public static final TaskClientStore INSTANCE = new TaskClientStore();

    private final Map<String, NBTTagCompound> tasks = new HashMap<>();
    private final Map<String, NBTTagCompound> teams = new HashMap<>();
    private NBTTagList invites = new NBTTagList();
    private String lastErrorCode = "";
    private String lastErrorMessage = "";

    private TaskClientStore() {}

    public synchronized void reset() {
        tasks.clear();
        teams.clear();
        invites = new NBTTagList();
        lastErrorCode = "";
        lastErrorMessage = "";
    }

    public synchronized void acceptTaskSync(NBTTagList data, boolean merge) {
        if (!merge) tasks.clear();
        for (int i = 0; i < data.tagCount(); i++) {
            NBTTagCompound entry = data.getCompoundTagAt(i);
            if (!entry.hasKey("id")) continue;
            tasks.put(entry.getString("id"), (NBTTagCompound) entry.copy());
        }
    }

    public synchronized void acceptTeamSync(NBTTagList data, boolean merge) {
        if (!merge) teams.clear();
        for (int i = 0; i < data.tagCount(); i++) {
            NBTTagCompound entry = data.getCompoundTagAt(i);
            if (!entry.hasKey("id")) continue;
            teams.put(entry.getString("id"), (NBTTagCompound) entry.copy());
        }
    }

    public synchronized void acceptInviteSync(NBTTagList data) {
        invites = data;
    }

    public synchronized void acceptError(String code, String message) {
        lastErrorCode = code == null ? "" : code;
        lastErrorMessage = message == null ? "" : message;
    }

    public synchronized Map<String, NBTTagCompound> getTasksSnapshot() {
        return new HashMap<>(tasks);
    }

    public synchronized List<Task> getTaskList(boolean includeCompleted) {
        List<Task> result = new ArrayList<>();
        for (NBTTagCompound tag : tasks.values()) {
            Task task = readTask(tag);
            if (includeCompleted || !isDone(task.getStatus())) result.add(task);
        }
        return result;
    }

    public synchronized Task getTask(String taskId) {
        NBTTagCompound tag = tasks.get(taskId);
        return tag == null ? null : readTask(tag);
    }

    public synchronized List<Task> getSubtasks(String parentTaskId) {
        List<Task> result = new ArrayList<>();
        for (NBTTagCompound tag : tasks.values()) {
            if (parentTaskId.equals(tag.getString("parentTaskId"))) {
                result.add(readTask(tag));
            }
        }
        return result;
    }

    public synchronized Map<String, NBTTagCompound> getTeamsSnapshot() {
        return new HashMap<>(teams);
    }

    public synchronized NBTTagList getInvites() {
        return (NBTTagList) invites.copy();
    }

    public synchronized String getLastErrorCode() {
        return lastErrorCode;
    }

    public synchronized String getLastErrorMessage() {
        return lastErrorMessage;
    }

    private Task readTask(NBTTagCompound tag) {
        Task task = new Task();
        task.setId(tag.getString("id"));
        task.setTitle(tag.getString("title"));
        task.setDescription(tag.getString("description"));
        task.setVersion(tag.getInteger("version"));
        task.setStatus(readEnum(Task.TaskStatus.class, tag.getString("status"), Task.TaskStatus.UnClaimed));
        task.setImportance(readEnum(Task.Importance.class, tag.getString("importance"), Task.Importance.UNDEFINED));
        task.setUrgency(readEnum(Task.Urgency.class, tag.getString("urgency"), Task.Urgency.UNDEFINED));
        task.setPriority(readEnum(
            Task.Priority.class,
            tag.getString("priority"),
            Task.calculatePriority(task.getImportance(), task.getUrgency())));
        task.setVisibility(readEnum(Task.PrivacyLevel.class, tag.getString("visibility"), Task.PrivacyLevel.PRIVATE));
        if (!tag.getString("parentTaskId")
            .isEmpty()) task.setParentTaskId(tag.getString("parentTaskId"));
        if (!tag.getString("creator")
            .isEmpty()) task.setCreator(UUID.fromString(tag.getString("creator")));
        if (!tag.getString("teamId")
            .isEmpty()) task.setTeamId(UUID.fromString(tag.getString("teamId")));
        if (!tag.getString("createTime")
            .isEmpty()) task.setCreateTime(LocalDateTime.parse(tag.getString("createTime")));
        if (!tag.getString("updateTime")
            .isEmpty()) task.setUpdateTime(LocalDateTime.parse(tag.getString("updateTime")));
        return task;
    }

    private boolean isDone(Task.TaskStatus status) {
        return status == Task.TaskStatus.Completed || status == Task.TaskStatus.Closed
            || status == Task.TaskStatus.Canceled;
    }

    private <T extends Enum<T>> T readEnum(Class<T> type, String value, T fallback) {
        if (value == null || value.isEmpty()) return fallback;
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
