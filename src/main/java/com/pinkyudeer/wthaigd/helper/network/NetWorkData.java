package com.pinkyudeer.wthaigd.helper.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import lombok.Getter;

@Getter
public class NetWorkData implements IMessage {

    public static final Kryo kryo = new Kryo();
    public Object dataToSend;
    public DataType dataType;
    public int statusCode;

    public enum DataType {
        PLAYER_LOGIN
    }

    public NetWorkData() {}

    public NetWorkData(Object dataToSend, DataType type, int statusCode) {
        this.dataToSend = dataToSend;
        this.dataType = type;
        this.statusCode = statusCode;
    }

    public NetWorkData(String s) {}

    @Override
    public void fromBytes(ByteBuf buf) {
        try (Input input = new Input(new ByteBufInputStream(buf))) {
            // 读取状态码
            statusCode = buf.readInt();
            // 读取数据类型
            dataType = DataType.values()[buf.readInt()];
            // 读取对象数据
            dataToSend = kryo.readClassAndObject(input);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try (Output output = new Output(new ByteBufOutputStream(buf))) {
            // 写入状态码
            buf.writeInt(statusCode);
            // 写入数据类型
            buf.writeInt(dataType.ordinal());
            // 写入对象数据
            kryo.writeClassAndObject(output, dataToSend);
            output.flush();
        }
    }
}
