package com.github.sqs.spring.core;

/**
 * Pull messages from remote queue and consume
 *
 * @author 无羡 2020-04-30
 */
public interface Consumer {
    /**
     * start consume
     *
     * @param messageListener message listener
     */
    void start(MessageListener messageListener);

    /**
     * stop consume
     */
    void stop();
}
