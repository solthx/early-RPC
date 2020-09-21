package com.earlyrpc.client.handler;

import com.earlyrpc.client.connect.Sender;
import com.earlyrpc.commons.protocol.RpcRequest;
import com.earlyrpc.commons.protocol.RpcResponse;
import com.earlyrpc.commons.utils.async.RpcResponsePromise;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * rpc协议业务处理handler
 *
 * @author czf
 * @Date 2020/8/18 8:40 下午
 */
@Slf4j
public class RpcProcessHandler extends SimpleChannelInboundHandler<RpcResponse> implements Sender {

    /* 用于发送消息的通道 */
    private volatile Channel channel;

    /* 保存正在等待的promise */
    private Map<Integer, RpcResponsePromise> promiseMap = new ConcurrentHashMap<>(16);


    /**
     * 连接成功时，保存channel用于之后发送数
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    /**
     * 接收到rpcResponse，将其放到future中
     * @param channelHandlerContext
     * @param rpcResponse
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        Integer responseId = rpcResponse.getResponseId();
        System.out.println("request:"+responseId);
        RpcResponsePromise promise = promiseMap.get(responseId);
        if (promise!=null) {
            promise.setSuccess(rpcResponse);
            promiseMap.remove(responseId); // 从map中删除
        }else{
            log.error("pomiseId:{} 不存在",responseId);
        }
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 发送数据
     *
     *
     * @param rpcRequest
     * @return
     */
    @Override
    public RpcResponsePromise sendRequest(final RpcRequest rpcRequest){
        RpcResponsePromise promise = new RpcResponsePromise();
        int requestId = rpcRequest.getRequestId();
        // 以requestId为key，存到map中，当收到response时，根据requestId进行获取并更新promise
        promiseMap.put(requestId, promise);
        channel.writeAndFlush(rpcRequest).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()){
                    log.error("rpc请求发送失败: {}", rpcRequest);
                }
            }
        });
        return promise;
    }
}
