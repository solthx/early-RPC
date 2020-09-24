package com.earlyrpc.client.config.processor;

import com.earlyrpc.client.config.ConsumerDescription;
import com.earlyrpc.client.enums.Prefix;
import com.earlyrpc.client.proxy.RpcProxyCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * 后置处理器，创建代理对象
 *
 * @author: czf
 * @date: 2020/8/18 10:41
 */
@Slf4j
@Component
public class CreateRPCProxyBeanPostProcessor implements BeanFactoryPostProcessor {

    /**
     * 加载完所有BeanDefinition之后回调该方法
     *
     * 提前注册ConsumerDesc，并获取interfaceName
     *
     * 根据interfaceName进行rpc动态代理，并将结果注册到Spring容器中，之后就可以直接autowire了
     *
     * @param beanFactory
     * @throws BeansException
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] consumerDesc = beanFactory.getBeanNamesForType(ConsumerDescription.class);
        for( String descName:consumerDesc ){
            ConsumerDescription desc = (ConsumerDescription) beanFactory.getBean(descName);
            String interfaceName = desc.getInterfaceName();
            String beanName = getBeanName(interfaceName);
            // 在这里生成动态代理对象
            Object rpcProxy = RpcProxyCreator.createProxy(desc);
            if (rpcProxy==null){
                log.warn("create proxyObject failed, the interface is named : {}", interfaceName);
            }
            // 注册rpc动态代理对象到底层容器
            beanFactory.registerSingleton(beanName, rpcProxy);
        }
    }

    /**
     * 重新设置BeanName为  固定前缀 + Rpc接口名(首字母小写)
     *
     * @param interfaceName
     * @return
     */
    private String getBeanName(String interfaceName) {
        // interfaceName是全限定类名
        String[] split = interfaceName.split("\\.");
        System.out.println(interfaceName);
        char [] name = split[split.length-1].toCharArray();
        name[0] = (char)((int)name[0] | 0x20); // 首字母变小写
        return Prefix.ERPC_CONSUMER_BEANNAME_PREFIX + new String(name);
    }
}
