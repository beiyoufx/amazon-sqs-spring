package com.github.sqs.spring.autoconfiguration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 无羡 2020-04-29
 */
@ConfigurationProperties("aws")
public class AmazonSqsProperties {
    /**
     * aws access key id
     */
    private String accessKeyId;

    /**
     * aws secret access key
     */
    private String secretAccessKey;

    /**
     * aws service region
     */
    private String region;
    private Sqs sqs;

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Sqs getSqs() {
        return sqs;
    }

    public void setSqs(Sqs sqs) {
        this.sqs = sqs;
    }

    public static class Sqs {
        /**
         * enable listener to listening messages
         */
        private boolean enableListening;
        /**
         * aws endpoint
         * example:
         * https://sqs.eu-west-1.amazonaws.com
         * https://sqs.cn-northwest-1.amazonaws.com.cn
         */
        private String endpoint;

        /**
         * account
         * example: 123456789012
         */
        private String account;

        public String getDomain() {
            return endpoint + "/" + account + "/";
        }

        public boolean isEnableListening() {
            return enableListening;
        }

        public void setEnableListening(boolean enableListening) {
            this.enableListening = enableListening;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }
    }
}
