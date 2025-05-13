package com.asterexcrisys.acm.types.console;

import com.asterexcrisys.acm.services.console.Validator;
import java.util.Optional;

@SuppressWarnings("unused")
public sealed interface CommandType permits GenericCommandType, VaultCommandType, CredentialCommandType {

    boolean is(String command);

    String shortName();

    String longName();

    int argumentCount();

    Class<?>[] argumentTypes();

    Validator[] argumentValidators();

    static Optional<CommandType> fromValue(String value) {
        return Optional.empty();
    }

}