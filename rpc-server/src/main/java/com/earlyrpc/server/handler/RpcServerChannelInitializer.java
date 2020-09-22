package com.earlyrpc.server.handler;

import com.earlyrpc.commons.protocol.RpcRequest;
import com.earlyrpc.commons.protocol.RpcResponse;
import com.earlyrpc.server.service.AliveService;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: czf
 * @date: 2020/9/22 9:20
 */
public class RpcServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private Map<String, AliveService> aliveServiceMap;

    private ThreadPoolExecutor threadPoolExecutor;

    public RpcServerChannelInitializer(Map<String, AliveService> aliveServiceMap, ThreadPoolExecutor threadPoolExecutor) {
        this.aliveServiceMap = aliveServiceMap;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new RpcDecodeHandler(RpcRequest.class));
        pipeline.addLast(new RpcEncodeHandler(RpcResponse.class));
        pipeline.addLast(new RpcCallbackHandler(aliveServiceMap, threadPoolExecutor));
        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
    }
}
