package com.earlyrpc.client.proxy;

import com.earlyrpc.client.config.ConsumerDescription;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;


/**
 * rpc代理创建器
 *
 * @author: czf
 * @date: 2020/8/18 10:21
 */
@Slf4j
public class RpcProxyCreator {
    public static Object createProxy(ConsumerDescription consumerDesc){
        String interfaceName = consumerDesc.getInterfaceName();
        Object proxyEntity = null;
        try {
            Class<?> interfaceClazz = Class.forName(interfaceName);
            proxyEntity = createProxy(interfaceClazz, consumerDesc);
        } catch (ClassNotFoundException e) {
            log.error("not found the interface : {}", interfaceName);
            e.printStackTrace();
        }
        return proxyEntity;
    }

    public static<T> T createProxy(Class<T> interfaceClazz, ConsumerDescription consumerDesc){
        return (T) Proxy.newProxyInstance(
                interfaceClazz.getClassLoader(),
                new Class[]{interfaceClazz},
                new RpcProxy(consumerDesc));
    }

}
