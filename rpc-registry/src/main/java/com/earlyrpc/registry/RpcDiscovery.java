package com.earlyrpc.registry;

import com.earlyrpc.registry.description.BaseInfoDesc;

import java.util.List;

/**
 * 发现
 *
 * @author czf
 * @Date 2020/8/25 10:03 下午
 */
public interface RpcDiscovery {
    /**
     * 获取注册中心所有已经注册的信息
     * @return
     */
    List<BaseInfoDesc> listRegisteredInfoDesc();
}
