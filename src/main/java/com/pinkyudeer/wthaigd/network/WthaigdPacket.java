package com.pinkyudeer.wthaigd.network;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import com.pinkyudeer.wthaigd.Wthaigd;
import com.pinkyudeer.wthaigd.core.ServerTaskScheduler;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class WthaigdPacket implements IMessage {

    private NBTTagCompound tags = new NBTTagCompound();

    @SuppressWarnings("unused")
    public WthaigdPacket() {}

    public WthaigdPacket(NBTTagCompound tags) {
        this.tags = tags;
    }

    public NBTTagCompound getTags() {
        return tags;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        tags = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, tags);
    }

    public static class ServerHandler implements IMessageHandler<WthaigdPacket, IMessage> {

        @Override
        public IMessage onMessage(WthaigdPacket packet, MessageContext ctx) {
            if (packet == null || packet.tags == null || ctx.getServerHandler() == null) {
                Wthaigd.LOG.error("Received invalid wthaigd packet on server");
                return null;
            }

            EntityPlayerMP sender = ctx.getServerHandler().playerEntity;
            UUID owner = sender == null ? null : sender.getUniqueID();
            NBTTagCompound message = PacketAssembly.INSTANCE.assemblePacket(owner, packet.tags);
            if (message == null) return null;
            if (!message.hasKey("ID")) {
                Wthaigd.LOG.warn("Received wthaigd server packet without ID");
                return null;
            }

            ResourceLocation id = new ResourceLocation(message.getString("ID"));
            PacketTypeRegistry.ServerPacketHandler handler = PacketTypeRegistry.INSTANCE.getServerHandler(id);
            if (handler == null) {
                Wthaigd.LOG.warn("Received wthaigd server packet with unknown ID: {}", id);
                return null;
            }

            ServerTaskScheduler.INSTANCE.schedule(() -> handler.handle(message, sender), true);
            return null;
        }
    }
}
