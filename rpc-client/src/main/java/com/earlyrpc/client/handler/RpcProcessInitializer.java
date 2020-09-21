package com.earlyrpc.client.handler;

import com.earlyrpc.client.fake.RpcDecodeHandler;
import com.earlyrpc.client.fake.RpcEncodeHandler;
import com.earlyrpc.commons.protocol.RpcRequest;
import com.earlyrpc.commons.protocol.RpcResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * todo: xxx
 *
 * @author: czf
 * @date: 2020/9/21 16:58
 */
public class RpcProcessInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new RpcEncodeHandler(RpcRequest.class));
        pipeline.addLast(new RpcDecodeHandler(RpcResponse.class));
        pipeline.addLast(new RpcProcessHandler());
    }
}
