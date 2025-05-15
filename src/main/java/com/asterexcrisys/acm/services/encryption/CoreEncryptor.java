package com.asterexcrisys.acm.services.encryption;

import com.asterexcrisys.acm.constants.EncryptionConstants;
import com.asterexcrisys.acm.types.utility.Pair;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
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
            byte[] vector = new byte[EncryptionConstants.INITIALIZATION_VECTOR_SIZE];
            SecureRandom.getInstanceStrong().nextBytes(vector);
            Cipher cipher = Cipher.getInstance(EncryptionConstants.ENCRYPTION_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(EncryptionConstants.AUTHENTICATION_TAG_SIZE, vector));
            Optional<byte[]> result = construct(vector, cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
            if (result.isEmpty()) {
                return Optional.empty();
            }
            return Optional.ofNullable(Base64.getEncoder().encodeToString(result.get()));
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
            Optional<Pair<byte[], byte[]>> pair = deconstruct(Base64.getDecoder().decode(data));
            if (pair.isEmpty()) {
                return Optional.empty();
            }
            Cipher cipher = Cipher.getInstance(EncryptionConstants.ENCRYPTION_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(EncryptionConstants.AUTHENTICATION_TAG_SIZE, pair.get().first()));
            byte[] result = cipher.doFinal(pair.get().second());
            return Optional.of(new String(result, StandardCharsets.UTF_8));
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                 InvalidAlgorithmParameterException | BadPaddingException | InvalidKeyException e) {
            LOGGER.severe("Error decrypting data: " + e.getMessage());
            return Optional.empty();
        }
    }

    private static Optional<byte[]> construct(byte[] vector, byte[] encryptedData) {
        if (vector == null || vector.length != EncryptionConstants.INITIALIZATION_VECTOR_SIZE) {
            return Optional.empty();
        }
        if (encryptedData == null || encryptedData.length < 1) {
            return Optional.empty();
        }
        ByteBuffer buffer = ByteBuffer.allocate(vector.length + encryptedData.length);
        buffer.put(vector);
        buffer.put(encryptedData);
        return Optional.of(buffer.array());
    }

    private static Optional<Pair<byte[], byte[]>> deconstruct(byte[] result) {
        if (result == null || result.length <= EncryptionConstants.INITIALIZATION_VECTOR_SIZE) {
            return Optional.empty();
        }
        ByteBuffer buffer = ByteBuffer.wrap(result).asReadOnlyBuffer();
        byte[] vector = new byte[EncryptionConstants.INITIALIZATION_VECTOR_SIZE];
        byte[] encryptedData = new byte[result.length - EncryptionConstants.INITIALIZATION_VECTOR_SIZE];
        buffer.get(vector);
        buffer.get(encryptedData);
        return Optional.of(new Pair<>(vector, encryptedData));
    }

}