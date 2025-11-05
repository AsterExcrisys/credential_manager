package com.asterexcrisys.acm.types.console;

import com.asterexcrisys.acm.services.console.validators.*;
import com.asterexcrisys.acm.types.encryption.VaultType;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("unused")
public enum GenericNonInteractiveCommandType implements CommandType {

    SHOW_INFORMATION(
           "-si",
            "--show-information",
            0,
            new Class[] {},
            new Validator[] {}
    ),
    IMPORT_VAULT(
            "-iv",
            "--import-vault",
            4,
            new Class[] {String.class, String.class, String.class, String.class},
            new Validator[] {new PathValidator(), new EnumerationValidator(Set.of(VaultType.names()), false), new GenericValidator(), new PasswordValidator()}
    ),
    EXPORT_VAULT(
            "-ev",
            "--export-vault",
            4,
            new Class[] {String.class, String.class, String.class},
            new Validator[] {new PathValidator(), new EnumerationValidator(Set.of(VaultType.names()), false), new GenericValidator(), new PasswordValidator()}
    ),
    CLEAR_CONTEXT(
            "-cc",
            "--clear-context",
            0,
            new Class[] {},
            new Validator[] {}
    ),
    WIPE_DATA(
         "-wd",
         "--wipe-data",
         1,
         new Class[] {String.class},
         new Validator[] {new GenericValidator()}
    ),
    TEST_GIVEN_PASSWORD(
            "-tgv",
            "--test-given-password",
            1,
            new Class[] {String.class},
            new Validator[] {new PasswordValidator()}
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

    public static boolean has(String command) {
        return fromValue(command).isPresent();
    }

    public static Optional<GenericNonInteractiveCommandType> fromValue(String value) {
        if (value == null || value.isBlank()) {
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