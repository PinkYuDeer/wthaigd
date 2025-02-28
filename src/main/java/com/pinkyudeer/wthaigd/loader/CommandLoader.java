package com.pinkyudeer.wthaigd.loader;

import com.pinkyudeer.wthaigd.task.TaskCommand;

import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class CommandLoader {

    public static void init(FMLServerStartingEvent event) {
        event.registerServerCommand(new TaskCommand());
    }
}
