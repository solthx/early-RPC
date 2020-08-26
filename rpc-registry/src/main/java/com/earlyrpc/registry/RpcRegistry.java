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
}
