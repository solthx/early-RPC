package com.earlyrpc.client;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.Resource;

/**
 * @author: czf
 * @date: 2020/8/17 11:05
 */
public class Main {

    @Resource
    public HelloService helloService;

    public static void main(String[] args) {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring.xml");
//        ConsumerDescription consumerDescription = (ConsumerDescription) applicationContext.getBean("helloDesc");
//        System.out.println(consumerDescription);
//        HelloService bean = (HelloService) applicationContext.getBean();
//        System.out.println(bean.hello("czf"));

        System.out.println(applicationContext.getBeansOfType(HelloService.class));
    }
}
