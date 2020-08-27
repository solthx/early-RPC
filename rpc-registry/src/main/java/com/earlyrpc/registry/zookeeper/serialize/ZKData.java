package com.earlyrpc.registry.zookeeper.serialize;

import lombok.Data;

/**
 * @author: czf
 * @date: 2020/8/27 15:10
 */
@Data
public class ZKData {

    private ConsumerNode consumerNode;

    private ProviderNode providerNode;

}
