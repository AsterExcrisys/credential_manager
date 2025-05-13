package com.asterexcrisys.acm;

import com.asterexcrisys.acm.exceptions.DerivationException;
import com.asterexcrisys.acm.services.Utility;
import com.asterexcrisys.acm.services.persistence.CredentialDatabase;
import com.asterexcrisys.acm.services.utility.PasswordGenerator;
import com.asterexcrisys.acm.services.utility.PasswordTester;
import com.asterexcrisys.acm.types.encryption.Vault;
import com.asterexcrisys.acm.types.utility.Pair;
import com.asterexcrisys.acm.types.encryption.Credential;
import com.asterexcrisys.acm.types.utility.PasswordStrength;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@SuppressWarnings("unused")
public class CredentialManager implements AutoCloseable {

    private final Vault vault;
    private final CredentialDatabase database;
    private final PasswordGenerator generator;

    public CredentialManager(String name, String password, String sealedSalt) throws NullPointerException, DerivationException, NoSuchAlgorithmException {
        vault = new Vault(name, password, sealedSalt);
        database = new CredentialDatabase(
                Objects.requireNonNull(name),
                Utility.derive(
                        Objects.requireNonNull(password),
                        Base64.getDecoder().decode(Objects.requireNonNull(sealedSalt))
                ).orElseThrow(DerivationException::new)
        );
        generator = new PasswordGenerator();
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
        return database.saveCredential(
                new Credential(
                        credential.get().getEncryptor().getDecryptedKey(),
                        credential.get().getPlatform(),
                        username,
                        password
                ),
                vault.getEncryptor()
        );
    }

    public boolean addCredential(String platform, String username, String password) {
        if (getCredential(platform).isPresent()) {
            return false;
        }
        return database.saveCredential(
                new Credential(platform, username, password),
                vault.getEncryptor()
        );
    }

    public boolean removeCredential(String platform) {
        return database.removeCredential(platform);
    }

    public boolean removeAllCredentials() {
        return database.removeAllCredentials();
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
        return Optional.of(new Pair<>(passwordTester.getStrengthGrade(), passwordTester.getSafetyAdvices()));
    }

    public Pair<PasswordStrength, String[]> testGivenPassword(String password) {
        PasswordTester passwordTester = new PasswordTester(password);
        return new Pair<>(passwordTester.getStrengthGrade(), passwordTester.getSafetyAdvices());
    }

    public void close() {
        database.close();
    }

}