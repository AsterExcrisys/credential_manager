package com.asterexcrisys.acm.types.console;

import com.asterexcrisys.acm.services.console.GenericValidator;
import com.asterexcrisys.acm.services.console.IntegerNumberValidator;
import com.asterexcrisys.acm.services.console.PasswordValidator;
import com.asterexcrisys.acm.services.console.Validator;
import java.util.Optional;

@SuppressWarnings("unused")
public enum GenericCommandType implements CommandType {

    CURRENT_VAULT(
            "-c",
            "currentVault",
            0,
            new Class[]{},
            new Validator[]{}
    ),
    GENERATE_PASSWORD(
            "-gp",
            "generatePassword",
            1,
            new Class[]{Integer.class},
            new Validator[]{new IntegerNumberValidator()}
    ),
    TEST_GIVEN(
            "-tg",
            "testGiven",
            1,
            new Class[]{String.class},
            new Validator[]{new PasswordValidator()}
    ),
    TEST_EXISTING(
            "-te",
            "testExisting",
            1,
            new Class[]{String.class},
            new Validator[]{new GenericValidator()}
    ),
    QUIT(
            "-q",
            "quit",
            0,
            new Class[]{},
            new Validator[]{}
    );

    private final String shortName;
    private final String longName;
    private final int argumentCount;
    private final Class<?>[] argumentTypes;
    private final Validator[] argumentValidators;

    GenericCommandType(String shortName, String longName, int argumentCount, Class<?>[] argumentTypes, Validator[] argumentValidators) {
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

    public static Optional<GenericCommandType> fromValue(String value) {
        if (value == null) {
            return Optional.empty();
        }
        for (GenericCommandType type : GenericCommandType.values()) {
            if (type.shortName().equalsIgnoreCase(value) || type.longName().equalsIgnoreCase(value)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

}