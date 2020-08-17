package com.earlyrpc.client.config;

import lombok.Data;

/**
 * erpc中对consumer的描述
 *
 * @author: czf
 * @date: 2020/8/17 15:25
 */
@Data
public class ConsumerDescription {
    /* consumer对应的rpc接口名称 */
    String interfaceName;

    /* 超时时间 */
    String timeout;

    /* 序列化方式 */
    String serialization;

    /* 支持的协议类型 */
    String protocal;

    public ConsumerDescription(String interfaceName, String timeout, String serialization, String protocal) {
        this.interfaceName = interfaceName;
        this.timeout = timeout;
        this.serialization = serialization;
        this.protocal = protocal;
    }
}
