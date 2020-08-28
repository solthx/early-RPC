package com.earlyrpc.registry;

import com.earlyrpc.registry.description.local.ConsumerLocalDesc;
import com.earlyrpc.registry.description.local.ServiceLocalDesc;
import com.earlyrpc.registry.description.remote.BaseInfoDesc;
import com.earlyrpc.registry.description.remote.ConsumerInfoDesc;
import com.earlyrpc.registry.description.remote.ProviderInfoDesc;
import com.earlyrpc.registry.description.remote.ServiceInfoDesc;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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
     * 静态内部类 使得该对象为单例对象
     */
    private LocalCacheTable(){
        consumerLocalDescList = new ArrayList<ConsumerLocalDesc>();
        serviceMap = new HashMap<String, ServiceLocalDesc>();
    }

    private static class LocalCacheTableInstance{
        public static LocalCacheTable INSTANCE = new LocalCacheTable();
    }

    public static LocalCacheTable getInstance(){
        return LocalCacheTableInstance.INSTANCE;
    }



    /**
     * 消费者信息存储
     */
    private volatile List<ConsumerLocalDesc> consumerLocalDescList;

    /**
     * 服务信息
     *
     * todo: 日后优化点：唯一标识不使用全限定类名（key太长），改用分布式id
     * serviceMap< 服务唯一标识(接口全限定名), 服务描述 >
     */
    private volatile Map<String, ServiceLocalDesc> serviceMap;


    /**
     * 根据infoList对当前LocalCacheTable进行更新（以cow的方式进行）
     *
     * @param infoList
     */
    public void updateLocalCacheTable(List<BaseInfoDesc> infoList){
        List<ConsumerLocalDesc> newConsumersList = new ArrayList<ConsumerLocalDesc>();
        Map<String, ServiceLocalDesc> newServiceMap = new HashMap<String, ServiceLocalDesc>();

        for( BaseInfoDesc infoDesc:infoList ){
            addInfoDesc(infoDesc, newConsumersList, newServiceMap);
        }

        // copy-on-write
        this.consumerLocalDescList = newConsumersList;
        this.serviceMap = newServiceMap;
    }

    /**
     * 服务信息
     *
     * @param infoDesc
     * @param newConsumersList
     * @param newServiceMap
     */
    private void addInfoDesc(BaseInfoDesc infoDesc, List<ConsumerLocalDesc> newConsumersList, Map<String, ServiceLocalDesc> newServiceMap) {
        if (ProviderInfoDesc.class.equals(infoDesc.getClass())) {
            addServiceInfoDesc((ProviderInfoDesc) infoDesc, newServiceMap);
        }else if ( ConsumerLocalDesc.class.equals(infoDesc.getClass()) ){
            addConsumerInfoDesc((ConsumerInfoDesc) infoDesc, newConsumersList);
        }
    }

    /**
     * 向本地缓存表增加一个Consumer的信息
     * @param infoDesc
     * @param newConsumersList
     */
    private void addConsumerInfoDesc(ConsumerInfoDesc infoDesc, List<ConsumerLocalDesc> newConsumersList) {
        ConsumerLocalDesc consumerLocalDesc = new ConsumerLocalDesc();
        BeanUtils.copyProperties(infoDesc, consumerLocalDesc);
        newConsumersList.add(consumerLocalDesc);
    }

    /**
     * 向本地缓存表增加一个Provider的信息
     * @param infoDesc
     * @param newServiceMap
     */
    private void addServiceInfoDesc(ProviderInfoDesc infoDesc, Map<String, ServiceLocalDesc> newServiceMap) {
        if ( infoDesc == null
                || infoDesc.getServiceInfoDescList()==null
                || infoDesc.getServiceInfoDescList().size()==0 ) {
            return;
        }
        for( ServiceInfoDesc service:infoDesc.getServiceInfoDescList() ){
            // 如果不存在就初始化
            if ( newServiceMap.containsKey(service.getInterfaceName()) == false ){
                newServiceMap.put(service.getInterfaceName(), new ServiceLocalDesc(service));
            }
            ServiceLocalDesc serviceLocalDesc = newServiceMap.get(service.getInterfaceName());
            serviceLocalDesc.getAddressList().add(infoDesc.getLocalAddress());
        }
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
