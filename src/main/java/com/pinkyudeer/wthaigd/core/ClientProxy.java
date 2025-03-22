package com.pinkyudeer.wthaigd.core;

import com.pinkyudeer.wthaigd.gui.KeyBindGuiHandler;
import com.pinkyudeer.wthaigd.gui.ModularTheme;
import com.pinkyudeer.wthaigd.helper.shader.OptimizedBlurHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    // Override CommonProxy methods here, if you want a different behaviour on the client (e.g. registering renders).
    // Don't forget to call the super methods as well.

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        FMLCommonHandler.instance()
            .bus()
            .register(new KeyBindGuiHandler());
        ModularTheme.init();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        super.init(event);
        OptimizedBlurHandler.init();
    }
}
