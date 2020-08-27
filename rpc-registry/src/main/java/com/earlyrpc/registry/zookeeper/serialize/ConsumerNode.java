package com.earlyrpc.registry.zookeeper.serialize;

import com.earlyrpc.registry.constant.RegistryCenterConfig;
import com.earlyrpc.registry.description.remote.ConsumerInfoDesc;
import lombok.Data;

import java.util.List;

/**
 *
 * @author: czf
 * @date: 2020/8/27 14:51
 */
@Data
public class ConsumerNode {

    private List<ConsumerInfoDesc>  consumersNodeList;

    // 节点路径
    public static String path = RegistryCenterConfig.CONSUMER_TYPE.getPath();

}
