package com.earlyrpc.registry.description.remote;

import lombok.Data;

/**
 * 在注册中心上存储着的，每个实体的，描述信息基类
 *
 * @author czf
 * @Date 2020/8/25 10:06 下午
 */
@Data
public class BaseInfoDesc {
    /**
     * 当前节点地址
     * ip:port (例如: 127.0.0.1:8080 )
     */
    protected String localAddress;

    /**
     * 在zookeeper服务器上的绝对路径
     */
    protected String zkAbsolutePath;

    public BaseInfoDesc(String localAddress) {
        this.localAddress = localAddress;
    }
}
