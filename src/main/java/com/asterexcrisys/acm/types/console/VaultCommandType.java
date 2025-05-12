package com.asterexcrisys.acm.types.console;

import com.asterexcrisys.acm.services.console.GenericValidator;
import com.asterexcrisys.acm.services.console.PasswordValidator;
import com.asterexcrisys.acm.services.console.PathValidator;
import com.asterexcrisys.acm.services.console.Validator;

public enum VaultCommandType implements CommandType {

    GET(
            "-g",
            "get", 2,
            new Class[]{String.class, String.class},
            new Validator[]{new GenericValidator(), new PasswordValidator()}
    ),
    GET_ALL(
            "-ga",
            "getAll", 0,
            new Class[]{},
            new Validator[]{}
    ),
    ADD(
            "-a",
            "add",
            2,
            new Class[]{String.class, String.class},
            new Validator[]{new GenericValidator(), new PasswordValidator()}
    ),
    REMOVE(
            "-r",
            "remove",
            2,
            new Class[]{String.class, String.class},
            new Validator[]{new GenericValidator(), new PasswordValidator()}
    ),
    IMPORT(
            "-i",
            "import",
            2,
            new Class[]{String.class, String.class},
            new Validator[]{new PathValidator(), new PasswordValidator()}
    ),
    EXPORT(
            "-e",
            "export",
            2,
            new Class[]{String.class, String.class},
            new Validator[]{new GenericValidator(), new PasswordValidator()}
    );

    private final String shortName;
    private final String longName;
    private final int argumentCount;
    private final Class<?>[] argumentTypes;
    private final Validator[] argumentValidators;

    VaultCommandType(String shortName, String longName, int argumentCount, Class<?>[] argumentTypes, Validator[] argumentValidators) {
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