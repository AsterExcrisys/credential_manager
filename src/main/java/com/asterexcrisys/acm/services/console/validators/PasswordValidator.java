package com.asterexcrisys.acm.services.console.validators;

import java.util.regex.Pattern;

@SuppressWarnings("unused")
public final class PasswordValidator implements Validator {

    public boolean validate(String data) {
        if (data == null || data.isBlank()) {
            return false;
        }
        return Pattern.matches("[a-zA-Z0-9\\-+.*^$;,!£%&=@#_<>]+", data);
    }

}