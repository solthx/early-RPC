package com.earlyrpc.commons.protocol.enums;

/**
 * 响应报文状态枚举类型
 *
 * @author czf
 * @Date 2020/8/15 9:15 下午
 */
public enum  RpcResStateEnum {
    SUCCESS(0,"成功"),

    INTERFACE_NOT_FOUND(1000,"未找到指定接口"),

    METHOD_NOT_FOUND(2000,"未找到指定方法");


    private Integer code;
    private String msg;

    RpcResStateEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
