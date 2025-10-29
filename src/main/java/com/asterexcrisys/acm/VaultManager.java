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

    public VaultManager(SecretKey key) throws NullPointerException, DatabaseException {
        database = new VaultDatabase(Base64.getEncoder().encodeToString(key.getEncoded()));
        manager = null;
        initialize();
    }

    public VaultManager(String masterKey, String sealedSalt) throws NullPointerException, DerivationException, DatabaseException {
        database = new VaultDatabase(EncryptionUtility.deriveKey(
                Objects.requireNonNull(masterKey),
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
                    name,
                    password,
                    vault.get().getEncryptor().getSealedSalt(),
                    vault.get().getHashedPassword()
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

    public boolean setVault(String name, String password) {
        // TODO: implement the change password method
        return false;
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
        Optional<byte[]> salt = DatabaseUtility.deconstructImport(file);
        if (salt.isEmpty()) {
            return false;
        }
        if (!addVault(Base64.getEncoder().encodeToString(salt.get()), name, password)) {
            return false;
        }
        if (!authenticate(name, password)) {
            return false;
        }
        return manager.importVault(file, password, salt.get(), false);
    }

    public boolean exportVault(Path file, String name, String password) {
        if (manager != null) {
            return false;
        }
        if (!authenticate(name, password)) {
            return false;
        }
        return manager.exportVault(file);
    }

    public Pair<PasswordStrength, String[]> testGivenPassword(String password) {
        PasswordTester passwordTester = new PasswordTester(password);
        return Pair.of(passwordTester.getStrengthGrade(), passwordTester.getSafetyAdvices());
    }

    @Override
    public void close() {
        database.close();
        if (manager != null) {
            manager.close();
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