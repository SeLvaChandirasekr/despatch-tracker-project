package com.abi.exception;

import com.abi.enums.ErrorCode;
import com.abi.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(final InvalidCredentialsException ex,
                                                                    final HttpServletRequest request) {
        return buildResponse(ex.getErrorCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(final DuplicateResourceException ex,
                                                                   final HttpServletRequest request) {
        return buildResponse(ex.getErrorCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(final ValidationException ex,
                                                             final HttpServletRequest request) {
        return buildResponse(ErrorCode.INVALID_INPUT, ex.getMessage(), request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(final BadCredentialsException ex,
                                                                final HttpServletRequest request) {
        return buildResponse(ErrorCode.AUTHENTICATION_FAILED, ErrorCode.AUTHENTICATION_FAILED.getDefaultMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(final MethodArgumentNotValidException ex,
                                                            final HttpServletRequest request) {
        final String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return buildResponse(ErrorCode.INVALID_INPUT, message, request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(final ErrorCode errorCode, final String message,
                                                          final HttpServletRequest request) {
        log.warn("Request to [{}] failed with [{}]: {}", request.getRequestURI(), errorCode.getCode(), message);

        final ErrorResponse body = ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(message)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
    }
}
