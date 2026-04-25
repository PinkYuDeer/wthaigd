package com.pinkyudeer.wthaigd.client.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import com.pinkyudeer.wthaigd.Wthaigd;
import com.pinkyudeer.wthaigd.network.PacketAssembly;
import com.pinkyudeer.wthaigd.network.PacketTypeRegistry;
import com.pinkyudeer.wthaigd.network.WthaigdPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class ClientWthaigdPacketHandler implements IMessageHandler<WthaigdPacket, IMessage> {

    @Override
    public IMessage onMessage(WthaigdPacket packet, MessageContext ctx) {
        if (packet == null || packet.getTags() == null) {
            Wthaigd.LOG.error("Received invalid wthaigd packet on client");
            return null;
        }

        NBTTagCompound message = PacketAssembly.INSTANCE.assemblePacket(null, packet.getTags());
        if (message == null) return null;
        if (!message.hasKey("ID")) {
            Wthaigd.LOG.warn("Received wthaigd client packet without ID");
            return null;
        }

        ResourceLocation id = new ResourceLocation(message.getString("ID"));
        PacketTypeRegistry.ClientPacketHandler handler = PacketTypeRegistry.INSTANCE.getClientHandler(id);
        if (handler == null) {
            Wthaigd.LOG.warn("Received wthaigd client packet with unknown ID: {}", id);
            return null;
        }

        Minecraft.getMinecraft()
            .func_152343_a(() -> {
                handler.handle(message);
                return null;
            });
        return null;
    }
}
