package com.pinkyudeer.wthaigd.task;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import com.pinkyudeer.wthaigd.task.entity.Task;

public class TaskCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "task";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/task <add|remove|list|update> [任务名称] [任务描述]";
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
                if (args.length < 3) {
                    sender.addChatMessage(new ChatComponentText("用法: /task add <任务名称> <任务描述>"));
                    return;
                }
                String taskName = args[1];
                String description = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                TaskManager.getInstance()
                    .addTask(new Task(taskName, description));
                sender.addChatMessage(new ChatComponentText("成功添加任务: " + taskName));
            }
            case "remove" -> {
                if (args.length < 2) {
                    sender.addChatMessage(new ChatComponentText("用法: /task remove <任务名称>"));
                    return;
                }
                if (TaskManager.getInstance()
                    .removeTask(args[1])) {
                    sender.addChatMessage(new ChatComponentText("成功删除任务: " + args[1]));
                } else {
                    sender.addChatMessage(new ChatComponentText("未找到任务: " + args[1]));
                }
            }
            case "list" -> {
                List<Task> tasks = TaskManager.getInstance()
                    .getAllTasks();
                if (tasks.isEmpty()) {
                    sender.addChatMessage(new ChatComponentText("当前没有任务"));
                } else {
                    sender.addChatMessage(new ChatComponentText("任务列表:"));
                    for (Task task : tasks) {
                        sender.addChatMessage(
                            new ChatComponentText("- " + task.getTitle() + ": " + task.getDescription()));
                    }
                }
            }
            case "update" -> {
                if (args.length < 3) {
                    sender.addChatMessage(new ChatComponentText("用法: /task update <任务名称> <新描述>"));
                    return;
                }
                String newDescription = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                if (TaskManager.getInstance()
                    .updateTask(args[1], newDescription)) {
                    sender.addChatMessage(new ChatComponentText("成功更新任务: " + args[1]));
                } else {
                    sender.addChatMessage(new ChatComponentText("未找到任务: " + args[1]));
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
            return getListOfStringsMatchingLastWord(args, "add", "remove", "list", "update");
        }
        if (args.length == 2 && args[0].equals("remove")) {
            return getListOfStringsMatchingLastWord(
                args,
                TaskManager.getInstance()
                    .getAllTasks()
                    .stream()
                    .map(Task::getTitle)
                    .toArray(String[]::new));
        }
        if (args.length == 2 && args[0].equals("update")) {
            return getListOfStringsMatchingLastWord(
                args,
                TaskManager.getInstance()
                    .getAllTasks()
                    .stream()
                    .map(Task::getTitle)
                    .toArray(String[]::new));
        }
        return null;
    }
}
