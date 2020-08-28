package com.earlyrpc.registry;

import com.earlyrpc.registry.description.remote.BaseInfoDesc;
import lombok.Data;

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
@Data
public abstract class LocalCacheTableManager implements RpcDiscovery {

    /**
     * 本地缓存表
     */
    private LocalCacheTable cacheTable;


    public LocalCacheTableManager() {
        // 初始化本地缓存
        cacheTable = LocalCacheTable.getInstance();
    }

    /**
     * 刷新更新本地缓存表
     */
    public abstract void refreshLocalCacheTable();

    public List<String> getProviderList(String interfaceName) {
        return cacheTable.getProviderList(interfaceName);
    }

}
