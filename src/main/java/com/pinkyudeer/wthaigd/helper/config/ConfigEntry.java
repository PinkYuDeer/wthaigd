package com.pinkyudeer.wthaigd.helper.config;

import net.minecraftforge.common.config.Configuration;

public abstract class ConfigEntry<T> {

    public final String key; // 配置项的唯一标识符
    public final String category; // 配置项所属的类别
    public T value; // 配置项的值
    public final String comment; // 配置项的描述
    public final String langKey; // 配置项的国际化键
    public final T defaultValue; // 配置项的默认值

    ConfigEntry(String key, T defaultValue, String comment, String langKey) {
        this.key = key;
        this.category = Configuration.CATEGORY_GENERAL;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.comment = comment;
        this.langKey = langKey;
    }

    public abstract void loadFromConfig(Configuration config);

    public abstract void saveToConfig(Configuration config);

    public static class StringConfigEntry extends ConfigEntry<String> {

        @SuppressWarnings("SameParameterValue")
        public StringConfigEntry(String key, String defaultValue, String comment, String langKey) {
            super(key, defaultValue, comment, langKey);
        }

        @Override
        public void loadFromConfig(Configuration config) {
            value = config.getString(key, category, value, comment, langKey);
        }

        @Override
        public void saveToConfig(Configuration config) {
            config.get(category, key, value)
                .set(value);
        }
    }

    public static class BooleanConfigEntry extends ConfigEntry<Boolean> {

        public BooleanConfigEntry(String key, boolean defaultValue, String comment, String langKey) {
            super(key, defaultValue, comment, langKey);
        }

        @Override
        public void loadFromConfig(Configuration config) {
            value = config.getBoolean(key, category, value, comment, langKey);
        }

        @Override
        public void saveToConfig(Configuration config) {
            config.get(category, key, value)
                .set(value);
        }
    }

    @SuppressWarnings("unused")
    public static class IntConfigEntry extends ConfigEntry<Integer> {

        public final int minValue;
        public final int maxValue;

        public IntConfigEntry(String key, int defaultValue, String comment, String langKey, int minValue,
            int maxValue) {
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
        public void loadFromConfig(Configuration config) {
            value = config.getInt(key, category, value, minValue, maxValue, comment, langKey);
        }

        @Override
        public void saveToConfig(Configuration config) {
            config.get(category, key, value)
                .set(value);
        }
    }

    @SuppressWarnings("unused")
    public static class FloatConfigEntry extends ConfigEntry<Float> {

        public final float minValue;
        public final float maxValue;

        public FloatConfigEntry(String key, float defaultValue, String comment, String langKey, float minValue,
            float maxValue) {
            super(key, defaultValue, comment, langKey);
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        public FloatConfigEntry(String key, float defaultValue, String comment, String langKey) {
            super(key, defaultValue, comment, langKey);
            this.minValue = Float.MIN_VALUE;
            this.maxValue = Float.MAX_VALUE;
        }

        @Override
        public void loadFromConfig(Configuration config) {
            value = config.getFloat(key, category, value, minValue, maxValue, comment, langKey);
        }

        @Override
        public void saveToConfig(Configuration config) {
            config.get(category, key, value)
                .set(value);
        }
    }
}
