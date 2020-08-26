package com.earlyrpc.registry.constant;

import lombok.Data;

/**
 * 配置中心相关信息
 *
 * @author czf
 * @Date 2020/8/25 10:59 下午
 */
public enum RegistryCenterConfig {
    REGISTRY_ROOT_TYPE("/eprc_registry"),
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

    public void setPath(String path) {
        this.path = path;
    }
}
