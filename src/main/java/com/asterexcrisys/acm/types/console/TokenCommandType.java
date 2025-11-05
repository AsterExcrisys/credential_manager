package com.asterexcrisys.acm.types.console;

import com.asterexcrisys.acm.services.console.validators.GenericValidator;
import com.asterexcrisys.acm.services.console.validators.Validator;
import java.util.Optional;

@SuppressWarnings("unused")
public enum TokenCommandType implements CommandType {

    GET(
            "-gt",
            "--get-token",
            1,
            new Class[] {String.class},
            new Validator[] {new GenericValidator()}
    ),
    GET_ALL(
            "-gat",
            "--get-all-tokens",
            0,
            new Class[] {},
            new Validator[] {}
    ),
    SET(
            "-st",
            "--set-token",
            1,
            new Class[] {String.class},
            new Validator[] {new GenericValidator()}
    ),
    ADD(
            "-at",
            "--add-token",
            1,
            new Class[] {String.class},
            new Validator[] {new GenericValidator()}
    ),
    REMOVE(
            "-rt",
            "--remove-token",
            1,
            new Class[] {String.class},
            new Validator[] {new GenericValidator()}
    ),
    REMOVE_ALL(
            "-rat",
            "--remove-all-tokens",
            0,
            new Class[] {},
            new Validator[] {}
    );

    private final String shortName;
    private final String longName;
    private final int argumentCount;
    private final Class<?>[] argumentTypes;
    private final Validator[] argumentValidators;

    TokenCommandType(String shortName, String longName, int argumentCount, Class<?>[] argumentTypes, Validator[] argumentValidators) {
        this.shortName = shortName;
        this.longName = longName;
        this.argumentCount = argumentCount;
        this.argumentTypes = argumentTypes;
        this.argumentValidators = argumentValidators;
    }

    public boolean is(String command) {
        return shortName.equalsIgnoreCase(command) || longName.equalsIgnoreCase(command);
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

    public static boolean has(String command) {
        return fromValue(command).isPresent();
    }

    public static Optional<TokenCommandType> fromValue(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        for (TokenCommandType type : TokenCommandType.values()) {
            if (type.shortName().equalsIgnoreCase(value) || type.longName().equalsIgnoreCase(value)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

}