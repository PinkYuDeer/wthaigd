package com.pinkyudeer.wthaigd.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import com.pinkyudeer.wthaigd.core.PacketHandler;

public class NetWorkHelper {

    public static void SendMessageToClient(String message, EntityPlayer player, NetWorkData.DataType dataType,
        int statusCode) {
        NetWorkData netWorkData = new NetWorkData(message, dataType, statusCode);
        PacketHandler.INSTANCE.sendTo(netWorkData, (EntityPlayerMP) player);
    }

    public static void SendMessageToServer(String message, NetWorkData.DataType dataType, int statusCode) {
        NetWorkData netWorkData = new NetWorkData(message, dataType, statusCode);
        PacketHandler.INSTANCE.sendToServer(netWorkData);
    }
}
