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
import org.apache.zookeeper.CreateMode;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于Zookeeper实现的注册中心
 *
 * @author czf
 * @Date 2020/8/25 10:18 下午
 */
@Slf4j
public class ZKClient extends LocalCacheTableManager implements RpcRegistry{

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

        cf.start(); // 启动

        initZKPath();  // 初始化Zookeepr节点路径

        pathChildrenCache = new PathChildrenCache(cf, listeningRootPath, true);

        // 当有子节点发生改变时，就触发更新本地信息
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            public void childEvent(CuratorFramework cf, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                    case CHILD_ADDED:
                        log.info("有新服务上线!, 服务标识为：{}", new String(event.getData().getData()));
                        refreshLocalCacheTable(); // 更新本地缓存
                        break;
                    case CHILD_REMOVED:
                        log.info("有服务下线, 服务标识为：{}", new String(event.getData().getData()));
                        refreshLocalCacheTable(); // 更新本地缓存
                        break;
                    case CHILD_UPDATED:
                        log.info("监听到新服务更新, 服务标识为：{}", new String(event.getData().getData()));
                        refreshLocalCacheTable(); // 更新本地缓存
                        break;
                }
            }
        });
        try {
            pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        } catch (Exception e) {
            log.warn("pathChildCache初始化时异常{}",e.getMessage());
        }

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
     * 初始化zookeeper服务器上的路径
     */
    private void initZKPath() {
        for (RegistryCenterConfig type : RegistryCenterConfig.values()) {
            try {
                if ( cf.checkExists().forPath(type.getPath()) == null ) {
                    cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(type.getPath());
                }
            } catch (Exception e) {
                log.info("初始化 {} 节点时出现异常", type.getPath());
            }
        }
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
            if ( false == type.equals(RegistryCenterConfig.REGISTRY_ROOT_TYPE) ){
                appendInfoDesc(resList, type);
            }
        }

        return resList;
    }


    /**
     * 将zookeeper上path路径下存储的 节点 的信息
     * 进行反序列化，并将其存储的信息加到resList中
     *
     * @param resList
     * @param type
     */
    private void appendInfoDesc(List<BaseInfoDesc> resList, RegistryCenterConfig type) {
        try {
            String path = type.getPath();
            List<String> nodePathList = cf.getChildren().forPath(path);
            for(String nodePath:nodePathList){
                String nodeAbsolutePath = path + "/" + nodePath;

                byte[] infoBytes = cf.getData().forPath(nodeAbsolutePath);

                BaseInfoDesc providerInfo = serializer.deserialize(infoBytes, RegistryCenterConfig.getClass(type));
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
        RegistryCenterConfig typePath = RegistryCenterConfig.getPath(desc);
        if (typePath!=null ){
            // 3. 注册（create节点）
            try {
                // 节点路径 = 类型路径(如消费者路径) + 唯一标识(ip+端口)
                String nodePath = typePath.getPath() + "/" + desc.getLocalAddress();
                if ( cf.checkExists().forPath(nodePath) == null ) {
                    cf.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(nodePath, descBytes);
                }
            } catch (Exception e) {
                log.warn("注册时异常, 创建zk节点时出现异常：{}",e.getMessage());
            }
        }else{
            log.warn("{}注册失败.", desc);
        }
    }

    /**
     * 对指定节点进行更新
     *
     * @param path          节点对应的本地ip
     * @param baseInfoDesc 更新的节点信息
     */
    @Override
    public void update(String path, BaseInfoDesc baseInfoDesc) {
        byte[] bytesData = serializer.serialize(baseInfoDesc);
        try {
            cf.setData().forPath(path, bytesData);
        } catch (Exception e) {
            log.warn(" {} 在更新时出现异常. 异常信息为 {} ", path, e.getMessage());
        }
    }

    /**
     * 若节点存在则更新， 若不存在则注册
     *
     * @param baseInfoDesc
     */
    public void registerOrUpdate(BaseInfoDesc baseInfoDesc){
        try {
            if ( cf.checkExists().forPath(baseInfoDesc.getZkAbsolutePath()) == null ){
                register(baseInfoDesc);
            }else{
                update(baseInfoDesc.getZkAbsolutePath(), baseInfoDesc);
            }
        } catch (Exception e) {
            log.warn("进行checkExsits操作时出现异常, 节点信息: {}, 异常信息: {}",baseInfoDesc, e.getMessage());
        }
    }

    /**
     * 删除指定节点
     *
     * @param key
     */
    @Override
    public void delete(String key) {
        try {
            cf.delete().forPath(key);
        } catch (Exception e) {
            log.warn("节点 {} 在删除时出现异常, 异常信息为 {}", key, e.getMessage());
        }
    }

    /**
     * 删除指定节点
     *
     * @param baseInfoDesc
     */
    @Override
    public void delete(BaseInfoDesc baseInfoDesc) {
        try {
            cf.delete().forPath(baseInfoDesc.getZkAbsolutePath());
        } catch (Exception e) {
            log.warn("节点 {} 在删除时出现异常, 异常信息为 {}", baseInfoDesc.getZkAbsolutePath(), e.getMessage());
        }
    }

    /**
     * 刷新更新本地缓存表
     */
    @Override
    public void refreshLocalCacheTable() {
        List<BaseInfoDesc> baseInfoDescs = listRegisteredInfoDesc();
        getCacheTable().updateLocalCacheTable(baseInfoDescs);
    }

    public void close() {
        cf.close();
    }
}
