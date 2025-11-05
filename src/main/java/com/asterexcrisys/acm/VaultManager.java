package com.asterexcrisys.acm;

import com.asterexcrisys.acm.exceptions.DatabaseException;
import com.asterexcrisys.acm.exceptions.DerivationException;
import com.asterexcrisys.acm.exceptions.HashingException;
import com.asterexcrisys.acm.services.authentication.Authentication;
import com.asterexcrisys.acm.services.authentication.filters.VerificationDatabaseFilter;
import com.asterexcrisys.acm.types.encryption.VaultType;
import com.asterexcrisys.acm.utility.DatabaseUtility;
import com.asterexcrisys.acm.utility.EncryptionUtility;
import com.asterexcrisys.acm.services.persistence.VaultDatabase;
import com.asterexcrisys.acm.services.utility.PasswordTester;
import com.asterexcrisys.acm.types.encryption.Vault;
import com.asterexcrisys.acm.types.utility.Pair;
import com.asterexcrisys.acm.types.utility.PasswordStrength;
import com.asterexcrisys.acm.utility.PathUtility;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class VaultManager implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(VaultManager.class.getName());

    private final VaultDatabase database;
    private CredentialManager credentialManager;
    private TokenManager tokenManager;

    public VaultManager(SecretKey masterKey) throws NullPointerException, DatabaseException {
        database = new VaultDatabase(Base64.getEncoder().encodeToString(masterKey.getEncoded()));
        credentialManager = null;
        tokenManager = null;
        initialize();
    }

    public VaultManager(String sealedKey, String sealedSalt) throws NullPointerException, DerivationException, DatabaseException {
        database = new VaultDatabase(EncryptionUtility.deriveKey(
                Objects.requireNonNull(sealedKey),
                Base64.getDecoder().decode(Objects.requireNonNull(sealedSalt))
        ).orElseThrow(DerivationException::new));
        credentialManager = null;
        tokenManager = null;
        initialize();
    }

    public Optional<CredentialManager> getCredentialManager() {
        if (credentialManager == null) {
            return Optional.empty();
        }
        return Optional.of(credentialManager);
    }

    public Optional<TokenManager> getTokenManager() {
        if (tokenManager == null) {
            return Optional.empty();
        }
        return Optional.of(tokenManager);
    }

    public boolean isAuthenticated() {
        return credentialManager != null && tokenManager != null;
    }

    public boolean authenticate(String name, String password) {
        if (credentialManager != null) {
            return false;
        }
        if (name == null || password == null || name.isBlank() || password.isBlank()) {
            return false;
        }
        try {
            Optional<Vault> vault = Authentication.authenticate(database, name, password, new VerificationDatabaseFilter(name));
            if (vault.isEmpty()) {
                return false;
            }
            credentialManager = new CredentialManager(
                    vault.get().getEncryptor().getSealedSalt(),
                    vault.get().getHashedPassword(),
                    name,
                    password
            );
            tokenManager = new TokenManager(
                    vault.get().getEncryptor().getSealedSalt(),
                    vault.get().getHashedPassword(),
                    name,
                    password
            );
            return true;
        } catch (NoSuchAlgorithmException | DerivationException | HashingException | DatabaseException e) {
            LOGGER.severe("Error authenticating user to vault: " + e.getMessage());
            return false;
        }
    }

    public Optional<Vault> getVault(String name, String password) {
        return database.getVault(name, password);
    }

    public Optional<List<String>> getAllVaults() {
        return database.getAllVaults(false);
    }

    // TODO: make sure to also change SQLite's master key
    public boolean setVault(String name, String oldPassword, String newPassword) {
        if (credentialManager != null) {
            return false;
        }
        if (!authenticate(name, oldPassword)) {
            return false;
        }
        try {
            Optional<Vault> oldVault = credentialManager.getVault();
            if (oldVault.isEmpty()) {
                return false;
            }
            Vault newVault = new Vault(name, newPassword, oldVault.get().isLocked());
            if (!database.saveVault(newVault)) {
                return false;
            }
            return credentialManager.setVault(
                    newVault.getEncryptor().getSealedSalt(),
                    newVault.getHashedPassword(),
                    newPassword
            ) && tokenManager.setVault(
                    newVault.getEncryptor().getSealedSalt(),
                    newVault.getHashedPassword(),
                    newPassword
            );
        } catch (DerivationException | NoSuchAlgorithmException | HashingException e) {
            LOGGER.warning("Error setting vault: " + e.getMessage());
            return false;
        }
    }

    public boolean addVault(String name, String password) {
        if (getVault(name, password).isPresent()) {
            return false;
        }
        try {
            if (!database.saveVault(new Vault(name, password))) {
                return false;
            }
            Files.createDirectories(Paths.get(String.format("./data/%s/", name)));
            return true;
        } catch (NoSuchAlgorithmException | IOException | HashingException | DerivationException e) {
            LOGGER.warning("Error adding vault: " + e.getMessage());
            return false;
        }
    }

    public boolean addVault(String sealedSalt, String name, String password) {
        if (getVault(name, password).isPresent()) {
            return false;
        }
        try {
            if (!database.saveVault(new Vault(sealedSalt, name, password))) {
                return false;
            }
            Files.createDirectories(Paths.get(String.format("./data/%s/", name)));
            return true;
        } catch (NoSuchAlgorithmException | IOException | HashingException | DerivationException e) {
            LOGGER.warning("Error adding vault: " + e.getMessage());
            return false;
        }
    }

    public boolean removeVault(String name, String password) {
        if (!database.removeVault(name, password)) {
            return false;
        }
        PathUtility.deleteRecursively(Paths.get(String.format("./data/%s/", name)));
        return true;
    }

    public boolean lockVault(String name, String password) {
        Optional<Vault> vault = database.getVault(name, password);
        if (vault.isEmpty()) {
            return false;
        }
        vault.get().setLocked(true);
        return database.saveVault(vault.get());
    }

    public boolean unlockVault(String name, String password) {
        Optional<Vault> vault = database.getVault(name, password);
        if (vault.isEmpty()) {
            return false;
        }
        vault.get().setLocked(false);
        return database.saveVault(vault.get());
    }

    public boolean importVault(Path file, VaultType type, String name, String password) {
        if (isAuthenticated()) {
            return false;
        }
        Optional<byte[]> salt = getVault(name, password).or(() -> {
            Optional<byte[]> decodedSalt = DatabaseUtility.deconstructImport(file);
            if (decodedSalt.isEmpty()) {
                return Optional.empty();
            }
            if (!addVault(Base64.getEncoder().encodeToString(decodedSalt.get()), name, password)) {
                return Optional.empty();
            }
            return getVault(name, password);
        }).map((vault) -> {
            String encodedSalt = vault.getEncryptor().getSealedSalt();
            return Base64.getDecoder().decode(encodedSalt);
        });
        if (salt.isEmpty()) {
            return false;
        }
        if (!authenticate(name, password)) {
            return false;
        }
        return switch (type) {
            case CREDENTIAL -> credentialManager.importVault(file, password, salt.get(), false);
            case TOKEN -> tokenManager.importVault(file, password, salt.get(), false);
        };
    }

    public boolean exportVault(Path file, VaultType type) {
        if (!isAuthenticated()) {
            return false;
        }
        return switch (type) {
            case CREDENTIAL -> credentialManager.exportVault(file);
            case TOKEN -> tokenManager.exportVault(file);
        };
    }

    public boolean exportVault(Path file, VaultType type, String name, String password) {
        if (isAuthenticated()) {
            return false;
        }
        if (!authenticate(name, password)) {
            return false;
        }
        return exportVault(file, type);
    }

    public Pair<PasswordStrength, String[]> testGivenPassword(String password) {
        PasswordTester passwordTester = new PasswordTester(password);
        return Pair.of(passwordTester.getStrengthGrade(), passwordTester.getSafetyAdvices());
    }
    
    public void close() {
        database.close();
        if (credentialManager != null) {
            credentialManager.close();
            credentialManager = null;
        }
        if (tokenManager != null) {
            tokenManager.close();
            tokenManager = null;
        }
    }

    private void initialize() throws DatabaseException {
        if (!database.connect()) {
            throw new DatabaseException("Could not connect to vaults database");
        }
        if (!database.createTable()) {
            throw new DatabaseException("Could not create table on vaults database");
        }
    }

}