package com.pinkyudeer.wthaigd.block;

import com.pinkyudeer.wthaigd.loader.CreativeTabsLoader;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

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
