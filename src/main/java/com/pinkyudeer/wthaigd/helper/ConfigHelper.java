package com.pinkyudeer.wthaigd.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.config.Configuration;

import com.pinkyudeer.wthaigd.core.Wthaigd;
import com.pinkyudeer.wthaigd.helper.entity.ConfigEntry;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ConfigHelper {

    public static Configuration config;

    private static final List<ConfigEntry<?>> CONFIG_ENTRIES = new ArrayList<>();

    private static void addConfigEntry() {
        CONFIG_ENTRIES.add(
            new ConfigEntry.StringConfigEntry(
                "greeting",
                "Hello World",
                "Welcome to WTHAIGD!!!",
                "config.comment.greeting"));
        CONFIG_ENTRIES.add(
            new ConfigEntry.BooleanConfigEntry(
                "debugMode",
                false,
                "Whether to enable debug mode",
                "config.comment.debugMode"));
        CONFIG_ENTRIES.add(
            new ConfigEntry.BooleanConfigEntry(
                "isMemoryMode",
                true,
                "Whether to enable sqlite memory optimization mode",
                "config.comment.isMemoryMode"));
        // CONFIG_ENTRIES.add(new IntConfigEntry("someInt", 50, "一个整数", "config.comment.someInt")); // 使用默认的最大最小值
        // CONFIG_ENTRIES.add(new FloatConfigEntry("someFloat", 0.5f, "一个小数", "config.comment.someFloat")); //
        // 使用默认的最大最小值
        // CONFIG_ENTRIES.add(new IntConfigEntry("someInt", 50, "一个0到100的整数", "config.comment.someInt", 0, 100)); //
        // 限制在0-100之间
        // CONFIG_ENTRIES.add(new FloatConfigEntry("someFloat", 0.5f, "一个0到1的小数", "config.comment.someFloat", 0.0f,
        // 1.0f)); // 限制在0-1之间
    }

    public static void synchronizeConfiguration(boolean loadFromFile) {
        try {
            if (loadFromFile) {
                config.load();
                CONFIG_ENTRIES.forEach(entry -> entry.loadFromConfig(config));
            } else {
                CONFIG_ENTRIES.forEach(entry -> entry.saveToConfig(config));
            }
        } catch (Exception e) {
            Wthaigd.LOG.error("配置文件加载失败，使用默认值", e);
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equals("wthaigd")) {
            synchronizeConfiguration(false);
        }
    }

    public static void init(File configFile) {
        config = new Configuration(configFile);
        addConfigEntry();
        synchronizeConfiguration(true);
    }

    public static String getStringConfig(String configName) {
        return getConfigValue(configName, String.class);
    }

    public static Boolean getBooleanConfig(String configName) {
        return getConfigValue(configName, Boolean.class);
    }

    public static Integer getIntConfig(String configName) {
        return getConfigValue(configName, Integer.class);
    }

    public static Float getFloatConfig(String configName) {
        return getConfigValue(configName, Float.class);
    }

    private static <T> T getConfigValue(String configName, Class<T> type) {
        ConfigEntry<?> entry = CONFIG_ENTRIES.stream()
            .filter(e -> e.key.equals(configName))
            .findFirst()
            .orElse(null);

        if (entry == null) {
            Wthaigd.LOG.error("配置项不存在: {}", configName);
            return null;
        }

        if (!type.isInstance(entry.value)) {
            Wthaigd.LOG.error(
                "配置项类型不匹配: {} 预期类型: {}, 实际类型: {}",
                configName,
                type.getSimpleName(),
                entry.value.getClass()
                    .getSimpleName());
            return null;
        }

        return type.cast(entry.value);
    }

    /**
     * 输出所有配置项到日志
     *
     * @param sender   ICommandSender命令发送者
     * @param detailed 是否输出详细信息
     */
    public static void logAllConfigs(ICommandSender sender, boolean detailed) {
        Wthaigd.LOG.info("当前配置项列表:");
        CONFIG_ENTRIES.forEach(entry -> {
            if (detailed) {
                String details = getConfigDetails(entry);
                Wthaigd.LOG.info(details);
                sender.addChatMessage(new ChatComponentText(details));
            } else {
                Wthaigd.LOG.info("配置项: {} = {}", entry.key, entry.value);
                sender.addChatMessage(new ChatComponentText(entry.key + " = " + entry.value));
            }
        });
    }

    @Nonnull
    private static String getConfigDetails(ConfigEntry<?> entry) {
        String details = String.format(
            "配置项: %s | 类别: %s | 当前值: %s | 默认值: %s | 描述: %s | 国际化键: %s",
            entry.key,
            entry.category,
            entry.value,
            entry.defaultValue,
            entry.comment,
            entry.langKey);

        if (entry instanceof ConfigEntry.IntConfigEntry intEntry) {
            details += String.format(" | 最小值: %d | 最大值: %d", intEntry.minValue, intEntry.maxValue);
        }
        return details;
    }

    // TODO:使服务器可以动态修改配置
}
