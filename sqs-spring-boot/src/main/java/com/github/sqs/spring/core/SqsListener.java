package com.github.sqs.spring.core;


import java.lang.annotation.*;

/**
 * sqs listener
 * prefer queue url
 * else use 'queue name' and 'aws-sqs' configuration(endpoint + account)
 *
 * @author wuxian 2019-07-05
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SqsListener {
    /**
     * sqs queue name
     */
    String value() default "";
    /**
     * sqs queue url
     */
    String queueUrl() default "";
}
