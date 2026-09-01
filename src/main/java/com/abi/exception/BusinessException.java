package com.abi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.PRECONDITION_FAILED, reason = "Business Rule Failure")
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 2853224502750891390L;

    public BusinessException(Throwable e) {
	super(e);
    }

    /**
     * @param msg
     */
    public BusinessException(String msg) {
	super(msg);
    }

    public BusinessException(String msg, Throwable e) {
	super(msg, e);
    }
}
