package com.pinkyudeer.wthaigd.helper;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class PlayerHelper {

    public static EntityPlayer getPlayerByName(String name) {
        for (EntityPlayer serverPlayer : (List<EntityPlayerMP>) MinecraftServer.getServer()
            .getConfigurationManager().playerEntityList) {
            if (serverPlayer.getCommandSenderName()
                .equals(name)) return serverPlayer;
        }
        return null;
    }

    public static EntityPlayer getPlayerByEntityID(int id) {
        for (EntityPlayer serverPlayer : (ArrayList<EntityPlayerMP>) MinecraftServer.getServer()
            .getConfigurationManager().playerEntityList) {
            if (serverPlayer.getEntityId() == id) return serverPlayer;
        }
        return null;
    }

    public static boolean isOp(String name) {
        for (String n : MinecraftServer.getServer()
            .getConfigurationManager()
            .func_152606_n()) {
            if (n.equals(name)) return true;
        }
        return false;
    }
}
