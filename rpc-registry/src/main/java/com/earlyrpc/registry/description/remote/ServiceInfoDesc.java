package com.earlyrpc.registry.description.remote;

import lombok.Data;

/**
 * 在注册中心中，存储服务信息的数据结构
 *
 * @author czf
 * @Date 2020/8/25 9:26 下午
 */
@Data
public class ServiceInfoDesc {
    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务的全限定接口名
     */
    private String interfaceName;

    /**
     * 服务的别名
     */
    private String alias;

    public ServiceInfoDesc(String serviceName, String interfaceName, String alias) {
        this.serviceName = serviceName;
        this.interfaceName = interfaceName;
        this.alias = alias;
    }

    public ServiceInfoDesc(){}

    public ServiceInfoDesc(String serviceName, String interfaceName) {
        this.serviceName = serviceName;
        this.interfaceName = interfaceName;
    }
}
