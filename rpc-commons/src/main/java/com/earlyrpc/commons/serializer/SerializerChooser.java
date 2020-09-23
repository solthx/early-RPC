package com.earlyrpc.commons.serializer;

/**
 * 序列化器选择器
 *
 * @author czf
 * @Date 2020/9/23 11:45 下午
 */
public class SerializerChooser {

    /**
     * json序列化器
     */
    public static Serializer jsonSerializer = new JSONSerializer();

    /**
     * protoBuf序列化器
     */
    public static Serializer protobufSerializer = new ProtoBufSerializer();

    /**
     * 0: protoBuf
     * 1: json
     *
     * 其他默认: protoBuf
     *
     * @param kind
     * @return
     */
    public static Serializer choose(Byte kind){
        switch (kind){
            case 0:
                return protobufSerializer;
            case 1:
                return jsonSerializer;
        }
        return protobufSerializer;
    }

}
