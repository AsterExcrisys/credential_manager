package com.asterexcrisys.acm.services.console.validators;

@SuppressWarnings("unused")
public sealed interface Validator permits GenericValidator, PasswordValidator, IntegerNumberValidator, FloatNumberValidator, EnumerationValidator, DateValidator, PathValidator {

    boolean validate(String data);

}