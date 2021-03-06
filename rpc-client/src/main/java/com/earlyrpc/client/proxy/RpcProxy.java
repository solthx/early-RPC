package com.earlyrpc.client.proxy;

import com.earlyrpc.client.config.ConsumerDescription;
import com.earlyrpc.client.connect.ConnectionManager;
import com.earlyrpc.client.connect.Sender;
import com.earlyrpc.commons.protocol.RpcRequest;
import com.earlyrpc.commons.protocol.RpcResponse;
import com.earlyrpc.commons.utils.async.RpcResponsePromise;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 实现了rpc接口的代理对象
 *
 * @author: czf
 * @date: 2020/8/18 9:55
 */
public class RpcProxy implements InvocationHandler {

    /* 根据改desc来构建RpcRequest */
    private ConsumerDescription desc;

    private ConnectionManager connectionManager;

    public RpcProxy(ConsumerDescription desc, ConnectionManager connectionManager) {
        this.desc = desc;
        this.connectionManager = connectionManager;
    }

    /**
     * 对方法进行动态代理
     *
     * 1. 生成request
     * 2. 发送
     * 3. 获取response并返回
     *
     *
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // 0. 方法过滤, 对于toString这种方法就不用进行代理
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }

        // 1. 生成request
        RpcRequest rpcRequest = getRpcRequest(method, args);

        // 2. 发送端负载均衡获取一个sender
        Sender sender = connectionManager.getSender(rpcRequest.getClazzName());

        if (sender==null) {
            return null;
        }

        // 3. 使用sender进行发送
        RpcResponsePromise rpcResponsePromise = sender.sendRequest(rpcRequest);

        // 4. 获取response并返回, 最多等3s
        RpcResponse response = rpcResponsePromise.get(2700, TimeUnit.MILLISECONDS);
        return response.getReturnData();
    }

    /**
     *
     * ps: rpc接口在consumer端和provider端的全限定类名必须一样！
     *
     * 生成rpc请求对象头
     * @return
     */
    private RpcRequest getRpcRequest(Method method, Object [] params) {
        RpcRequest req = new RpcRequest();
        req.setRequestId(UUID.randomUUID().toString().hashCode());
        req.setClazzName(method.getDeclaringClass().getName());
        req.setMethodName(method.getName());
        req.setParamTypeList(method.getParameterTypes());
        req.setParamList(params);
//        System.out.println(req); //todo
        return req;
    }
}
