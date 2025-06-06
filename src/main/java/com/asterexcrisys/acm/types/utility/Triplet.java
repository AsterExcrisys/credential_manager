package com.asterexcrisys.acm.types.utility;

@SuppressWarnings("unused")
public record Triplet<F, S, T>(F first, S second, T third) implements Tuple {

    // All necessary methods are generated by default

    public static <F, S, T> Triplet<F, S, T> of(F first, S second, T third) {
        return new Triplet<>(first, second, third);
    }

}