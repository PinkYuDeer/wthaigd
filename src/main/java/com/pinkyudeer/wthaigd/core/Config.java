package com.pinkyudeer.wthaigd.core;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class Config {

    public static Configuration config;
    public static String greeting = "Hello World";
    public static boolean debugMode;
    public static boolean isMemoryMode;

    public static void synchronizeConfiguration() {
        greeting = config.getString(
            "greeting",
            Configuration.CATEGORY_GENERAL,
            greeting,
            "Welcome to WTHAIGD!!!",
            "config.comment.greeting");
        debugMode = config.getBoolean(
            "debugMode",
            Configuration.CATEGORY_GENERAL,
            debugMode,
            "Whether to enable debug mode",
            "config.comment.debugMode");
        isMemoryMode = config.getBoolean(
            "isMemoryMode",
            Configuration.CATEGORY_GENERAL,
            isMemoryMode,
            "Whether to enable sqlite memory optimization mode",
            "config.comment.isMemoryMode");
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equals("wthaigd")) {
            synchronizeConfiguration();
        }
    }

    public static void init(File configFile) {
        config = new Configuration(configFile);
        synchronizeConfiguration();
    }

    // TODO:使服务器可以动态修改配置
}
