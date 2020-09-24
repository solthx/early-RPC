package com.earlyrpc.example.provider;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author czf
 * @Date 2020/9/24 11:11 下午
 */
public class ProviderMain {
    public static void main(String[] args) {
        /**
         * 启动方法:
         *      1. 就是让spring扫描com.earlyrpc包下的内容
         *      2. 配置eprc的几个参数（erpc.registry.address， erpc.provider.localAddress）
         *      3. 启动spring
         */
        new ClassPathXmlApplicationContext("spring-config.xml");
     }
 }

