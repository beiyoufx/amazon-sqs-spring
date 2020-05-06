package com.github.sqs.spring.core;

import software.amazon.awssdk.services.sqs.model.Message;

/**
 * filter
 *
 * @author 无羡 2020-04-30
 */
public interface Filter {
    /**
     * execute filter
     *
     * @param input message
     * @param filterChain chain
     */
    boolean doFilter(Message input, FilterChain filterChain);
}
