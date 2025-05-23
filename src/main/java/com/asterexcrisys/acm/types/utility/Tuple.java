package com.asterexcrisys.acm.types.utility;

@SuppressWarnings("unused")
public sealed interface Tuple permits Pair, Triplet {

    static Tuple of(Object... parameters) {
        return null;
    }

}