package com.earlyrpc.server;

import com.earlyrpc.registry.RpcRegistry;
import com.earlyrpc.registry.constant.RegistryCenterConfig;
import com.earlyrpc.registry.description.remote.ProviderInfoDesc;
import com.earlyrpc.registry.zookeeper.ZKClient;
import com.earlyrpc.server.handler.RpcServerChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 基于Netty实现的server
 *
 * @author czf
 * @Date 2020/9/21 10:51 下午
 */
@Slf4j
public class RpcServer extends Server {

    /**
     * 服务器真正工作在这个线程中
     */
    private Thread worker;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    /**
     * 用于向zk服务器注册
     */
    private RpcRegistry rpcRegistry;

    /**
     * 注册中心地址
     */
    private String registryAddress;

    /**
     * server本地要绑定的地址
     */
    private String localAddress;

    /**
     * provider端存储在zk服务器上的数据抽象（也可以理解成所有的service）
     */
    private ProviderInfoDesc providerInfoDesc;

    /**
     * 预先实例化好那些实现了接口的serviceBean
     */
    private Map<String, Object> serviceBeanCache;


    public RpcServer(String address, ProviderInfoDesc providerInfoDesc){
        this.registryAddress = "127.0.0.1:2181"; // todo:change
        this.localAddress = address;
        this.providerInfoDesc = providerInfoDesc;
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
        this.rpcRegistry = new ZKClient(registryAddress, RegistryCenterConfig.PROVIDER_TYPE);
        this.serviceBeanCache = new ConcurrentHashMap<>();
    }

    /**
     * 服务器启动
     */
    public void start() {
        this.worker = new Thread(new Runnable() {

            private ThreadPoolExecutor threadPoolExecutor
                    = new ThreadPoolExecutor(4, 8, 600, TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(1000));

            public void run() {
                ServerBootstrap b = new ServerBootstrap();
                try {
                    b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new RpcServerChannelInitializer(serviceBeanCache, threadPoolExecutor))
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true); // todo:...

                    String[] addr = localAddress.split(":");

                    String host = addr[0];
                    int port = Integer.parseInt(addr[1]);

                    ChannelFuture future = b.bind(host, port);

                    rpcRegistry.register(providerInfoDesc);

                    future.channel().closeFuture().sync();
                } catch (Exception e) {
                    if ( e instanceof InterruptedException ){
                        log.warn("服务器启动时被中断...");
                    }else {
                        log.warn("出现异常{}", e);
                    }
                } finally {
                    // 优雅关闭
                    workerGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                    rpcRegistry.close();
                }
            }
        });

        this.worker.start();
    }

    /**
     * 服务器关闭
     */
    public void stop() {
        if ( this.worker!=null && this.worker.isAlive() ){
            this.worker.interrupt();
        }
    }
}
