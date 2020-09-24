package com.earlyrpc.commons.protocol;

import lombok.Data;

/**
 * 进行rpc通信的响应报文
 *
 * @author czf
 * @Date 2020/8/15 9:10 下午
 */
@Data
public class RpcResponse {
    /* 响应Id 和请求Id相同 */
    private Integer responseId;

    /* 调用返回结果 */
    private Object returnData;

    /* 若调用发生异常，则返回错误信息，若调用成功则为空 */
    private String errMsg;
}
