package com.earlyrpc.registry.zookeeper;

import com.earlyrpc.commons.serializer.ProtoBufSerializer;
import com.earlyrpc.commons.serializer.Serializer;
import com.earlyrpc.registry.constant.RegistryCenterConfig;
import com.earlyrpc.registry.description.remote.ConsumerInfoDesc;
import com.earlyrpc.registry.description.remote.ProviderInfoDesc;
import com.earlyrpc.registry.description.remote.ServiceInfoDesc;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author czf
 * @Date 2020/8/29 12:36 上午
 */
@Slf4j
public class ZKClientTest {

    private ZKClient provider;

    private ZKClient consumer;

    private ProviderInfoDesc providerInfoDesc;

    /**
     * 初始化
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        Serializer serializer = new ProtoBufSerializer();
        provider = new ZKClient("127.0.0.1:2181", RegistryCenterConfig.PROVIDER_TYPE, serializer);
//        provider =  new ZKClient("provider2:8000", RegistryCenterConfig.PROVIDER_TYPE, serializer);
//        provider =  new ZKClient("provider3:8000", RegistryCenterConfig.PROVIDER_TYPE, serializer);

        consumer = new ZKClient("127.0.0.1:2181", RegistryCenterConfig.CONSUMER_TYPE, serializer);
//        consumer = new ZKClient("consumer2:8000", RegistryCenterConfig.CONSUMER_TYPE, serializer);
//        consumer = new ZKClient("consumer3:8000", RegistryCenterConfig.CONSUMER_TYPE, serializer);

        providerInfoDesc = new ProviderInfoDesc("61.147.37.1:8080");
        providerInfoDesc.getServiceInfoDescList().add(new ServiceInfoDesc("testService",this.getClass().getName(),this.getClass().getSimpleName()));
    }

    /**
     * 安全关闭
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        provider.close();
    }


    @Ignore
    @Test
    public void listRegisteredInfoDesc() {
    }

    /**
     * 注册测试
     */
    @Test
    public void register() throws InterruptedException {
//        log.info("开始执行注册操作... 注册内容为 {}", provider);
//        provider.register(providerInfoDesc);
//        Thread.sleep(10*1000);
//
//
//        ProviderInfoDesc newProvider = new ProviderInfoDesc("61.147.37.1:9999");
//        newProvider.getServiceInfoDescList().add(new ServiceInfoDesc("newTestService",this.getClass().getName(),this.getClass().getSimpleName()));
//        provider.register(newProvider);
//        newProvider.getServiceInfoDescList().remove(0);
//        log.info("开始执行更新操作... 更新为 {}", newProvider);
//        provider.registerOrUpdate(newProvider);
//
//
//        Thread.sleep(10*1000);
//        log.info("开始执行删除操作... 删除{}", newProvider);
//        provider.delete(newProvider.getZkAbsolutePath());
        consumer.register(new ConsumerInfoDesc("consumer:8080","czf"));
        log.info("{}",new ConsumerInfoDesc("consumer:8080","czf").getZkAbsolutePath());
        Thread.sleep(60*60*1000);

    }

}