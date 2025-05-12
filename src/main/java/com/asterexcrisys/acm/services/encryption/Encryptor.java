package com.asterexcrisys.acm.services.encryption;

import java.util.Optional;

@SuppressWarnings("unused")
public sealed interface Encryptor permits CoreEncryptor, KeyEncryptor, CredentialEncryptor {

    Optional<String> encrypt(String data);

    Optional<String> decrypt(String data);

}