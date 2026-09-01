package com.abi.exception;

/**
 * @author Pandiyan
 *
 */
public class ClaimBookException extends Exception {

	private static final long serialVersionUID = 707369711049269074L;

	public ClaimBookException() {
	}

	/**
	 * @param message
	 */
	public ClaimBookException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ClaimBookException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ClaimBookException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public ClaimBookException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
