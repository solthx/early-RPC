package com.earlyrpc.registry.zookeeper.serialize;

import com.earlyrpc.registry.constant.RegistryCenterConfig;
import com.earlyrpc.registry.description.remote.ServiceInfoDesc;
import lombok.Data;

import java.util.List;

/**
 *
 * @author: czf
 * @date: 2020/8/27 14:55
 */
@Data
public class ProviderNode {

    private List<ServiceInfoDesc> providersList;

    public static String path = RegistryCenterConfig.PROVIDER_TYPE.getPath();
}