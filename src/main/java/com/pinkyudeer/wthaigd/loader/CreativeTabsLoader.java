package com.pinkyudeer.wthaigd.loader;

import net.minecraft.creativetab.CreativeTabs;

import com.pinkyudeer.wthaigd.creativeTabs.CreativeTabsWthaigd;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CreativeTabsLoader {

    public static CreativeTabs creativeTabWthaigd;

    public CreativeTabsLoader(FMLPreInitializationEvent event) {
        creativeTabWthaigd = new CreativeTabsWthaigd();
    }
}
