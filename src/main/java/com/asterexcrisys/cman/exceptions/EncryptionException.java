package com.asterexcrisys.cman.exceptions;

@SuppressWarnings("unused")
public class EncryptionException extends RuntimeException {

    public EncryptionException() {
        super();
    }

    public EncryptionException(String message) {
        super(message);
    }

}