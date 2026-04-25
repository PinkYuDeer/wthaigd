package com.pinkyudeer.wthaigd.network;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import com.pinkyudeer.wthaigd.network.handler.NetError;
import com.pinkyudeer.wthaigd.network.handler.NetInviteSync;
import com.pinkyudeer.wthaigd.network.handler.NetMainSync;
import com.pinkyudeer.wthaigd.network.handler.NetTaskAction;
import com.pinkyudeer.wthaigd.network.handler.NetTaskSync;
import com.pinkyudeer.wthaigd.network.handler.NetTeamAction;
import com.pinkyudeer.wthaigd.network.handler.NetTeamSync;

public final class PacketTypeRegistry {

    public static final PacketTypeRegistry INSTANCE = new PacketTypeRegistry();

    private final Map<ResourceLocation, ServerPacketHandler> serverHandlers = new HashMap<>();
    private final Map<ResourceLocation, ClientPacketHandler> clientHandlers = new HashMap<>();
    private boolean initialized;

    private PacketTypeRegistry() {}

    public void init() {
        if (initialized) return;
        initialized = true;
        NetMainSync.registerHandler();
        NetTaskAction.registerHandler();
        NetTaskSync.registerHandler();
        NetTeamAction.registerHandler();
        NetTeamSync.registerHandler();
        NetInviteSync.registerHandler();
        NetError.registerHandler();
    }

    public void registerServerHandler(@Nonnull ResourceLocation idName, @Nonnull ServerPacketHandler handler) {
        if (serverHandlers.containsKey(idName)) {
            throw new IllegalArgumentException("Duplicate packet handler: " + idName);
        }
        serverHandlers.put(idName, handler);
    }

    public void registerClientHandler(@Nonnull ResourceLocation idName, @Nonnull ClientPacketHandler handler) {
        if (clientHandlers.containsKey(idName)) {
            throw new IllegalArgumentException("Duplicate packet handler: " + idName);
        }
        clientHandlers.put(idName, handler);
    }

    @Nullable
    public ServerPacketHandler getServerHandler(@Nonnull ResourceLocation idName) {
        return serverHandlers.get(idName);
    }

    @Nullable
    public ClientPacketHandler getClientHandler(@Nonnull ResourceLocation idName) {
        return clientHandlers.get(idName);
    }

    public interface ServerPacketHandler {

        void handle(NBTTagCompound payload, EntityPlayerMP sender);
    }

    public interface ClientPacketHandler {

        void handle(NBTTagCompound payload);
    }
}
