package com.earlyrpc.registry.constant;

import com.earlyrpc.registry.description.remote.BaseInfoDesc;
import com.earlyrpc.registry.description.remote.ConsumerInfoDesc;
import com.earlyrpc.registry.description.remote.ProviderInfoDesc;
import com.earlyrpc.registry.description.remote.ServiceInfoDesc;
import lombok.Data;
import org.springframework.stereotype.Component;

import javax.management.RuntimeErrorException;

/**
 * 配置中心相关信息
 *
 * @author czf
 * @Date 2020/8/25 10:59 下午
 */
@Component
public enum RegistryCenterConfig {
    REGISTRY_ROOT_TYPE("/erpc_registry"),
    PROVIDER_TYPE(REGISTRY_ROOT_TYPE.getPath()+"/provider"),
    CONSUMER_TYPE(REGISTRY_ROOT_TYPE.getPath()+"/consumer")
    ;
    private String path;

    RegistryCenterConfig(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    // 增加类型映射

    public static RegistryCenterConfig getPath(BaseInfoDesc baseInfoDesc){
        if ( baseInfoDesc.getClass().equals(ProviderInfoDesc.class) ){
            return PROVIDER_TYPE;
        }else if ( baseInfoDesc.getClass().equals(ConsumerInfoDesc.class) ){
            return CONSUMER_TYPE;
        }
        throw new RuntimeException("不存在的类型...");
    }

}
