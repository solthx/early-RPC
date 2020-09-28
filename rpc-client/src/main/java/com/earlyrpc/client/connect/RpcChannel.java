package com.earlyrpc.client.connect;

import com.earlyrpc.client.enums.RpcChannelState;
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
    private String address;

    private ChannelFuture channelFuture;

    private RpcProcessHandler rpcProcessHandler;

    private RpcChannelState state;


    public RpcChannel(String address, ChannelFuture channelFuture, RpcProcessHandler rpcProcessHandler, RpcChannelState state) {
        this.address = address;
        this.channelFuture = channelFuture;
        this.rpcProcessHandler = rpcProcessHandler;
        this.state = state;
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
