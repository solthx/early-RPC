package com.earlyrpc.example.consumer;

import com.earlyrpc.client.annotation.RemoteInvoke;
import com.earlyrpc.export.HelloService;
import lombok.Data;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Scanner;

/**
 * @author czf
 * @Date 2020/9/24 11:07 下午
 */
@Data
@Component
public class HelloConsumer {

    @RemoteInvoke
    private HelloService helloService;

    public static void main(String[] args) {
        ClassPathXmlApplicationContext app = new ClassPathXmlApplicationContext("spring-config.xml");

        HelloConsumer helloConsumer = app.getBean("helloConsumer", HelloConsumer.class);

        HelloService helloService = helloConsumer.getHelloService();

        Scanner in = new Scanner(System.in);
        int k = 1000;
        while(k-->0){
            System.out.println("["+k+"]："+helloService.hello(in.next()));
        }
        app.close();
    }
}
