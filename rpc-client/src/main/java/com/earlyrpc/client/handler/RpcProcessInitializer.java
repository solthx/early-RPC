package com.earlyrpc.client.handler;

import com.earlyrpc.client.connect.ConnectionManager;
import com.earlyrpc.commons.protocol.RpcRequest;
import com.earlyrpc.commons.protocol.RpcResponse;
import com.earlyrpc.commons.protocol.handler.RpcDecodeHandler;
import com.earlyrpc.commons.protocol.handler.RpcEncodeHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author: czf
 * @date: 2020/9/21 16:58
 */
public class RpcProcessInitializer extends ChannelInitializer<SocketChannel> {

    private ConnectionManager connectionManager;

    public RpcProcessInitializer(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        /*
         * netty自带拆包神器（解决粘包半包问题, 返回的是data的byte）
         * [a,b,c,d,e] =>
         *       a是包的最大长度
         *       开始找data的长度: (ps: 一个包可以是[length|header|data]，也可以是[header|length|data], 也可以是[length|data])
         *           b: offset
         *           c: data数据的长度, 故 [b, b+c]就是数据的长度（如果是一个int的话，就是4）
         *       开始确定最终返回的内容:
         *           d: 获取到长度len后，确定从哪个位置开始读取len个字节（就是读取的offset）
         *           e: 此时已经获取到了整个包体，e是指舍弃前e个字节之后，返回实际内容...
         * */
        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0,0));
        pipeline.addLast(new RpcEncodeHandler(RpcRequest.class));
        pipeline.addLast(new RpcDecodeHandler(RpcResponse.class));
        pipeline.addLast(new RpcProcessHandler(this.connectionManager));
        pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
    }
}
