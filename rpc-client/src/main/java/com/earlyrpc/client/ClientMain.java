package com.earlyrpc.client;

import com.earlyrpc.client.annotation.RemoteInvoke;
import com.earlyrpc.client.connect.ConnectionManager;
import com.earlyrpc.export.HelloService;
import lombok.Data;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author: czf
 * @date: 2020/8/17 11:05
 */
@Component
@Data
public class ClientMain {

//    @RemoteInvoke
    private HelloService helloService;

    public static void main(String[] args) {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring.xml");
        HelloService bean = (HelloService) applicationContext.getBean("helloService");
//        ClientMain clientMain = (ClientMain) applicationContext.getBean("clientMain");
//        HelloService bean = clientMain.getHelloService();


//        String res = null;

//        while( res == null ){
        String res = bean.hello("czf");
        System.out.println(res);
//            try {
//                Thread.sleep(5*1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }



//        System.out.println(applicationContext.getBeansOfType(HelloService.class));

        int t = 1000;
        long start = System.currentTimeMillis();

        while( t-->0 ){
            System.out.println(t+","+bean.hello("czf"));
        }
        long end = System.currentTimeMillis();

        System.out.println("cost_time: "+(end-start));

//        ConnectionManager.getInstance().close();
//        applicationContext.close();
        ConnectionManager.getInstance().close();
        System.out.println("结束。。。");
    }
}
