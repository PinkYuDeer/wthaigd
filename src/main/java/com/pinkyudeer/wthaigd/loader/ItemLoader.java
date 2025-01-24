package com.pinkyudeer.wthaigd.loader;

import net.minecraft.item.Item;

import com.pinkyudeer.wthaigd.item.ItemDebugStick;
import com.pinkyudeer.wthaigd.item.ItemHandViewer;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemLoader {

    public static ItemDebugStick itemDebugStick = new ItemDebugStick();
    public static ItemHandViewer itemHandViewer = new ItemHandViewer();

    public ItemLoader(FMLPreInitializationEvent event) {
        registerItems(itemDebugStick, "debugStick");
        registerItems(itemHandViewer, "handViewer");
    }

    private void registerItems(Item item, String name) {
        GameRegistry.registerItem(item, name);
    }
}
