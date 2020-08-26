package com.earlyrpc.registry.description.remote;

import com.earlyrpc.registry.annotation.InfoDesc;
import lombok.Data;

/**
 * consumer的信息在注册中心的存储形式
 *
 * @author: czf
 * @date: 2020/8/26 9:04
 */
@Data
@InfoDesc
public class ConsumerInfoDesc extends BaseInfoDesc{

    private String consumerName;

}
