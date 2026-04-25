package com.pinkyudeer.wthaigd.core;

import com.pinkyudeer.wthaigd.Wthaigd;
import com.pinkyudeer.wthaigd.network.PacketTypeRegistry;
import com.pinkyudeer.wthaigd.network.WthaigdPacket;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class PacketHandler {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Wthaigd.MODID);

    public static void registerMessages() {
        PacketTypeRegistry.INSTANCE.init();
        INSTANCE.registerMessage(WthaigdPacket.ServerHandler.class, WthaigdPacket.class, 0, Side.SERVER);
    }
}
