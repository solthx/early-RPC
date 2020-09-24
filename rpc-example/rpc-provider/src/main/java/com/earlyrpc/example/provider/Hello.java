package com.earlyrpc.example.provider;

import com.earlyrpc.export.HelloService;
import com.earlyrpc.server.annotation.RpcService;

/**
 * @author czf
 * @Date 2020/9/24 11:09 下午
 */
@RpcService(value = HelloService.class, serviceName = "hello-service")
public class Hello implements HelloService{
    public String hello(String name) {
        return "hello, "+name+"~";
    }
}
