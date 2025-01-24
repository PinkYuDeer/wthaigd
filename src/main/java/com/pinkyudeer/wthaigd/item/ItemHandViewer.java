package com.pinkyudeer.wthaigd.item;

import net.minecraft.item.Item;

import com.pinkyudeer.wthaigd.loader.CreativeTabsLoader;

public class ItemHandViewer extends Item {

    public ItemHandViewer() {
        super();
        this.setUnlocalizedName("handViewer");
        this.setTextureName("wthaigd:hand_viewer");
        this.setCreativeTab(CreativeTabsLoader.creativeTabWthaigd);
    }
}
