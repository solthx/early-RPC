package com.earlyrpc.client.proxy;

import com.earlyrpc.client.config.ConsumerDescription;
import com.earlyrpc.client.connect.ConnectionManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;


/**
 * rpc代理创建器
 *
 * @author: czf
 * @date: 2020/8/18 10:21
 */
@Slf4j
@Component
@Data
public class RpcProxyCreator  {

    @Autowired
    private ConnectionManager connectionManager;

    private ApplicationContext applicationContext;

    public RpcProxyCreator() {
    }

    public Object createProxy(ConsumerDescription consumerDesc){
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

    public <T> T createProxy(Class<T> interfaceClazz, ConsumerDescription consumerDesc){
        return (T) Proxy.newProxyInstance(
                interfaceClazz.getClassLoader(),
                new Class[]{interfaceClazz},
                new RpcProxy(consumerDesc, connectionManager));
    }

//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        this.applicationContext = applicationContext;
//        this.connectionManager = applicationContext.getBean(ConnectionManager.class);
//    }
}
