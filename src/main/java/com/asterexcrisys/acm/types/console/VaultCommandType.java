package com.asterexcrisys.acm.types.console;

import com.asterexcrisys.acm.services.console.validators.GenericValidator;
import com.asterexcrisys.acm.services.console.validators.PasswordValidator;
import com.asterexcrisys.acm.services.console.validators.Validator;
import java.util.Optional;

@SuppressWarnings("unused")
public enum VaultCommandType implements CommandType {

    GET(
            "-gv",
            "getVault", 2,
            new Class[]{String.class, String.class},
            new Validator[]{new GenericValidator(), new PasswordValidator()}
    ),
    GET_ALL(
            "-gav",
            "getAllVaults", 0,
            new Class[]{},
            new Validator[]{}
    ),
    ADD(
            "-av",
            "addVault",
            2,
            new Class[]{String.class, String.class},
            new Validator[]{new GenericValidator(), new PasswordValidator()}
    ),
    REMOVE(
            "-rv",
            "removeVault",
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

    public static Optional<VaultCommandType> fromValue(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        for (VaultCommandType type : VaultCommandType.values()) {
            if (type.shortName().equalsIgnoreCase(value) || type.longName().equalsIgnoreCase(value)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

}