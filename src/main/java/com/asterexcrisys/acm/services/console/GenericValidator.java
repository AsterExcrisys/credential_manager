package com.asterexcrisys.acm.services.console;

import java.util.regex.Pattern;

@SuppressWarnings("unused")
public final class GenericValidator implements Validator {

    public boolean validate(String data) {
        if (data == null || data.isBlank()) {
            return false;
        }
        return Pattern.matches("[@a-zA-Z0-9_\\-+]+", data);
    }

}