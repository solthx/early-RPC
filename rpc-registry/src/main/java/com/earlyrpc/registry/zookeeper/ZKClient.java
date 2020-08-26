package com.earlyrpc.registry.zookeeper;

import com.earlyrpc.registry.LocalCacheTable;
import com.earlyrpc.registry.LocalCacheTableManager;
import com.earlyrpc.registry.RpcDiscovery;
import com.earlyrpc.registry.RpcRegistry;
import com.earlyrpc.registry.constant.RegistryCenterConfig;
import com.earlyrpc.registry.description.remote.BaseInfoDesc;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

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

    public ZKClient(String address, Integer sessionTimeout, RetryPolicy retryPolicy, String listeningRootPath) {
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
                switch (event.getType()){
                    case CHILD_ADDED:
                        log.info("有新服务上线!, 服务相关信息内容：{}",event.getData().getData());
                        updateLocalCacheTable(); // 更新本地缓存
                    case CHILD_REMOVED:
                        log.info("监听到新服务上线, 服务相关信息内容：{}",event.getData().getData());
                        updateLocalCacheTable(); // 更新本地缓存
                    case CHILD_UPDATED:
                        log.info("监听到新服务更新, 服务相关信息内容：{}",event.getData().getData());
                        updateLocalCacheTable(); // 更新本地缓存
                }
            }
        });
    }

    public ZKClient(String address, Integer sessionTimeout, RegistryCenterConfig path) {
        this(address, sessionTimeout, new ExponentialBackoffRetry(5000,10),path.getPath());
    }

    public ZKClient(String address, RegistryCenterConfig path) {
        this(address, 5000, path);
    }


    /**
     * 获取注册中心所有已经注册的信息
     *
     * @return
     */
    @Override
    public List<BaseInfoDesc> listRegisteredInfoDesc() {
        // todo: next
        return null;
    }

    /**
     * 向注册中心注册信息
     *
     * @param desc
     */
    public void register(BaseInfoDesc desc) {
        // todo
        // 1. 序列化

        // 2. 根据类型获取注册节点

        // 3. 注册（create节点）

    }
}
