package com.github.sqs.spring;

import com.github.sqs.spring.core.Filter;
import com.github.sqs.spring.core.FilterChain;
import com.github.sqs.spring.core.MessageHandler;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 无羡 2020-04-30
 */
public class MessageFilterChain implements FilterChain {
    private List<Filter> filters = new ArrayList<>();
    private int pos = 0;
    private int n;
    private MessageHandler messageHandler;

    public MessageFilterChain(List<Filter> filters, MessageHandler messageHandler) {
        if (filters != null && !filters.isEmpty()) {
            this.filters = filters;
            this.n = filters.size();
        }
        this.messageHandler = messageHandler;
    }

    /**
     * execute filter
     *
     * @param input message
     */
    @Override
    public boolean doFilter(Message input) {
        if (pos < n) {
            return filters.get(pos++).doFilter(input, this);
        }

        return messageHandler.handle();
    }
}
