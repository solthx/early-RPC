# early-RPC

基于 spring + netty + zookeeper 实现轻量级RPC框架

## 1. 快速开始

### 第一步：引入pom依赖
**a. consumer端**
```xml
<!-- rpc客户端 -->
<dependency>
    <groupId>com.czf</groupId>
    <artifactId>rpc-client</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>

<!-- 演示用例的export包 -->
<dependency>
    <groupId>com.czf</groupId>
    <artifactId>rpc-export</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

**b. provider端**
```xml
<!-- rpc服务端 -->
<dependency>
    <groupId>com.czf</groupId>
    <artifactId>rpc-server</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>

<!-- 演示用例的export包 -->
<dependency>
    <groupId>com.czf</groupId>
    <artifactId>rpc-export</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 第二步：配置启动信息
本项目是基于spring启动的，因此无论是consumer端还是provider端, 在启动时的配置阶段，需要做两件事：
1. 让spring扫描到"com.earlyrpc"包
2. 让spring扫描到配置文件

#### a. consumer端
1. spring-config.xml
    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xmlns:context="http://www.springframework.org/schema/context"
           xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">
    
    
        <!-- 扫描到包 -->
        <context:component-scan base-package="com.earlyrpc"></context:component-scan>
        
        <!-- 引入配置文件 -->
        <context:property-placeholder location="consumer-config.properties"></context:property-placeholder>
    </beans>
    ```
2. consumer-config.properties
    ```properties
    # 在consumer端配置注册中心的address (ip:port)
    erpc.consumer.registry.address=127.0.0.1:2181
    ```

#### b. provider端
1. spring-config.xml
    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xmlns:context="http://www.springframework.org/schema/context"
           xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">
    
    
        <!-- 扫描到包 -->
        <context:component-scan base-package="com.earlyrpc"></context:component-scan>
        
        <!-- 引入配置文件 -->
        <context:property-placeholder location="provider-config.properties"></context:property-placeholder>
    </beans>
    ```
2. provider-config.properties
    ```properties
    # provider服务端绑定的ip:port地址
    erpc.provider.localAddress=127.0.0.1:7777
    
    # 在consumer端配置注册中心的address (ip:port)
    erpc.registry.address=127.0.0.1:2181
    ```

### 第三步：启动

**本次例子的rpc接口**
```java
package com.earlyrpc.export;

/**
 *
 * @author czf
 * @Date 2020/9/24 15:31
 */
public interface HelloService {
    String hello(String name);
}
```

#### a. provider端
1. 对rpc服务接口的实现
    ```java
    /**
     * @author czf
     * @Date 2020/9/24 11:09 下午
     */
    @RpcService(value = HelloService.class, serviceName = "hello-service")
    public class Hello implements HelloService{
        public String hello(String name) {
            return "hello, " + name + "~";
        }
    }
    ```

2. provider端的RpcServer启动
```java
package com.earlyrpc.example.provider;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author czf
 * @Date 2020/9/24 11:11 下午
 */
public class ProviderMain {
    public static void main(String[] args) {
          // rpcServer生命周期与spring上下文容器同步
          new ClassPathXmlApplicationContext("spring-config.xml");
     }
 }
```


#### c. consumer端
```java
package com.earlyrpc.example.consumer;

import com.earlyrpc.client.annotation.RemoteInvoke;
import com.earlyrpc.export.HelloService;
import lombok.Data;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 *
 * helloConsumer作为测试bean
 *  
 * @author czf
 * @Date 2020/9/24 11:07 下午
 */
@Data
@Component
public class HelloConsumer {

    // 自动Autowire进rpc服务接口
    @RemoteInvoke
    private HelloService helloService;

    public static void main(String[] args) {
        // 启动spring
        ClassPathXmlApplicationContext app = new ClassPathXmlApplicationContext("spring-config.xml");
        
        // 获取测试bean
        HelloConsumer helloConsumer = app.getBean("helloConsumer", HelloConsumer.class);

        // 获取被RemoteInvoke自动填装的bean
        HelloService helloService = helloConsumer.getHelloService();

        System.out.println(helloService.hello("early-rpc"));
        
        // earlyrpc的生命周期与spring的applicationContext同步
        app.close();
    }
}

```

打印结果:
```
....launch-log....

hello, early-rpc~

....close-log....

```

## 2. 包结构

## 3. 原理