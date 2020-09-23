package com.earlyrpc.client.connect;

import com.earlyrpc.client.handler.RpcProcessHandler;
import io.netty.channel.ChannelFuture;
import lombok.Data;

/**
 * 和 provider服务器的连接的抽象
 *
 * @author: czf
 * @date: 2020/9/23 10:24
 */
@Data
public class RpcChannel {

    private ChannelFuture channelFuture;

    private RpcProcessHandler rpcProcessHandler;

    public RpcChannel(ChannelFuture channelFuture, RpcProcessHandler rpcProcessHandler) {
        this.channelFuture = channelFuture;
        this.rpcProcessHandler = rpcProcessHandler;
    }

    /**
     * 关闭rpcChannel
     */
    public void close(){
        try {
            channelFuture.channel().close().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
