package com.asterexcrisys.acm.services.console.validators;

@SuppressWarnings("unused")
public sealed interface Validator permits GenericValidator, IntegerNumberValidator, FloatNumberValidator, PasswordValidator, PathValidator {

    boolean validate(String data);

}