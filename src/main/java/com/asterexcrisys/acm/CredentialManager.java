package com.asterexcrisys.acm;

import com.asterexcrisys.acm.exceptions.DatabaseException;
import com.asterexcrisys.acm.exceptions.DerivationException;
import com.asterexcrisys.acm.exceptions.EncryptionException;
import com.asterexcrisys.acm.exceptions.HashingException;
import com.asterexcrisys.acm.utility.DatabaseUtility;
import com.asterexcrisys.acm.utility.EncryptionUtility;
import com.asterexcrisys.acm.services.persistence.CredentialDatabase;
import com.asterexcrisys.acm.services.utility.PasswordGenerator;
import com.asterexcrisys.acm.services.utility.PasswordTester;
import com.asterexcrisys.acm.types.encryption.Vault;
import com.asterexcrisys.acm.types.utility.Pair;
import com.asterexcrisys.acm.types.encryption.Credential;
import com.asterexcrisys.acm.types.utility.PasswordStrength;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class CredentialManager implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(CredentialManager.class.getName());

    private final CredentialDatabase database;
    private final PasswordGenerator generator;
    private Vault vault;

    public CredentialManager(String sealedSalt, String name, String password) throws NullPointerException, DerivationException, NoSuchAlgorithmException, HashingException, DatabaseException {
        database = new CredentialDatabase(
                Objects.requireNonNull(name),
                EncryptionUtility.deriveKey(
                        Objects.requireNonNull(password),
                        Base64.getDecoder().decode(Objects.requireNonNull(sealedSalt))
                ).orElseThrow(DerivationException::new)
        );
        generator = new PasswordGenerator();
        vault = new Vault(sealedSalt, name, password);
        initialize();
    }

    public CredentialManager(String sealedSalt, String hashedPassword, String name, String password) throws NullPointerException, DerivationException, NoSuchAlgorithmException, HashingException, DatabaseException {
        database = new CredentialDatabase(
                Objects.requireNonNull(name),
                EncryptionUtility.deriveKey(
                        Objects.requireNonNull(password),
                        Base64.getDecoder().decode(Objects.requireNonNull(sealedSalt))
                ).orElseThrow(DerivationException::new)
        );
        generator = new PasswordGenerator();
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
            Optional<List<String>> platforms = database.getAllCredentials();
            if (platforms.isEmpty()) {
                return false;
            }
            for (String platform : platforms.get()) {
                Optional<Credential> credential = database.getCredential(platform, this.vault.getEncryptor());
                if (credential.isEmpty()) {
                    return false;
                }
                if (!database.saveCredential(credential.get(), vault.getEncryptor())) {
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

    // TODO: add a command to check expired credentials/tokens and do not allow them to be seen
    public Optional<Credential> getCredential(String platform) {
        return database.getCredential(platform, vault.getEncryptor());
    }

    public Optional<List<String>> getAllCredentials() {
        return database.getAllCredentials();
    }

    public boolean setCredential(String platform, String username, String password) {
        Optional<Credential> oldCredential = getCredential(platform);
        if (oldCredential.isEmpty()) {
            return false;
        }
        try {
            Credential newCredential = new Credential(
                    oldCredential.get().getEncryptor().getDecryptedKey(),
                    oldCredential.get().getPlatform(),
                    username,
                    password,
                    false
            );
            oldCredential.get().getExpiration().ifPresent(newCredential::setExpiration);
            return database.saveCredential(newCredential, vault.getEncryptor());
        } catch (EncryptionException e) {
            LOGGER.warning("Error updating credential: " + e.getMessage());
            return false;
        }
    }

    public boolean setCredential(String platform, String username, String password, Instant expiration) {
        Optional<Credential> oldCredential = getCredential(platform);
        if (oldCredential.isEmpty()) {
            return false;
        }
        try {
            Credential newCredential = new Credential(
                    oldCredential.get().getEncryptor().getDecryptedKey(),
                    oldCredential.get().getPlatform(),
                    username,
                    password,
                    false
            );
            newCredential.setExpiration(expiration);
            return database.saveCredential(newCredential, vault.getEncryptor());
        } catch (EncryptionException e) {
            LOGGER.warning("Error updating credential: " + e.getMessage());
            return false;
        }
    }

    public boolean addCredential(String platform, String username, String password) {
        if (getCredential(platform).isPresent()) {
            return false;
        }
        try {
            return database.saveCredential(
                    new Credential(platform, username, password),
                    vault.getEncryptor()
            );
        } catch (EncryptionException e) {
            LOGGER.warning("Error adding credential: " + e.getMessage());
            return false;
        }
    }

    public boolean addCredential(String platform, String username, String password, Instant expiration) {
        if (getCredential(platform).isPresent()) {
            return false;
        }
        try {
            Credential credential = new Credential(platform, username, password);
            credential.setExpiration(expiration);
            return database.saveCredential(credential, vault.getEncryptor());
        } catch (EncryptionException e) {
            LOGGER.warning("Error adding credential: " + e.getMessage());
            return false;
        }
    }

    public boolean removeCredential(String platform) {
        return database.removeCredential(platform);
    }

    public boolean removeAllCredentials() {
        return database.removeAllCredentials();
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

    public String generatePassword() {
        return generator.generate();
    }

    public String generatePassword(int length) {
        return generator.generate(length);
    }

    public Optional<Pair<PasswordStrength, String[]>> testExistingPassword(String platform) {
        Optional<Credential> credential = getCredential(platform);
        if (credential.isEmpty()) {
            return Optional.empty();
        }
        Optional<String> password = credential.get().getDecryptedPassword();
        if (password.isEmpty()) {
            return Optional.empty();
        }
        PasswordTester passwordTester = new PasswordTester(password.get());
        return Optional.of(Pair.of(passwordTester.getStrengthGrade(), passwordTester.getSafetyAdvices()));
    }
    
    public void close() {
        database.close();
    }

    private void initialize() throws DatabaseException {
        if (!database.connect()) {
            throw new DatabaseException("Could not connect to credentials database");
        }
        if (!database.createTable()) {
            throw new DatabaseException("Could not create table on credentials database");
        }
    }

}