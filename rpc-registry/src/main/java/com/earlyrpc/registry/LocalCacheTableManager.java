package com.earlyrpc.registry;

import com.earlyrpc.registry.description.remote.BaseInfoDesc;

import javax.annotation.Resource;
import java.util.List;

/**
 * 本地缓存表管理
 *
 * 在注册中心上对服务相关的注册，是以provider为单位的
 *
 * 本地缓存表除了缓存并维护注册中心上的信息以外，也实现了 服务相关的存储格式的转换
 *
 * todo
 *
 * @author: czf
 * @date: 2020/8/26 9:51
 */
public abstract class LocalCacheTableManager implements RpcDiscovery {

    /**
     * 本地缓存表
     */
    @Resource
    private LocalCacheTable cacheTable;


    public LocalCacheTableManager() {
        // 初始化本地缓存
        // todo: bug:此语句会在子类初始化之前执行，导致空指针.
        updateLocalCacheTable();
    }

    /**
     * 更新本地缓存表（即重新拉取信息，重新转换）
     */
    public void updateLocalCacheTable(){
        List<BaseInfoDesc> baseInfoDescs = listRegisteredInfoDesc();
        cacheTable.updateLocalCacheTable(baseInfoDescs);
    }

    public List<String> getProviderList(String interfaceName) {
        return cacheTable.getProviderList(interfaceName);
    }

}
