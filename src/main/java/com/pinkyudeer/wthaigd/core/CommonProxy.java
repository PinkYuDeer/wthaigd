package com.pinkyudeer.wthaigd.core;

import com.pinkyudeer.wthaigd.Tags;
import com.pinkyudeer.wthaigd.helper.ModFileManager;
import com.pinkyudeer.wthaigd.loader.BlockLoader;
import com.pinkyudeer.wthaigd.loader.CreativeTabsLoader;
import com.pinkyudeer.wthaigd.loader.ItemLoader;
import com.pinkyudeer.wthaigd.loader.RecipeLoader;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items, etc., and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        Wthaigd.LOG.info(Config.greeting);
        Wthaigd.LOG.info("wthaigd version {}", Tags.VERSION);
        CreativeTabsLoader.init();
        ItemLoader.init();
        BlockLoader.init();
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        new RecipeLoader(event);
    }

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {}

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        ModFileManager.init();
        Config.init();
    }
}
