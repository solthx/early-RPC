package com.earlyrpc.registry.zookeeper;

import com.earlyrpc.registry.RpcDiscovery;
import com.earlyrpc.registry.RpcRegistry;
import com.earlyrpc.registry.constant.RegistryCenterConfig;
import com.earlyrpc.registry.description.BaseInfoDesc;
import com.earlyrpc.registry.description.ServiceInfoDesc;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.List;

/**
 * @author czf
 * @Date 2020/8/25 10:18 下午
 */
public class ZKClient implements RpcDiscovery, RpcRegistry {

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
    public List<BaseInfoDesc> listRegisteredInfoDesc() {
        //todo
        return null;
    }

    /**
     * 向注册中心注册服务信息
     *
     * @param serviceInfoDesc
     */
    public void register(ServiceInfoDesc serviceInfoDesc) {
        //todo
    }
}
