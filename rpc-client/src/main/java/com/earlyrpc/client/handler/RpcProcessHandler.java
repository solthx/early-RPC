package com.earlyrpc.client.handler;

import com.alibaba.fastjson.JSONObject;
import com.earlyrpc.client.connect.Sender;
import com.earlyrpc.commons.protocol.RpcRequest;
import com.earlyrpc.commons.protocol.RpcResponse;
import com.earlyrpc.commons.utils.async.RpcResponsePromise;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * rpc协议业务处理handler
 *
 * @author czf
 * @Date 2020/8/18 8:40 下午
 */
@Slf4j
@Data
public class RpcProcessHandler extends SimpleChannelInboundHandler<RpcResponse> implements Sender {

    /* 用于发送消息的通道 */
    private volatile Channel channel;

    /* 保存正在等待的promise (发出request之后，尚未收到response的那些消息) */
    private Map<Integer, RpcResponsePromise> promiseMap = new ConcurrentHashMap<>(16);


    /**
     * 连接成功时，保存channel用于之后发送数
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.debug("a new channel has been registered.");
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
        log.debug("received a rpc-response: {}", JSONObject.toJSON(rpcResponse));
        Integer responseId = rpcResponse.getResponseId();
        RpcResponsePromise promise = promiseMap.get(responseId);
        if (promise!=null) {
            promise.setSuccess(rpcResponse);
            promiseMap.remove(responseId); // 从map中删除
        }else{
            log.error("pomiseId:{} not found in promiseMap",responseId);
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
        log.debug("send a rpc-request: {}", JSONObject.toJSON(rpcRequest));
        RpcResponsePromise promise = new RpcResponsePromise();
        final int requestId = rpcRequest.getRequestId();
        // 以requestId为key，存到map中，当收到response时，根据requestId进行获取并更新promise
        promiseMap.put(requestId, promise);
        channel.writeAndFlush(rpcRequest).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()){
                    log.error("rpc-request sending failed, the request content is: {}", JSONObject.toJSON(rpcRequest));
                }
            }
        });
        return promise;
    }
}
