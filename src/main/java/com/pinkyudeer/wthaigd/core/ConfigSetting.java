package com.pinkyudeer.wthaigd.core;

import java.util.ArrayList;
import java.util.List;

import com.pinkyudeer.wthaigd.helper.config.ConfigEntry;

public class ConfigSetting {
    // 增加配置方法：
    // CONFIG_ENTRIES.add(new [类型]ConfigEntry("[配置名称]", [默认值], "[描述]", "[配置文件langKey]"));
    // 例如：
    // CONFIG_ENTRIES.add(new StringConfigEntry("greeting", "Hello World", "欢迎使用WTHAIGD!!!",
    // "config.comment.greeting"));
    // 对于整数和小数，还可以指定最小值和最大值：
    // CONFIG_ENTRIES.add(new [类型]ConfigEntry("[配置名称]", [默认值], "[描述]", "[配置文件langKey], [最小值], [最大值]));
    // 例如：
    // CONFIG_ENTRIES.add(new IntConfigEntry("fadeTime", 200, "模糊效果淡入的时间 (毫秒)", "config.comment.fadeTime", 0, 1000));
    // CONFIG_ENTRIES.add(new FloatConfigEntry("blurRadius", 8.0f, "模糊半径",
    // "config.comment.blurRadius"));(不指定最大值最小值则为类型对应的上下限)

    private static final List<ConfigEntry<?>> CONFIG_ENTRIES = initConfigEntries();

    private static List<ConfigEntry<?>> initConfigEntries() {
        List<ConfigEntry<?>> entries = new ArrayList<>();

        entries.add(
            new ConfigEntry.StringConfigEntry(
                "greeting",
                "Hello World",
                "Welcome to WTHAIGD!!!",
                "config.comment.greeting"));
        entries.add(
            new ConfigEntry.BooleanConfigEntry(
                "debugMode",
                false,
                "Whether to enable debug mode",
                "config.comment.debugMode"));
        entries.add(
            new ConfigEntry.BooleanConfigEntry(
                "isMemoryMode",
                true,
                "Whether to enable sqlite memory optimization mode",
                "config.comment.isMemoryMode"));

        // blur
        entries.add(
            new ConfigEntry.IntConfigEntry(
                "blur.downscaleLevels",
                2,
                "Downscale levels for blur effect (Decreased performance cost by 4x per stage with some quality loss)",
                "config.comment.ui.blur.downscaleLevels",
                1,
                3));
        entries.add(
            new ConfigEntry.FloatConfigEntry(
                "blur.radius",
                12.0f,
                "Radius for blur effect (higher values mean more blur with little performance cost)",
                "config.comment.ui.blur.radius",
                0.01f,
                48.0f));
        entries.add(
            new ConfigEntry.IntConfigEntry(
                "blur.blurPasses",
                2,
                "Number of passes for blur effect(Increased performance cost by 2x per pass with 2x smoothness)",
                "config.comment.ui.blur.blurPasses",
                1,
                5));

        // 界面动画配置
        entries.add(
            new ConfigEntry.BooleanConfigEntry(
                "ui.animation.enabled",
                true,
                "Whether to enable fade in/out animation effect",
                "config.comment.ui.animation.blur.enabled"));
        entries.add(
            new ConfigEntry.IntConfigEntry(
                "ui.animation.fadeDuration",
                300,
                "Fade in/out animation duration (ms)",
                "config.comment.ui.animation.blur.fadeDuration",
                1,
                10000));

        return entries;
    }

    public static List<ConfigEntry<?>> getConfigEntry() {
        return CONFIG_ENTRIES;
    }
}
