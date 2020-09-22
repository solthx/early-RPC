package com.earlyrpc.server.annotation;

import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * value是服务接口对象
 * 被这个注解标记的就是服务接口的实现类
 *
 * @author czf
 * @Date 2020/9/21 9:44 下午
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcService {
    Class<?> value();

    String alias() default "";

    String serviceName();

}
