package com.pinkyudeer.wthaigd.network.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.pinkyudeer.wthaigd.Wthaigd;
import com.pinkyudeer.wthaigd.helper.UtilHelper;
import com.pinkyudeer.wthaigd.network.PacketIds;
import com.pinkyudeer.wthaigd.network.PacketSender;
import com.pinkyudeer.wthaigd.network.PacketTypeRegistry;
import com.pinkyudeer.wthaigd.task.entity.Tag;
import com.pinkyudeer.wthaigd.task.entity.Task;
import com.pinkyudeer.wthaigd.task.service.TagService;
import com.pinkyudeer.wthaigd.task.service.TaskService;
import com.pinkyudeer.wthaigd.task.service.TeamService;

public final class NetTaskAction {

    private NetTaskAction() {}

    public static void registerHandler() {
        PacketTypeRegistry.INSTANCE.registerServerHandler(PacketIds.TASK_ACTION, NetTaskAction::onServer);
    }

    public static void sendAction(NBTTagCompound payload) {
        PacketSender.INSTANCE.sendToServer(PacketIds.TASK_ACTION, payload);
    }

    private static void onServer(NBTTagCompound payload, EntityPlayerMP sender) {
        if (sender == null) return;
        String action = payload.getString("action");
        try {
            if ("create".equals(action)) {
                TaskService.PermissionContext context = TeamService
                    .contextFor(sender.getUniqueID(), readUuid(payload, "teamId"), isOp(sender));
                Task task = TaskService.createTask(
                    context,
                    payload.getString("title"),
                    payload.getString("description"),
                    readImportance(payload),
                    readUrgency(payload));
                if (task == null) {
                    NetError.send(sender, NetError.SERVER_ERROR, "task create failed");
                } else {
                    NetTaskSync.sendSync(sender, true);
                }
            } else if ("create_subtask".equals(action)) {
                Task parent = TaskService.getTask(payload.getString("parentTaskId"));
                if (parent == null) {
                    NetError.send(sender, NetError.NOT_FOUND, "parent task not found");
                    return;
                }
                assertCanWrite(sender, parent);
                Task task = TaskService.createSubtask(
                    payload.getString("title"),
                    payload.getString("description"),
                    sender.getUniqueID(),
                    payload.getString("parentTaskId"),
                    readImportance(payload),
                    readUrgency(payload));
                if (task == null) NetError.send(sender, NetError.SERVER_ERROR, "subtask create failed");
                else NetTaskSync.sendSync(sender, true);
            } else if ("update".equals(action)) {
                Task task = TaskService.getTask(payload.getString("taskId"));
                if (task == null) {
                    NetError.send(sender, NetError.NOT_FOUND, "task not found");
                    return;
                }
                assertCanWrite(sender, task);
                int currentVersion = task.getVersion() == null ? 0 : task.getVersion();
                if (payload.hasKey("version") && payload.getInteger("version") != currentVersion) {
                    NetError.send(sender, NetError.VERSION_CONFLICT, "task version conflict");
                    return;
                }

                Task oldTask = UtilHelper.deepClone(task, Task.class);
                task.setTitle(payload.getString("title"));
                task.setDescription(payload.getString("description"));
                task.setImportance(readImportance(payload));
                task.setUrgency(readUrgency(payload));
                task.setPriority(Task.calculatePriority(task.getImportance(), task.getUrgency()));
                boolean ok = TaskService.updateTask(task, oldTask);
                if (ok && payload.hasKey("status")) {
                    ok = TaskService
                        .changeStatus(payload.getString("taskId"), readStatus(payload), sender.getUniqueID());
                }
                if (ok) NetTaskSync.sendSync(sender, true);
                else NetError.send(sender, NetError.SERVER_ERROR, "task update failed");
            } else if ("change_status".equals(action)) {
                Task task = TaskService.getTask(payload.getString("taskId"));
                if (task == null) {
                    NetError.send(sender, NetError.NOT_FOUND, "task not found");
                    return;
                }
                assertCanWrite(sender, task);
                boolean ok = TaskService
                    .changeStatus(payload.getString("taskId"), readStatus(payload), sender.getUniqueID());
                if (ok) NetTaskSync.sendSync(sender, true);
                else NetError.send(sender, NetError.NOT_FOUND, "task not found");
            } else if ("complete".equals(action)) {
                Task task = TaskService.getTask(payload.getString("taskId"));
                if (task == null) {
                    NetError.send(sender, NetError.NOT_FOUND, "task not found");
                    return;
                }
                assertCanWrite(sender, task);
                boolean ok = TaskService.completeTask(payload.getString("taskId"), sender.getUniqueID());
                if (ok) NetTaskSync.sendSync(sender, true);
                else NetError.send(sender, NetError.NOT_FOUND, "task not found");
            } else if ("delete".equals(action)) {
                Task task = TaskService.getTask(payload.getString("taskId"));
                if (task == null) {
                    NetError.send(sender, NetError.NOT_FOUND, "task not found");
                    return;
                }
                assertCanWrite(sender, task);
                boolean ok = TaskService.deleteTask(payload.getString("taskId"));
                if (ok) NetTaskSync.sendSync(sender, true);
                else NetError.send(sender, NetError.NOT_FOUND, "task not found");
            } else if ("add_tag".equals(action)) {
                Task task = requireTask(payload.getString("taskId"), sender);
                boolean ok = TagService
                    .addTagToTask(contextForTask(sender, task), task.getId(), readUuid(payload, "tagId"));
                if (ok) NetTaskSync.sendSync(sender, true);
                else NetError.send(sender, NetError.SERVER_ERROR, "tag add failed");
            } else if ("add_tag_by_name".equals(action)) {
                Task task = requireTask(payload.getString("taskId"), sender);
                Tag tag = TagService.addTagToTask(
                    contextForTask(sender, task),
                    task.getId(),
                    payload.getString("tagName"),
                    payload.getString("description"),
                    payload.getString("colorCode"),
                    readTagScope(payload));
                if (tag != null) NetTaskSync.sendSync(sender, true);
                else NetError.send(sender, NetError.SERVER_ERROR, "tag add failed");
            } else if ("remove_tag".equals(action)) {
                Task task = requireTask(payload.getString("taskId"), sender);
                boolean ok = TagService
                    .removeTagFromTask(contextForTask(sender, task), task.getId(), readUuid(payload, "tagId"));
                if (ok) NetTaskSync.sendSync(sender, true);
                else NetError.send(sender, NetError.NOT_FOUND, "tag link not found");
            } else if ("set_tags".equals(action)) {
                Task task = requireTask(payload.getString("taskId"), sender);
                boolean ok = TagService
                    .setTagsForTask(contextForTask(sender, task), task.getId(), readUuidList(payload, "tagIds"));
                if (ok) NetTaskSync.sendSync(sender, true);
                else NetError.send(sender, NetError.SERVER_ERROR, "tag set failed");
            } else {
                NetError.send(sender, NetError.INVALID_ACTION, action);
            }
        } catch (SecurityException e) {
            NetError.send(sender, NetError.PERMISSION_DENIED, e.getMessage());
        } catch (TaskNotFoundException e) {
            NetError.send(sender, NetError.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            NetError.send(sender, NetError.INVALID_PAYLOAD, e.getMessage());
        } catch (Exception e) {
            Wthaigd.LOG.error("Task action failed: {}", action, e);
            NetError.send(sender, NetError.SERVER_ERROR, e.getMessage());
        }
    }

    private static UUID readUuid(NBTTagCompound payload, String key) {
        if (!payload.hasKey(key) || payload.getString(key)
            .isEmpty()) return null;
        return UUID.fromString(payload.getString(key));
    }

    private static Task.Importance readImportance(NBTTagCompound payload) {
        if (!payload.hasKey("importance")) return Task.Importance.UNDEFINED;
        return Task.Importance.valueOf(payload.getString("importance"));
    }

    private static Task.Urgency readUrgency(NBTTagCompound payload) {
        if (!payload.hasKey("urgency")) return Task.Urgency.UNDEFINED;
        return Task.Urgency.valueOf(payload.getString("urgency"));
    }

    private static Task.TaskStatus readStatus(NBTTagCompound payload) {
        if (!payload.hasKey("status")) return Task.TaskStatus.UnClaimed;
        return Task.TaskStatus.valueOf(payload.getString("status"));
    }

    private static Tag.TagScope readTagScope(NBTTagCompound payload) {
        if (!payload.hasKey("scope")) return Tag.TagScope.PUBLIC;
        return Tag.TagScope.valueOf(payload.getString("scope"));
    }

    private static List<UUID> readUuidList(NBTTagCompound payload, String key) {
        List<UUID> values = new ArrayList<>();
        if (!payload.hasKey(key)) return values;
        NBTTagList list = payload.getTagList(key, 8);
        for (int i = 0; i < list.tagCount(); i++) {
            values.add(UUID.fromString(list.getStringTagAt(i)));
        }
        return values;
    }

    private static boolean isOp(EntityPlayerMP player) {
        return player.mcServer != null && player.mcServer.getConfigurationManager()
            .func_152596_g(player.getGameProfile());
    }

    private static void assertCanWrite(EntityPlayerMP sender, Task task) {
        if (isOp(sender) || sender.getUniqueID()
            .equals(task.getCreator())) {
            return;
        }
        if (task.getTeamId() != null) {
            TaskService.PermissionContext context = TeamService
                .contextFor(sender.getUniqueID(), task.getTeamId(), false);
            if (context.canAssignTeamTask()) return;
        }
        throw new SecurityException("无权操作此任务");
    }

    private static Task requireTask(String taskId, EntityPlayerMP sender) {
        Task task = TaskService.getTask(taskId);
        if (task == null) throw new TaskNotFoundException("task not found");
        return task;
    }

    private static TaskService.PermissionContext contextForTask(EntityPlayerMP sender, Task task) {
        return TeamService.contextFor(sender.getUniqueID(), task.getTeamId(), isOp(sender));
    }

    private static final class TaskNotFoundException extends RuntimeException {

        private TaskNotFoundException(String message) {
            super(message);
        }
    }
}
