package com.dashx;

import com.dashx.exception.DashXConfigurationException;

public final class DashXConfig {
    private final String baseUrl;
    private final String publicKey;
    private final String privateKey;
    private final String targetEnvironment;
    private final Integer connectionTimeout;
    private final Integer responseTimeout;
    private final Integer maxConnections;
    private final Integer maxIdleTime;

    private DashXConfig(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.publicKey = builder.publicKey;
        this.privateKey = builder.privateKey;
        this.targetEnvironment = builder.targetEnvironment;
        this.connectionTimeout = builder.connectionTimeout;
        this.responseTimeout = builder.responseTimeout;
        this.maxConnections = builder.maxConnections;
        this.maxIdleTime = builder.maxIdleTime;
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

    public static class Builder {
        private String baseUrl = Constants.DEFAULT_BASE_URL;
        private String publicKey;
        private String privateKey;
        private String targetEnvironment;
        private Integer connectionTimeout = 10000; // 10 seconds (in milliseconds)
        private Integer responseTimeout = 30000; // 30 seconds (in milliseconds)
        private Integer maxConnections = 500;
        private Integer maxIdleTime = 20000; // 20 seconds (in milliseconds)

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

        /**
         * Sets the connection timeout in milliseconds.
         * @param connectionTimeout timeout in milliseconds (default: 10000)
         */
        public Builder connectionTimeout(Integer connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        /**
         * Sets the response timeout in milliseconds.
         * @param responseTimeout timeout in milliseconds (default: 30000)
         */
        public Builder responseTimeout(Integer responseTimeout) {
            this.responseTimeout = responseTimeout;
            return this;
        }

        public Builder maxConnections(Integer maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        /**
         * Sets the max idle time for connections in milliseconds.
         * @param maxIdleTime idle time in milliseconds (default: 20000)
         */
        public Builder maxIdleTime(Integer maxIdleTime) {
            this.maxIdleTime = maxIdleTime;
            return this;
        }

        public DashXConfig build() {
            if (publicKey == null) {
                throw new DashXConfigurationException("publicKey must not be null");
            }
            if (privateKey == null) {
                throw new DashXConfigurationException("privateKey must not be null");
            }
            if (targetEnvironment == null) {
                throw new DashXConfigurationException("targetEnvironment must not be null");
            }

            if (connectionTimeout != null && connectionTimeout <= 0) {
                throw new DashXConfigurationException("connectionTimeout must be positive, got: " + connectionTimeout);
            }
            if (responseTimeout != null && responseTimeout <= 0) {
                throw new DashXConfigurationException("responseTimeout must be positive, got: " + responseTimeout);
            }
            if (maxConnections != null && maxConnections <= 0) {
                throw new DashXConfigurationException("maxConnections must be positive, got: " + maxConnections);
            }
            if (maxIdleTime != null && maxIdleTime <= 0) {
                throw new DashXConfigurationException("maxIdleTime must be positive, got: " + maxIdleTime);
            }

            return new DashXConfig(this);
        }
    }
}
