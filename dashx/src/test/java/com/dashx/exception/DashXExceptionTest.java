package com.dashx.exception;

import com.netflix.graphql.dgs.client.GraphQLError;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashXExceptionTest {

    @Test
    void testDashXExceptionWithMessage() {
        String message = "Test error message";
        DashXException exception = new DashXException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testDashXExceptionWithMessageAndCause() {
        String message = "Test error message";
        Throwable cause = new RuntimeException("Cause");
        DashXException exception = new DashXException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testDashXExceptionWithCause() {
        Throwable cause = new RuntimeException("Cause");
        DashXException exception = new DashXException(cause);

        assertEquals(cause, exception.getCause());
    }

    @Test
    void testDashXValidationException() {
        String message = "Validation failed";
        DashXValidationException exception = new DashXValidationException(message);

        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof DashXException);
    }

    @Test
    void testDashXConfigurationException() {
        String message = "Configuration error";
        DashXConfigurationException exception = new DashXConfigurationException(message);

        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof DashXException);
    }

    @Test
    void testDashXGraphQLExceptionWithErrors() {
        GraphQLError error1 = mock(GraphQLError.class);
        when(error1.getMessage()).thenReturn("Error 1");

        GraphQLError error2 = mock(GraphQLError.class);
        when(error2.getMessage()).thenReturn("Error 2");

        List<GraphQLError> errors = Arrays.asList(error1, error2);
        DashXGraphQLException exception = new DashXGraphQLException(errors);

        assertEquals(errors, exception.getErrors());
        assertTrue(exception.getMessage().contains("Error 1"));
        assertTrue(exception.getMessage().contains("Error 2"));
        assertTrue(exception instanceof DashXException);
    }

    @Test
    void testDashXGraphQLExceptionWithSingleError() {
        GraphQLError error = mock(GraphQLError.class);
        when(error.getMessage()).thenReturn("Single error");

        List<GraphQLError> errors = Collections.singletonList(error);
        DashXGraphQLException exception = new DashXGraphQLException(errors);

        assertEquals("GraphQL error: Single error", exception.getMessage());
        assertEquals(errors, exception.getErrors());
    }

    @Test
    void testDashXGraphQLExceptionWithEmptyErrors() {
        List<GraphQLError> errors = Collections.emptyList();
        DashXGraphQLException exception = new DashXGraphQLException(errors);

        assertEquals("GraphQL error occurred", exception.getMessage());
        assertEquals(errors, exception.getErrors());
    }

    @Test
    void testDashXGraphQLExceptionWithCustomMessage() {
        GraphQLError error = mock(GraphQLError.class);
        List<GraphQLError> errors = Collections.singletonList(error);
        String customMessage = "Custom error message";

        DashXGraphQLException exception = new DashXGraphQLException(customMessage, errors);

        assertEquals(customMessage, exception.getMessage());
        assertEquals(errors, exception.getErrors());
    }
}
