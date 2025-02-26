package com.pinkyudeer.wthaigd.block;

import com.pinkyudeer.wthaigd.loader.CreativeTabsLoader;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

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
