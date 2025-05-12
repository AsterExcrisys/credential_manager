package com.asterexcrisys.acm.services.encryption;

import com.asterexcrisys.acm.constants.Hashing;
import com.asterexcrisys.acm.exceptions.DerivationException;
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
public final class KeyEncryptor implements Encryptor {

    private final byte[] salt;
    private final CoreEncryptor encryptor;

    public KeyEncryptor(String password) throws NullPointerException, DerivationException, NoSuchAlgorithmException {
        salt = new byte[Hashing.SALT_SIZE];
        SecureRandom.getInstanceStrong().nextBytes(salt);
        encryptor = new CoreEncryptor(deriveKey(Objects.requireNonNull(password), salt).orElseThrow(DerivationException::new));
    }

    public KeyEncryptor(String password, String sealedSalt) throws NullPointerException, DerivationException {
        this.salt = Base64.getDecoder().decode(Objects.requireNonNull(sealedSalt));
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
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, Hashing.KEY_ITERATION_COUNT, Hashing.KEY_SIZE);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(Hashing.KEY_DERIVATION_ALGORITHM);
            return Optional.ofNullable(secretKeyFactory.generateSecret(keySpec));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            return Optional.empty();
        }
    }

}