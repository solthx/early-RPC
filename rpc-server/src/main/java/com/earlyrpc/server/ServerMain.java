package com.earlyrpc.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author: czf
 * @date: 2020/9/22 19:55
 */
public class ServerMain {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext("spring.xml");
    }
}
