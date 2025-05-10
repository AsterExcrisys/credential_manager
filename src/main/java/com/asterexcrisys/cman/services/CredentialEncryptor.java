package com.asterexcrisys.cman.services;

import com.asterexcrisys.cman.exceptions.EncryptionException;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unused")
public class CredentialEncryptor implements Encryptor {

    private final CoreEncryptor encryptor;

    public CredentialEncryptor() throws EncryptionException {
        encryptor = new CoreEncryptor(generateKey().orElseThrow(EncryptionException::new));
    }

    public CredentialEncryptor(String sealedKey, String algorithm) throws NullPointerException, EncryptionException {
        encryptor = new CoreEncryptor(generateKey(Objects.requireNonNull(sealedKey), Objects.requireNonNull(algorithm)));
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

    private Optional<SecretKey> generateKey() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(256);
            return Optional.ofNullable(generator.generateKey());
        } catch (NoSuchAlgorithmException | InvalidParameterException e) {
            return Optional.empty();
        }
    }

    private static SecretKey generateKey(String sealedKey, String algorithm) {
        byte[] decodedKey = Base64.getDecoder().decode(sealedKey);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, algorithm);
    }

}