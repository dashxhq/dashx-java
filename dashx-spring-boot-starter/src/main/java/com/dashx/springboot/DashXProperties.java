package com.dashx.springboot;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for DashX client.
 * All timeout values are in milliseconds.
 */
@ConfigurationProperties(prefix = "dashx")
public class DashXProperties {
    private String baseUrl = com.dashx.Constants.DEFAULT_BASE_URL;
    private String publicKey;
    private String privateKey;
    private String targetEnvironment;

    /** Connection timeout in milliseconds (default: 10000) */
    private Integer connectionTimeout = 10000;

    /** Response timeout in milliseconds (default: 30000) */
    private Integer responseTimeout = 30000;

    /** Maximum number of connections (default: 500) */
    private Integer maxConnections = 500;

    /** Max idle time for connections in milliseconds (default: 20000) */
    private Integer maxIdleTime = 20000;

    private static String nullIfEmpty(String s) {
        return (s != null && s.isEmpty()) ? null : s;
    }

    public String getBaseUrl() {
        return nullIfEmpty(baseUrl);
    }

    public String getPublicKey() {
        return nullIfEmpty(publicKey);
    }

    public String getPrivateKey() {
        return nullIfEmpty(privateKey);
    }

    public String getTargetEnvironment() {
        return nullIfEmpty(targetEnvironment);
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public Integer getResponseTimeout() {
        return responseTimeout;
    }

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public Integer getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public void setTargetEnvironment(String targetEnvironment) {
        this.targetEnvironment = targetEnvironment;
    }

    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public void setResponseTimeout(Integer responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }

    public void setMaxIdleTime(Integer maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }
}
