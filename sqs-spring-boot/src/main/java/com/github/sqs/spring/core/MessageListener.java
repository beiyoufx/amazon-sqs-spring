package com.github.sqs.spring.core;

import software.amazon.awssdk.services.sqs.model.Message;

/**
 * SQS message listener
 *
 * @author 无羡 2019-07-09
 */
public interface MessageListener {

    /**
     * handle message
     * should delete the message when consume success
     *
     * @param message message
     * @return success true, fail false
     */
    boolean onMessage(Message message);
}
