package com.earlyrpc.commons.protocol.handler;

import com.earlyrpc.commons.protocol.EarlyRpcProtocol;
import com.earlyrpc.commons.serializer.Serializer;
import com.earlyrpc.commons.serializer.SerializerChooser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author czf
 * @Date 2020/9/23 11:10 下午
 */
@Slf4j
public class RpcDecodeHandler extends ByteToMessageDecoder {

    /**
     * 协议体的类对象
     */
    private Class<?> protocolBodyClass;

    public RpcDecodeHandler(Class<?> protocolBodyClass) {
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
        if (byteBuf.readableBytes() < 4) {
//            byteBuf.markReaderIndex();
//            log.info("{}<4 返回, 数据为：\n", byteBuf.readableBytes());
//            char[] ch = byteBuf.readBytes(byteBuf.readableBytes()).toString().toCharArray();
//            for( int i=0; i<ch.length; ++i ){
//                System.out.print(ch[i]);
//            }
//            System.out.println();
//            byteBuf.resetReaderIndex();
            return;
        }

        byteBuf.markReaderIndex();

        // 消息总长度
        int messageLength = byteBuf.readInt();


//        if ( byteBuf.readableBytes()<messageLength ){
//            log.info("{}<messageLength={} 返回", byteBuf.readableBytes(), messageLength);
//            byteBuf.resetReaderIndex();
//            return ;
//        }

        // 魔数
        byte magicCode = byteBuf.readByte();

        if ( magicCode != EarlyRpcProtocol.MAGIC_CODE ){
            // 如果不是earlyRpc协议，则不处理
            byteBuf.resetReaderIndex();
            return ;
        }


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

        // 获取协议体 = 总长度 - 协议头长度(protocolHeaderLength) - 协议头长度的长度(short=2字节) - 魔数长度(1字节)
        //           = 总长度 - 协议头长度(protocolHeaderLength) - 3
        byte [] protocolBodyBytes = new byte[(messageLength-protocolHeaderLength-3)];
        byteBuf.readBytes(protocolBodyBytes);
        Object protocolBody = serializer.deserialize(protocolBodyBytes, protocolBodyClass);

        list.add(protocolBody);

        log.debug("received a message : \n"
                +"\tmessageLength:{},\n"
                +"\tprotocolHeaderLength:{}\n"
                +"\tversion:{},\n"
                +"\tmessageType:{},\n"
                +"\tserializeType:{},\n"
                +"\tprotocolBody:{}", messageLength, protocolHeaderLength,version,messageType, serializeType,protocolBody);

    }
}
