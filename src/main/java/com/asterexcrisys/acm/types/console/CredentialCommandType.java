package com.asterexcrisys.acm.types.console;

import com.asterexcrisys.acm.services.console.GenericValidator;
import com.asterexcrisys.acm.services.console.PasswordValidator;
import com.asterexcrisys.acm.services.console.Validator;

@SuppressWarnings("unused")
public enum CredentialCommandType implements CommandType {

    GET(
            "-g",
            "get", 1,
            new Class[]{String.class},
            new Validator[]{new GenericValidator()}
    ),
    GET_ALL(
            "-ga",
            "getAll", 0,
            new Class[]{},
            new Validator[]{}
    ),
    SET(
            "-s",
            "set",
            3,
            new Class[]{String.class, String.class, String.class},
            new Validator[]{new GenericValidator(), new GenericValidator(), new PasswordValidator()}
    ),
    ADD(
            "-a",
            "add",
            3,
            new Class[]{String.class, String.class, String.class},
            new Validator[]{new GenericValidator(), new GenericValidator(), new PasswordValidator()}
    ),
    REMOVE(
            "-r",
            "remove",
            1,
            new Class[]{String.class},
            new Validator[]{new GenericValidator()}
    );

    private final String shortName;
    private final String longName;
    private final int argumentCount;
    private final Class<?>[] argumentTypes;
    private final Validator[] argumentValidators;

    CredentialCommandType(String shortName, String longName, int argumentCount, Class<?>[] argumentTypes, Validator[] argumentValidators) {
        this.shortName = shortName;
        this.longName = longName;
        this.argumentCount = argumentCount;
        this.argumentTypes = argumentTypes;
        this.argumentValidators = argumentValidators;
    }

    public String shortName() {
        return shortName;
    }

    public String longName() {
        return longName;
    }

    public int argumentCount() {
        return argumentCount;
    }

    public Class<?>[] argumentTypes() {
        return argumentTypes;
    }

    public Validator[] argumentValidators() {
        return argumentValidators;
    }

}