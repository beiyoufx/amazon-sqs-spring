package com.github.sqs.spring.configuration;

import com.github.sqs.spring.DefaultSimpleMessageListenerContainer;
import com.github.sqs.spring.core.MessageListener;
import com.github.sqs.spring.core.SqsListener;
import com.github.sqs.spring.autoconfiguration.AmazonSqsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import software.amazon.awssdk.services.sqs.SqsClient;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AWS SQS message handler config
 *
 * @author 无羡 2019-07-05
 */
@Configuration
public class ListenerContainerConfiguration implements ApplicationContextAware, SmartInitializingSingleton {
    private final static Logger log = LoggerFactory.getLogger(ListenerContainerConfiguration.class);

    private ConfigurableApplicationContext applicationContext;

    private AtomicLong counter = new AtomicLong(0);

    @Resource
    private AmazonSqsProperties amazonSqsProperties;

    private FilterRegistry filterRegistry;

    @Override
    public void afterSingletonsInstantiated() {
        Map<String, Object> beans = this.applicationContext.getBeansWithAnnotation(SqsListener.class);

        FilterRegistry filterRegistry = this.applicationContext.getBean(FilterRegistry.class);
        if (filterRegistry != null) {
            this.filterRegistry = filterRegistry;
        }

        if (Objects.nonNull(beans)) {
            beans.forEach(this::registerContainer);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    private void registerContainer(String beanName, Object bean) {
        Class<?> clazz = AopProxyUtils.ultimateTargetClass(bean);

        if (!MessageListener.class.isAssignableFrom(bean.getClass())) {
            throw new IllegalStateException(clazz + " is not instance of " + MessageListener.class.getName());
        }

        SqsListener sqsListener = clazz.getAnnotation(SqsListener.class);

        String containerBeanName = String.format("%s_%s", DefaultSimpleMessageListenerContainer.class.getName(),
            counter.incrementAndGet());
        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;

        genericApplicationContext.registerBean(containerBeanName, DefaultSimpleMessageListenerContainer.class,
            () -> createSimpleMessageListenerContainer(bean, sqsListener));
        DefaultSimpleMessageListenerContainer container = genericApplicationContext.getBean(containerBeanName,
            DefaultSimpleMessageListenerContainer.class);
        if (!container.isRunning()) {
            try {
                container.start();
            } catch (Exception e) {
                log.error("Started sqs container failed. {}", container, e);
                throw new RuntimeException(e);
            }
        }

        String queueUrl = container.getQueueUrl();
        log.info("Register the listener to container, listenerBeanName:{}, containerBeanName:{}, queueUrl:{}", beanName, containerBeanName, queueUrl);
    }

    private DefaultSimpleMessageListenerContainer createSimpleMessageListenerContainer(Object bean, SqsListener sqsListener) {
        String queueUrl = amazonSqsProperties.getSqs().getDomain() + sqsListener.value();
        if (!sqsListener.queueUrl().trim().equals("")) {
            queueUrl = sqsListener.queueUrl();
        }

        DefaultSimpleMessageListenerContainer container = new DefaultSimpleMessageListenerContainer(
            queueUrl,
            (MessageListener) bean,
            sqsListener,
            amazonSqsProperties.getSqs().isEnableListening(),
            applicationContext.getBean(SqsClient.class),
            Optional.ofNullable(filterRegistry).map(FilterRegistry::getFilters).orElse(new ArrayList<>())
        );

        return container;
    }

}
