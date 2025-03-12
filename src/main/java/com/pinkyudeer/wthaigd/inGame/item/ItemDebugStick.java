package com.pinkyudeer.wthaigd.inGame.item;

import net.minecraft.item.Item;

import com.pinkyudeer.wthaigd.loader.CreativeTabsLoader;

public class ItemDebugStick extends Item {

    public ItemDebugStick() {
        super();
        this.setUnlocalizedName("debugStick");
        this.setTextureName("wthaigd:debug_stick");
        this.setCreativeTab(CreativeTabsLoader.creativeTabWthaigd);
    }
}
