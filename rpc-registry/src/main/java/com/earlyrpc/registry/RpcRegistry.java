package com.earlyrpc.registry;

import com.earlyrpc.registry.description.remote.BaseInfoDesc;

/**
 * 注册
 *
 * @author: czf
 * @date: 2020/8/19 21:03
 */
public interface RpcRegistry {

    /**
     * 向注册中心注册服务信息
     * @param baseInfoDesc
     */
    void register(BaseInfoDesc baseInfoDesc);

    /**
     * 对指定节点进行更新
     * @param key 节点对应的本地ip
     * @param baseInfoDesc 更新的节点信息
     */
    void update(String key, BaseInfoDesc baseInfoDesc);

    /**
     * 删除指定节点
     * @param key
     */
    void delete(String key);

    /**
     * 删除指定节点
     * @param baseInfoDesc
     */
    void delete(BaseInfoDesc baseInfoDesc);

    void close();
}
