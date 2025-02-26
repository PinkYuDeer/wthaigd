package com.pinkyudeer.wthaigd.item;

import com.pinkyudeer.wthaigd.loader.CreativeTabsLoader;
import net.minecraft.item.Item;

public class ItemHandViewer extends Item {

    public ItemHandViewer() {
        super();
        this.setUnlocalizedName("handViewer");
        this.setTextureName("wthaigd:hand_viewer");
        this.setCreativeTab(CreativeTabsLoader.creativeTabWthaigd);
    }
}
