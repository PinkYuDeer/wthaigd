package com.pinkyudeer.wthaigd.task;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import com.pinkyudeer.wthaigd.core.Wthaigd;
import com.pinkyudeer.wthaigd.helper.ConfigHelper;

public class TaskCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "task";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/task <add|remove|list|update|test> [任务名称] [任务描述]";
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
                // TODO:添加任务
            }
            case "remove" -> {
                // TODO:移除任务
            }
            case "list" -> {
                // TODO:列出任务
            }
            case "update" -> {
                // TODO:更新任务
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
            return getListOfStringsMatchingLastWord(args, "add", "remove", "list", "update", "test");
        }
        return null;
    }
}
