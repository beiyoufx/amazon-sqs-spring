# amazon-sqs-spring
帮助开发者在Spring Boot应用中快速集成亚马逊SQS消息服务。


# 使用
添加Maven依赖:

```xml
<dependency>
    <groupId>com.github</groupId>
    <artifactId>sqs-spring-boot-starter</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

# 示例

## 监听器

配置
```
aws.access-key-id=your_access_key_id
aws.secret-access-key=your_secret
aws.region=cn-northwest-1
aws.sqs.enable-listening=true
aws.sqs.endpoint=https://sqs.cn-northwest-1.amazonaws.com.cn
aws.sqs.account=your_account
```
监听器
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

## 过滤器

自定义过滤器

```java
/**
 * 定义
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
 * 注册
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

# 开源许可
Apache License, Version 2.0
