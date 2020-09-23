package com.earlyrpc.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author: czf
 * @date: 2020/8/13 18:55
 */
public class RpcDecodeHandler extends ByteToMessageDecoder {
    private Class<?> messageClazz;

    public RpcDecodeHandler(Class<?> messageClazz) {
        this.messageClazz = messageClazz;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
//        in.markReaderIndex();
//        if (in.readableBytes()<4) return;
//        int length = in.readInt();
//        if ( length <= in.readableBytes() ){
//            RpcRequest rpcRequest = new RpcRequest();
//            ByteBuf byteBuf = in.readBytes(length);
//
//            Object entity = SerializationUtil.deserialize(byteBuf.array(), messageClazz);
//
//            out.add(entity);
//
//        }else{
//            in.resetReaderIndex();
//            return;
//        }
        System.out.println("有数据来了");
        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        int dataLength = in.readInt();
        if (dataLength < 0) {
            ctx.close();
        }
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        Object obj = SerializationUtil.deserialize(data, messageClazz);
        System.out.println(obj);
        out.add(obj);
    }
}
