package com.earlyrpc.registry;

import com.earlyrpc.registry.constant.EventType;

/**
 *
 * @author: czf
 * @date: 2020/9/21 15:24
 */
public interface CallBack {
    void callback(EventType event);
}
