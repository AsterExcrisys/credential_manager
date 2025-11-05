package com.asterexcrisys.acm.services.console.validators;

import java.util.regex.Pattern;

@SuppressWarnings("unused")
public final class GenericValidator implements Validator {

    private static final Pattern GENERIC_PATTERN;

    static {
        GENERIC_PATTERN = Pattern.compile("[@a-zA-Z0-9_\\-+.=\\\\/]+");
    }

    public boolean validate(String data) {
        if (data == null || data.isBlank()) {
            return false;
        }
        return GENERIC_PATTERN.matcher(data).matches();
    }

}