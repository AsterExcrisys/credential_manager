package com.asterexcrisys.acm.services.encryption;

import java.util.Optional;

@SuppressWarnings("unused")
public sealed interface Encryptor permits CoreEncryptor, KeyEncryptor, GenericEncryptor {

    Optional<String> encrypt(String data);

    Optional<String> encrypt(byte[] data);

    Optional<String> decrypt(String data);

    Optional<String> decrypt(byte[] data);

}