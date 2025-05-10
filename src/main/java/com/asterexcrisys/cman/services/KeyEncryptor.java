package com.asterexcrisys.cman.services;

import com.asterexcrisys.cman.exceptions.DerivationException;
import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unused")
public class KeyEncryptor implements Encryptor {

    private final byte[] salt;
    private final CoreEncryptor encryptor;

    public KeyEncryptor(String password) throws DerivationException, NoSuchAlgorithmException {
        salt = new byte[16];
        SecureRandom.getInstanceStrong().nextBytes(salt);
        encryptor = new CoreEncryptor(deriveKey(Objects.requireNonNull(password), salt).orElseThrow(DerivationException::new));
    }

    public KeyEncryptor(String password, byte[] salt) throws DerivationException {
        this.salt = Objects.requireNonNull(salt);
        encryptor = new CoreEncryptor(deriveKey(Objects.requireNonNull(password), this.salt).orElseThrow(DerivationException::new));
    }

    public String getSealedSalt() {
        return Base64.getEncoder().encodeToString(salt);
    }

    public String getAlgorithm() {
        return encryptor.getAlgorithm();
    }

    public Optional<String> encrypt(String data) {
        return encryptor.encrypt(data);
    }

    public Optional<String> decrypt(String data) {
        return encryptor.decrypt(data);
    }

    private static Optional<SecretKey> deriveKey(String password, byte[] salt) {
        try {
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 1000000, 256);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return Optional.ofNullable(secretKeyFactory.generateSecret(keySpec));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            return Optional.empty();
        }
    }

}