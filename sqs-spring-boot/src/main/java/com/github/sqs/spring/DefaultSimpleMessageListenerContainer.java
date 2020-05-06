package com.github.sqs.spring;

import com.github.sqs.spring.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.SmartLifecycle;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.List;

/**
 * AWS SQS listener container
 *
 * @author 无羡 2019-07-05
 */
public class DefaultSimpleMessageListenerContainer implements MessageListenerContainer, InitializingBean, SmartLifecycle {
    private final static Logger log = LoggerFactory.getLogger(DefaultSimpleMessageListenerContainer.class);
    private volatile boolean running;

    private String queueUrl;
    private MessageListener messageListener;
    private SqsListener sqsListener;
    private boolean enableListening;

    private Consumer consumer;

    public String getQueueUrl() {
        return queueUrl;
    }

    public DefaultSimpleMessageListenerContainer(String queueUrl,
                                                 MessageListener messageListener,
                                                 SqsListener sqsListener,
                                                 boolean enableListening,
                                                 SqsClient sqsClient,
                                                 List<Filter> filters) {
        this.queueUrl = queueUrl;
        this.messageListener = messageListener;
        this.sqsListener = sqsListener;
        this.enableListening = enableListening;
        consumer = new SimpleConsumer(queueUrl, sqsClient, filters);
    }

    /**
     * start to listening
     */
    private void listening() {
        if (!enableListening) {
            log.warn("sqs listener is disabled.");
            return;
        }
        log.info("sqs queue {} is being listened.", queueUrl);

        consumer.start(messageListener);
    }

    /**
     * stop listening when the container closing
     */
    @Override
    public void destroy() {
        stop();
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public void start() {
        running = true;
        Class<?> clazz = AopProxyUtils.ultimateTargetClass(messageListener);
        Thread worker = new Thread(this::listening);
        worker.setName("sqs-worker-" + clazz.getSimpleName());
        worker.start();
    }

    @Override
    public void stop() {
        consumer.stop();
        log.info("stop running to sqs queue {}", queueUrl);
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        // keep latest
        return Integer.MAX_VALUE;
    }

    /**
     * setup a listener for container
     *
     * @param messageListener message listener
     */
    @Override
    public void setupListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    @Override
    public void afterPropertiesSet() {
        initConsumer();
    }

    private void initConsumer() {
        if (sqsListener == null) {
            throw new IllegalArgumentException("annotation SqsListener is required for MessageListener instance.");
        }
        if (sqsListener.value().trim().equals("") && sqsListener.queueUrl().trim().equals("")) {
            throw new IllegalArgumentException("sqs queue name or queue url is required for MessageListener instance.");
        }
        log.info("sqs queue {} has been initialized.", queueUrl);
    }
}
