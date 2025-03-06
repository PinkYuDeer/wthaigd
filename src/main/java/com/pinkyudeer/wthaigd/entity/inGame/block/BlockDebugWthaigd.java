package com.pinkyudeer.wthaigd.entity.inGame.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import com.pinkyudeer.wthaigd.loader.CreativeTabsLoader;

public class BlockDebugWthaigd extends Block {

    public BlockDebugWthaigd() {
        super(Material.gourd);
        this.setBlockName("debugWthaigd");
        this.setBlockTextureName("wthaigd:debug_wthaigd");
        this.setHardness(50F);
        this.setResistance(6000000.0F);
        this.setLightLevel(15.0F);
        this.setLightOpacity(0);
        this.setStepSound(soundTypeMetal);
        this.setCreativeTab(CreativeTabsLoader.creativeTabWthaigd);
    }
}
