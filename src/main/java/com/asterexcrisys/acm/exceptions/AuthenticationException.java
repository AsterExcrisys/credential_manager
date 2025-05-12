package com.asterexcrisys.acm.exceptions;

@SuppressWarnings("unused")
public class AuthenticationException extends RuntimeException {

    public AuthenticationException() {
        super();
    }

    public AuthenticationException(String message) {
        super(message);
    }

}