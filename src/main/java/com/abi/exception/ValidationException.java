package com.abi.exception;

/**
 * @author Pandiyan
 *
 */
public class ValidationException extends ClaimBookException {

    private static final long serialVersionUID = -4881954695220122767L;

    public ValidationException() {
    }

    public ValidationException(String message) {
	super(message);
    }

    public ValidationException(Throwable cause) {
	super(cause);
    }

    public ValidationException(String message, Throwable cause) {
	super(message, cause);
    }

    public ValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
	super(message, cause, enableSuppression, writableStackTrace);
    }

}
