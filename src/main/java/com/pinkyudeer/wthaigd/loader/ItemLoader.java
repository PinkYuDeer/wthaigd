package com.pinkyudeer.wthaigd.loader;

import net.minecraft.item.Item;

import com.pinkyudeer.wthaigd.item.ItemDebugStick;
import com.pinkyudeer.wthaigd.item.ItemHandViewer;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemLoader {

    public static final ItemDebugStick itemDebugStick = new ItemDebugStick();
    public static final ItemHandViewer itemHandViewer = new ItemHandViewer();

    public ItemLoader(FMLPreInitializationEvent event) {
        init();
    }

    private static void registerItems(Item item, String name) {
        GameRegistry.registerItem(item, name);
    }

    public static void init() {
        registerItems(itemDebugStick, "debugStick");
        registerItems(itemHandViewer, "handViewer");
    }
}
