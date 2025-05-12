package com.asterexcrisys.acm;

import com.asterexcrisys.acm.exceptions.DerivationException;
import com.asterexcrisys.acm.services.authentication.Authentication;
import com.asterexcrisys.acm.services.Utility;
import com.asterexcrisys.acm.services.persistence.VaultDatabase;
import com.asterexcrisys.acm.types.encryption.Vault;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
public class VaultManager implements AutoCloseable {

    private final VaultDatabase database;
    private CredentialManager credentialManager;

    public VaultManager(String masterKey, String sealedSalt) throws NullPointerException, DerivationException {
        database = new VaultDatabase(Utility.derive(
                masterKey,
                Base64.getDecoder().decode(sealedSalt)
        ).orElseThrow(DerivationException::new));
        credentialManager = null;
    }

    public Optional<CredentialManager> getCredentialManager() {
        if (credentialManager == null) {
            return Optional.empty();
        }
        return Optional.of(credentialManager);
    }

    public boolean authenticate(String name, String password) {
        if (credentialManager != null) {
            return false;
        }
        if (name == null || password == null || name.isBlank() || password.isBlank()) {
            return false;
        }
        try {
            Optional<Vault> vault = Authentication.authenticate(database, name, password);
            if (vault.isEmpty()) {
                return false;
            }
            credentialManager = new CredentialManager(name, password, vault.get().getEncryptor().getSealedSalt());
            return true;
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }

    public Optional<Vault> getVault(String name, String password) {
        return database.getVault(name, password);
    }

    public Optional<List<String>> getAllVaults() {
        return database.getAllVaults();
    }

    public boolean setVault(String name, String password) {
        return false;
    }

    public boolean addVault(String name, String password) {
        if (getVault(name, password).isPresent()) {
            return false;
        }
        try {
            database.saveVault(new Vault(name, password));
            return true;
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }

    public boolean removeVault(String name, String password) {
        return database.removeVault(name, password);
    }

    public void close() {
        database.close();
        if (credentialManager != null) {
            credentialManager.close();
        }
    }

}