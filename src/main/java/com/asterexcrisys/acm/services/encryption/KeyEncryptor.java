package com.asterexcrisys.acm.services.encryption;

import com.asterexcrisys.acm.constants.EncryptionConstants;
import com.asterexcrisys.acm.constants.HashingConstants;
import com.asterexcrisys.acm.exceptions.DerivationException;
import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class KeyEncryptor implements Encryptor {

    private static final Logger LOGGER = Logger.getLogger(KeyEncryptor.class.getName());
    private final byte[] salt;
    private final CoreEncryptor encryptor;

    public KeyEncryptor(String password) throws NullPointerException, DerivationException, NoSuchAlgorithmException {
        salt = new byte[HashingConstants.SALT_SIZE];
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

    public static Optional<SecretKey> deriveKey(String password, byte[] salt) {
        if (password == null || password.isBlank()) {
            return Optional.empty();
        }
        if (salt == null || salt.length != HashingConstants.SALT_SIZE) {
            return Optional.empty();
        }
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray(), salt, HashingConstants.KEY_ITERATION_COUNT, HashingConstants.KEY_SIZE);
        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(HashingConstants.KEY_DERIVATION_ALGORITHM);
            return Optional.of(new SecretKeySpec(
                    secretKeyFactory.generateSecret(pbeKeySpec).getEncoded(),
                    EncryptionConstants.KEY_GENERATION_ALGORITHM
            ));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOGGER.severe("Error deriving key: " + e.getMessage());
            return Optional.empty();
        } finally {
            pbeKeySpec.clearPassword();
        }
    }

}