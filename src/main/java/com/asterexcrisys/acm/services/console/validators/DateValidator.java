package com.asterexcrisys.acm.services.console.validators;

import java.time.Instant;
import java.time.format.DateTimeParseException;

@SuppressWarnings("unused")
public final class DateValidator implements Validator {

    public boolean validate(String data) {
        if (data == null || data.isBlank()) {
            return false;
        }
        try {
            Instant.parse(data);
            return true;
        } catch (DateTimeParseException ignored) {
            return false;
        }
    }

}