package com.pinkyudeer.wthaigd.helper.config;

import java.io.File;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.config.Configuration;

import com.pinkyudeer.wthaigd.Wthaigd;
import com.pinkyudeer.wthaigd.core.ConfigSetting;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ConfigHelper {

    public static Configuration config;

    private static final List<ConfigEntry<?>> CONFIG_ENTRIES = ConfigSetting.getConfigEntry();

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
        synchronizeConfiguration(true);
    }

    public static String getString(String configKey) {
        return getConfigValue(configKey, String.class);
    }

    public static Boolean getBoolean(String configKey) {
        return getConfigValue(configKey, Boolean.class);
    }

    public static Integer getInt(String configKey) {
        return getConfigValue(configKey, Integer.class);
    }

    public static Float getFloat(String configKey) {
        return getConfigValue(configKey, Float.class);
    }

    /**
     * 获取字符串配置值，如果不存在则返回默认值
     * 
     * @param configKey    配置名称
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static String getString(String configKey, String defaultValue) {
        String value = getString(configKey);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取布尔配置值，如果不存在则返回默认值
     * 
     * @param configKey    配置名称
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static boolean getBoolean(String configKey, boolean defaultValue) {
        Boolean value = getBoolean(configKey);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取整数配置值，如果不存在则返回默认值
     * 
     * @param configKey    配置名称
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static int getInt(String configKey, int defaultValue) {
        Integer value = getInt(configKey);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取浮点数配置值，如果不存在则返回默认值
     * 
     * @param configKey    配置名称
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static float getFloat(String configKey, float defaultValue) {
        Float value = getFloat(configKey);
        return value != null ? value : defaultValue;
    }

    private static <T> T getConfigValue(String configKey, Class<T> type) {
        ConfigEntry<?> entry = CONFIG_ENTRIES.stream()
            .filter(e -> e.key.equals(configKey))
            .findFirst()
            .orElse(null);

        if (entry == null) {
            Wthaigd.LOG.error("配置项不存在: {}", configKey);
            return null;
        }

        if (!type.isInstance(entry.value)) {
            Wthaigd.LOG.error(
                "配置项类型不匹配: {} 预期类型: {}, 实际类型: {}",
                configKey,
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
