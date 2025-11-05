package com.asterexcrisys.acm.types.encryption;

import com.asterexcrisys.acm.exceptions.EncryptionException;
import com.asterexcrisys.acm.services.encryption.GenericEncryptor;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unused")
public class Credential {

    private final GenericEncryptor encryptor;
    private final String platform;
    private final String username;
    private final String password;
    private final Instant lastModification;
    private Instant expiration;

    public Credential(String platform, String username, String password) throws NullPointerException, EncryptionException {
        encryptor = new GenericEncryptor();
        this.platform = Objects.requireNonNull(platform);
        this.username = encryptor.encrypt(Objects.requireNonNull(username)).orElseThrow(EncryptionException::new);
        this.password = encryptor.encrypt(Objects.requireNonNull(password)).orElseThrow(EncryptionException::new);
        this.lastModification = Instant.now();
        expiration = null;
    }

    public Credential(String platform, String username, String password, Instant lastModification) throws NullPointerException, EncryptionException {
        encryptor = new GenericEncryptor();
        this.platform = Objects.requireNonNull(platform);
        this.username = encryptor.encrypt(Objects.requireNonNull(username)).orElseThrow(EncryptionException::new);
        this.password = encryptor.encrypt(Objects.requireNonNull(password)).orElseThrow(EncryptionException::new);
        this.lastModification = Objects.requireNonNull(lastModification);
        expiration = null;
    }

    public Credential(String platform, String username, String password, Instant lastModification, Instant expiration) throws NullPointerException, EncryptionException {
        encryptor = new GenericEncryptor();
        this.platform = Objects.requireNonNull(platform);
        this.username = encryptor.encrypt(Objects.requireNonNull(username)).orElseThrow(EncryptionException::new);
        this.password = encryptor.encrypt(Objects.requireNonNull(password)).orElseThrow(EncryptionException::new);
        this.lastModification = Objects.requireNonNull(lastModification);
        this.expiration = Objects.requireNonNull(expiration);
    }

    public Credential(String sealedKey, String platform, String username, String password, boolean isEncrypted) throws NullPointerException, EncryptionException {
        encryptor = new GenericEncryptor(sealedKey);
        this.platform = Objects.requireNonNull(platform);
        if (isEncrypted) {
            this.username = Objects.requireNonNull(username);
            this.password = Objects.requireNonNull(password);
        } else {
            this.username = encryptor.encrypt(Objects.requireNonNull(username)).orElseThrow(EncryptionException::new);
            this.password = encryptor.encrypt(Objects.requireNonNull(password)).orElseThrow(EncryptionException::new);
        }
        this.lastModification = Instant.now();
        expiration = null;
    }

    public Credential(String sealedKey, String platform, String username, String password, Instant lastModification, boolean isEncrypted) throws NullPointerException, EncryptionException {
        encryptor = new GenericEncryptor(sealedKey);
        this.platform = Objects.requireNonNull(platform);
        if (isEncrypted) {
            this.username = Objects.requireNonNull(username);
            this.password = Objects.requireNonNull(password);
        } else {
            this.username = encryptor.encrypt(Objects.requireNonNull(username)).orElseThrow(EncryptionException::new);
            this.password = encryptor.encrypt(Objects.requireNonNull(password)).orElseThrow(EncryptionException::new);
        }
        this.lastModification = Objects.requireNonNull(lastModification);
        expiration = null;
    }

    public Credential(String sealedKey, String platform, String username, String password, Instant lastModification, Instant expiration, boolean isEncrypted) throws NullPointerException, EncryptionException {
        encryptor = new GenericEncryptor(sealedKey);
        this.platform = Objects.requireNonNull(platform);
        if (isEncrypted) {
            this.username = Objects.requireNonNull(username);
            this.password = Objects.requireNonNull(password);
        } else {
            this.username = encryptor.encrypt(Objects.requireNonNull(username)).orElseThrow(EncryptionException::new);
            this.password = encryptor.encrypt(Objects.requireNonNull(password)).orElseThrow(EncryptionException::new);
        }
        this.lastModification = Objects.requireNonNull(lastModification);
        this.expiration = Objects.requireNonNull(expiration);
    }

    public GenericEncryptor getEncryptor() {
        return encryptor;
    }

    public String getPlatform() {
        return platform;
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

    public Instant getLastModification() {
        return lastModification;
    }

    public Optional<Instant> getExpiration() {
        if (expiration == null) {
            return Optional.empty();
        }
        return Optional.of(expiration);
    }

    public void setExpiration(Instant expiration) {
        this.expiration = Objects.requireNonNull(expiration);
    }

    public void clearExpiration() {
        expiration = null;
    }

}