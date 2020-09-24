package com.earlyrpc.server;

import com.earlyrpc.server.annotation.RpcService;
import com.earlyrpc.server.service.AliveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: czf
 * @date: 2020/9/22 17:28
 */
@Slf4j
@Component
public class RpcServerBootstrap implements ApplicationContextAware, InitializingBean, DisposableBean {

    private RpcServer rpcServer;

    @Value("${erpc.provider.localAddress}")
    private String localAddress;

    @Value("${erpc.registry.address}")
    private String registryAddress;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    /**
     * Bean自动填装之后，init-method方法之前执行该方法(实际上可以理解注解化的init-method, 但是优先级优于init-method)
     *
     * 初始化 rpcServer
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        // 1. 初始化aliveServiceMap
        Map<String, AliveService> aliveServiceMap
                = createAliveServiceMap(applicationContext);

        // 2. 初始化rpcServer
        this.rpcServer = new RpcServer(
                registryAddress,
                localAddress,
                aliveServiceMap
        );

        log.warn("start rpc-server...");
        // 3. 启动rpcServer
        rpcServer.start();
    }

    /**
     *
     * ps: 通过applicationContext获取所有实现了RpcService注解的bean，
     * 这些bean就是service， 通过解析注解生成当前server提供的所有服务集合
     * 即 aliveServiceMap
     *
     * @param applicationContext
     * @return
     */
    private Map<String, AliveService> createAliveServiceMap(ApplicationContext applicationContext) {
        // < beanName, serviceBean >
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);

        Map<String, AliveService> aliveServiceMap = new HashMap<>();

        for( Object serviceBean:serviceBeanMap.values() ){
            // 获取服务接口的类对象
            AliveService aliveService = createAliveService(serviceBean);
            aliveServiceMap.put(aliveService.getInterfaceName(), aliveService);
        }
        return aliveServiceMap;
    }

    /**
     * serviceBean => AliveService
     * @param serviceBean
     * @return
     */
    private AliveService createAliveService(Object serviceBean) {
        RpcService rpcServiceAnno = serviceBean.getClass().getAnnotation(RpcService.class);

        Class<?> serviceInterfaceClazz = rpcServiceAnno.value();

        String serviceName = rpcServiceAnno.serviceName();
        String alias = rpcServiceAnno.alias();

        String interfaceName = serviceInterfaceClazz.getName();

        AliveService aliveService = new AliveService();

        aliveService.setAlias(alias);
        aliveService.setInterfaceName(interfaceName);
        aliveService.setServiceName(serviceName);
        aliveService.setServiceBean(serviceBean);

        return aliveService;
    }


    @Override
    public void destroy() throws Exception {
        rpcServer.stop();
        log.info("rpc-server is closed...");
    }
}
