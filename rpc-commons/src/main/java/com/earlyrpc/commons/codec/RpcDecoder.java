package com.earlyrpc.commons.codec;

import com.earlyrpc.commons.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 解码器: byte -> message
 *
 * @author czf
 * @Date 2020/8/15 9:41 下午
 */
@Deprecated
public class RpcDecoder extends ByteToMessageDecoder {

    /* 将字节流解码成baseClazz对象 */
    private Class<?> baseClazz;

    /* 序列化器 */
    private Serializer serializer;

    public RpcDecoder(Class<?> baseClazz, Serializer serializer) {
        this.baseClazz = baseClazz;
        this.serializer = serializer;
    }

    /**
     * byte -> message(baseClazz)
     * @param channelHandlerContext
     * @param byteBuf
     * @param list
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes()<4){
            // 首个int为长度
            return;
        }
        byteBuf.markReaderIndex(); // 记录位置，若缓冲区中序列化对象没有准备完则回滚
        int length = byteBuf.readInt();
        if (byteBuf.readableBytes()<length){
            byteBuf.resetReaderIndex();
            return;
        }
        ByteBuf baseClazzBytesBuf = byteBuf.readBytes(length);

        if ( baseClazzBytesBuf!=null ) {
            Object entity = serializer.deserialize(baseClazzBytesBuf.array(), baseClazz);
            list.add(entity);
        }
    }
}
