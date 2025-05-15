package com.asterexcrisys.acm.types.utility;

@SuppressWarnings("unused")
public sealed interface Tuple permits Pair, Triple {

    static Tuple of(Object... parameters) {
        return null;
    }

}