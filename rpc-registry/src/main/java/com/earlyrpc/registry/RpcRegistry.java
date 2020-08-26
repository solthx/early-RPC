package com.earlyrpc.registry;

import com.earlyrpc.registry.description.ServiceInfoDesc;

/**
 * 注册
 *
 * @author: czf
 * @date: 2020/8/19 21:03
 */
public interface RpcRegistry {

    /**
     * 向注册中心注册服务信息
     * @param serviceInfoDesc
     */
    void register(ServiceInfoDesc serviceInfoDesc);
}
