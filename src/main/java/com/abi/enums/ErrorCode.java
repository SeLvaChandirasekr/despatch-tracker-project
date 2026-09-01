package com.abi.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Authentication & Security Errors (1000-1099)
    AUTHENTICATION_FAILED("AUTH_1000", "Authentication failed", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("AUTH_1001", "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("AUTH_1002", "JWT token has expired", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("AUTH_1003", "Access denied. Insufficient permissions", HttpStatus.FORBIDDEN),
    REFRESH_TOKEN_EXPIRED("AUTH_1004", "Refresh token has expired or been revoked", HttpStatus.UNAUTHORIZED),

    // Resource Errors (2000-2099)
    RESOURCE_NOT_FOUND("RES_2000", "Requested resource was not found", HttpStatus.NOT_FOUND),
    DUPLICATE_RESOURCE("RES_2001", "Resource already exists", HttpStatus.CONFLICT),
    RESOURCE_LOCKED("RES_2002", "Resource is currently locked or modified concurrently", HttpStatus.CONFLICT),

    // Validation & Request Errors (3000-3099)
    INVALID_INPUT("VAL_3000", "Invalid request body or parameter validation failure", HttpStatus.BAD_REQUEST),
    CONSTRAINT_VIOLATION("VAL_3001", "Database constraint violation", HttpStatus.BAD_REQUEST),
    INVALID_STATE_TRANSITION("VAL_3002", "Illegal business state transition", HttpStatus.UNPROCESSABLE_ENTITY),

    // System & Third-Party Integration Errors (5000-5099)
    INTERNAL_SERVER_ERROR("SYS_5000", "An internal system error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    EXTERNAL_SERVICE_FAILURE("SYS_5001", "External partner service invocation failed", HttpStatus.SERVICE_UNAVAILABLE),
    CIRCUIT_BREAKER_OPEN("SYS_5002", "Circuit breaker is open. Request blocked", HttpStatus.SERVICE_UNAVAILABLE),
    RATE_LIMIT_EXCEEDED("SYS_5003", "Too many requests. Rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ErrorCode(final String code, final String defaultMessage, final HttpStatus httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }
}
