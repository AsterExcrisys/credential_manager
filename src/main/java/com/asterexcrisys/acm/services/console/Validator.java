package com.asterexcrisys.acm.services.console;

@SuppressWarnings("unused")
public sealed interface Validator permits GenericValidator, PasswordValidator, PathValidator {

    boolean validate(String data);

}