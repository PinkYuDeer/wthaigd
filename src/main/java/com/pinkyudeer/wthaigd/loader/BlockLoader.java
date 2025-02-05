package com.pinkyudeer.wthaigd.loader;

import net.minecraft.block.Block;

import com.pinkyudeer.wthaigd.block.BlockDebugWthaigd;
import com.pinkyudeer.wthaigd.block.BlockViewer;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

public class BlockLoader {

    public static BlockDebugWthaigd debugWthaigd = new BlockDebugWthaigd();
    public static BlockViewer viewer = new BlockViewer();

    public BlockLoader(FMLPreInitializationEvent event) {
        registerBlock(debugWthaigd, "debugWthaigd");
        registerBlock(viewer, "viewer");
    }

    private static void registerBlock(Block block, String name) {
        GameRegistry.registerBlock(block, name);
    }

    public static void init() {
        registerBlock(debugWthaigd, "debugWthaigd");
        registerBlock(viewer, "viewer");
    }
}
