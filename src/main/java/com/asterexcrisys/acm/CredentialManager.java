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
import java.util.*;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class CredentialManager implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(CredentialManager.class.getName());

    private final Vault vault;
    private final CredentialDatabase database;
    private final PasswordGenerator generator;

    public CredentialManager(String sealedSalt, String name, String password) throws NullPointerException, DerivationException, NoSuchAlgorithmException, HashingException, DatabaseException {
        vault = new Vault(sealedSalt, name, password);
        database = new CredentialDatabase(
                Objects.requireNonNull(name),
                EncryptionUtility.deriveKey(
                        Objects.requireNonNull(password),
                        Base64.getDecoder().decode(Objects.requireNonNull(sealedSalt))
                ).orElseThrow(DerivationException::new)
        );
        generator = new PasswordGenerator();
        initialize();
    }

    public CredentialManager(String sealedSalt, String hashedPassword, String name, String password) throws NullPointerException, DerivationException, NoSuchAlgorithmException, HashingException, DatabaseException {
        vault = new Vault(sealedSalt, hashedPassword, name, password);
        database = new CredentialDatabase(
                Objects.requireNonNull(name),
                EncryptionUtility.deriveKey(
                        Objects.requireNonNull(password),
                        Base64.getDecoder().decode(Objects.requireNonNull(sealedSalt))
                ).orElseThrow(DerivationException::new)
        );
        generator = new PasswordGenerator();
        initialize();
    }

    public Vault getVault() {
        return vault;
    }

    public Optional<Credential> getCredential(String platform) {
        return database.getCredential(platform, vault.getEncryptor());
    }

    public Optional<List<String>> getAllCredentials() {
        return database.getAllCredentials();
    }

    public boolean setCredential(String platform, String username, String password) {
        Optional<Credential> credential = getCredential(platform);
        if (credential.isEmpty()) {
            return false;
        }
        try {
            return database.saveCredential(
                    new Credential(
                            credential.get().getEncryptor().getDecryptedKey(),
                            credential.get().getPlatform(),
                            username,
                            password,
                            false
                    ),
                    vault.getEncryptor()
            );
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

    public boolean removeCredential(String platform) {
        return database.removeCredential(platform);
    }

    public boolean removeAllCredentials() {
        return database.removeAllCredentials();
    }

    public boolean importVault(Path file, String password, byte[] salt, boolean shouldOverwrite) {
        Optional<String> masterKey = EncryptionUtility.deriveKey(password, salt);
        if (masterKey.isEmpty()) {
            return false;
        }
        if (shouldOverwrite) {
            return database.restoreFrom(file);
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
            throw new DatabaseException("Could not connect to database");
        }
        if (!database.createTable()) {
            throw new DatabaseException("Could not create table on database");
        }
    }

}
