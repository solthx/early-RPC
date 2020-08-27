package com.earlyrpc.registry.zookeeper;

import com.earlyrpc.commons.serializer.ProtoBufSerializer;
import com.earlyrpc.commons.serializer.Serializer;
import com.earlyrpc.registry.LocalCacheTableManager;
import com.earlyrpc.registry.RpcRegistry;
import com.earlyrpc.registry.constant.RegistryCenterConfig;
import com.earlyrpc.registry.description.remote.BaseInfoDesc;
import com.earlyrpc.registry.description.remote.ConsumerInfoDesc;
import com.earlyrpc.registry.description.remote.ProviderInfoDesc;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author czf
 * @Date 2020/8/25 10:18 下午
 */
@Slf4j
public class ZKClient extends LocalCacheTableManager implements RpcRegistry {

    /**
     * 注册中心的连接地址
     */
    private String address;

    /**
     * 超时时间, 默认为毫秒
     */
    private Integer sessionTimeout;

    /**
     * 重试策略
     */
    private RetryPolicy retryPolicy;

    private CuratorFramework cf;

    private PathChildrenCache pathChildrenCache;

    /**
     * 序列化器
     */
    private Serializer serializer;

    public ZKClient(String address, Integer sessionTimeout, RetryPolicy retryPolicy, String listeningRootPath, Serializer serializer) {
        this.address = address;
        this.sessionTimeout = sessionTimeout;
        this.retryPolicy = retryPolicy;

        cf = CuratorFrameworkFactory.builder()
                .connectString(address)
                .sessionTimeoutMs(sessionTimeout)
                .retryPolicy(retryPolicy)
                .build();

        pathChildrenCache = new PathChildrenCache(cf, listeningRootPath, true);

        // 当有子节点发生改变时，就触发更新本地信息
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            public void childEvent(CuratorFramework cf, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                    case CHILD_ADDED:
                        log.info("有新服务上线!, 服务相关信息内容：{}", event.getData().getData());
                        updateLocalCacheTable(); // 更新本地缓存
                    case CHILD_REMOVED:
                        log.info("监听到新服务上线, 服务相关信息内容：{}", event.getData().getData());
                        updateLocalCacheTable(); // 更新本地缓存
                    case CHILD_UPDATED:
                        log.info("监听到新服务更新, 服务相关信息内容：{}", event.getData().getData());
                        updateLocalCacheTable(); // 更新本地缓存
                }
            }
        });

        this.serializer = serializer;
    }

    public ZKClient(String address, Integer sessionTimeout, RegistryCenterConfig path, Serializer serializer) {
        this(address, sessionTimeout, new ExponentialBackoffRetry(5000, 10), path.getPath(), serializer);
    }

    public ZKClient(String address, RegistryCenterConfig path, Serializer serializer) {
        this(address, 3000, path, serializer);
    }

    public ZKClient(String address, RegistryCenterConfig path) {
        this(address, 3000, path, new ProtoBufSerializer());
    }




    /**
     * 获取注册中心所有已经注册的信息
     *
     * @return
     */
    @Override
    public List<BaseInfoDesc> listRegisteredInfoDesc() {
        List<BaseInfoDesc> resList = new ArrayList<BaseInfoDesc>();

        for( RegistryCenterConfig type:RegistryCenterConfig.values()){
            if ( type.equals(RegistryCenterConfig.PROVIDER_TYPE) ){
                appendProviderInfoDesc(resList, type.getPath());
            }else if ( type.equals(RegistryCenterConfig.CONSUMER_TYPE) ){
                appendConsumerInfoDesc(resList, type.getPath());
            }
        }

        return resList;
    }

    /**
     * 将zookeeper上path下存储的 consumerInfoDesc( consumer的相关信息 )
     * 进行反序列化，并将其存储的信息加到resList中
     *
     * @param resList
     * @param path
     */
    private void appendConsumerInfoDesc(List<BaseInfoDesc> resList, String path) {
        try {
            List<String> consumerInfoStrList = cf.getChildren().forPath(path);
            for(String consumerInfoStr:consumerInfoStrList){
                ConsumerInfoDesc consumerInfo = serializer.deserialize(consumerInfoStr.getBytes(), ConsumerInfoDesc.class);
                resList.add(consumerInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将zookeeper上path下存储的 providerInfoDesc( provider的相关信息 )
     * 进行反序列化，并将其存储的信息加到resList中
     *
     * @param resList
     * @param path
     */
    private void appendProviderInfoDesc(List<BaseInfoDesc> resList, String path) {
        try {
            List<String> providerInfoStrList = cf.getChildren().forPath(path);
            for(String providerInfoStr:providerInfoStrList){
                ProviderInfoDesc providerInfo = serializer.deserialize(providerInfoStr.getBytes(), ProviderInfoDesc.class);
                resList.add(providerInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 向注册中心注册信息
     *
     * @param desc
     */
    public void register(BaseInfoDesc desc) {
        // 1. 序列化
        byte[] descBytes = serializer.serialize(desc);
        // 2. 根据类型获取注册节点
        RegistryCenterConfig path = RegistryCenterConfig.getPath(desc);
        if (path!=null ){
            // 3. 注册（create节点）
            try {
                cf.create().forPath(path.getPath(),descBytes);
            } catch (Exception e) {
                log.warn("注册时异常, 创建zk节点时出现异常：{}",e.getMessage());
            }
        }else{
            log.warn("{}注册失败.", desc);
        }
    }
}
