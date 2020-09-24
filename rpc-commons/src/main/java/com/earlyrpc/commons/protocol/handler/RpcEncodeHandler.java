package com.earlyrpc.commons.protocol.handler;

import com.earlyrpc.commons.protocol.EarlyRpcProtocol;
import com.earlyrpc.commons.serializer.Serializer;
import com.earlyrpc.commons.serializer.SerializerChooser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author czf
 * @Date 2020/9/23 11:09 下午
 */
@Slf4j
public class RpcEncodeHandler extends MessageToByteEncoder {

    /**
     * 协议体的类对象
     */
    private Class<?> protocolBodyClass;

    /**
     * 默认协议头长度为3（版本号，消息类型，序列化方式）
     */
    private static Short protocolHeaderLength = 3;


    public RpcEncodeHandler(Class<?> protocolBodyClass) {
        this.protocolBodyClass = protocolBodyClass;
    }

    /**
     *  协议内容：
     *      1. 消息包的总长度（4字节）
     *      2. 协议头：
     *          a. 魔数(1字节)
     *          b. 协议头长度(2字节) short
     *          c. 协议版本号(1字节)
     *          d. 消息类型(1字节)
     *          e. 序列化方式(1字节)
     *      3. 协议体：
     *          a. 协议体的序列化内容（序列化为protocolBodyClass类型的对象）
     *
     * @param channelHandlerContext
     * @param protocolBody
     * @param byteBuf
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object protocolBody, ByteBuf byteBuf) throws Exception {

        // 只对protocolBodyClass的类型进行处理
        if ( protocolBody.getClass().equals(protocolBodyClass)  ){

            // 1. 生成协议头
            // 1.1 获取协议版本号
            Byte version = 1;

            // 1.2 确定消息类型(普通rpc通信)
            Byte messageType = 1; // 0x0001表示远程调用协议

            // 1.3 序列化方式(从配置文件里读取, 表示使用protoBuf来进行序列化)
            Byte serializeType = 0;  // 表示protobuf的序列化方式
            Serializer serializer = SerializerChooser.choose(serializeType);

            // 2. 生成协议体
            // 2.1 对object进行序列化
            byte[] protocolBodyBytes = serializer.serialize(protocolBody);

            // 3. 确定消息总长度, 总长度 = sizeof(protocolHeaderLength)
            //                          + protocolHeaderLength
            //                          + protocolBodyLength
//            int messageLength = 1 + Short.BYTES + protocolHeaderLength + protocolBodyBytes.length;

            // 根据协议长度分配buffer
            ByteBuf protocol = Unpooled.buffer();

            protocol.writeByte(EarlyRpcProtocol.MAGIC_CODE);  //1B
            protocol.writeShort(protocolHeaderLength);  // 2B
            protocol.writeByte(version);        // 1B
            protocol.writeByte(messageType);    // 1B
            protocol.writeByte(serializeType);  // 1B

            protocol.writeBytes(protocolBodyBytes); // ?B

            int messageLength = protocol.readableBytes();

            // 4. 填装
            byteBuf.writeInt(messageLength);
            byteBuf.writeBytes(protocol);


            log.debug("send a message : \n"
            +"\tmessageLength:{},\n"
            +"\tprotocolHeaderLength:{}\n"
            +"\tversion:{},\n"
            +"\tmessageType:{},\n"
            +"\tserializeType:{},\n"
            +"\tprotocolBody:{}", messageLength, protocolHeaderLength,version,messageType, serializeType,protocolBody);

        }

    }
}
