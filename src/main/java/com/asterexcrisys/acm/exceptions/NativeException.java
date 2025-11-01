package com.asterexcrisys.acm.exceptions;

@SuppressWarnings("unused")
public class NativeException extends Exception {

    public NativeException() {
        super();
    }

    public NativeException(String message) {
        super(message);
    }

    public NativeException(String message, Throwable cause) {
        super(message, cause);
    }

}
