package com.abi.exception;

import com.abi.enums.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidCredentialsException extends RuntimeException {

    private final ErrorCode errorCode;

    public InvalidCredentialsException() {
        super(ErrorCode.AUTHENTICATION_FAILED.getDefaultMessage());
        this.errorCode = ErrorCode.AUTHENTICATION_FAILED;
    }
}
