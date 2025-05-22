package com.asterexcrisys.acm.types.console;

import com.asterexcrisys.acm.services.console.validators.Validator;
import java.util.Optional;

@SuppressWarnings("unused")
public sealed interface CommandType permits GenericNonInteractiveCommandType, GenericInteractiveCommandType, VaultCommandType, CredentialCommandType {

    boolean is(String command);

    String shortName();

    String longName();

    int argumentCount();

    Class<?>[] argumentTypes();

    Validator[] argumentValidators();

    static boolean has(String command) {
        return false;
    }

    static Optional<CommandType> fromValue(String value) {
        return Optional.empty();
    }

}