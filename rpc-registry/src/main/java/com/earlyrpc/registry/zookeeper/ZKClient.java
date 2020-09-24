package com.earlyrpc.registry.zookeeper;

import com.earlyrpc.commons.serializer.ProtoBufSerializer;
import com.earlyrpc.commons.serializer.Serializer;
import com.earlyrpc.registry.CallBack;
import com.earlyrpc.registry.LocalCacheTableManager;
import com.earlyrpc.registry.RpcRegistry;
import com.earlyrpc.registry.constant.EventType;
import com.earlyrpc.registry.constant.RegistryCenterConfig;
import com.earlyrpc.registry.description.remote.BaseInfoDesc;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * 基于Zookeeper实现的注册中心
 *
 * 底层维护的 局部缓存表是单例模式，所有client共享，以cow的方式进行更新
 *
 * 主要功能:
 *      1. zk的增删查改
 *      2. zk的监听(一旦数据发生改变，则重新pull数据，这里pull的数据就是本地缓存表)
 *          监听钩子方法(数据改变时要进行回调的方法)
 *
 *
 * @author czf
 * @Date 2020/8/25 10:18 下午
 */
@Slf4j
public class ZKClient extends LocalCacheTableManager implements RpcRegistry{

    /**
     * 注册中心的连接地址
     */
    protected String address;

    /**
     * 超时时间, 默认为毫秒
     */
    private Integer sessionTimeout;

    /**
     * 重试策略
     */
    private RetryPolicy retryPolicy;

    private CuratorFramework cf;

    /**
     * 所有需要回调的方法
     */
    private List<CallBack> eventListeners;


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

        PathChildrenCache pathChildrenCache = new PathChildrenCache(cf, listeningRootPath, true);

        // 当有子节点发生改变时，就触发更新本地信息
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            public void childEvent(CuratorFramework cf, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                    case CHILD_ADDED:
                        log.info("a new service become online... the content is：{}", new String(event.getData().getData()));
                        refreshLocalCacheTable(); // 更新本地缓存
                        break;
                    case CHILD_REMOVED:
                        log.info("a service become down... the content is：{}", new String(event.getData().getData()));
                        refreshLocalCacheTable(); // 更新本地缓存
                        break;
                    case CHILD_UPDATED:
                        log.info("a service had been updated... the content is：{}", new String(event.getData().getData()));
                        refreshLocalCacheTable(); // 更新本地缓存
                        break;
                }
            }
        });

        try {
            pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        } catch (Exception e) {
            log.warn("pathChildCache has an exception during initialization , the errMsg is: {}",e.getMessage());
        }

        this.serializer = serializer;
        this.eventListeners = new ArrayList<CallBack>();

        refreshLocalCacheTable();
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
     * 将重写的callback方法存入list
     * @param callBack
     */
    @Override
    public void addListener(CallBack callBack){
        eventListeners.add(callBack);
    }

    /**
     * 回调
     * @param eventType
     */
    public void invodeListener(EventType eventType){
        for( CallBack f:eventListeners ){
            f.callback(eventType);
        }
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
    @Override
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
                log.warn("throw an exception when creating ZK node, the errMsg is：[{}]",e.getMessage());
            }
        }else{
            log.warn("[{}] register failed.", desc);
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
            log.warn(" [{}] exception occurred during update. The exception information is [{}] ", path, e.getMessage());
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
            log.warn("Exception occurred during checkExits operation, node information: [{}], exception information: [{}]",baseInfoDesc, e.getMessage());
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
            log.warn("Exception occurred when the node [{}] was deleted. The exception information is [{}]", key, e.getMessage());
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
            log.warn("Exception occurred when node [{}] was deleted. The exception information is [{}]", baseInfoDesc.getZkAbsolutePath(), e.getMessage());
        }
    }

    /**
     * 刷新更新本地缓存表
     */
    @Override
    public void refreshLocalCacheTable() {
        // 若服务还没启起来
//        if ( !cf.getState().equals(CuratorFrameworkState.STARTED) )
//            return;

        List<BaseInfoDesc> baseInfoDescs = listRegisteredInfoDesc();
        getCacheTable().updateLocalCacheTable(baseInfoDescs);
        // 回调那些listener方法
        invodeListener(EventType.AFTER_UPDATE_CACHETABLE);
    }

    @Override
    public void close() {
        cf.close();
        log.info("zkClinet closed success...");
    }
}
