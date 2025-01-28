package com.pinkyudeer.wthaigd.loader;

import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ConfigLoader {

    private static Configuration config;

    private static Logger logger;

    public ConfigLoader(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        config = new Configuration(event.getSuggestedConfigurationFile());
        syncConfig();
    }

    public static void syncConfig() {
        try {
            config.load();
            // Config.greeting = config.getString("greeting", Configuration.CATEGORY_GENERAL, Config.greeting, "How
            // shall I greet?");
        } catch (Exception e) {
            logger.error("Error loading config file!", e);
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}
