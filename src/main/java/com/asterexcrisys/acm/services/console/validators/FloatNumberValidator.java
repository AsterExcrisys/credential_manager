package com.asterexcrisys.acm.services.console.validators;

import java.util.regex.Pattern;

public final class FloatNumberValidator implements Validator {

    private static final Pattern FLOAT_NUMBER_PATTERN;

    static {
        FLOAT_NUMBER_PATTERN = Pattern.compile("[+-]?[0-9]*[.,]?[0-9]+");
    }

    public boolean validate(String data) {
        if (data == null || data.isBlank()) {
            return false;
        }
        return FLOAT_NUMBER_PATTERN.matcher(data).matches();
    }

}