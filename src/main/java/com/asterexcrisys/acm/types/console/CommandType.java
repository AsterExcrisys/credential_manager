package com.asterexcrisys.acm.types.console;

import com.asterexcrisys.acm.services.console.Validator;

@SuppressWarnings("unused")
public sealed interface CommandType permits VaultCommandType, CredentialCommandType {

    String shortName();

    String longName();

    int argumentCount();

    Class<?>[] argumentTypes();

    Validator[] argumentValidators();

}