package com.earlyrpc.client.config;

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
        // this will never be null since the schema explicitly requires that a value be supplied
        String interfaceName = element.getAttribute("interface");
        bean.addConstructorArgValue(interfaceName);

        String timeout = element.getAttribute("timeout");
        bean.addConstructorArgValue(timeout);

        String serialization = element.getAttribute("serialization");
        bean.addConstructorArgValue(serialization);

        String protocal = element.getAttribute("protocal");
        bean.addConstructorArgValue(protocal);
    }
}
