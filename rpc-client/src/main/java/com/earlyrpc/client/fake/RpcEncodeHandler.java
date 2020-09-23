package com.earlyrpc.client.fake;

import com.earlyrpc.commons.serializer.JSONSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author: czf
 * @date: 2020/8/13 20:12
 */
public class RpcEncodeHandler extends MessageToByteEncoder {
    private Class<?> messageClazz;

    public RpcEncodeHandler(Class<?> messageClazz) {
        this.messageClazz = messageClazz;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
//        if ( msg!=null && messageClazz.isInstance(msg) ){
//            byte[] serialize = SerializationUtil.serialize(msg);
//            out.writeInt(serialize.length);
//            out.writeBytes(serialize);
////            ctx.writeAndFlush(out);
//            System.out.println("写入完毕");
//        }
        if (messageClazz.isInstance(msg)) {
            byte[] data = SerializationUtil.serialize(msg);

            // 編碼
//            JSONSerializer jsonSerializer = new JSONSerializer();
//            byte [] data = jsonSerializer.serialize(msg);

            out.writeInt(data.length);
            out.writeBytes(data);
            System.out.println("写入完毕..");
            System.out.println(data.length);
        }
    }
}
