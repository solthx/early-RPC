package com.earlyrpc.commons.protocol;

import com.earlyrpc.commons.protocol.enums.RpcResStateEnum;
import lombok.Data;

/**
 * 进行rpc通信的响应报文
 *
 * @author czf
 * @Date 2020/8/15 9:10 下午
 */
@Data
public class RpcResponse {
    private Long messageId;
    private Object returnData;
    private RpcResStateEnum state;
}
