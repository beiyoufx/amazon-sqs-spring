package com.github.sqs.spring.core;

import org.springframework.beans.factory.DisposableBean;

/**
 * message listener container
 *
 * @author 无羡 2020-04-29
 */
public interface MessageListenerContainer extends DisposableBean {
    /**
     * setup a listener for container
     *
     * @param messageListener message listener
     */
    void setupListener(MessageListener messageListener);
}
