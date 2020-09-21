package com.earlyrpc.client.connect;

import com.earlyrpc.client.handler.RpcProcessHandler;
import com.earlyrpc.client.handler.RpcProcessInitializer;
import com.earlyrpc.registry.CallBack;
import com.earlyrpc.registry.constant.EventType;
import com.earlyrpc.registry.constant.RegistryCenterConfig;
import com.earlyrpc.registry.description.local.ServiceLocalDesc;
import com.earlyrpc.registry.zookeeper.ZKClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author czf
 * @Date 2020/8/18 9:49 下午
 */
@Slf4j
public class ConnectionManager {

    /**
     * 监听consumer节点
     */
    private ZKClient consumerManager;

    /**
     * 监听provider节点
     */
    private ZKClient providerManager;


    private EventLoopGroup group = new NioEventLoopGroup();

    /**
     * 需要进行通信的channelMap, key是ip, value对应找到channel
     */
    private Map<String, RpcProcessHandler> rpcProcessHandlerMap = new ConcurrentHashMap<>();

    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            4,8, 600L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000));

    private Random randomChoose = new Random();

    private String address;

    // 静态内部类实现单例
    private ConnectionManager(){
        this.address = "127.0.0.1:2181";
        this.consumerManager = new ZKClient(address, RegistryCenterConfig.CONSUMER_TYPE);
        this.providerManager = new ZKClient(address, RegistryCenterConfig.PROVIDER_TYPE);
        InitProviderListener();
    }

    /**
     * 初始化providerManager的listener
     */
    private void InitProviderListener() {
        this.providerManager.addListener(new CallBack() {
            @Override
            public void callback(EventType event) {
                if ( event.equals(EventType.AFTER_UPDATE_CACHETABLE) ){
                    // provider发生了更新, 此时需要对channels也进行更新
                    updateRpcProcessHandlerMap();
                }
            }
        });
    }

    /**
     * 根据provider更新rpcProcessHandlerMap
     */
    private void updateRpcProcessHandlerMap() {
        List<String> newProviderServerAddressList = this.providerManager.getCacheTable().getProviderServerAddressList();

        Set<String> serverAddressSet = rpcProcessHandlerMap.keySet();

        // 加入新的
        for( String address:newProviderServerAddressList ){
            if ( serverAddressSet.contains(address) == false ){
                connectServerNode(address);
            }
        }

        HashSet<String> newAddressSet = new HashSet<>(newProviderServerAddressList);

        // 删除下线的
        for( String address:serverAddressSet ){
            if ( newAddressSet.contains(address) == false ){
                RpcProcessHandler rpcProcessHandler = rpcProcessHandlerMap.get(address);
                rpcProcessHandlerMap.remove(address);
                rpcProcessHandler.close();
            }
        }
    }

    /**
     * 和 address 建立channel，更新到map里
     *
     * @param address
     */
    private void connectServerNode(final String address) {
        String[] addr = address.split(":");
        if ( addr.length!=2 )
            throw new RuntimeException("address格式不正确, 应为ip:port或host:port");
        final String host = addr[0];
        final int port = Integer.parseInt(addr[1]);

        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Bootstrap b = new Bootstrap();
                b.group(group)
                        .channel(NioSocketChannel.class)
                        .remoteAddress(host, port)
                        .handler(new RpcProcessInitializer());

                ChannelFuture future = b.connect();
                future.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if ( future.isSuccess() ){
                            // 连接成功之后
                            RpcProcessHandler rpcProcessHandler = future.channel().pipeline().get(RpcProcessHandler.class);
                            addHandler(address, rpcProcessHandler);
                        }
                    }
                });
            }
        });

    }

    /**
     * connect成功之后，将 handler 给 add 到 map中
     * @param address
     * @param rpcProcessHandler
     */
    private void addHandler(String address, RpcProcessHandler rpcProcessHandler) {
        rpcProcessHandlerMap.put(address, rpcProcessHandler);
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
    public Sender getSender(String clazzName){
        // 1. 根据clazzName获取对应的desc
        Map<String, ServiceLocalDesc> serviceMap = providerManager.getCacheTable().getServiceMap();
        ServiceLocalDesc serviceLocalDesc = serviceMap.get(clazzName);

        if ( serviceLocalDesc == null ){
            // 注册中心没有能提供这个服务的节点
            log.warn("未能找到提供{}服务的节点",clazzName);
            return null;
        }

        // 2. 获取提供该接口服务的所有server的address
        List<String> addressList = serviceLocalDesc.getAddressList();

        Sender sender = null;
        int maxRetryTime = 4;
        // 3. 随机的选取一个, 尝试从map里获取，如果获取不到说明被下线了，就重试，最大重试次数默认为4
        while( sender==null && maxRetryTime-- > 0 ){
            int idx = randomChoose.nextInt(addressList.size());
            String addr = addressList.get(idx);
            sender = rpcProcessHandlerMap.get(addr);
        }

        if (sender==null){
            throw new RuntimeException("获取sender失败，超过最大重试次数...");
        }

        return sender;
    }

    public void stop(){
        consumerManager.close();
        providerManager.close();
        threadPoolExecutor.shutdown();
    }
}
