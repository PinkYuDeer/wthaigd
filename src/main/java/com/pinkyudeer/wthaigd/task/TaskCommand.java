package com.pinkyudeer.wthaigd.task;

import java.util.List;
import java.util.UUID;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

import com.pinkyudeer.wthaigd.Wthaigd;
import com.pinkyudeer.wthaigd.config.ConfigHelper;
import com.pinkyudeer.wthaigd.helper.UtilHelper;
import com.pinkyudeer.wthaigd.task.entity.Task;
import com.pinkyudeer.wthaigd.task.service.TaskService;
import com.pinkyudeer.wthaigd.task.service.TeamService;

public class TaskCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "task";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/task <add|remove|done|list|update|test> ...";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
            return;
        }

        String operation = args[0].toLowerCase();

        switch (operation) {
            case "add" -> {
                handleAdd(sender, args);
            }
            case "remove" -> {
                handleRemove(sender, args);
            }
            case "list" -> {
                handleList(sender, args);
            }
            case "update" -> {
                handleUpdate(sender, args);
            }
            case "done" -> {
                handleDone(sender, args);
            }
            case "test" -> {
                sender.addChatMessage(new ChatComponentText("debug"));
                // 在此处运行测试代码，根据test后args不同运行不同的测试模块
                // TODO:正式发布前清理此处
                if (args.length < 2) {
                    sender.addChatMessage(new ChatComponentText("用法: /task test <测试模块>"));
                    return;
                }
                String testModule = args[1].toLowerCase();
                Wthaigd.LOG.info("进入测试模式");
                switch (testModule) {
                    case "sql" -> {
                        sender.addChatMessage(new ChatComponentText("进入SQL测试模块..."));
                        Wthaigd.LOG.info("进入SQL测试模块");
                    }
                    case "config" -> {
                        sender.addChatMessage(new ChatComponentText("进入配置测试模块..."));
                        Wthaigd.LOG.info("进入配置测试模块");
                        // 打印全部配置
                        ConfigHelper.logAllConfigs(sender, true);
                        Wthaigd.LOG.info("测试配置完成");
                    }
                    default -> {
                        sender.addChatMessage(new ChatComponentText("未知的测试模块: " + testModule));
                        Wthaigd.LOG.info("未知的测试模块: {}", testModule);
                    }
                }
            }
            default -> sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "add", "remove", "done", "list", "update", "test");
        }
        return null;
    }

    private void handleAdd(ICommandSender sender, String[] args) {
        EntityPlayerMP player = requirePlayer(sender);
        if (player == null) return;
        if (args.length < 2) {
            sender.addChatMessage(new ChatComponentText("用法: /task add <标题> [描述]"));
            return;
        }

        String title = args[1];
        String description = joinArgs(args, 2);
        TaskService.PermissionContext context = TeamService.contextFor(player.getUniqueID(), null, isOp(player));
        Task task = TaskService.createTask(context, title, description, Task.Importance.MEDIUM, Task.Urgency.MEDIUM);
        sender.addChatMessage(new ChatComponentText(task == null ? "任务创建失败" : "任务已创建: " + task.getId()));
    }

    private void handleList(ICommandSender sender, String[] args) {
        UUID playerId = sender instanceof EntityPlayerMP player ? player.getUniqueID() : null;
        boolean op = !(sender instanceof EntityPlayerMP player) || isOp(player);
        boolean showAll = args.length > 1 && "all".equalsIgnoreCase(args[1]);
        List<Task> tasks = showAll && op ? TaskService.getAllTasks() : TaskService.getVisibleTasks(playerId, op);
        if (tasks.isEmpty()) {
            sender.addChatMessage(new ChatComponentText("暂无可见任务"));
            return;
        }
        sender.addChatMessage(new ChatComponentText("可见任务: " + tasks.size()));
        for (Task task : tasks) {
            sender
                .addChatMessage(new ChatComponentText(task.getId() + " [" + task.getStatus() + "] " + task.getTitle()));
        }
    }

    private void handleRemove(ICommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.addChatMessage(new ChatComponentText("用法: /task remove <任务ID>"));
            return;
        }
        Task task = TaskService.getTask(args[1]);
        if (!canWriteTask(sender, task)) return;
        boolean ok = TaskService.deleteTask(args[1]);
        sender.addChatMessage(new ChatComponentText(ok ? "任务已删除" : "任务删除失败"));
    }

    private void handleDone(ICommandSender sender, String[] args) {
        EntityPlayerMP player = requirePlayer(sender);
        if (player == null) return;
        if (args.length != 2) {
            sender.addChatMessage(new ChatComponentText("用法: /task done <任务ID>"));
            return;
        }
        Task task = TaskService.getTask(args[1]);
        if (!canWriteTask(sender, task)) return;
        boolean ok = TaskService.completeTask(args[1], player.getUniqueID());
        sender.addChatMessage(new ChatComponentText(ok ? "任务已完成" : "任务完成失败"));
    }

    private void handleUpdate(ICommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.addChatMessage(new ChatComponentText("用法: /task update <任务ID> <标题> [描述]"));
            return;
        }
        Task task = TaskService.getTask(args[1]);
        if (!canWriteTask(sender, task)) return;
        Task oldTask = UtilHelper.deepClone(task, Task.class);
        task.setTitle(args[2]);
        task.setDescription(joinArgs(args, 3));
        boolean ok = TaskService.updateTask(task, oldTask);
        sender.addChatMessage(new ChatComponentText(ok ? "任务已更新" : "任务更新失败"));
    }

    private boolean canWriteTask(ICommandSender sender, Task task) {
        if (task == null) {
            sender.addChatMessage(new ChatComponentText("任务不存在"));
            return false;
        }
        if (!(sender instanceof EntityPlayerMP player)) return true;
        if (isOp(player) || player.getUniqueID()
            .equals(task.getCreator())) {
            return true;
        }
        sender.addChatMessage(new ChatComponentText("无权操作此任务"));
        return false;
    }

    private EntityPlayerMP requirePlayer(ICommandSender sender) {
        if (sender instanceof EntityPlayerMP player) return player;
        sender.addChatMessage(new ChatComponentText("此命令须由玩家执行"));
        return null;
    }

    private boolean isOp(EntityPlayerMP player) {
        return player.mcServer != null && player.mcServer.getConfigurationManager()
            .func_152596_g(player.getGameProfile());
    }

    private String joinArgs(String[] args, int from) {
        if (args.length <= from) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < args.length; i++) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(args[i]);
        }
        return sb.toString();
    }
}
