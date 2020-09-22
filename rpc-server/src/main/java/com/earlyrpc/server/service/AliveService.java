package com.earlyrpc.server.service;

import com.earlyrpc.registry.description.remote.ServiceInfoDesc;
import lombok.Data;

/**
 * @author: czf
 * @date: 2020/9/22 18:34
 */
@Data
public class AliveService {
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

    /**
     * 服务接口的实例化对象
     */
    private Object serviceBean;
}
