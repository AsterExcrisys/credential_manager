package org.pwman.services;

import org.pwman.types.EncryptorMode;
import org.pwman.types.Resource;
import org.pwman.exceptions.EncryptionException;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.Serializable;
import java.security.*;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

public class PasswordEncryptor implements Serializable {

    private final SecretKey key;
    private final byte[] vector;

    public PasswordEncryptor() throws EncryptionException {
        key = generateKey(Resource.KEY_SIZE).orElseThrow(EncryptionException::new);
        vector = generateVector();
    }

    public PasswordEncryptor(SecretKey key, byte[] vector) throws NullPointerException, EncryptionException {
        this.key = Objects.requireNonNull(key);
        this.vector = Objects.requireNonNull(vector);
    }

    public PasswordEncryptor(String sealedKey, String sealedVector) throws NullPointerException, EncryptionException {
        key = generateKey(Objects.requireNonNull(sealedKey));
        vector = generateVector(Objects.requireNonNull(sealedVector));
    }

    public PasswordEncryptor(String sealedKey, String sealedVector, String algorithm) throws NullPointerException, EncryptionException {
        key = generateKey(Objects.requireNonNull(sealedKey), Objects.requireNonNull(algorithm));
        vector = generateVector(Objects.requireNonNull(sealedVector));
    }

    public String getSealedKey() {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public String getSealedVector() {
        return Base64.getEncoder().encodeToString(vector);
    }

    public String getAlgorithm() {
        return key.getAlgorithm();
    }

    public Optional<Cipher> getCipher(EncryptorMode encryptorMode) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(Resource.ENCRYPTION_ALGORITHM);
            cipher.init(encryptorMode.getValue(), key, new IvParameterSpec(vector));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException e) {
            return Optional.empty();
        }
        return Optional.of(cipher);
    }

    public Optional<String> encryptPassword(String password) {
        if (Objects.isNull(password)) {
            return Optional.empty();
        }
        byte[] result;
        try {
            Cipher cipher = Cipher.getInstance(Resource.ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(vector));
            result = cipher.doFinal(password.getBytes());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            return Optional.empty();
        }
        return Optional.ofNullable(Base64.getEncoder().encodeToString(result));
    }

    public Optional<String> decryptPassword(String password) {
        if (Objects.isNull(password)) {
            return Optional.empty();
        }
        byte[] result;
        try {
            Cipher cipher = Cipher.getInstance(Resource.ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(vector));
            result = cipher.doFinal(Base64.getDecoder().decode(password));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            return Optional.empty();
        }
        return Optional.of(new String(result));
    }

    private Optional<SecretKey> generateKey(int size) {
        KeyGenerator generator;
        try {
            generator = KeyGenerator.getInstance(Resource.ENCRYPTION_STANDARD);
            generator.init(size);
        } catch (NoSuchAlgorithmException | InvalidParameterException e) {
            return Optional.empty();
        }
        return Optional.ofNullable(generator.generateKey());
    }

    private static SecretKey generateKey(String sealedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(sealedKey);
        return new javax.crypto.spec.SecretKeySpec(decodedKey, 0, decodedKey.length, Resource.ENCRYPTION_ALGORITHM);
    }

    private static SecretKey generateKey(String sealedKey, String algorithm) {
        byte[] decodedKey = Base64.getDecoder().decode(sealedKey);
        return new javax.crypto.spec.SecretKeySpec(decodedKey, 0, decodedKey.length, algorithm);
    }

    private static byte[] generateVector() {
        byte[] vector = new byte[Resource.VECTOR_SIZE];
        new SecureRandom().nextBytes(vector);
        return vector;
    }

    private static byte[] generateVector(String sealedVector) {
        return Base64.getDecoder().decode(sealedVector);
    }

}