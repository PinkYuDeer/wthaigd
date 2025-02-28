package com.pinkyudeer.wthaigd.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import com.pinkyudeer.wthaigd.loader.CreativeTabsLoader;

public class BlockViewer extends Block {

    public BlockViewer() {
        super(Material.gourd);
        this.setBlockName("viewer");
        this.setBlockTextureName("wthaigd:viewer");
        this.setHardness(50F);
        this.setResistance(6000000.0F);
        this.setLightLevel(15.0F);
        this.setLightOpacity(0);
        this.setStepSound(soundTypeMetal);
        this.setCreativeTab(CreativeTabsLoader.creativeTabWthaigd);
    }
}
