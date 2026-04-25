package com.pinkyudeer.wthaigd.network.handler;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.pinkyudeer.wthaigd.client.TaskClientStore;
import com.pinkyudeer.wthaigd.network.PacketIds;
import com.pinkyudeer.wthaigd.network.PacketSender;
import com.pinkyudeer.wthaigd.network.PacketTypeRegistry;
import com.pinkyudeer.wthaigd.task.entity.Tag;
import com.pinkyudeer.wthaigd.task.entity.Task;
import com.pinkyudeer.wthaigd.task.service.TagService;
import com.pinkyudeer.wthaigd.task.service.TaskService;

public final class NetTaskSync {

    private NetTaskSync() {}

    public static void registerHandler() {
        PacketTypeRegistry.INSTANCE.registerServerHandler(PacketIds.TASK_SYNC, NetTaskSync::onServer);
        PacketTypeRegistry.INSTANCE.registerClientHandler(PacketIds.TASK_SYNC, NetTaskSync::onClient);
    }

    public static void requestSync() {
        PacketSender.INSTANCE.sendToServer(PacketIds.TASK_SYNC, new NBTTagCompound());
    }

    public static void sendSync(EntityPlayerMP player, boolean merge) {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setBoolean("merge", merge);
        NBTTagList list = new NBTTagList();
        for (Task task : TaskService.getVisibleTasks(player.getUniqueID(), isOp(player))) {
            list.appendTag(writeTask(task));
        }
        payload.setTag("data", list);
        PacketSender.INSTANCE.sendToPlayers(PacketIds.TASK_SYNC, payload, player);
    }

    private static void onServer(NBTTagCompound payload, EntityPlayerMP sender) {
        if (sender != null) sendSync(sender, false);
    }

    private static void onClient(NBTTagCompound payload) {
        TaskClientStore.INSTANCE.acceptTaskSync(payload.getTagList("data", 10), payload.getBoolean("merge"));
    }

    private static NBTTagCompound writeTask(Task task) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("id", task.getId());
        tag.setString("title", safe(task.getTitle()));
        tag.setString("description", safe(task.getDescription()));
        tag.setInteger("version", task.getVersion() == null ? 0 : task.getVersion());
        tag.setString(
            "status",
            task.getStatus() == null ? ""
                : task.getStatus()
                    .name());
        tag.setString(
            "priority",
            task.getPriority() == null ? ""
                : task.getPriority()
                    .name());
        tag.setString(
            "importance",
            task.getImportance() == null ? ""
                : task.getImportance()
                    .name());
        tag.setString(
            "urgency",
            task.getUrgency() == null ? ""
                : task.getUrgency()
                    .name());
        tag.setString(
            "visibility",
            task.getVisibility() == null ? ""
                : task.getVisibility()
                    .name());
        tag.setString("parentTaskId", safe(task.getParentTaskId()));
        tag.setString(
            "teamId",
            task.getTeamId() == null ? ""
                : task.getTeamId()
                    .toString());
        tag.setString(
            "creator",
            task.getCreator() == null ? ""
                : task.getCreator()
                    .toString());
        tag.setString(
            "createTime",
            task.getCreateTime() == null ? ""
                : task.getCreateTime()
                    .toString());
        tag.setString(
            "updateTime",
            task.getUpdateTime() == null ? ""
                : task.getUpdateTime()
                    .toString());
        NBTTagList tags = new NBTTagList();
        for (Tag taskTag : TagService.getTagsForTask(task.getId())) {
            NBTTagCompound tagEntry = new NBTTagCompound();
            tagEntry.setString(
                "id",
                taskTag.getId() == null ? ""
                    : taskTag.getId()
                        .toString());
            tagEntry.setString("name", safe(taskTag.getName()));
            tagEntry.setString("description", safe(taskTag.getDescription()));
            tagEntry.setString("colorCode", safe(taskTag.getColorCode()));
            tagEntry.setString("fontColorCode", safe(taskTag.getFontColorCode()));
            tagEntry.setString(
                "scope",
                taskTag.getScope() == null ? ""
                    : taskTag.getScope()
                        .name());
            tags.appendTag(tagEntry);
        }
        tag.setTag("tags", tags);
        return tag;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static boolean isOp(EntityPlayerMP player) {
        return player.mcServer != null && player.mcServer.getConfigurationManager()
            .func_152596_g(player.getGameProfile());
    }
}
