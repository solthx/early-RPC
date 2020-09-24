package com.earlyrpc.commons.codec;

import com.earlyrpc.commons.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * message -> byte
 *
 * @author czf
 * @Date 2020/8/15 10:27 下午
 */
@Deprecated
public class RpcEncoder extends MessageToByteEncoder {
    private Class<?> baseClass;
    private Serializer serializer;

    public RpcEncoder(Class<?> baseClass, Serializer serializer) {
        this.baseClass = baseClass;
        this.serializer = serializer;
    }

    /**
     * message -> byte
     * @param channelHandlerContext
     * @param entity
     * @param byteBuf
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object entity, ByteBuf byteBuf) throws Exception {
        byte[] bytes = serializer.serialize(entity);
        int length = bytes.length;
        byteBuf.writeInt(length);
        byteBuf.writeBytes(bytes);
    }
}
