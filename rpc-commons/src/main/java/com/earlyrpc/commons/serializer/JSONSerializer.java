package com.earlyrpc.commons.serializer;

import com.alibaba.fastjson.JSONObject;

/**
 * json序列化 (基于fastjson进行，无法对内部类进行序列化(例如一个java源文件内有多个类的情况))
 *
 * @author czf
 * @Date 2020/8/15 10:21 下午
 */
public class JSONSerializer extends Serializer {

    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        Object entity = JSONObject.parseObject(bytes, clazz);
        return (T)entity;
    }

    public <T> byte[] serialize(T entity) {
        return JSONObject.toJSONBytes(entity);
    }
}
