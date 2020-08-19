package com.earlyrpc.client.proxy;

import com.earlyrpc.client.config.ConsumerDescription;
import com.earlyrpc.client.connect.ConnectionManager;
import com.earlyrpc.client.connect.Sender;
import com.earlyrpc.client.fake.RpcClient;
import com.earlyrpc.commons.protocol.RpcRequest;
import com.earlyrpc.commons.protocol.RpcResponse;
import com.earlyrpc.commons.utils.async.RpcResponsePromise;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * todo:
 * 实现了rpc接口的代理对象
 *
 * @author: czf
 * @date: 2020/8/18 9:55
 */
public class RpcProxy implements InvocationHandler {

    /* 根据改desc来构建RpcRequest */
    private ConsumerDescription desc;

    public RpcProxy(ConsumerDescription desc) {
        this.desc = desc;
    }

    /**
     * todo: 先简单实现下
     *
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = getRpcRequest(method, args);
        // todo: change
        Sender sender = ConnectionManager.getInstance().getSender();
        RpcResponsePromise rpcResponsePromise = sender.sendRequest(rpcRequest);
        RpcResponse response = rpcResponsePromise.get();
        return response.getReturnData();
    }

    /**
     * todo: 接口不一致问题
     *
     * 生成rpc请求对象头
     * @return
     */
    private RpcRequest getRpcRequest(Method method, Object [] params) {
        RpcRequest req = new RpcRequest();
//        req.setInterfaceName(desc.getInterfaceName());
        //req.setInterfaceName(method.getDeclaringClass().getName());
        req.setClazzName("com.czf.service.export.HelloService");
        req.setMethodName(method.getName());
        req.setParamTypeList(method.getParameterTypes());
        req.setParamList(params);
        req.setRequestId(1); // todo:change
        System.out.println(req);
        return req;
    }
}
