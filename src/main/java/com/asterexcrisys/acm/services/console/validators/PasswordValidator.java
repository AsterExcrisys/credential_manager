package com.asterexcrisys.acm.services.console.validators;

import java.util.regex.Pattern;

@SuppressWarnings("unused")
public final class PasswordValidator implements Validator {

    private static final Pattern PASSWORD_PATTERN;

    static {
        PASSWORD_PATTERN = Pattern.compile("[a-zA-Z0-9\\-+.*^$;,!£%&=@#_<>]+");
    }

    public boolean validate(String data) {
        if (data == null || data.isBlank()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(data).matches();
    }

}