package com.asterexcrisys.acm.types.utility;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unused")
public class Result<S, F> {

    private final boolean isSuccess;
    private final S value;
    private final F error;

    private Result(boolean isSuccess, S value, F error) throws NullPointerException {
        this.isSuccess = isSuccess;
        if (this.isSuccess) {
            this.value = Objects.requireNonNull(value);
            this.error = error;
        } else {
            this.value = value;
            this.error = Objects.requireNonNull(error);
        }
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public boolean isFailure() {
        return !isSuccess;
    }

    public Optional<S> getValue() {
        if (!isSuccess) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    public Optional<F> getError() {
        if (isSuccess) {
            return Optional.empty();
        }
        return Optional.of(error);
    }

    public static <S, F> Result<S, F> success(S value) throws NullPointerException {
        return new Result<>(true, value, null);
    }

    public static <S, F> Result<S, F> failure(F error) throws NullPointerException {
        return new Result<>(false, null, error);
    }

}