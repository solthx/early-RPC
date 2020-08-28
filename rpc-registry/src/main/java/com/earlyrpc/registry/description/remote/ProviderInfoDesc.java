package com.earlyrpc.registry.description.remote;

import com.earlyrpc.registry.annotation.InfoDesc;
import com.earlyrpc.registry.constant.RegistryCenterConfig;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * provider的信息在注册中心的存储形式
 *
 * @author czf
 * @Date 2020/8/25 9:46 下午
 */
@Data
public class ProviderInfoDesc extends BaseInfoDesc {

    /**
     * 服务提供方提供的服务列表
     */
    private List<ServiceInfoDesc> serviceInfoDescList;

    public ProviderInfoDesc(String localAddress) {
        super(localAddress);
        this.serviceInfoDescList=new ArrayList<ServiceInfoDesc>();
        this.zkAbsolutePath = RegistryCenterConfig.PROVIDER_TYPE.getPath() + "/" + localAddress;
    }
}
