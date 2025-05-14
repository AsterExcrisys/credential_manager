package com.asterexcrisys.acm.services.console.validators;

import java.nio.file.Files;
import java.nio.file.Paths;

@SuppressWarnings("unused")
public final class PathValidator implements Validator {

    public boolean validate(String data) {
        if (data == null || data.isBlank()) {
            return false;
        }
        return Files.exists(Paths.get(data));
    }

}