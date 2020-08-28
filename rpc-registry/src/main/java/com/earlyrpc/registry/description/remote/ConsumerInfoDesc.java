package com.earlyrpc.registry.description.remote;

import com.earlyrpc.registry.annotation.InfoDesc;
import com.earlyrpc.registry.constant.RegistryCenterConfig;
import lombok.Data;

/**
 * consumer的信息在注册中心的存储形式
 *
 * @author: czf
 * @date: 2020/8/26 9:04
 */
@Data
public class ConsumerInfoDesc extends BaseInfoDesc{

    public ConsumerInfoDesc(String localAddress, String consumerName) {
        super(localAddress);
        this.consumerName = consumerName;
        this.zkAbsolutePath = RegistryCenterConfig.CONSUMER_TYPE.getPath() + "/" + localAddress;
    }

    private String consumerName;

}
