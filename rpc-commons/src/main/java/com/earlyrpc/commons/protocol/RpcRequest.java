package com.earlyrpc.commons.protocol;

import lombok.Data;

/**
 * 进行rpc通信的请求报文
 *
 * @author czf
 * @Date 2020/8/15 9:01 下午
 */
@Data
public class RpcRequest {
    /* 消息id */
    private Long messgeId;

    /* rpc接口名 */
    private String interfaceName;

    /* 等待被调用方法名 */
    private String methodName;

    /* 调用方法时的参数列表 */
    private Object[] paramList;

    /* 方法对应de参数列表类型 */
    private Class<?>[] paramTypeList;
}
