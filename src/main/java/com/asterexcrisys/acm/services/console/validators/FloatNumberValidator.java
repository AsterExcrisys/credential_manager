package com.asterexcrisys.acm.services.console.validators;

import java.util.regex.Pattern;

public final class FloatNumberValidator implements Validator {

    public boolean validate(String data) {
        if (data == null || data.isBlank()) {
            return false;
        }
        return Pattern.matches("[+-]?[0-9]*[.,]?[0-9]+", data);
    }

}