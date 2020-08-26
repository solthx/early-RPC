package com.earlyrpc.registry;

import com.earlyrpc.registry.description.local.ConsumerLocalDesc;
import com.earlyrpc.registry.description.local.ServiceLocalDesc;
import com.earlyrpc.registry.description.remote.BaseInfoDesc;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 本地缓存表
 *
 * 即从注册中心种缓存到的数据
 *
 * ps: 属于临界资源, 要保证线程安全
 *
 * todo
 *
 * @author: czf
 * @date: 2020/8/26 9:55
 */
@Data
public class LocalCacheTable {

    /**
     * 消费者信息存储
     */
    private volatile List<ConsumerLocalDesc> consumerLocalDesc;

    /**
     * 服务信息
     *
     * todo: 日后优化点：唯一标识不使用全限定类名（key太长），改用分布式id
     * serviceMap< 服务唯一标识(接口全限定名), 服务描述 >
     */
    private volatile Map<String, ServiceLocalDesc> serviceMap;


    /**
     * 对当前LocalCacheTable进行初始化
     *
     * @param infoList
     */
    public void updateLocalCacheTable(List<BaseInfoDesc> infoList){
        // todo

    }

    /**
     * 根据 服务唯一标识 (接口名) 获取所有能够为该接口提供服务的所有provider的ip:port
     * @param interfaceName
     * @return
     */
    public List<String> getProviderList(String interfaceName){
        ServiceLocalDesc serviceLocalDesc = serviceMap.get(interfaceName);
        List<String> providersList = serviceLocalDesc.getAddressList();
        return providersList;
    }

}
