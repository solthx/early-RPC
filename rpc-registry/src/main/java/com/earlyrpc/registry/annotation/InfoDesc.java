package com.earlyrpc.registry.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 被改注解标记，且是BaseInfoDesc的子类 的这些类 ,
 * 将作为一种存储类型存在注册中心上
 *
 * 之后在进行类型匹配的时候，就通过获取到这些类的对象，然后就能得到
 * 其class对象，然后就可以进行类型判断了
 *
 * @author: czf
 * @date: 2020/8/26 9:08
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
@Deprecated
public @interface InfoDesc {
}
