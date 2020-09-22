package com.earlyrpc.server.handler;

import com.earlyrpc.commons.protocol.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 处理 RpcRequest, 本地回调服务方法
 *
 * @author: czf
 * @date: 2020/9/22 9:30
 */
public class RpcCallbackHandler extends SimpleChannelInboundHandler<RpcRequest> {


    /**
     * 提前实例化好的 实现了服务接口的 object对象
     */
    private Map<String, Object> serviceBeanCache;

    private ThreadPoolExecutor threadPoolExecutor;

    public RpcCallbackHandler(Map<String, Object> serviceBeanCache, ThreadPoolExecutor threadPoolExecutor) {
        this.serviceBeanCache = serviceBeanCache;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        // todo:...
    }
}
