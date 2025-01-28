package com.pinkyudeer.wthaigd.loader;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.IFuelHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

public class RecipeLoader {

    public RecipeLoader(FMLInitializationEvent event) {
        registerCraftingRecipes();
        registerSmeltingRecipes();
        registerFuel();
    }

    private static void registerCraftingRecipes() {
        GameRegistry.addRecipe(
            new ItemStack(ItemLoader.itemHandViewer),
            "#X#",
            "IX-",
            "#X#",
            '#',
            Items.leather,
            'X',
            Items.paper,
            'I',
            Items.stick,
            '-',
            Items.diamond);
    }

    private static void registerSmeltingRecipes() {

    }

    private static void registerFuel() {
        GameRegistry.registerFuelHandler(new IFuelHandler() {

            @Override
            public int getBurnTime(ItemStack fuel) {
                return ItemLoader.itemHandViewer != fuel.getItem() ? 0 : 103600;// 把钻石作为燃料（啊这）,这里的12800指能烧多久，单位是gametick,1秒=20个gametick
            }
        });
    }
}
