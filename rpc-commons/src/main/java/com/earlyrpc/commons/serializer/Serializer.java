package com.earlyrpc.commons.serializer;

/**
 * 序列化器
 *
 * @author czf
 * @Date 2020/8/15 10:04 下午
 */
public abstract class Serializer {
    abstract public <T> T deserialize(byte[] bytes, Class<T> clazz);
    abstract public <T> byte [] serialize(T entity);
}
