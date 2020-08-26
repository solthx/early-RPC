package com.earlyrpc.registry.description.local;

import com.earlyrpc.registry.description.remote.ServiceInfoDesc;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务的本地描述
 *
 * @author: czf
 * @date: 2020/8/26 10:16
 */
@Data
public class ServiceLocalDesc {

    private String serviceName;

    private String interfaceName;

    private String alias;

    /**
     * 提供该服务的provider地址
     */
    private List<String> addressList;

    public ServiceLocalDesc() {
        addressList = new ArrayList<String>();
    }

    public ServiceLocalDesc(ServiceInfoDesc service) {
        this();
        serviceName = service.getServiceName();
        interfaceName = service.getInterfaceName();
        alias = service.getAlias();
    }
}
