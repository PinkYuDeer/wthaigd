package com.pinkyudeer.wthaigd.network.handler;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.pinkyudeer.wthaigd.client.TaskClientStore;
import com.pinkyudeer.wthaigd.network.PacketIds;
import com.pinkyudeer.wthaigd.network.PacketSender;
import com.pinkyudeer.wthaigd.network.PacketTypeRegistry;

public final class NetInviteSync {

    private NetInviteSync() {}

    public static void registerHandler() {
        PacketTypeRegistry.INSTANCE.registerClientHandler(PacketIds.INVITE_SYNC, NetInviteSync::onClient);
    }

    public static void sendSync(EntityPlayerMP player) {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setTag("data", new NBTTagList());
        PacketSender.INSTANCE.sendToPlayers(PacketIds.INVITE_SYNC, payload, player);
    }

    private static void onClient(NBTTagCompound payload) {
        TaskClientStore.INSTANCE.acceptInviteSync(payload.getTagList("data", 10));
    }
}
