package com.asterexcrisys.cman.types;

import com.asterexcrisys.cman.exceptions.EncryptionException;
import com.asterexcrisys.cman.services.CredentialEncryptor;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unused")
public class Credential {

    private final CredentialEncryptor encryptor;
    private final String platform;
    private final String username;
    private final String password;

    public Credential(String platform, String username, String password) throws NullPointerException, EncryptionException {
        encryptor = new CredentialEncryptor();
        this.platform = Objects.requireNonNull(platform);
        this.username = encryptor.encrypt(Objects.requireNonNull(username)).orElseThrow(EncryptionException::new);
        this.password = encryptor.encrypt(Objects.requireNonNull(password)).orElseThrow(EncryptionException::new);
    }

    public Credential(String sealedKey, String algorithm, String platform, String username, String password) throws NullPointerException, EncryptionException {
        encryptor = new CredentialEncryptor(sealedKey, algorithm);
        this.platform = Objects.requireNonNull(platform);
        this.username = Objects.requireNonNull(username);
        this.password = Objects.requireNonNull(password);
    }

    public CredentialEncryptor getEncryptor() {
        return encryptor;
    }

    public String getEncryptedUsername() {
        return username;
    }

    public Optional<String> getDecryptedUsername() {
        return encryptor.decrypt(username);
    }

    public String getEncryptedPassword() {
        return password;
    }

    public Optional<String> getDecryptedPassword() {
        return encryptor.decrypt(password);
    }

    public String getPlatform() {
        return platform;
    }

}