package com.asterexcrisys.acm.exceptions;

@SuppressWarnings("unused")
public class EncryptionException extends Exception {

    public EncryptionException() {
        super();
    }

    public EncryptionException(String message) {
        super(message);
    }

    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }

}