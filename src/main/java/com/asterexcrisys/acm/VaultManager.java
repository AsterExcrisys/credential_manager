package com.asterexcrisys.acm;

import com.asterexcrisys.acm.exceptions.DatabaseException;
import com.asterexcrisys.acm.exceptions.DerivationException;
import com.asterexcrisys.acm.exceptions.HashingException;
import com.asterexcrisys.acm.services.authentication.Authentication;
import com.asterexcrisys.acm.services.authentication.filters.VerificationDatabaseFilter;
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
    private CredentialManager manager;

    public VaultManager(SecretKey masterKey) throws NullPointerException, DatabaseException {
        database = new VaultDatabase(Base64.getEncoder().encodeToString(masterKey.getEncoded()));
        manager = null;
        initialize();
    }

    public VaultManager(String sealedKey, String sealedSalt) throws NullPointerException, DerivationException, DatabaseException {
        database = new VaultDatabase(EncryptionUtility.deriveKey(
                Objects.requireNonNull(sealedKey),
                Base64.getDecoder().decode(Objects.requireNonNull(sealedSalt))
        ).orElseThrow(DerivationException::new));
        manager = null;
        initialize();
    }

    public Optional<CredentialManager> getManager() {
        if (manager == null) {
            return Optional.empty();
        }
        return Optional.of(manager);
    }

    public boolean isAuthenticated() {
        return manager != null;
    }

    public boolean authenticate(String name, String password) {
        if (manager != null) {
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
            manager = new CredentialManager(
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
        if (manager != null) {
            return false;
        }
        if (!authenticate(name, oldPassword)) {
            return false;
        }
        try {
            Optional<Vault> oldVault = manager.getVault();
            if (oldVault.isEmpty()) {
                return false;
            }
            Vault newVault = new Vault(name, newPassword, oldVault.get().isLocked());
            if (!database.saveVault(newVault)) {
                return false;
            }
            return manager.setVault(
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

    public boolean importVault(Path file, String name, String password) {
        if (manager != null) {
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
        return manager.importVault(file, password, salt.get(), false);
    }

    public boolean exportVault(Path file) {
        if (manager == null) {
            return false;
        }
        return manager.exportVault(file);
    }

    public boolean exportVault(Path file, String name, String password) {
        if (manager != null) {
            return false;
        }
        if (!authenticate(name, password)) {
            return false;
        }
        return exportVault(file);
    }

    public Pair<PasswordStrength, String[]> testGivenPassword(String password) {
        PasswordTester passwordTester = new PasswordTester(password);
        return Pair.of(passwordTester.getStrengthGrade(), passwordTester.getSafetyAdvices());
    }
    
    public void close() {
        database.close();
        if (manager != null) {
            manager.close();
            manager = null;
        }
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