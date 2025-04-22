package com.dashx.springboot;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dashx")
public class DashXProperties {
    private String baseUrl = "https://api.dashx.com/graphql";
    private String publicKey;
    private String privateKey;
    private String targetEnvironment;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getPublicKey() {
        if (publicKey != null && publicKey.isEmpty()) {
            return null;
        }

        return publicKey;
    }

    public String getPrivateKey() {
        if (privateKey != null && privateKey.isEmpty()) {
            return null;
        }

        return privateKey;
    }

    public String getTargetEnvironment() {
        if (targetEnvironment != null && targetEnvironment.isEmpty()) {
            return null;
        }

        return targetEnvironment;
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
}
