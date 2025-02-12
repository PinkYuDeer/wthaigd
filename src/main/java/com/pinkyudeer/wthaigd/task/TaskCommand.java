package com.pinkyudeer.wthaigd.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import com.pinkyudeer.wthaigd.core.Wthaigd;
import com.pinkyudeer.wthaigd.helper.ModFileHelper;
import com.pinkyudeer.wthaigd.helper.ModFileHelper.LocationType;
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
                    case "player" -> {
                        sender.addChatMessage(new ChatComponentText("进入玩家测试模块..."));
                        Wthaigd.LOG.info("进入玩家测试模块");
                    }
                    case "file" -> {
                        sender.addChatMessage(new ChatComponentText("进入文件测试模块..."));
                        Wthaigd.LOG.info("进入文件测试模块");
                        if (args.length < 3) {
                            sender.addChatMessage(new ChatComponentText("用法: /task test file <create|get|delete>"));
                            return;
                        }
                        String fileOp = args[2].toLowerCase();
                        switch (fileOp) {
                            case "create" -> {
                                if (args.length < 5) {
                                    sender.addChatMessage(
                                        new ChatComponentText(
                                            "用法: /task test file create <world|global|config|default> <文件名>"));
                                    return;
                                }
                                String location = args[3].toLowerCase();
                                String fileName = args[4];

                                LocationType locationType = switch (location) {
                                    case "world" -> LocationType.WORLD;
                                    case "global" -> LocationType.GLOBAL;
                                    case "config" -> LocationType.CONFIG;
                                    case "default" -> ModFileHelper.getDefaultLocationType();
                                    default -> {
                                        sender.addChatMessage(new ChatComponentText("无效的位置类型: " + location));
                                        yield null;
                                    }
                                };

                                if (locationType == null) return;

                                File file = ModFileHelper.getFile(fileName, locationType);
                                if (file == null) {
                                    sender.addChatMessage(new ChatComponentText("获取文件路径失败"));
                                    return;
                                }
                                if (ModFileHelper.saveFile(file, locationType, true)) {
                                    sender.addChatMessage(new ChatComponentText("成功创建文件: " + file.getAbsolutePath()));
                                } else {
                                    sender.addChatMessage(new ChatComponentText("文件已存在: " + file.getAbsolutePath()));
                                }

                                try (FileWriter writer = new FileWriter(file)) {
                                    writer.write("Hello World!");
                                    sender.addChatMessage(new ChatComponentText("已写入Hello World!"));
                                } catch (IOException e) {
                                    sender.addChatMessage(new ChatComponentText("写入文件失败: " + e.getMessage()));
                                }
                            }
                            case "get" -> {
                                if (args.length < 4) {
                                    sender.addChatMessage(
                                        new ChatComponentText(
                                            "用法: /task test file get <world|global|config|default> <文件名>"));
                                    return;
                                }
                                String location = args[3].toLowerCase();
                                String fileName = args[4];

                                LocationType locationType = switch (location) {
                                    case "world" -> LocationType.WORLD;
                                    case "global" -> LocationType.GLOBAL;
                                    case "config" -> LocationType.CONFIG;
                                    case "default" -> ModFileHelper.getDefaultLocationType();
                                    default -> {
                                        sender.addChatMessage(new ChatComponentText("无效的位置类型: " + location));
                                        yield null;
                                    }
                                };

                                if (locationType == null) return;

                                File file = ModFileHelper.getFile(fileName, locationType);
                                if (file == null) {
                                    sender.addChatMessage(new ChatComponentText("获取文件路径失败"));
                                    return;
                                }

                                sender.addChatMessage(new ChatComponentText("文件路径: " + file.getAbsolutePath()));
                                sender.addChatMessage(new ChatComponentText("文件存在: " + file.exists()));

                                if (!file.exists()) {
                                    sender.addChatMessage(new ChatComponentText("文件不存在"));
                                    return;
                                }

                                try {
                                    java.nio.file.Path path = file.toPath();
                                    List<String> lines = java.nio.file.Files.readAllLines(path);
                                    sender.addChatMessage(new ChatComponentText("文件内容:"));
                                    for (String line : lines) {
                                        sender.addChatMessage(new ChatComponentText(line));
                                    }
                                } catch (IOException e) {
                                    sender.addChatMessage(new ChatComponentText("读取文件失败: " + e.getMessage()));
                                }
                            }
                            case "delete" -> {
                                if (args.length < 4) {
                                    sender.addChatMessage(
                                        new ChatComponentText(
                                            "用法: /task test file delete <world|global|config|default> <文件名>"));
                                    return;
                                }
                                String location = args[3].toLowerCase();
                                String fileName = args[4];

                                LocationType locationType = switch (location) {
                                    case "world" -> LocationType.WORLD;
                                    case "global" -> LocationType.GLOBAL;
                                    case "config" -> LocationType.CONFIG;
                                    case "default" -> ModFileHelper.getDefaultLocationType();
                                    default -> {
                                        sender.addChatMessage(new ChatComponentText("无效的位置类型: " + location));
                                        yield null;
                                    }
                                };

                                if (locationType == null) return;

                                if (ModFileHelper.deleteFile(fileName, locationType)) {
                                    sender.addChatMessage(new ChatComponentText("成功删除文件"));
                                } else {
                                    sender.addChatMessage(new ChatComponentText("删除文件失败"));
                                }
                            }
                            default -> sender
                                .addChatMessage(new ChatComponentText("用法: /task test file <create|get|delete>"));
                        }
                    }
                    case "config" -> {
                        sender.addChatMessage(new ChatComponentText("进入配置测试模块..."));
                        Wthaigd.LOG.info("进入配置测试模块");
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
            return getListOfStringsMatchingLastWord(args, "add", "remove", "list", "update", "test");
        }
        if (args.length == 2 && "test".equals(args[0])) {
            return getListOfStringsMatchingLastWord(args, "file", "config");
        }
        if (args.length == 3 && "test".equals(args[0]) && "file".equals(args[1])) {
            return getListOfStringsMatchingLastWord(args, "create", "get", "delete");
        }
        if (args.length == 4 && "test".equals(args[0])
            && "file".equals(args[1])
            && ("create".equals(args[2]) || "get".equals(args[2]) || "delete".equals(args[2]))) {
            return getListOfStringsMatchingLastWord(args, "world", "global", "config", "default");
        }
        return null;
    }
}
