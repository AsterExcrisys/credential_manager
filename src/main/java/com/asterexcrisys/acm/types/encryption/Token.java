package com.asterexcrisys.acm.types.encryption;

import com.asterexcrisys.acm.exceptions.EncryptionException;
import com.asterexcrisys.acm.services.encryption.GenericEncryptor;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unused")
public class Token {

    private final String identifier;
    private final GenericEncryptor encryptor;
    private final Instant lastModification;
    private Instant expiration;

    public Token(String identifier) throws EncryptionException {
        this.identifier = Objects.requireNonNull(identifier);
        encryptor = new GenericEncryptor();
        lastModification = Instant.now();
        expiration = null;
    }

    public Token(String identifier, Instant lastModification) throws EncryptionException {
        this.identifier = Objects.requireNonNull(identifier);
        encryptor = new GenericEncryptor();
        this.lastModification = Objects.requireNonNull(lastModification);
        expiration = null;
    }

    public Token(String identifier, Instant lastModification, Instant expiration) throws EncryptionException {
        this.identifier = Objects.requireNonNull(identifier);
        encryptor = new GenericEncryptor();
        this.lastModification = Objects.requireNonNull(lastModification);
        this.expiration = Objects.requireNonNull(expiration);
    }

    public Token(String identifier, String sealedKey) throws EncryptionException {
        this.identifier = Objects.requireNonNull(identifier);
        encryptor = new GenericEncryptor(sealedKey);
        lastModification = Instant.now();
        expiration = null;
    }

    public Token(String identifier, String sealedKey, Instant lastModification) throws EncryptionException {
        this.identifier = Objects.requireNonNull(identifier);
        encryptor = new GenericEncryptor(sealedKey);
        this.lastModification = Objects.requireNonNull(lastModification);
        expiration = null;
    }

    public Token(String identifier, String sealedKey, Instant lastModification, Instant expiration) throws EncryptionException {
        this.identifier = Objects.requireNonNull(identifier);
        encryptor = new GenericEncryptor(sealedKey);
        this.lastModification = Objects.requireNonNull(lastModification);
        this.expiration = Objects.requireNonNull(expiration);
    }

    public String getIdentifier() {
        return identifier;
    }

    public GenericEncryptor getEncryptor() {
        return encryptor;
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