package com.earlyrpc.client.connect;

import com.earlyrpc.client.fake.RpcClient;
import com.earlyrpc.client.fake.RpcDecodeHandler;
import com.earlyrpc.client.fake.RpcEncodeHandler;
import com.earlyrpc.client.handler.RpcProcessHandler;
import com.earlyrpc.commons.protocol.RpcRequest;
import com.earlyrpc.commons.protocol.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * @author czf
 * @Date 2020/8/18 9:49 下午
 */
@Slf4j
public class ConnectionManager {

    private EventLoopGroup group = new NioEventLoopGroup();

    // todo: 预分配（之后改为池化）
    private RpcProcessHandler rpcProcessHandler = new RpcProcessHandler();


    // 静态内部类实现单例
    private ConnectionManager(){
        // todo: 之后加服务注册功能之后，这部分要修改
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .remoteAddress(new InetSocketAddress("127.0.0.1",8000))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new RpcEncodeHandler(RpcRequest.class));
                        pipeline.addLast(new RpcDecodeHandler(RpcResponse.class));
                        pipeline.addLast(rpcProcessHandler);
                    }
                });
        try {
            bootstrap.connect().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

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
        return rpcProcessHandler;
    }


}
