package com.dashx;

import java.util.Objects;

public final class DashXConfig {
    private final String baseUrl;
    private final String publicKey;
    private final String privateKey;
    private final String targetEnvironment;

    private DashXConfig(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.publicKey = builder.publicKey;
        this.privateKey = builder.privateKey;
        this.targetEnvironment = builder.targetEnvironment;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getTargetEnvironment() {
        return targetEnvironment;
    }

    public static class Builder {
        private String baseUrl = "https://api.dashx.com/graphql";
        private String publicKey;
        private String privateKey;
        private String targetEnvironment;

        public Builder() {}

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder publicKey(String publicKey) {
            this.publicKey = publicKey;
            return this;
        }

        public Builder privateKey(String privateKey) {
            this.privateKey = privateKey;
            return this;
        }

        public Builder targetEnvironment(String targetEnvironment) {
            this.targetEnvironment = targetEnvironment;
            return this;
        }

        public DashXConfig build() {
            Objects.requireNonNull(publicKey, "publicKey must not be null");
            Objects.requireNonNull(privateKey, "privateKey must not be null");
            return new DashXConfig(this);
        }
    }
}
