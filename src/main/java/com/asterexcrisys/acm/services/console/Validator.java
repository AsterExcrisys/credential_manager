package com.asterexcrisys.acm.services.console;

@SuppressWarnings("unused")
public sealed interface Validator permits GenericValidator, IntegerNumberValidator, FloatNumberValidator, PasswordValidator, PathValidator {

    boolean validate(String data);

}