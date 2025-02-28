package com.pinkyudeer.wthaigd.gui;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.pinkyudeer.wthaigd.loader.BlockLoader;

public class CreativeTabsWthaigd extends CreativeTabs {

    public CreativeTabsWthaigd() {
        super("wthaigd");
    }

    @Override
    public Item getTabIconItem() {
        return this.getIconItemStack()
            .getItem();
    }

    @Override
    public ItemStack getIconItemStack() {
        return new ItemStack(BlockLoader.debugWthaigd);
    }
}
