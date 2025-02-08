package com.pinkyudeer.wthaigd.helper;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

public class CommandHelper {

    /**
     * 发送格式化消息给命令发送者
     * 
     * @param sender  命令发送者
     * @param message 消息内容
     * @param color   消息颜色
     */
    public static void sendMessage(ICommandSender sender, String message, EnumChatFormatting color) {
        ChatComponentText text = new ChatComponentText(message);
        ChatStyle style = new ChatStyle().setColor(color);
        text.setChatStyle(style);
        sender.addChatMessage(text);
    }

    /**
     * 发送错误消息
     * 
     * @param sender  命令发送者
     * @param message 错误消息
     */
    public static void sendError(ICommandSender sender, String message) {
        sendMessage(sender, message, EnumChatFormatting.RED);
    }

    /**
     * 发送成功消息
     * 
     * @param sender  命令发送者
     * @param message 成功消息
     */
    public static void sendSuccess(ICommandSender sender, String message) {
        sendMessage(sender, message, EnumChatFormatting.GREEN);
    }

    /**
     * 发送信息消息
     * 
     * @param sender  命令发送者
     * @param message 信息消息
     */
    public static void sendInfo(ICommandSender sender, String message) {
        sendMessage(sender, message, EnumChatFormatting.YELLOW);
    }

    /**
     * 检查参数数量是否正确
     * 
     * @param args     参数数组
     * @param expected 期望的参数数量
     * @return 是否符合期望数量
     */
    public static boolean checkArguments(String[] args, int expected) {
        return args.length == expected;
    }

    /**
     * 检查参数数量是否在范围内
     * 
     * @param args 参数数组
     * @param min  最小参数数量
     * @param max  最大参数数量
     * @return 是否在范围内
     */
    public static boolean checkArgumentsRange(String[] args, int min, int max) {
        return args.length >= min && args.length <= max;
    }
}
