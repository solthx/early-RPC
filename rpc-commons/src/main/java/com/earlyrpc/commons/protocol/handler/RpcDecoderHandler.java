package com.earlyrpc.commons.protocol.handler;

import com.earlyrpc.commons.serializer.Serializer;
import com.earlyrpc.commons.serializer.SerializerChooser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author czf
 * @Date 2020/9/23 11:10 下午
 */
public class RpcDecoderHandler extends ByteToMessageDecoder {

    /**
     * 协议体的类对象
     */
    private Class<?> protocolBodyClass;

    public RpcDecoderHandler(Class<?> protocolBodyClass) {
        this.protocolBodyClass = protocolBodyClass;
    }

    /**
     * 协议解码:
     *      1. 消息包的总长度（4字节）
     *      2. 协议头：
     *          a. 协议头长度(2字节) short
     *          b. 协议版本号(1字节)
     *          c. 消息类型(1字节)
     *          d. 序列化方式(1字节)
     *      3. 协议体：
     *          a. 协议体的序列化内容（序列化为protocolBodyClass类型的对象）
     *
     * @param channelHandlerContext
     * @param byteBuf
     * @param list
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

        int messageLength = byteBuf.readInt();
        // 协议头长度
        short protocolHeaderLength = byteBuf.readShort();
        // 协议头
        ByteBuf protocolHeaderByteBuf = byteBuf.readBytes(protocolHeaderLength);

        // 协议版本号
        Byte version = protocolHeaderByteBuf.readByte();

        // 消息类型
        Byte messageType = protocolHeaderByteBuf.readByte();

        // 序列化方式
        Byte serializeType = protocolHeaderByteBuf.readByte();
        Serializer serializer = SerializerChooser.choose(serializeType);

        // 获取协议体
        byte [] protocolBodyBytes = new byte[(messageLength-protocolHeaderLength)];
        byteBuf.readBytes(protocolBodyBytes);
        Object protocolBody = serializer.deserialize(protocolBodyBytes, protocolBodyClass);

        list.add(protocolBody);
    }
}
