package com.earlyrpc.client.connect;

import com.earlyrpc.commons.protocol.RpcRequest;
import com.earlyrpc.commons.utils.async.RpcResponsePromise;

/**
 * 异步发送器
 *
 * sender的作用仅仅是“发送数据”， 而不是“发送数据+等待数据”
 *
 * @author czf
 * @Date 2020/8/18 9:42 下午
 */
public interface Sender {
    RpcResponsePromise sendRequest(RpcRequest rpcRequest);
}
