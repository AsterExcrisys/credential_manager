package com.asterexcrisys.acm.exceptions;

@SuppressWarnings("unused")
public class DerivationException extends Exception {

    public DerivationException() {
        super();
    }

    public DerivationException(String message) {
        super(message);
    }

    public DerivationException(String message, Throwable cause) {
        super(message, cause);
    }

}
