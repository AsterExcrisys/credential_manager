package com.asterexcrisys.acm.services.encryption;

import com.asterexcrisys.acm.constants.Encryption;
import com.asterexcrisys.acm.types.utility.Pair;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class CoreEncryptor implements Encryptor {

    private static final Logger LOGGER = Logger.getLogger(CoreEncryptor.class.getName());
    private final SecretKey key;

    public CoreEncryptor(SecretKey key) throws NullPointerException {
        this.key = Objects.requireNonNull(key);
    }

    public Optional<String> getEncryptedKey(Encryptor encryptor) {
        if (encryptor == null) {
            return Optional.empty();
        }
        return encryptor.encrypt(Base64.getEncoder().encodeToString(key.getEncoded()));
    }

    public String getDecryptedKey() {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public String getAlgorithm() {
        return key.getAlgorithm();
    }

    public Optional<String> encrypt(String data) {
        if (data == null || data.isBlank()) {
            return Optional.empty();
        }
        try {
            byte[] vector = new byte[Encryption.INITIALIZATION_VECTOR_SIZE];
            SecureRandom.getInstanceStrong().nextBytes(vector);
            Cipher cipher = Cipher.getInstance(Encryption.ENCRYPTION_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(Encryption.AUTHENTICATION_TAG_SIZE, vector));
            byte[] result = construct(vector, cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
            return Optional.ofNullable(Base64.getEncoder().encodeToString(result));
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException |
                 IllegalBlockSizeException | InvalidKeyException | BadPaddingException e) {
            LOGGER.severe("Error encrypting data: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<String> decrypt(String data) {
        if (data == null || data.isBlank()) {
            return Optional.empty();
        }
        try {
            Pair<byte[], byte[]> pair = deconstruct(Base64.getDecoder().decode(data), Encryption.INITIALIZATION_VECTOR_SIZE);
            Cipher cipher = Cipher.getInstance(Encryption.ENCRYPTION_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(Encryption.AUTHENTICATION_TAG_SIZE, pair.first()));
            byte[] result = cipher.doFinal(pair.second());
            return Optional.of(new String(result, StandardCharsets.UTF_8));
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                 InvalidAlgorithmParameterException | BadPaddingException | InvalidKeyException e) {
            LOGGER.severe("Error decrypting data: " + e.getMessage());
            return Optional.empty();
        }
    }

    private static byte[] construct(byte[] vector, byte[] encryptedData) {
        byte[] result = new byte[vector.length + encryptedData.length];
        System.arraycopy(vector, 0, result, 0, vector.length);
        System.arraycopy(encryptedData, 0, result, vector.length, encryptedData.length);
        return result;
    }

    private static Pair<byte[], byte[]> deconstruct(byte[] result, int vectorLength) {
        byte[] vector = Arrays.copyOfRange(result, 0, vectorLength);
        byte[] encryptedData = Arrays.copyOfRange(result, vectorLength, result.length);
        return new Pair<>(vector, encryptedData);
    }

}