package com.earlyrpc.client;

import com.earlyrpc.client.connect.ConnectionManager;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * 在spring容器关闭时，关闭 rpc
 *
 * @author: czf
 * @date: 2020/9/23 18:20
 */
@Component
public class ShutDownRpc implements ApplicationListener<ApplicationEvent> {
    /**
     * 当spring容器关闭时，也关闭connectManager
     *
     * @param applicationEvent
     */
    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if ( applicationEvent instanceof ContextClosedEvent){
            ConnectionManager.getInstance().close();
        }
    }
}
