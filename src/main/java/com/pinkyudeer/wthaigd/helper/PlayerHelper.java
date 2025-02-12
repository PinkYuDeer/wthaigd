package com.pinkyudeer.wthaigd.helper;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.server.management.UserListOps;
import net.minecraft.server.management.UserListOpsEntry;

import com.mojang.authlib.GameProfile;

public class PlayerHelper {

    public static EntityPlayer getPlayerByName(String name) {
        for (EntityPlayer serverPlayer : MinecraftServer.getServer()
            .getConfigurationManager().playerEntityList) {
            if (serverPlayer.getCommandSenderName()
                .equals(name)) return serverPlayer;
        }
        return null;
    }

    public static EntityPlayer getPlayerByEntityID(int id) {
        for (EntityPlayer serverPlayer : MinecraftServer.getServer()
            .getConfigurationManager().playerEntityList) {
            if (serverPlayer.getEntityId() == id) return serverPlayer;
        }
        return null;
    }

    public static int getOpLevel(ICommandSender sender) {
        if (sender.canCommandSenderUseCommand(4, "")) {
            return 4;
        }

        MinecraftServer server = MinecraftServer.getServer();

        if (sender instanceof EntityPlayerMP player) {
            GameProfile profile = player.getGameProfile();
            ServerConfigurationManager configManager = server.getConfigurationManager();
            UserListOps ops = configManager.func_152603_m();
            UserListOpsEntry entry = (UserListOpsEntry) ops.func_152683_b(profile);
            return entry == null ? 0 : entry.func_152644_a();
        }
        return 0;

    }
}
