package com.asterexcrisys.acm.types.utility;

import java.util.NoSuchElementException;

@SuppressWarnings("unused")
public sealed interface Outcome<V, E> permits Value, Error {

    boolean isSuccess();

    boolean isFailure();

    V getValue() throws NoSuchElementException;

    E getError() throws NoSuchElementException;

    static <V, E> Outcome<V, E> ofValue(V value) {
        return Value.of(value);
    }

    static <V, E> Outcome<V, E> ofError(E error) {
        return Error.of(error);
    }

}