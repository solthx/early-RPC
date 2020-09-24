package com.earlyrpc.example.consumer;

import com.earlyrpc.client.annotation.RemoteInvoke;
import com.earlyrpc.export.HelloService;
import lombok.Data;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

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

        System.out.println(helloService.hello("程子丰"));

        app.close();
    }
}
