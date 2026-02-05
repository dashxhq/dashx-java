package com.dashx.demo.springboot;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<Map<String, Object>> handleCompletionException(
            CompletionException ex, WebRequest request) {

        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return buildErrorResponse(
            cause.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR,
            request.getDescription(false),
            cause.getClass().getSimpleName()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        return buildErrorResponse(
            ex.getMessage(),
            HttpStatus.BAD_REQUEST,
            request.getDescription(false),
            "IllegalArgumentException"
        );
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFoundException(
            NoHandlerFoundException ex, WebRequest request) {

        return buildErrorResponse(
            "Route not found: " + ex.getRequestURL(),
            HttpStatus.NOT_FOUND,
            request.getDescription(false),
            "NoHandlerFoundException"
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex, WebRequest request) {

        return buildErrorResponse(
            ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred",
            HttpStatus.INTERNAL_SERVER_ERROR,
            request.getDescription(false),
            ex.getClass().getSimpleName()
        );
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            String message,
            HttpStatus status,
            String path,
            String exceptionType) {

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("exceptionType", exceptionType);
        errorResponse.put("path", path.replace("uri=", ""));

        return new ResponseEntity<>(errorResponse, status);
    }
}
