package com.earlyrpc.client.connect;

import com.earlyrpc.commons.protocol.RpcRequest;
import com.earlyrpc.commons.utils.async.RpcResponsePromise;

/**
 * @author czf
 * @Date 2020/8/18 9:42 下午
 */
public interface Sender {
    RpcResponsePromise sendRequest(RpcRequest rpcRequest);
}
