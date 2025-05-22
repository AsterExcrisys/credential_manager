package com.asterexcrisys.acm.services.encryption;

import com.asterexcrisys.acm.constants.EncryptionConstants;
import com.asterexcrisys.acm.exceptions.EncryptionException;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class GenericEncryptor implements Encryptor {

    private static final Logger LOGGER = Logger.getLogger(GenericEncryptor.class.getName());
    private final CoreEncryptor encryptor;

    public GenericEncryptor() throws NullPointerException, EncryptionException {
        encryptor = new CoreEncryptor(generateKey().orElseThrow(EncryptionException::new));
    }

    public GenericEncryptor(String sealedKey) throws NullPointerException, EncryptionException {
        encryptor = new CoreEncryptor(generateKey(Objects.requireNonNull(sealedKey)).orElseThrow(EncryptionException::new));
    }

    public Optional<String> getEncryptedKey(KeyEncryptor encryptor) {
        return this.encryptor.getEncryptedKey(encryptor);
    }

    public String getDecryptedKey() {
        return encryptor.getDecryptedKey();
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

    public static Optional<SecretKey> generateKey() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance(EncryptionConstants.KEY_GENERATION_ALGORITHM);
            generator.init(EncryptionConstants.KEY_SIZE);
            return Optional.ofNullable(generator.generateKey());
        } catch (NoSuchAlgorithmException | InvalidParameterException e) {
            LOGGER.warning("Error generating key: " + e.getMessage());
            return Optional.empty();
        }
    }

    public static Optional<SecretKey> generateKey(String sealedKey) {
        if (sealedKey == null || sealedKey.isBlank()) {
            return Optional.empty();
        }
        byte[] decodedKey = Base64.getDecoder().decode(sealedKey);
        return Optional.of(new SecretKeySpec(decodedKey, EncryptionConstants.KEY_GENERATION_ALGORITHM));
    }

}