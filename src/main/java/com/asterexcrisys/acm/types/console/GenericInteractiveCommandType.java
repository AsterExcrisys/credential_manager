package com.asterexcrisys.acm.types.console;

import com.asterexcrisys.acm.services.console.validators.GenericValidator;
import com.asterexcrisys.acm.services.console.validators.IntegerNumberValidator;
import com.asterexcrisys.acm.services.console.validators.PathValidator;
import com.asterexcrisys.acm.services.console.validators.Validator;
import java.util.Optional;

@SuppressWarnings("unused")
public enum GenericInteractiveCommandType implements CommandType {

    CURRENT_VAULT(
            "-cv",
            "--current-vault",
            0,
            new Class[] {},
            new Validator[] {}
    ),
    GENERATE_PASSWORD(
            "-gp",
            "--generate-password",
            1,
            new Class[] {Integer.class},
            new Validator[] {new IntegerNumberValidator()}
    ),
    TEST_EXISTING_PASSWORD(
            "-tep",
            "--test-existing-password",
            1,
            new Class[] {String.class},
            new Validator[] {new GenericValidator()}
    ),
    ENCRYPT_TEXT(
            "-et",
            "--encrypt-text",
            2,
            new Class[] {String.class, String.class},
            new Validator[] {new GenericValidator(), new GenericValidator()}
    ),
    DECRYPT_TEXT(
            "-dt",
            "--decrypt-text",
            2,
            new Class[] {String.class, String.class},
            new Validator[] {new GenericValidator(), new GenericValidator()}
    ),
    ENCRYPT_FILE(
            "-ef",
            "--encrypt-file",
            2,
            new Class[] {String.class, String.class},
            new Validator[] {new GenericValidator(), new PathValidator()}
    ),
    DECRYPT_FILE(
            "-df",
            "--decrypt-file",
            2,
            new Class[] {String.class, String.class},
            new Validator[] {new GenericValidator(), new PathValidator()}
    ),
    QUIT_SHELL(
            "-qs",
            "--quit-shell",
            0,
            new Class[] {},
            new Validator[] {}
    ),
    EXIT_SHELL(
            "-es",
            "--exit-shell",
            0,
            new Class[] {},
            new Validator[] {}
    );

    private final String shortName;
    private final String longName;
    private final int argumentCount;
    private final Class<?>[] argumentTypes;
    private final Validator[] argumentValidators;

    GenericInteractiveCommandType(String shortName, String longName, int argumentCount, Class<?>[] argumentTypes, Validator[] argumentValidators) {
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

    public static Optional<GenericInteractiveCommandType> fromValue(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        for (GenericInteractiveCommandType type : GenericInteractiveCommandType.values()) {
            if (type.shortName().equalsIgnoreCase(value) || type.longName().equalsIgnoreCase(value)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

}