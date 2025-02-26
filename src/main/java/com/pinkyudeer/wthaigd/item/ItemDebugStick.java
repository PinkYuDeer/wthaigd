package com.pinkyudeer.wthaigd.item;

import com.pinkyudeer.wthaigd.loader.CreativeTabsLoader;
import net.minecraft.item.Item;

public class ItemDebugStick extends Item {

    public ItemDebugStick() {
        super();
        this.setUnlocalizedName("debugStick");
        this.setTextureName("wthaigd:debug_stick");
        this.setCreativeTab(CreativeTabsLoader.creativeTabWthaigd);
    }
}
