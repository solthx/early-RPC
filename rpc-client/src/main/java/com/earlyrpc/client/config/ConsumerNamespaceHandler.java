package com.earlyrpc.client.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author: czf
 * @date: 2020/8/17 14:30
 */
@Slf4j
public class ConsumerNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("consumer", new ConsumerBeanDefinitionParser());
    }
}
