# amazon-sqs-spring
Help developers quickly integrate Amazon Simple Queue Service(SQS) in Spring Boot.

# Usage
Add a dependency using maven:

<!--add dependency in pom.xml-->
```xml
<dependency>
    <groupId>com.github</groupId>
    <artifactId>sqs-spring-boot-starter</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

# Sample

## Handler

configuration properties
```
aws.access-key-id=your_access_key_id
aws.secret-access-key=your_secret
aws.region=cn-northwest-1
aws.sqs.enable-listening=true
aws.sqs.endpoint=https://sqs.cn-northwest-1.amazonaws.com.cn
aws.sqs.account=your_account
```
handler
```java
@Component
@SqsListener("your_queue_name")
public class TestMsgHandler implements MessageListener {

    @Override
    public boolean onMessage(Message message) {
        System.out.println("hello world. " + message);
        return true;
    }
}
```

## Filter

customer filter

```java
/**
 * define
 */
public class SqsTrackFilter implements Filter {

    @Override
    public boolean doFilter(Message message, FilterChain filterChain) {
        // 全局追踪
        String traceId = UuidUtils.getUuidWithoutLine();
        MDC.put(AppConstant.TRACE_ID, traceId);

        try {
            return filterChain.doFilter(message);
        } finally {
            MDC.remove(AppConstant.TRACE_ID);
        }
    }
}

/**
 * register
 */
@Configuration
public class SqsFilterConfig {
    @Bean
    public FilterRegistry filterRegistry() {
        final FilterRegistry filterRegistry = new FilterRegistry();
        // execute follow the registration order
        final SqsTrackFilter sqsTrackFilter = new SqsTrackFilter();
        filterRegistry.registerFilter(sqsTrackFilter);

        return filterRegistry;
    }
}

```

# License
Apache License, Version 2.0
