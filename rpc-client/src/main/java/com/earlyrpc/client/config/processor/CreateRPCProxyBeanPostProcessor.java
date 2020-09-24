package com.earlyrpc.client.config.processor;

import com.earlyrpc.client.annotation.RemoteInvoke;
import com.earlyrpc.client.config.ConsumerDescription;
import com.earlyrpc.client.enums.Prefix;
import com.earlyrpc.client.proxy.RpcProxyCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 后置处理器，创建代理对象
 *
 * @author: czf
 * @date: 2020/8/18 10:41
 */
@Slf4j
@Component
public class CreateRPCProxyBeanPostProcessor implements BeanFactoryPostProcessor, InstantiationAwareBeanPostProcessor{

    private ConfigurableListableBeanFactory beanFactory;

    /**
     *  加载完所有BeanDefinition之后回调该方法
     *
     *  提前注册ConsumerDesc，并获取interfaceName
     *
     *  根据interfaceName进行rpc动态代理，并将结果注册到Spring容器中，之后就可以直接autowire了
     *
     * @param beanFactory
     * @throws BeansException
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // xml配置的扫描
        String[] consumerDesc = beanFactory.getBeanNamesForType(ConsumerDescription.class);
        List<ConsumerDescription> cdl = new ArrayList<>();
        for( String descName:consumerDesc ){
            ConsumerDescription desc = (ConsumerDescription) beanFactory.getBean(descName);

            String interfaceName = desc.getInterfaceName();
            String beanName = getBeanName(interfaceName);
            // 在这里生成动态代理对象
            Object rpcProxy = RpcProxyCreator.createProxy(desc);
            if (rpcProxy==null){
                log.warn("create proxyObject failed, the interface is named : {}", interfaceName);
            }

            Object bean = null;
            if ( beanFactory.containsBean(beanName) ) {
                bean = beanFactory.getBean(beanName);
            }
            // 如果是和consumerDesc的beanName冲突，就直接删了consumerDesc的
            if ( bean != null ){
                beanFactory.destroyBean(beanName, bean);
            }
            // 注册rpc动态代理对象到底层容器
            beanFactory.registerSingleton(beanName, rpcProxy);
            log.debug("create proxyObject by xml successfully ... beanName is [{}]",  beanName);
        }

        this.beanFactory = beanFactory;
    }

    /**
     * 全限定类名 -> 接口名 -> 将接口名的首字母置为小写
     *
     * @param interfaceName
     * @return
     */
    private String getBeanName(String interfaceName) {
        // interfaceName是全限定类名
        String[] split = interfaceName.split("\\.");
//        System.out.println(interfaceName);
        char [] name = split[split.length-1].toCharArray();
        name[0] = (char)((int)name[0] | 0x20); // 首字母变小写
        return new String(name);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * 在bean初始化结束后，调用该方法, 遍历所有的Field，对于标记了 @RemoteInvoke注解
     * 的Field就对其进行装配
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for( Field field:fields ){
            if ( field.isAnnotationPresent(RemoteInvoke.class) ){
                // 为其进行注入
                RemoteInvoke remoteInvokeAnno = field.getAnnotation(RemoteInvoke.class);

                String rpcBeanName = getBeanName(field.getType().getName());

                if ( !beanFactory.containsBean(rpcBeanName) ){
                    ConsumerDescription desc = new ConsumerDescription(
                            field.getType().getName(),
                            remoteInvokeAnno.timeout(),
                            remoteInvokeAnno.serialization(),
                            remoteInvokeAnno.protocal());
                    // 创建的代理bean对象
                    Object rpcProxy = RpcProxyCreator.createProxy(desc);
                    if (rpcProxy==null){
                        log.warn("create proxyObject failed, the interface is named : {}", field.getType().getName());
                    }
                    // 注入到field中
                    field.setAccessible(true);
                    try {
                        field.set(bean,rpcProxy);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    // 将代理对象注册到spring中
                    this.beanFactory.registerSingleton(rpcBeanName, rpcProxy);
                    log.debug("create proxyObject by annotation:[{}] successfully ... beanName is [{}]", RemoteInvoke.class.getSimpleName(), beanName);
                }else{
                    log.error("there is a duplicate beanName in spring-ApplicationContext," +
                                    " maybe the interface [{}] has been injected into spring...",
                            field.getType().getName());
                }
            }
        }
        return bean;
    }

    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
//        if ( beanClass.isAnnotationPresent(RemoteInvoke.class) ){
//            // 根据RemoteInvoke来实例化bean
//            RemoteInvoke remoteInvokeAnno = beanClass.getAnnotation(RemoteInvoke.class);
//
//            ConsumerDescription desc = new ConsumerDescription(
//                    beanClass.getName(),
//                    remoteInvokeAnno.timeout(),
//                    remoteInvokeAnno.serialization(),
//                    remoteInvokeAnno.protocal());

//            Object rpcProxy = RpcProxyCreator.createProxy(desc);
//            if (rpcProxy==null){
//                log.warn("create proxyObject failed, the interface is named : {}", beanClass.getName());
//            }
//            log.debug("create proxyObject by annotation:[{}] successfully ... beanName is [{}]", RemoteInvoke.class.getSimpleName(), beanName);
//            return rpcProxy;
//        }
        return null;
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        return true;
    }

    @Override
    public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {
        return pvs;
    }

}
