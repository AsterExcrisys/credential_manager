package com.asterexcrisys.cman;

import com.asterexcrisys.cman.exceptions.EncryptionException;
import com.asterexcrisys.cman.services.KeyEncryptor;
import com.asterexcrisys.cman.services.PasswordGenerator;
import com.asterexcrisys.cman.services.PasswordTester;
import com.asterexcrisys.cman.types.Pair;
import com.asterexcrisys.cman.types.Credential;
import com.asterexcrisys.cman.types.PasswordStrength;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.*;

@SuppressWarnings("unused")
public class CredentialManager {

    private final KeyEncryptor encryptor;
    private final ArrayList<Credential> credentials;
    private final PasswordGenerator passwordGenerator;

    public CredentialManager(String password) throws EncryptionException, NoSuchAlgorithmException, NoSuchProviderException {
        encryptor = new KeyEncryptor(password);
        credentials = new ArrayList<>();
        passwordGenerator = new PasswordGenerator();
    }

    public CredentialManager(String password, byte[] salt) throws EncryptionException, NoSuchAlgorithmException, NoSuchProviderException {
        encryptor = new KeyEncryptor(password, salt);
        credentials = new ArrayList<>();
        passwordGenerator = new PasswordGenerator();
    }

    public boolean hasCredential(String platform) {
        for (Credential credential : credentials) {
            if (credential.getPlatform().equals(platform)) {
                return true;
            }
        }
        return false;
    }

    public Optional<Credential> getCredential(String platform) {
        for (Credential credential : credentials) {
            if (credential.getPlatform().equals(platform)) {
                return Optional.of(credential);
            }
        }
        return Optional.empty();
    }

    public boolean setCredential(String platform, String username, String password) {
        for (Credential credential : credentials) {
            if (credential.getPlatform().equals(platform)) {
                credentials.set(credentials.indexOf(credential), new Credential(platform, username, password));
                return true;
            }
        }
        return false;
    }

    public boolean addCredential(String platform, String username, String password) {
        if (this.hasCredential(platform)) {
            return false;
        }
        try {
            credentials.add(new Credential(platform, username, password));
            return true;
        } catch (EncryptionException e) {
            return false;
        }
    }

    public boolean addCredential(String sealedKey, String algorithm, String platform, String username, String password) {
        if (this.hasCredential(platform)) {
            return false;
        }
        try {
            credentials.add(new Credential(sealedKey, algorithm, platform, username, password));
            return true;
        } catch (EncryptionException e) {
            return false;
        }
    }

    public boolean removeCredential(String platform) {
        for (Credential credential : credentials) {
            if (credential.getPlatform().equals(platform)) {
                credentials.remove(credential);
                return true;
            }
        }
        return false;
    }

    public String generatePassword() {
        return passwordGenerator.generate();
    }

    public String generatePassword(int length) {
        return passwordGenerator.generate(length);
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

}