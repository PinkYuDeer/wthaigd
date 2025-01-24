package com.pinkyudeer.wthaigd.loader;

import net.minecraft.block.Block;

import com.pinkyudeer.wthaigd.block.BlockDebugWthaigd;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

public class BlockLoader {

    public static Block debugWthaigd = new BlockDebugWthaigd();

    public BlockLoader(FMLPreInitializationEvent event) {
        registerBlock(debugWthaigd, "debugWthaigd");
    }

    private static void registerBlock(Block block, String name) {
        GameRegistry.registerBlock(block, name);

    }
}
