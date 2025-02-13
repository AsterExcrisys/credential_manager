package org.pwman;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class PasswordManager {

    private final PasswordEncryptor passwordEncryptor;
    private final ArrayList<PasswordObject> passwordObjects;

    public PasswordManager() throws EncryptionException {
        this.passwordEncryptor = new PasswordEncryptor();
        this.passwordObjects = new ArrayList<>(0);
    }

    public PasswordManager(SecretKey key, byte[] vector) throws NullPointerException, EncryptionException {
        this.passwordEncryptor = new PasswordEncryptor(Objects.requireNonNull(key), Objects.requireNonNull(vector));
        this.passwordObjects = new ArrayList<>(0);
    }

    public boolean hasPassword(String platform) {
        for (PasswordObject passwordObject : passwordObjects) {
            if (passwordObject.getPlatform().equals(platform)) {
                return true;
            }
        }
        return false;
    }

    public Optional<String> getPassword(String platform) {
        for (PasswordObject passwordObject : passwordObjects) {
            if (passwordObject.getPlatform().equals(platform)) {
                return passwordObject.getPassword();
            }
        }
        return Optional.empty();
    }

    public boolean setPassword(String password, String platform) {
        for (PasswordObject passwordObject : passwordObjects) {
            if (passwordObject.getPlatform().equals(platform)) {
                passwordObjects.set(passwordObjects.indexOf(passwordObject), new PasswordObject(password, platform));
                return true;
            }
        }
        return false;
    }

    public boolean addPassword(String password, String platform) {
        if (this.hasPassword(platform)) {
            return false;
        }
        try {
            passwordObjects.add(new PasswordObject(password, platform));
            return true;
        } catch (EncryptionException e) {
            return false;
        }
    }

    public boolean removePassword(String platform) {
        for (PasswordObject passwordObject : passwordObjects) {
            if (passwordObject.getPlatform().equals(platform)) {
                passwordObjects.remove(passwordObject);
                return true;
            }
        }
        return false;
    }

    public Optional<String[]> getSealedObjects() {
        Cipher cipher = this.passwordEncryptor.getCipher().orElse(null);
        if (Objects.isNull(cipher)) {
            return Optional.empty();
        }
        String[] sealedObjects = new String[passwordObjects.size()];
        for (int i = 0; i < passwordObjects.size(); i++) {
            sealedObjects[i] = passwordObjects.get(i).sealObject(cipher).orElse(null);
            if (Objects.isNull(sealedObjects[i])) {
                return Optional.empty();
            }
        }
        return Optional.of(sealedObjects);
    }

}
