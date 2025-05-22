package com.asterexcrisys.acm.types.utility;

@SuppressWarnings("unused")
public sealed interface Outcome<V, E> permits Value, Error {

    V getValue();

    E getError();

    static <V, E> Outcome<V, E> ofValue(V value) {
        return Value.of(value);
    }

    static <V, E> Outcome<V, E> ofError(E error) {
        return Error.of(error);
    }

}