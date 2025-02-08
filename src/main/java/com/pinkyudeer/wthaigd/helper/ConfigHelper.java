package com.pinkyudeer.wthaigd.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.config.Configuration;

import com.pinkyudeer.wthaigd.core.Wthaigd;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ConfigHelper {

    public static Configuration config;

    private static final List<ConfigEntry<?>> CONFIG_ENTRIES = new ArrayList<>();

    private static void addConfigEntry() {
        CONFIG_ENTRIES
            .add(new StringConfigEntry("greeting", "Hello World", "Welcome to WTHAIGD!!!", "config.comment.greeting"));
        CONFIG_ENTRIES.add(
            new BooleanConfigEntry("debugMode", false, "Whether to enable debug mode", "config.comment.debugMode"));
        CONFIG_ENTRIES.add(
            new BooleanConfigEntry(
                "isMemoryMode",
                false,
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
                for (ConfigEntry<?> entry : CONFIG_ENTRIES) {
                    entry.loadFromConfig(config);
                }
            } else {
                for (ConfigEntry<?> entry : CONFIG_ENTRIES) {
                    entry.saveToConfig(config);
                }
            }
        } catch (Exception e) {
            Wthaigd.LOG.error("Failed to load config file, using default values");
        } finally {
            if (config.hasChanged()) config.save();
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

    private static abstract class ConfigEntry<T> {

        final String key;
        final String category;
        T value;
        final String comment;
        final String langKey;

        ConfigEntry(String key, T defaultValue, String comment, String langKey) {
            this.key = key;
            this.category = Configuration.CATEGORY_GENERAL;
            this.value = defaultValue;
            this.comment = comment;
            this.langKey = langKey;
        }

        abstract void loadFromConfig(Configuration config);

        abstract void saveToConfig(Configuration config);
    }

    private static class StringConfigEntry extends ConfigEntry<String> {

        @SuppressWarnings("SameParameterValue")
        StringConfigEntry(String key, String defaultValue, String comment, String langKey) {
            super(key, defaultValue, comment, langKey);
        }

        @Override
        void loadFromConfig(Configuration config) {
            value = config.getString(key, category, value, comment, langKey);
        }

        @Override
        void saveToConfig(Configuration config) {
            config.get(category, key, value)
                .set(value);
        }
    }

    private static class BooleanConfigEntry extends ConfigEntry<Boolean> {

        BooleanConfigEntry(String key, boolean defaultValue, String comment, String langKey) {
            super(key, defaultValue, comment, langKey);
        }

        @Override
        void loadFromConfig(Configuration config) {
            value = config.getBoolean(key, category, value, comment, langKey);
        }

        @Override
        void saveToConfig(Configuration config) {
            config.get(category, key, value)
                .set(value);
        }
    }

    @SuppressWarnings("unused")
    private static class IntConfigEntry extends ConfigEntry<Integer> {

        private final int minValue;
        private final int maxValue;

        IntConfigEntry(String key, int defaultValue, String comment, String langKey, int minValue, int maxValue) {
            super(key, defaultValue, comment, langKey);
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        IntConfigEntry(String key, int defaultValue, String comment, String langKey) {
            super(key, defaultValue, comment, langKey);
            this.minValue = Integer.MIN_VALUE;
            this.maxValue = Integer.MAX_VALUE;
        }

        @Override
        void loadFromConfig(Configuration config) {
            value = config.getInt(key, category, value, minValue, maxValue, comment, langKey);
        }

        @Override
        void saveToConfig(Configuration config) {
            config.get(category, key, value)
                .set(value);
        }
    }

    @SuppressWarnings("unused")
    private static class FloatConfigEntry extends ConfigEntry<Float> {

        private final float minValue;
        private final float maxValue;

        FloatConfigEntry(String key, float defaultValue, String comment, String langKey, float minValue,
            float maxValue) {
            super(key, defaultValue, comment, langKey);
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        FloatConfigEntry(String key, float defaultValue, String comment, String langKey) {
            super(key, defaultValue, comment, langKey);
            this.minValue = Float.MIN_VALUE;
            this.maxValue = Float.MAX_VALUE;
        }

        @Override
        void loadFromConfig(Configuration config) {
            value = config.getFloat(key, category, value, minValue, maxValue, comment, langKey);
        }

        @Override
        void saveToConfig(Configuration config) {
            config.get(category, key, value)
                .set(value);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getConfigValue(String configName) {
        ConfigEntry<T> entry = CONFIG_ENTRIES.stream()
            .filter(e -> e.key.equals(configName))
            .map(e -> (ConfigEntry<T>) e)
            .findFirst()
            .orElse(null);
        if (entry == null) {
            Wthaigd.LOG.error("尝试获取不存在的配置项: {}", configName);
            return null;
        }
        return entry.value;
    }

    // TODO:使服务器可以动态修改配置
}
