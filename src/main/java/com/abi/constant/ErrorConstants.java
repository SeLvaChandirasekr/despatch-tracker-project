package com.abi.constant;

public final class ErrorConstants {

    private ErrorConstants() {
        // Prevent instantiation
    }

    public static final String ERR_VALIDATION_FAILED = "One or more validation constraints failed for the input payload";
    public static final String ERR_ACCESS_DENIED = "You do not have permission to access or modify this resource";
    public static final String ERR_INTERNAL_SERVER = "An unexpected error occurred. Please contact system support";
    public static final String ERR_CONCURRENT_UPDATE = "The entity was updated by another transaction. Please retry";
}
