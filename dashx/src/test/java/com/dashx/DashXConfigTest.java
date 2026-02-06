package com.dashx;

import com.dashx.exception.DashXConfigurationException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DashXConfigTest {

    @Test
    void testBuilderWithRequiredFields() {
        DashXConfig config = new DashXConfig.Builder()
                .publicKey("test-public-key")
                .privateKey("test-private-key")
                .targetEnvironment("test")
                .build();

        assertEquals("test-public-key", config.getPublicKey());
        assertEquals("test-private-key", config.getPrivateKey());
        assertEquals("test", config.getTargetEnvironment());
        assertEquals(Constants.DEFAULT_BASE_URL, config.getBaseUrl());
    }

    @Test
    void testBuilderWithAllFields() {
        DashXConfig config = new DashXConfig.Builder()
                .publicKey("test-public-key")
                .privateKey("test-private-key")
                .targetEnvironment("production")
                .baseUrl("https://custom.api.com/graphql")
                .connectionTimeout(15000)
                .responseTimeout(45000)
                .maxConnections(1000)
                .maxIdleTime(30000)
                .build();

        assertEquals("test-public-key", config.getPublicKey());
        assertEquals("test-private-key", config.getPrivateKey());
        assertEquals("production", config.getTargetEnvironment());
        assertEquals("https://custom.api.com/graphql", config.getBaseUrl());
        assertEquals(15000, config.getConnectionTimeout());
        assertEquals(45000, config.getResponseTimeout());
        assertEquals(1000, config.getMaxConnections());
        assertEquals(30000, config.getMaxIdleTime());
    }

    @Test
    void testBuilderWithDefaults() {
        DashXConfig config = new DashXConfig.Builder()
                .publicKey("test-public-key")
                .privateKey("test-private-key")
                .targetEnvironment("test")
                .build();

        // Check default values
        assertEquals(10000, config.getConnectionTimeout());
        assertEquals(30000, config.getResponseTimeout());
        assertEquals(500, config.getMaxConnections());
        assertEquals(20000, config.getMaxIdleTime());
    }

    @Test
    void testBuilderThrowsExceptionWhenPublicKeyIsNull() {
        assertThrows(DashXConfigurationException.class, () -> {
            new DashXConfig.Builder()
                    .privateKey("test-private-key")
                    .targetEnvironment("test")
                    .build();
        });
    }

    @Test
    void testBuilderThrowsExceptionWhenPrivateKeyIsNull() {
        assertThrows(DashXConfigurationException.class, () -> {
            new DashXConfig.Builder()
                    .publicKey("test-public-key")
                    .targetEnvironment("test")
                    .build();
        });
    }

    @Test
    void testBuilderThrowsExceptionWhenTargetEnvironmentIsNull() {
        assertThrows(DashXConfigurationException.class, () -> {
            new DashXConfig.Builder()
                    .publicKey("test-public-key")
                    .privateKey("test-private-key")
                    .build();
        });
    }

    @Test
    void testBuilderThrowsExceptionForNegativeConnectionTimeout() {
        assertThrows(DashXConfigurationException.class, () -> {
            new DashXConfig.Builder()
                    .publicKey("key")
                    .privateKey("secret")
                    .targetEnvironment("test")
                    .connectionTimeout(-1)
                    .build();
        });
    }

    @Test
    void testBuilderThrowsExceptionForZeroResponseTimeout() {
        assertThrows(DashXConfigurationException.class, () -> {
            new DashXConfig.Builder()
                    .publicKey("key")
                    .privateKey("secret")
                    .targetEnvironment("test")
                    .responseTimeout(0)
                    .build();
        });
    }

    @Test
    void testBuilderThrowsExceptionForNegativeMaxConnections() {
        assertThrows(DashXConfigurationException.class, () -> {
            new DashXConfig.Builder()
                    .publicKey("key")
                    .privateKey("secret")
                    .targetEnvironment("test")
                    .maxConnections(-5)
                    .build();
        });
    }

    @Test
    void testBuilderThrowsExceptionForZeroMaxIdleTime() {
        assertThrows(DashXConfigurationException.class, () -> {
            new DashXConfig.Builder()
                    .publicKey("key")
                    .privateKey("secret")
                    .targetEnvironment("test")
                    .maxIdleTime(0)
                    .build();
        });
    }

    @Test
    void testBuilderChaining() {
        DashXConfig.Builder builder = new DashXConfig.Builder();
        DashXConfig.Builder result = builder
                .publicKey("key")
                .privateKey("secret")
                .targetEnvironment("test");

        assertSame(builder, result.publicKey("key"));
        assertNotNull(result);
    }
}
