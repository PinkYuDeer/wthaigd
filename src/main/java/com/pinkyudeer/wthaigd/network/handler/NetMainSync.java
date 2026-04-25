package com.pinkyudeer.wthaigd.network.handler;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import com.pinkyudeer.wthaigd.client.TaskClientStore;
import com.pinkyudeer.wthaigd.network.PacketIds;
import com.pinkyudeer.wthaigd.network.PacketSender;
import com.pinkyudeer.wthaigd.network.PacketTypeRegistry;

public final class NetMainSync {

    private NetMainSync() {}

    public static void registerHandler() {
        PacketTypeRegistry.INSTANCE.registerServerHandler(PacketIds.MAIN_SYNC, NetMainSync::onServer);
        PacketTypeRegistry.INSTANCE.registerClientHandler(PacketIds.MAIN_SYNC, NetMainSync::onClient);
    }

    public static void requestSync() {
        PacketSender.INSTANCE.sendToServer(PacketIds.MAIN_SYNC, new NBTTagCompound());
    }

    public static void sendReset(EntityPlayerMP player, boolean reset, boolean respond) {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setBoolean("reset", reset);
        payload.setBoolean("respond", respond);
        PacketSender.INSTANCE.sendToPlayers(PacketIds.MAIN_SYNC, payload, player);
    }

    public static void sendFullSync(EntityPlayerMP player) {
        sendReset(player, true, false);
        NetTeamSync.sendSync(player, false);
        NetTaskSync.sendSync(player, false);
        NetInviteSync.sendSync(player);
    }

    private static void onServer(NBTTagCompound payload, EntityPlayerMP sender) {
        if (sender != null) sendFullSync(sender);
    }

    private static void onClient(NBTTagCompound payload) {
        if (payload.getBoolean("reset")) {
            TaskClientStore.INSTANCE.reset();
        }
        if (payload.getBoolean("respond")) {
            requestSync();
        }
    }
}
