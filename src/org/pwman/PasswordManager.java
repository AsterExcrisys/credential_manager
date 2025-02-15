package org.pwman;

import org.pwman.exceptions.EncryptionException;
import org.pwman.services.PasswordEncryptor;
import org.pwman.services.PasswordGenerator;
import org.pwman.services.PasswordTester;
import org.pwman.types.EncryptorMode;
import org.pwman.types.Pair;
import org.pwman.types.PasswordObject;
import org.pwman.types.PasswordStrength;
import javax.crypto.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.*;

public class PasswordManager {

    private final PasswordEncryptor passwordEncryptor;
    private final ArrayList<PasswordObject> passwordObjects;
    private final PasswordGenerator passwordGenerator;

    public PasswordManager() throws EncryptionException, NoSuchAlgorithmException, NoSuchProviderException {
        passwordEncryptor = new PasswordEncryptor();
        passwordObjects = new ArrayList<>();
        passwordGenerator = new PasswordGenerator();
    }

    public PasswordManager(SecretKey key, byte[] vector) throws NullPointerException, EncryptionException, NoSuchAlgorithmException, NoSuchProviderException {
        passwordEncryptor = new PasswordEncryptor(Objects.requireNonNull(key), Objects.requireNonNull(vector));
        passwordObjects = new ArrayList<>();
        passwordGenerator = new PasswordGenerator();
    }

    public PasswordManager(String sealedEncryptor, String[] sealedPasswords) throws NullPointerException, EncryptionException, NoSuchAlgorithmException, NoSuchProviderException {
        String[] encryptor = generateEncryptor(Objects.requireNonNull(sealedEncryptor));
        if (encryptor.length != 3) {
            throw new EncryptionException();
        }
        passwordEncryptor = new PasswordEncryptor(Objects.requireNonNull(encryptor[0]), Objects.requireNonNull(encryptor[1]), Objects.requireNonNull(encryptor[2]));
        passwordObjects = new ArrayList<>();
        Optional<PasswordObject[]> passwords = generatePasswords(this.passwordEncryptor.getCipher(EncryptorMode.DECRYPT).orElse(null), sealedPasswords);
        passwords.ifPresent(objects -> this.passwordObjects.addAll(Arrays.asList(objects)));
        passwordGenerator = new PasswordGenerator();
    }

    public String getSealedEncryptor() {
        String[] encryptor = {passwordEncryptor.getSealedKey(), passwordEncryptor.getSealedVector(), passwordEncryptor.getAlgorithm()};
        return Base64.getEncoder().encodeToString(Arrays.toString(encryptor).getBytes());
    }

    public Optional<String> getSealedPassword(String platform) {
        Cipher cipher = passwordEncryptor.getCipher(EncryptorMode.ENCRYPT).orElse(null);
        if (Objects.isNull(cipher)) {
            return Optional.empty();
        }
        for (PasswordObject passwordObject : passwordObjects) {
            if (passwordObject.getPlatform().equals(platform)) {
                return passwordObject.sealObject(cipher);
            }
        }
        return Optional.empty();
    }

    public Optional<String[]> getSealedPasswords() {
        Cipher cipher = passwordEncryptor.getCipher(EncryptorMode.ENCRYPT).orElse(null);
        if (Objects.isNull(cipher)) {
            return Optional.empty();
        }
        String[] sealedPasswords = new String[passwordObjects.size()];
        for (int i = 0; i < passwordObjects.size(); i++) {
            sealedPasswords[i] = passwordObjects.get(i).sealObject(cipher).orElse(null);
            if (Objects.isNull(sealedPasswords[i])) {
                return Optional.empty();
            }
        }
        return Optional.of(sealedPasswords);
    }

    public int countPasswords() {
        return passwordObjects.size();
    }

    public boolean hasPassword(String platform) {
        for (PasswordObject passwordObject : passwordObjects) {
            if (passwordObject.getPlatform().equals(platform)) {
                return true;
            }
        }
        return false;
    }

    public Optional<String> getEncryptedPassword(String platform) {
        for (PasswordObject passwordObject : passwordObjects) {
            if (passwordObject.getPlatform().equals(platform)) {
                return Optional.of(passwordObject.getEncryptedPassword());
            }
        }
        return Optional.empty();
    }

    public Optional<String> getDecryptedPassword(String platform) {
        for (PasswordObject passwordObject : passwordObjects) {
            if (passwordObject.getPlatform().equals(platform)) {
                return passwordObject.getDecryptedPassword();
            }
        }
        return Optional.empty();
    }

    public String[] getPlatforms() {
        String[] platforms = new String[passwordObjects.size()];
        for (int i = 0; i < passwordObjects.size(); i++) {
            platforms[i] = passwordObjects.get(i).getPlatform();
        }
        return platforms;
    }

    public String[] getPasswords() {
        String[] passwords = new String[passwordObjects.size()];
        for (int i = 0; i < passwordObjects.size(); i++) {
            passwords[i] = passwordObjects.get(i).getEncryptedPassword();
        }
        return passwords;
    }

    public HashMap<String, String> getPasswordObjects() {
        HashMap<String, String> passwords = new HashMap<>();
        for (PasswordObject passwordObject : passwordObjects) {
            passwords.put(passwordObject.getPlatform(), passwordObject.getEncryptedPassword());
        }
        return passwords;
    }

    public boolean setPassword(String platform, String password) {
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
        } catch (EncryptionException e) {
            return false;
        }
        return true;
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

    public String generatePassword() {
        return passwordGenerator.generate();
    }

    public String generatePassword(int length) {
        return passwordGenerator.generate(length);
    }

    public Optional<Pair<PasswordStrength, String[]>> testExistingPassword(String platform) {
        for (PasswordObject passwordObject : passwordObjects) {
            if (passwordObject.getPlatform().equals(platform)) {
                Optional<String> password = passwordObject.getDecryptedPassword();
                if (password.isEmpty()) {
                    return Optional.empty();
                }
                PasswordTester passwordTester = new PasswordTester(password.get());
                return Optional.of(new Pair<>(passwordTester.getStrengthGrade(), passwordTester.getSafetyAdvices()));
            }
        }
        return Optional.empty();
    }

    public Pair<PasswordStrength, String[]> testGivenPassword(String password) {
        PasswordTester passwordTester = new PasswordTester(password);
        return new Pair<>(passwordTester.getStrengthGrade(), passwordTester.getSafetyAdvices());
    }

    private static String[] generateEncryptor(String sealedEncryptor) {
        String tmp = new String(Base64.getDecoder().decode(sealedEncryptor));
        tmp = tmp.substring(1, tmp.length() - 1);
        return tmp.split(", ");
    }

    private static Optional<PasswordObject> generatePassword(Cipher cipher, String sealedPassword) {
        if (Objects.isNull(cipher) || Objects.isNull(sealedPassword)) {
            return Optional.empty();
        }
        PasswordObject passwordObject;
        try {
            byte[] bytes = cipher.doFinal(sealedPassword.getBytes());
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            passwordObject = (PasswordObject) objectInputStream.readObject();
        } catch (IOException | IllegalBlockSizeException | BadPaddingException | ClassNotFoundException e) {
            return Optional.empty();
        }
        return Optional.of(passwordObject);
    }

    private static Optional<PasswordObject[]> generatePasswords(Cipher cipher, String[] sealedPasswords) {
        if (Objects.isNull(cipher) || Objects.isNull(sealedPasswords) || sealedPasswords.length == 0) {
            return Optional.empty();
        }
        PasswordObject[] passwordObjects = new PasswordObject[sealedPasswords.length];
        try {
            for (int i = 0; i < sealedPasswords.length; i++) {
                ByteArrayInputStream byteStream = new ByteArrayInputStream(Base64.getDecoder().decode(sealedPasswords[i]));
                CipherInputStream cipherStream = new CipherInputStream(byteStream, cipher);
                ObjectInputStream objectStream = new ObjectInputStream(cipherStream);
                SealedObject sealedObject = (SealedObject) objectStream.readObject();
                passwordObjects[i] = (PasswordObject) sealedObject.getObject(cipher);
                cipherStream.close();
                objectStream.close();
            }
        } catch (IOException | ClassCastException | ClassNotFoundException | IllegalBlockSizeException | BadPaddingException e) {
            return Optional.empty();
        }
        return Optional.of(passwordObjects);
    }

}