package com.earlyrpc.client.config;

import com.earlyrpc.client.enums.Prefix;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

/**
 * 解析Erpc的配置bean信息
 *
 * @author: czf
 * @date: 2020/8/17 14:31
 */
@Slf4j
public class ConsumerBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
    protected Class getBeanClass(Element element) {
        return ConsumerDescription.class;
    }

    protected void doParse(Element element, BeanDefinitionBuilder bean) {
        String interfaceName = element.getAttribute("interface");
        bean.addConstructorArgValue(interfaceName);

        String timeout = element.getAttribute("timeout");
        bean.addConstructorArgValue(timeout);

        String serialization = element.getAttribute("serialization");
        bean.addConstructorArgValue(serialization);

        String protocal = element.getAttribute("protocal");
        bean.addConstructorArgValue(protocal);

        // 通过修改id标签来间接确定description对应的beanName的值
        element.setAttribute("id", Prefix.ERPC_CONSUMER_BEANNAME_PREFIX+element.getAttribute("id"));
    }
}
