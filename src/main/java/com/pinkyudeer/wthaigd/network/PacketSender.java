package com.pinkyudeer.wthaigd.network;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import com.pinkyudeer.wthaigd.core.PacketHandler;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

public final class PacketSender {

    public static final PacketSender INSTANCE = new PacketSender();

    private PacketSender() {}

    public void sendToPlayers(ResourceLocation handler, NBTTagCompound payload, EntityPlayerMP... players) {
        if (players == null || players.length == 0) return;
        List<NBTTagCompound> fragments = prepare(handler, payload);
        for (EntityPlayerMP player : players) {
            if (player == null) continue;
            for (NBTTagCompound tag : fragments) {
                PacketHandler.INSTANCE.sendTo(new WthaigdPacket(tag), player);
            }
        }
    }

    public void sendToAll(ResourceLocation handler, NBTTagCompound payload) {
        for (NBTTagCompound tag : prepare(handler, payload)) {
            PacketHandler.INSTANCE.sendToAll(new WthaigdPacket(tag));
        }
    }

    public void sendToServer(ResourceLocation handler, NBTTagCompound payload) {
        for (NBTTagCompound tag : prepare(handler, payload)) {
            PacketHandler.INSTANCE.sendToServer(new WthaigdPacket(tag));
        }
    }

    public void sendToAround(ResourceLocation handler, NBTTagCompound payload, TargetPoint point) {
        for (NBTTagCompound tag : prepare(handler, payload)) {
            PacketHandler.INSTANCE.sendToAllAround(new WthaigdPacket(tag), point);
        }
    }

    public void sendToDimension(ResourceLocation handler, NBTTagCompound payload, int dimension) {
        for (NBTTagCompound tag : prepare(handler, payload)) {
            PacketHandler.INSTANCE.sendToDimension(new WthaigdPacket(tag), dimension);
        }
    }

    private List<NBTTagCompound> prepare(ResourceLocation handler, NBTTagCompound payload) {
        NBTTagCompound copy = payload == null ? new NBTTagCompound() : (NBTTagCompound) payload.copy();
        copy.setString("ID", handler.toString());
        return PacketAssembly.INSTANCE.splitPacket(copy);
    }
}
