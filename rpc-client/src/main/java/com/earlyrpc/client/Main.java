package com.earlyrpc.client;

import com.earlyrpc.client.config.ConsumerDescription;
import com.earlyrpc.client.enums.Prefix;
import com.earlyrpc.client.fake.export.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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

        Hello hello = (Hello) applicationContext.getBean("hello");
        System.out.println(hello.getHelloService().hello("czf!!!"));
    }
}
