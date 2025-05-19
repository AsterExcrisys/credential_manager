package com.asterexcrisys.acm.types.console;

import com.asterexcrisys.acm.services.console.validators.GenericValidator;
import com.asterexcrisys.acm.services.console.validators.PasswordValidator;
import com.asterexcrisys.acm.services.console.validators.PathValidator;
import com.asterexcrisys.acm.services.console.validators.Validator;
import java.util.Optional;

@SuppressWarnings("unused")
public enum GenericNonInteractiveCommandType implements CommandType {

    IMPORT(
            "-iv",
            "importVault",
            3,
            new Class[]{String.class, String.class, String.class},
            new Validator[]{new PathValidator(), new GenericValidator(), new PasswordValidator()}
    ),
    EXPORT(
            "-ev",
            "exportVault",
            3,
            new Class[]{String.class, String.class, String.class},
            new Validator[]{new PathValidator(), new GenericValidator(), new PasswordValidator()}
    ),
    TEST_GIVEN(
            "-tgv",
            "testGivenPassword",
            1,
            new Class[]{String.class},
            new Validator[]{new PasswordValidator()}
    );

    private final String shortName;
    private final String longName;
    private final int argumentCount;
    private final Class<?>[] argumentTypes;
    private final Validator[] argumentValidators;

    GenericNonInteractiveCommandType(String shortName, String longName, int argumentCount, Class<?>[] argumentTypes, Validator[] argumentValidators) {
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

    public static Optional<GenericNonInteractiveCommandType> fromValue(String value) {
        if (value == null) {
            return Optional.empty();
        }
        for (GenericNonInteractiveCommandType type : GenericNonInteractiveCommandType.values()) {
            if (type.shortName().equalsIgnoreCase(value) || type.longName().equalsIgnoreCase(value)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

}