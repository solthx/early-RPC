package com.earlyrpc.server.handler;

import com.earlyrpc.commons.protocol.RpcRequest;
import com.earlyrpc.commons.protocol.RpcResponse;
import com.earlyrpc.server.service.AliveService;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 处理 RpcRequest, 本地回调服务方法
 *
 * @author: czf
 * @date: 2020/9/22 9:30
 */
@Slf4j
public class RpcCallbackHandler extends SimpleChannelInboundHandler<RpcRequest> {


    /**
     * 提前实例化好的 实现了服务接口的 object对象
     */
    private Map<String, AliveService> aliveServiceMap;

    private ThreadPoolExecutor threadPoolExecutor;

    public RpcCallbackHandler(Map<String, AliveService> aliveServiceMap, ThreadPoolExecutor threadPoolExecutor) {
        this.aliveServiceMap = aliveServiceMap;
        this.threadPoolExecutor = threadPoolExecutor;
    }


    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final RpcRequest req) throws Exception {
        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                final RpcResponse response = new RpcResponse();
                // 根据req 进行执行，并填充response
                handle(req, response);
                ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()){
                            log.info("response send finished... {} ", response.getReturnData());
                        }
                    }
                });
            }
        });
    }

    /**
     *
     * todo: change to cglib
     *
     * 根据 request 去调对应的方法，然后返回结果
     * @param req
     * @return
     */
    private void handle(RpcRequest req, RpcResponse res) {
        String clazzName = req.getClazzName();
        AliveService aliveService = aliveServiceMap.get(clazzName);
        if ( aliveService == null ){
            res.setErrMsg("the interface [" + clazzName +"] not found.");
            return;
        }

        Object serviceBean = aliveService.getServiceBean();
        // 进行调用
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = req.getMethodName();

        Object[] parameters = req.getParamList();
        Class<?>[] parameterTypes = req.getParamTypeList();

        Method method = null;
        Object result = null;

        try {
            method = serviceClass.getMethod(methodName, parameterTypes);
        }catch (Exception e){
            e.printStackTrace();
        }

        if ( method != null ){
            method.setAccessible(true);
            try {
                result = method.invoke(serviceBean, parameters);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }else{
            res.setErrMsg("the interface [" + clazzName +"] not found.");
        }

        // 填充response
        res.setResponseId(req.getRequestId());
        res.setReturnData(result);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("触发异常...{}",cause);
        System.out.println(ctx.name());
        ctx.close();
    }
}
