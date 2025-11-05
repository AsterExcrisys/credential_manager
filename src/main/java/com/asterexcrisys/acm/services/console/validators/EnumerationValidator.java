package com.asterexcrisys.acm.services.console.validators;

import java.util.Objects;
import java.util.Set;

@SuppressWarnings("unused")
public final class EnumerationValidator implements Validator {

    private final Set<String> values;
    private final boolean isLowercase;

    public EnumerationValidator(Set<String> values, boolean isLowercase) {
        this.values = Objects.requireNonNull(values);
        this.isLowercase = isLowercase;
    }

    public boolean validate(String data) {
        if (data == null || data.isBlank()) {
            return false;
        }
        if (isLowercase) {
            return values.contains(data.toLowerCase());
        }
        return values.contains(data.toUpperCase());
    }

}