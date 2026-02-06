package com.dashx;

import com.dashx.exception.DashXConfigurationException;
import com.dashx.exception.DashXValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class DashXValidationTest {

    private DashX dashx;

    @BeforeEach
    void setUp() {
        DashX.resetInstances();
        dashx = DashX.getInstance("test-validation");

        // Configure with valid settings
        DashXConfig config = new DashXConfig.Builder()
                .publicKey("test-public-key")
                .privateKey("test-private-key")
                .targetEnvironment("test")
                .build();

        dashx.configure(config);
    }

    @AfterEach
    void tearDown() {
        DashX.resetInstances();
    }

    @Test
    void testConfigureWithNullConfigThrowsException() {
        DashX instance = DashX.getInstance("test-null-config");

        assertThrows(DashXValidationException.class, () -> {
            instance.configure(null);
        });
    }

    @Test
    void testUnconfiguredClientThrowsException() {
        DashX unconfigured = DashX.getInstance("test-unconfigured");

        Map<String, Object> options = new HashMap<>();
        options.put("email", "test@example.com");

        assertThrows(DashXConfigurationException.class, () -> {
            unconfigured.identify(options);
        });
    }

    @Test
    void testIdentifyWithNullOptionsThrowsException() {
        CompletableFuture<?> future = dashx.identify(null);

        ExecutionException exception = assertThrows(ExecutionException.class, () -> {
            future.get();
        });

        assertTrue(exception.getCause() instanceof DashXValidationException);
        assertTrue(exception.getCause().getMessage().contains("cannot be called with null"));
    }

    @Test
    void testTrackWithNullEventThrowsException() {
        CompletableFuture<?> future = dashx.track(null, "user123");

        ExecutionException exception = assertThrows(ExecutionException.class, () -> {
            future.get();
        });

        assertTrue(exception.getCause() instanceof DashXValidationException);
        assertTrue(exception.getCause().getMessage().contains("Event name cannot be null"));
    }

    @Test
    void testTrackWithEmptyEventThrowsException() {
        CompletableFuture<?> future = dashx.track("", "user123");

        ExecutionException exception = assertThrows(ExecutionException.class, () -> {
            future.get();
        });

        assertTrue(exception.getCause() instanceof DashXValidationException);
        assertTrue(exception.getCause().getMessage().contains("Event name cannot be null or empty"));
    }

    @Test
    void testTrackWithWhitespaceEventThrowsException() {
        CompletableFuture<?> future = dashx.track("   ", "user123");

        ExecutionException exception = assertThrows(ExecutionException.class, () -> {
            future.get();
        });

        assertTrue(exception.getCause() instanceof DashXValidationException);
    }

    @Test
    void testGetAssetWithNullIdThrowsException() {
        CompletableFuture<?> future = dashx.getAsset(null);

        ExecutionException exception = assertThrows(ExecutionException.class, () -> {
            future.get();
        });

        assertTrue(exception.getCause() instanceof DashXValidationException);
        assertTrue(exception.getCause().getMessage().contains("Asset ID cannot be null"));
    }

    @Test
    void testGetAssetWithEmptyIdThrowsException() {
        CompletableFuture<?> future = dashx.getAsset("");

        ExecutionException exception = assertThrows(ExecutionException.class, () -> {
            future.get();
        });

        assertTrue(exception.getCause() instanceof DashXValidationException);
    }

    @Test
    void testSearchRecordsWithNullResourceThrowsException() {
        CompletableFuture<?> future = dashx.searchRecords(null);

        ExecutionException exception = assertThrows(ExecutionException.class, () -> {
            future.get();
        });

        assertTrue(exception.getCause() instanceof DashXValidationException);
        assertTrue(exception.getCause().getMessage().contains("Resource cannot be null"));
    }

    @Test
    void testSearchRecordsWithEmptyResourceThrowsException() {
        CompletableFuture<?> future = dashx.searchRecords("");

        ExecutionException exception = assertThrows(ExecutionException.class, () -> {
            future.get();
        });

        assertTrue(exception.getCause() instanceof DashXValidationException);
    }

    @Test
    void testIdentifyWithValidOptionsDoesNotThrow() {
        Map<String, Object> options = new HashMap<>();
        options.put("email", "test@example.com");

        // Should not throw exception - will fail later due to no actual backend
        CompletableFuture<?> future = dashx.identify(options);
        assertNotNull(future);
    }

    @Test
    void testSingletonPatternReturnsSameInstance() {
        DashX instance1 = DashX.getInstance("singleton-test");
        DashX instance2 = DashX.getInstance("singleton-test");

        assertSame(instance1, instance2);
    }

    @Test
    void testSingletonPatternReturnsDifferentInstancesForDifferentNames() {
        DashX instance1 = DashX.getInstance("test1");
        DashX instance2 = DashX.getInstance("test2");

        assertNotSame(instance1, instance2);
    }

    @Test
    void testGetDefaultInstance() {
        DashX defaultInstance = DashX.getInstance();
        assertNotNull(defaultInstance);
        assertEquals("default", defaultInstance.getInstanceName());
    }

    @Test
    void testRemoveInstance() {
        DashX instance1 = DashX.getInstance("removable");
        DashX.removeInstance("removable");
        DashX instance2 = DashX.getInstance("removable");

        assertNotSame(instance1, instance2);
    }
}
