package com.earlyrpc.client;

import com.earlyrpc.client.config.ConsumerDescription;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author: czf
 * @date: 2020/8/17 11:05
 */
public class Main {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring.xml");
        ConsumerDescription consumerDescription = (ConsumerDescription) applicationContext.getBean("helloDesc");
        System.out.println(consumerDescription);
    }
}
