package com.earlyrpc.commons.protocol.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author czf
 * @Date 2020/9/23 11:09 下午
 */
public class RpcEncoderHandler extends MessageToByteEncoder {

    /**
     * 协议体的类对象
     */
    private Class<?> protocolBodyClass;

    /**
     * 默认协议头长度为3（版本号，消息类型，序列化方式）
     */
    private static Short protocolHeaderLength = 3;


    public RpcEncoderHandler(Class<?> protocolBodyClass) {
        this.protocolBodyClass = protocolBodyClass;
    }

    /**
     *  协议内容：
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
     * @param o
     * @param byteBuf
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {


    }
}
