package com.github.sqs.spring.core;

import software.amazon.awssdk.services.sqs.model.Message;

/**
 * Filter Chain
 *
 * @author 无羡 2020-04-30
 */
public interface FilterChain {
    /**
     * execute filter
     *
     * @param input message
     */
    boolean doFilter(Message input);
}
