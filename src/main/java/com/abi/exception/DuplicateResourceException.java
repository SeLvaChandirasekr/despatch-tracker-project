package com.abi.exception;

import com.abi.enums.ErrorCode;
import lombok.Getter;

@Getter
public class DuplicateResourceException extends RuntimeException {

    private final ErrorCode errorCode;

    public DuplicateResourceException(final String message) {
        super(message);
        this.errorCode = ErrorCode.DUPLICATE_RESOURCE;
    }
}
