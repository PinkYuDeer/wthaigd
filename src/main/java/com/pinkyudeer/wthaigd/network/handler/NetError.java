package com.pinkyudeer.wthaigd.network.handler;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import com.pinkyudeer.wthaigd.client.TaskClientStore;
import com.pinkyudeer.wthaigd.network.PacketIds;
import com.pinkyudeer.wthaigd.network.PacketSender;
import com.pinkyudeer.wthaigd.network.PacketTypeRegistry;

public final class NetError {

    public static final String INVALID_ACTION = "INVALID_ACTION";
    public static final String INVALID_PAYLOAD = "INVALID_PAYLOAD";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String PERMISSION_DENIED = "PERMISSION_DENIED";
    public static final String VERSION_CONFLICT = "VERSION_CONFLICT";
    public static final String SERVER_ERROR = "SERVER_ERROR";

    private NetError() {}

    public static void registerHandler() {
        PacketTypeRegistry.INSTANCE.registerClientHandler(PacketIds.ERROR, NetError::onClient);
    }

    public static void send(EntityPlayerMP player, String code, String message) {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setString("code", code);
        payload.setString("message", message == null ? "" : message);
        PacketSender.INSTANCE.sendToPlayers(PacketIds.ERROR, payload, player);
    }

    private static void onClient(NBTTagCompound payload) {
        TaskClientStore.INSTANCE.acceptError(payload.getString("code"), payload.getString("message"));
    }
}
