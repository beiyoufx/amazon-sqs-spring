package com.github.sqs.spring;

import com.github.sqs.spring.core.Consumer;
import com.github.sqs.spring.core.Filter;
import com.github.sqs.spring.core.MessageListener;
import com.github.sqs.spring.core.FilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.util.List;

/**
 * Simple consumer
 * Only cover the basic functions of sqs
 *
 * @author 无羡 2020-04-30
 */
public class SimpleConsumer implements Consumer {
    private final static Logger log = LoggerFactory.getLogger(SimpleConsumer.class);
    private volatile boolean listening;

    private String queueUrl;

    private SqsClient sqsClient;

    private int maxNumberOfMessages = 10;
    private int waitTimeSeconds = 10;
    private int sleepSecondsWhenIdle = 3;

    private List<Filter> filters;

    public SimpleConsumer(String queueUrl, SqsClient sqsClient) {
        this.queueUrl = queueUrl;
        this.sqsClient = sqsClient;
    }

    public SimpleConsumer(String queueUrl, SqsClient sqsClient, List<Filter> filters) {
        this.queueUrl = queueUrl;
        this.sqsClient = sqsClient;
        this.filters = filters;
    }

    public void setMaxNumberOfMessages(int maxNumberOfMessages) {
        this.maxNumberOfMessages = maxNumberOfMessages;
    }

    public void setWaitTimeSeconds(int waitTimeSeconds) {
        this.waitTimeSeconds = waitTimeSeconds;
    }

    public void setSleepSecondsWhenIdle(int sleepSecondsWhenIdle) {
        this.sleepSecondsWhenIdle = sleepSecondsWhenIdle;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    @Override
    public void start(MessageListener messageListener) {
        if (listening) {
            throw new IllegalStateException("Consumer has started. consumer: " + queueUrl);
        }
        listening = true;
        // 单次最大返回10条
        // 长轮询10s
        ReceiveMessageRequest request = ReceiveMessageRequest
            .builder()
            .queueUrl(queueUrl)
            .maxNumberOfMessages(maxNumberOfMessages)
            .waitTimeSeconds(waitTimeSeconds)
            .build();

        while (listening) {
            List<Message> messages = pullMessage(request);
            if (messages == null || messages.size() == 0) {
                sleep();
                continue;
            }
            for (Message message : messages) {
                FilterChain filterChain = new MessageFilterChain(filters, () -> handleMessage(message, messageListener));
                boolean isSuccess = filterChain.doFilter(message);

                if (isSuccess) {
                    sqsClient.deleteMessage(deleteRequest -> deleteRequest
                        .queueUrl(queueUrl)
                        .receiptHandle(message.receiptHandle())
                        .build());
                }
            }
        }
    }

    @Override
    public void stop() {
        this.listening = false;
    }

    /**
     * pull messages
     *
     * @return message list
     */
    private List<Message> pullMessage(ReceiveMessageRequest pullRequest) {
        List<Message> messages;
        try {
            messages = sqsClient.receiveMessage(pullRequest).messages();
        } catch (SqsException ae) {
            log.error("receive message failed. queueUrl: " + queueUrl, ae);
            return  null;
        }
        return messages;
    }

    /**
     * handle one message
     *
     * @param message message
     */
    private boolean handleMessage(Message message, MessageListener messageListener) {
        if (log.isDebugEnabled()) {
            log.debug("handle sqs message. message: {}", getMessageString(message));
        }
        boolean isSuccess = false;
        try {
            isSuccess = messageListener.onMessage(message);
            if (!isSuccess) {
                log.error("handle sqs message failed. message: {}", getMessageString(message));
            }
        } catch (Exception e) {
            log.error("handle sqs message failed. message: {}", getMessageString(message), e);
        }
        return isSuccess;
    }

    private String getMessageString(Message message) {
        return message.toString().replace("\n", "");
    }

    /**
     * pause when no messages
     */
    private void sleep() {
        try {
            Thread.sleep(sleepSecondsWhenIdle * 1000);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }
}
