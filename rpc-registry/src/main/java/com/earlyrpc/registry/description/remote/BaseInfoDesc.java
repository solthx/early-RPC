package com.earlyrpc.registry.description.remote;

/**
 * 在注册中心上存储着的，每个实体的，描述信息基类
 *
 * @author czf
 * @Date 2020/8/25 10:06 下午
 */
public abstract class BaseInfoDesc {
    /**
     * 服务提供方的地址 ip:port (例如: 127.0.0.1:8080 )
     */
    protected String address;
}
