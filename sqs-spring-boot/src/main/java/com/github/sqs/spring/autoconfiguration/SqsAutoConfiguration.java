package com.github.sqs.spring.autoconfiguration;

import com.github.sqs.spring.configuration.ListenerContainerConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * @author 无羡 2020-04-29
 */
@Configuration
@Import(ListenerContainerConfiguration.class)
@EnableConfigurationProperties(AmazonSqsProperties.class)
@ConditionalOnProperty(prefix = "aws", value = {"access-key-id", "secret-access-key"})
public class SqsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AwsBasicCredentials.class)
    @ConditionalOnProperty(prefix = "aws", value = {"access-key-id", "secret-access-key"})
    public AwsBasicCredentials basicAWSCredentials(AmazonSqsProperties amazonProperties) {
        return AwsBasicCredentials.create(amazonProperties.getAccessKeyId(), amazonProperties.getSecretAccessKey());
    }

    @Bean
    @ConditionalOnMissingBean(StaticCredentialsProvider.class)
    public StaticCredentialsProvider awsStaticCredentialsProvider(AwsBasicCredentials awsBasicCredentials) {
        return StaticCredentialsProvider.create(awsBasicCredentials);
    }

    @Bean(name = "sqsClient")
    @ConditionalOnMissingBean(SqsClient.class)
    public SqsClient sqsClient(StaticCredentialsProvider awsStaticCredentialsProvider, AmazonSqsProperties amazonProperties) {
        return SqsClient.builder().credentialsProvider(awsStaticCredentialsProvider)
            .region(getRegion(amazonProperties.getRegion()))
            .build();
    }

    /**
     * the region must be available
     * you find it on amazon sqs console
     *
     * @return {@link Region}
     */
    public Region getRegion(String region) {
        return Region.of(region);
    }
}
