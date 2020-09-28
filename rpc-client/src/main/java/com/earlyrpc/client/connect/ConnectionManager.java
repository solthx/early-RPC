package com.earlyrpc.client.connect;

import com.earlyrpc.client.enums.RpcChannelState;
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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;

/**
 * 管理当前客户端与rpcServer的长连接
 *
 * @author czf
 * @Date 2020/8/18 9:49 下午
 */
@Slf4j
@Component
public class ConnectionManager implements ApplicationListener<ContextClosedEvent> {

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

//    private Set<String> aliveServerAddressSet;

    /* 保存所有存活service-server的address */
    private Set<String> aliveServerAddressSet;

    private Random randomChoose = new Random();

    @Value("${erpc.consumer.registry.address}")
    private String registryAddress;

    // 静态内部类实现单例
    public ConnectionManager(){
    }

    /**
     * connectionManager的初始化方法
     */
    @PostConstruct
   public void init(){
//        this.address = "127.0.0.1:2181";
//        this.consumerManager = new ZKClient(address, RegistryCenterConfig.CONSUMER_TYPE);
       this.providerManager = new ZKClient(registryAddress, RegistryCenterConfig.PROVIDER_TYPE);
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
                connectServerNode(address, false); // 异步连接
            }
        }

        HashSet<String> newAddressSet = new HashSet<>(newProviderServerAddressList);

        // 删除下线的
        for( String address:oldServerAddressSet ){
            if ( newAddressSet.contains(address) == false ){
                removeRpcChannel(address,false);
            }
        }

        log.debug("rpc channels refreshing has finished.");
    }

    /**
     * 删除rpcChannel
     * @param address  remoteAddress
     * @param idleDelete 是否是因为不够活跃才被删的...
     */
    public void removeRpcChannel(String address, boolean idleDelete) {
        rpcChannelMap.remove(address);
        if ( !idleDelete ){
            // 不是idle斷開
            this.aliveServerAddressSet.remove(address);
        }
        RpcProcessHandler rpcProcessHandler = null;
        if ( rpcChannelMap.containsKey(address) ) {
            rpcProcessHandler = rpcChannelMap.get(address).getRpcProcessHandler();
        }
        if (rpcProcessHandler!=null) {
            rpcProcessHandler.close();
        }
        log.info("delete invalid service-server : {}", address);
    }

    public void removeRpcChannel(RpcChannel rpcChannel, boolean idleDelete){
        this.removeRpcChannel(rpcChannel.getAddress(),idleDelete);
    }

    /**
     * 和address 建立channel，更新到map里
     *
     * @param address
     */
    private RpcChannel connectServerNode(final String address, final boolean sync) {
        String[] addr = address.split(":");
        if ( addr.length!=2 ) {
            log.error("Incorrect address format, the correct format should be 'ip:port'.");
            return null;
        }

        final String host = addr[0];
        final int port = Integer.parseInt(addr[1]);

        // 先将server更新
        this.aliveServerAddressSet.add(address);

        final ConnectionManager cm = this;

        this.rpcChannelMap.put(address, new RpcChannel(address, null, null, RpcChannelState.WAITTING));

        Runnable task = new Runnable() {
            @Override
            public void run() {
                Bootstrap b = new Bootstrap();
                b.group(group)
                        .channel(NioSocketChannel.class)
                        .remoteAddress(host, port)
                        .handler(new RpcProcessInitializer(cm));
                ChannelFuture future = null;
                if ( !sync ) {
                    future = b.connect();
                    future.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            // 连接成功之后
                            if (future.isSuccess()) {
                                log.info("connect with the server successfully , the address is {}", host + ":" + port);
                                RpcProcessHandler rpcProcessHandler = future.channel().pipeline().get(RpcProcessHandler.class);
                                addRpcChannel(address, new RpcChannel(host + ":" + port, future, rpcProcessHandler, RpcChannelState.CONNECT));
                            }
                        }
                    });
                }else{
                    // 同步
                    try {
                        future = b.connect().sync();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if ( future!=null && future.isSuccess()) {
                        log.info("connect with the server successfully , the address is {}", host + ":" + port);
                        RpcProcessHandler rpcProcessHandler = future.channel().pipeline().get(RpcProcessHandler.class);
                        addRpcChannel(address, new RpcChannel(host + ":" + port, future, rpcProcessHandler, RpcChannelState.CONNECT));
                    }else{
                        log.error("connect with the server failed in sync way, the address is {}", host + ":" + port);
                    }
                }
            }
        };

        // 和指定server建立连接
        if ( !sync ) {
            // 走异步
            threadPoolExecutor.submit(task);
        }else{
            // 走同步
            task.run();
        }
        return rpcChannelMap.get(address);
    }

    /**
     * connect成功之后，将 handler 给 add 到 map中
     * @param address
     * @param rpcChannel
     */
    private void addRpcChannel(String address, RpcChannel rpcChannel) {
        rpcChannelMap.put(address, rpcChannel);
    }


//    private static class ConnectionManagerSingleton{
//        public static ConnectionManager INSTANCE = new ConnectionManager();
//    }
//
//    public static ConnectionManager getInstance(){
//        return ConnectionManagerSingleton.INSTANCE;
//    }

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
        RpcChannel rpcChannel = null;
        // 如果确实被cached起来了
        if (rpcChannelMap.containsKey(addr)){
//            if ( this.cachedAliveServerAddressSet.contains(addr) == false ){
//                log.warn("get sender failed, because the alive-server-set doesn't have the server which address is: {}", addr);
//            }else{
            rpcChannel = this.rpcChannelMap.get(addr);
            if ( rpcChannel!=null && rpcChannel.getState().equals(RpcChannelState.WAITTING)){
                // 需要等待
                while (rpcChannel == null) {
                    log.warn("rpc-channel-map is refreshing, please wait a moment...");
                    rpcChannel = rpcChannelMap.get(addr);
                    try {
                        Thread.sleep(3 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            // connected or disconnected ...
        }else{
            // 说明有可能是存活的，但是没有被cached起来
            if( this.aliveServerAddressSet.contains(addr) ){
                // 这里需要走重新提交channel的逻辑
                rpcChannel = connectServerNode(addr, true);
            }
        }
        if ( null == rpcChannel || !rpcChannel.getState().equals(RpcChannelState.CONNECT) ){
            // 没有成功获取到rpcChannel的情况
            return null;
        }
        return rpcChannel.getRpcProcessHandler();
//        return (rpcChannel == null || !rpcChannel.getState().equals(RpcChannelState.CONNECT)) ? :rpcChannel.getRpcProcessHandler();
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

    /**
     * applicationContext关闭时调用
     * @param event
     */
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        this.close();
    }

//    @Override
//    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
//        return bean;
//    }

    /**
     * 初始化
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
//    @Override
//    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//        if ( beanName.equals("connectionManager") && bean instanceof ConnectionManager ){
//            ConnectionManager connectionManager = (ConnectionManager) bean;
//            connectionManager.init();
//        }
//        return bean;
//    }
}
