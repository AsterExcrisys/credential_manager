package com.asterexcrisys.acm.utility;

import com.asterexcrisys.acm.services.encryption.GenericEncryptor;
import com.asterexcrisys.acm.services.encryption.KeyEncryptor;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Optional;

@SuppressWarnings("unused")
public final class EncryptionUtility {

    private EncryptionUtility() {
        // This class should not be instantiable
    }

    public static Optional<String> generateKey() {
        Optional<SecretKey> key = GenericEncryptor.generateKey();
        if (key.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Base64.getEncoder().encodeToString(key.get().getEncoded()));
    }

    public static Optional<String> deriveKey(String password, byte[] salt) {
        Optional<SecretKey> key = KeyEncryptor.deriveKey(password, salt);
        if (key.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Base64.getEncoder().encodeToString(key.get().getEncoded()));
    }

}