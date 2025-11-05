package com.asterexcrisys.acm;

import com.asterexcrisys.acm.exceptions.DatabaseException;
import com.asterexcrisys.acm.exceptions.DerivationException;
import com.asterexcrisys.acm.exceptions.EncryptionException;
import com.asterexcrisys.acm.exceptions.HashingException;
import com.asterexcrisys.acm.services.persistence.TokenDatabase;
import com.asterexcrisys.acm.types.encryption.CipherMode;
import com.asterexcrisys.acm.types.encryption.Token;
import com.asterexcrisys.acm.types.encryption.Vault;
import com.asterexcrisys.acm.utility.DatabaseUtility;
import com.asterexcrisys.acm.utility.EncryptionUtility;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class TokenManager implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(TokenManager.class.getName());

    private final TokenDatabase database;
    private Vault vault;

    public TokenManager(String sealedSalt, String name, String password) throws NullPointerException, DerivationException, NoSuchAlgorithmException, HashingException, DatabaseException {
        database = new TokenDatabase(
                Objects.requireNonNull(name),
                EncryptionUtility.deriveKey(
                        Objects.requireNonNull(password),
                        Base64.getDecoder().decode(Objects.requireNonNull(sealedSalt))
                ).orElseThrow(DerivationException::new)
        );
        vault = new Vault(sealedSalt, name, password);
        initialize();
    }

    public TokenManager(String sealedSalt, String hashedPassword, String name, String password) throws NullPointerException, DerivationException, NoSuchAlgorithmException, HashingException, DatabaseException {
        database = new TokenDatabase(
                Objects.requireNonNull(name),
                EncryptionUtility.deriveKey(
                        Objects.requireNonNull(password),
                        Base64.getDecoder().decode(Objects.requireNonNull(sealedSalt))
                ).orElseThrow(DerivationException::new)
        );
        vault = new Vault(sealedSalt, hashedPassword, name, password);
        initialize();
    }

    public Optional<Vault> getVault() {
        if (vault == null) {
            return Optional.empty();
        }
        return Optional.of(vault);
    }

    // TODO: make sure to also change SQLite's master key
    public boolean setVault(String sealedSalt, String hashedPassword, String password) {
        try {
            Vault vault = new Vault(sealedSalt, hashedPassword, this.vault.getName(), password);
            Optional<List<String>> identifiers = database.getAllTokens();
            if (identifiers.isEmpty()) {
                return false;
            }
            for (String platform : identifiers.get()) {
                Optional<Token> token = database.getToken(platform, this.vault.getEncryptor());
                if (token.isEmpty()) {
                    return false;
                }
                if (!database.saveToken(token.get(), vault.getEncryptor())) {
                    return false;
                }
            }
            this.vault = vault;
            return true;
        } catch (DerivationException e) {
            LOGGER.warning("Error setting vault: " + e.getMessage());
            return false;
        }
    }

    public Optional<Token> getToken(String identifier) {
        return database.getToken(identifier, vault.getEncryptor());
    }

    public Optional<List<String>> getAllTokens() {
        return database.getAllTokens();
    }

    public boolean setToken(String identifier) {
        Optional<Token> oldToken = getToken(identifier);
        if (oldToken.isEmpty()) {
            return false;
        }
        try {
            Token newToken = new Token(oldToken.get().getIdentifier());
            oldToken.get().getExpiration().ifPresent(newToken::setExpiration);
            return database.saveToken(newToken, vault.getEncryptor());
        } catch (EncryptionException e) {
            LOGGER.warning("Error updating token: " + e.getMessage());
            return false;
        }
    }

    public boolean setToken(String identifier, Instant expiration) {
        Optional<Token> oldToken = getToken(identifier);
        if (oldToken.isEmpty()) {
            return false;
        }
        try {
            Token newToken = new Token(oldToken.get().getIdentifier());
            newToken.setExpiration(expiration);
            return database.saveToken(newToken, vault.getEncryptor());
        } catch (EncryptionException e) {
            LOGGER.warning("Error updating token: " + e.getMessage());
            return false;
        }
    }

    public boolean addToken(String identifier) {
        if (getToken(identifier).isPresent()) {
            return false;
        }
        try {
            return database.saveToken(new Token(identifier), vault.getEncryptor());
        } catch (EncryptionException e) {
            LOGGER.warning("Error adding token: " + e.getMessage());
            return false;
        }
    }

    public boolean addCredential(String identifier, Instant expiration) {
        if (getToken(identifier).isPresent()) {
            return false;
        }
        try {
            Token token = new Token(identifier);
            token.setExpiration(expiration);
            return database.saveToken(token, vault.getEncryptor());
        } catch (EncryptionException e) {
            LOGGER.warning("Error adding token: " + e.getMessage());
            return false;
        }
    }

    public boolean removeToken(String identifier) {
        return database.removeToken(identifier);
    }

    public boolean removeAllTokens() {
        return database.removeAllTokens();
    }

    public Optional<String> encryptText(String identifier, String text) {
        Optional<Token> token = getToken(identifier);
        if (token.isEmpty()) {
            return Optional.empty();
        }
        return token.get().getEncryptor().encrypt(text);
    }

    public Optional<String> decryptText(String identifier, String text) {
        Optional<Token> token = getToken(identifier);
        if (token.isEmpty()) {
            return Optional.empty();
        }
        return token.get().getEncryptor().decrypt(text);
    }

    public boolean encryptFile(String identifier, Path file) {
        Optional<Token> token = getToken(identifier);
        if (token.isEmpty()) {
            return false;
        }
        return EncryptionUtility.transformFile(token.get().getEncryptor(), CipherMode.ENCRYPT, file);
    }

    public boolean decryptFile(String identifier, Path file) {
        Optional<Token> token = getToken(identifier);
        if (token.isEmpty()) {
            return false;
        }
        return EncryptionUtility.transformFile(token.get().getEncryptor(), CipherMode.DECRYPT, file);
    }

    public boolean importVault(Path file, String password, byte[] salt, boolean shouldOverwrite) {
        if (shouldOverwrite) {
            return database.restoreFrom(file);
        }
        Optional<String> masterKey = EncryptionUtility.deriveKey(password, salt);
        if (masterKey.isEmpty()) {
            return false;
        }
        return database.mergeWith(file, masterKey.get());
    }

    public boolean exportVault(Path file) {
        if (!database.backupTo(file)) {
            return false;
        }
        return DatabaseUtility.constructExport(
                file,
                Base64.getDecoder().decode(vault.getEncryptor().getSealedSalt())
        );
    }

    public void close() {
        database.close();
    }

    private void initialize() throws DatabaseException {
        if (!database.connect()) {
            throw new DatabaseException("Could not connect to tokens database");
        }
        if (!database.createTable()) {
            throw new DatabaseException("Could not create table on tokens database");
        }
    }

}