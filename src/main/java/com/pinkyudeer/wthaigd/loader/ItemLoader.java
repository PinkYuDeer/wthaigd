package com.pinkyudeer.wthaigd.loader;

import com.pinkyudeer.wthaigd.item.ItemDebugStick;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemLoader {

    public static ItemDebugStick ds = new ItemDebugStick();

    public ItemLoader(FMLPreInitializationEvent event) {
        registerItems(ds, "debugStick");
    }

    private void registerItems(ItemDebugStick item, String name) {
        GameRegistry.registerItem(item, name);
    }
}
