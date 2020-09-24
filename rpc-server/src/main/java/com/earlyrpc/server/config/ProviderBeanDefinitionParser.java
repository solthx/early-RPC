package com.earlyrpc.server.config;

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
public class ProviderBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
    protected Class getBeanClass(Element element) {
        return ServerDescription.class;
    }

    protected void doParse(Element element, BeanDefinitionBuilder bean) {
        // todo
    }
}
