package com.pinkyudeer.wthaigd.core;

import com.pinkyudeer.wthaigd.Wthaigd;
import com.pinkyudeer.wthaigd.helper.network.NetWorkData;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class WthaigdPacketHandler {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Wthaigd.MODID);

    public static void registerMessages() {
        INSTANCE.registerMessage(NetWorkDataServerHandler.class, NetWorkData.class, 0, Side.SERVER);
        INSTANCE.registerMessage(NetWorkDataClientHandler.class, NetWorkData.class, 1, Side.CLIENT);
    }

    public static class NetWorkDataServerHandler implements IMessageHandler<NetWorkData, IMessage> {

        IMessage response = null;

        /**
         * 处理接收到的消息。
         *
         * @param message 消息对象
         * @param ctx     上下文对象
         * @return 返回处理结果
         */
        @Override
        public IMessage onMessage(NetWorkData message, MessageContext ctx) {
            // 处理接收到的消息
            if (message instanceof NetWorkData) {
                // 处理逻辑
                response = handleNetWorkDataMessage(message);
            }

            // 在这里添加处理逻辑
            return response; // 返回回复消息
        }

        private IMessage handleNetWorkDataMessage(NetWorkData message) {
            // 处理逻辑
            // 例如：根据 message.dataType 执行不同的操作
            switch (message.dataType) {
                case PLAYER_LOGIN:
                    // 处理类型1的数据
                    Wthaigd.LOG.info("Player login data from client received: {}", message.dataToSend);
                    break;
                default:
                    // 处理默认情况
                    break;
            }
            return null; // 返回处理结果
        }
    }

    public static class NetWorkDataClientHandler implements IMessageHandler<NetWorkData, IMessage> {

        IMessage response = null;

        @Override
        public IMessage onMessage(NetWorkData message, MessageContext ctx) {
            // 处理逻辑
            // 例如：根据 message.dataType 执行不同的操作
            switch (message.dataType) {
                case PLAYER_LOGIN:
                    // 处理类型1的数据
                    Wthaigd.LOG.info("Player login data from server received: {}", message.dataToSend);
                    response = new NetWorkData("hello server", NetWorkData.DataType.PLAYER_LOGIN, 200);
                    break;
                default:
                    // 处理默认情况
                    break;
            }
            return response;
        }
    }
}
