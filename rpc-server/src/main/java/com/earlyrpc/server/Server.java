package com.earlyrpc.server;

/**
 * 服务器抽象接口
 *
 * @author czf
 * @Date 2020/9/21 9:50 下午
 */
public abstract class Server {

    /**
     * 服务器启动
     */
    abstract public void start();

    /**
     * 服务器关闭
     */
    abstract public void stop();

}
