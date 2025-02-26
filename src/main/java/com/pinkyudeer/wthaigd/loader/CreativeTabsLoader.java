package com.pinkyudeer.wthaigd.loader;

import com.pinkyudeer.wthaigd.gui.CreativeTabsWthaigd;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.creativetab.CreativeTabs;

public class CreativeTabsLoader {

    public static CreativeTabs creativeTabWthaigd;

    public CreativeTabsLoader(FMLPreInitializationEvent event) {
        init();
    }

    public static void init() {
        creativeTabWthaigd = new CreativeTabsWthaigd();
    }
}
