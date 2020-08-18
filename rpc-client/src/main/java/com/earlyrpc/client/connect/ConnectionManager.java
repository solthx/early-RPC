package com.earlyrpc.client.connect;

import com.earlyrpc.client.handler.RpcProcessHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * @author czf
 * @Date 2020/8/18 9:49 下午
 */
public class ConnectionManager {

    private EventLoopGroup group = new NioEventLoopGroup(4);

    // todo: 预分配（之后改为池化）
    private RpcProcessHandler rpcProcessHandler;

    // 静态内部类实现单例
    private ConnectionManager(){}

    private static class ConnectionManagerSingleton{
        public static ConnectionManager INSTANCE = new ConnectionManager();
    }

    public static ConnectionManager getInstance(){
        return ConnectionManagerSingleton.INSTANCE;
    }

    /**
     * 获取一个sender
     * @return
     */
    public Sender getSender(){

    }


}
