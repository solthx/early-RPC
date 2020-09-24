package com.earlyrpc.client.annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 被该注解修饰的接口会自动为其创建rpc代理对象
 *
 * @author czf
 * @Date 2020/9/24 17:42
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RemoteInvoke {
    /* 超时时间 */
    String timeout() default "5000";

    /* 序列化方式 */
    String serialization() default "protobuf";

    /* 支持的协议类型 */
    String protocal() default "erpc";
}
