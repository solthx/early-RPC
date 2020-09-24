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

import java.util.*;
import java.util.concurrent.*;

/**
 * 管理当前客户端与rpcServer的长连接
 *
 * @author czf
 * @Date 2020/8/18 9:49 下午
 */
@Slf4j
public class ConnectionManager {

    /**
     * 监听consumer节点
     */
//    private ZKClient consumerManager;

    /**
     * 监听provider节点
     */
    private ZKClient providerManager;


    private EventLoopGroup group = new NioEventLoopGroup();

    /**
     * 需要进行通信的channelMap, key是ip, value对应找到channel
     */
    private Map<String, RpcChannel> rpcChannelMap = new ConcurrentHashMap<>();

    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            4,8, 600L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000));

    private Random randomChoose = new Random();

    private String address;

    private Set<String> aliveServerAddressSet;

    // 静态内部类实现单例
    private ConnectionManager(){
        this.address = "127.0.0.1:2181";
//        this.consumerManager = new ZKClient(address, RegistryCenterConfig.CONSUMER_TYPE);
        this.providerManager = new ZKClient(address, RegistryCenterConfig.PROVIDER_TYPE);
        this.aliveServerAddressSet = Collections.newSetFromMap(new ConcurrentHashMap());
        InitProviderListener();
        refreshRpcChannelMap(); // 初始化channel连接
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
                    refreshRpcChannelMap();
                }
            }
        });
    }

    /**
     * 根据provider更新rpcProcessHandlerMap
     */
    private void refreshRpcChannelMap() {
        log.debug("rpc channels start to refresh...");

        List<String> newProviderServerAddressList = this.providerManager.getCacheTable().getProviderServerAddressList();

        Set<String> oldServerAddressSet = rpcChannelMap.keySet();

        // 加入新的
        for( String address:newProviderServerAddressList ){
            if ( oldServerAddressSet.contains(address) == false ){
                log.info("create a new rpc_channel with the service-server : {}",address);
                connectServerNode(address);
            }
        }

        HashSet<String> newAddressSet = new HashSet<>(newProviderServerAddressList);

        // 删除下线的
        for( String address:oldServerAddressSet ){
            if ( newAddressSet.contains(address) == false ){
                RpcProcessHandler rpcProcessHandler = rpcChannelMap.get(address).getRpcProcessHandler();
                rpcChannelMap.remove(address);
                if (rpcProcessHandler!=null) {
                    rpcProcessHandler.close();
                }
                log.info("delete invalid service-server : {}", address);
            }
        }

        log.debug("rpc channels refreshing has finished.");
    }

    /**
     * 和address 建立channel，更新到map里
     *
     * @param address
     */
    private void connectServerNode(final String address) {
        String[] addr = address.split(":");
        if ( addr.length!=2 ) {
            log.error("Incorrect address format, the correct format should be 'ip:port'.");
            return;
        }

        final String host = addr[0];
        final int port = Integer.parseInt(addr[1]);

        // 先将server更新
        this.aliveServerAddressSet.add(address);

        // 和指定server建立连接
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
                            // 连接成功之后
                        if ( future.isSuccess() ) {
                            log.info("connect with the server successfully , the address is {}", host+port);
                            RpcProcessHandler rpcProcessHandler = future.channel().pipeline().get(RpcProcessHandler.class);
                            addRpcChannel(address, new RpcChannel(future, rpcProcessHandler));
                        }
                    }
                });
            }
        });

    }

    /**
     * connect成功之后，将 handler 给 add 到 map中
     * @param address
     * @param rpcChannel
     */
    private void addRpcChannel(String address, RpcChannel rpcChannel) {
        rpcChannelMap.put(address, rpcChannel);
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
            log.warn("there is no service-server can provide the service which className is {}", clazzName);
            return null;
        }

        // 2. 获取提供该接口服务的所有server的address
        List<String> addressList = serviceLocalDesc.getAddressList();

        // 3. 随机的选取一个, 尝试从map里获取
        int idx = randomChoose.nextInt(addressList.size());
        String addr = addressList.get(idx);
        RpcChannel rpcChannel = rpcChannelMap.get(addr);

        if (rpcChannel==null){
            if ( this.aliveServerAddressSet.contains(addr) == false ){
                log.warn("get sender failed, because the alive-server-set doesn't have the server which address is: {}", addr);
            }else{
                log.warn("rpc-channel-map is refreshing, please wait a moment...");
                while( rpcChannel==null ){
                    rpcChannel = rpcChannelMap.get(addr);
                    try {
                        Thread.sleep(3*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return rpcChannel.getRpcProcessHandler();
    }


    /**
     * 关闭当前connectManager
     */
    public void close(){
//        consumerManager.close();
        providerManager.close();
        threadPoolExecutor.shutdown();
        for( RpcChannel rpcChannel:rpcChannelMap.values() ){
            rpcChannel.close();
        }
        group.shutdownGracefully();
        log.info("connectManager closed..");
    }

}
