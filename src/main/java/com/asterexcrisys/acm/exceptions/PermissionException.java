package com.asterexcrisys.acm.exceptions;

@SuppressWarnings("unused")
public class PermissionException extends Exception {

    public PermissionException() {
        super();
    }

    public PermissionException(String message) {
        super(message);
    }

    public PermissionException(String message, Throwable cause) {
        super(message, cause);
    }

}