package com.earlyrpc.registry;

import com.earlyrpc.registry.description.remote.BaseInfoDesc;

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
    private LocalCacheTable cacheTable;


    public LocalCacheTableManager() {
    }
}
