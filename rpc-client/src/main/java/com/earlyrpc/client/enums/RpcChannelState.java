package com.earlyrpc.client.enums;

/**
 * @author czf
 * @Date 2020/9/28 19:10
 */
public enum RpcChannelState {
    CONNECT(2),
    DISCONNECT(0),
    WAITTING(1);
    private Integer state;

    private RpcChannelState(Integer state) {
        this.state = state;
    }

    public Integer getState() {
        return state;
    }

}
