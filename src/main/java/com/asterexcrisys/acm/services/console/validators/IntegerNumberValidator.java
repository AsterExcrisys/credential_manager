package com.asterexcrisys.acm.services.console.validators;

import java.util.regex.Pattern;

public final class IntegerNumberValidator implements Validator {

    private static final Pattern INTEGER_NUMBER_PATTERN;

    static {
        INTEGER_NUMBER_PATTERN = Pattern.compile("[0-9]+");
    }

    public boolean validate(String data) {
        if (data == null || data.isBlank()) {
            return false;
        }
        return INTEGER_NUMBER_PATTERN.matcher(data).matches();
    }

}