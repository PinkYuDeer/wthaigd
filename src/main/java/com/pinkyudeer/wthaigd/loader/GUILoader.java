package com.pinkyudeer.wthaigd.loader;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.pinkyudeer.wthaigd.Wthaigd;

import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;

public class GUILoader implements IGuiHandler {

    public GUILoader() {
        NetworkRegistry.INSTANCE.registerGuiHandler(Wthaigd.instance, this);
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        // TODO
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        // TODO
        return null;
    }
}
