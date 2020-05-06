package com.github.sqs.spring.core;

/**
 * message handler
 *
 * @author 无羡 2020-04-30
 */
@FunctionalInterface
public interface MessageHandler {
    /**
     * handle message
     */
    boolean handle();
}
