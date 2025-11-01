package com.asterexcrisys.acm.types.encryption;

import com.asterexcrisys.acm.exceptions.DerivationException;
import com.asterexcrisys.acm.exceptions.HashingException;
import com.asterexcrisys.acm.utility.HashingUtility;
import com.asterexcrisys.acm.services.encryption.KeyEncryptor;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

@SuppressWarnings("unused")
public class Vault {

    private final KeyEncryptor encryptor;
    private final String name;
    private final String password;
    private boolean isLocked;

    public Vault(String name, String password) throws NullPointerException, HashingException, DerivationException, NoSuchAlgorithmException {
        encryptor = new KeyEncryptor(password);
        this.name = Objects.requireNonNull(name);
        this.password = HashingUtility.hashPassword(Objects.requireNonNull(password)).orElseThrow(HashingException::new);
        isLocked = false;
    }

    public Vault(String name, String password, boolean isLocked) throws NullPointerException, HashingException, DerivationException, NoSuchAlgorithmException {
        encryptor = new KeyEncryptor(password);
        this.name = Objects.requireNonNull(name);
        this.password = HashingUtility.hashPassword(Objects.requireNonNull(password)).orElseThrow(HashingException::new);
        this.isLocked = isLocked;
    }

    public Vault(String sealedSalt, String name, String password) throws DerivationException, HashingException, NoSuchAlgorithmException {
        encryptor = new KeyEncryptor(password, sealedSalt);
        this.name = Objects.requireNonNull(name);
        this.password = HashingUtility.hashPassword(Objects.requireNonNull(password)).orElseThrow(HashingException::new);
        isLocked = false;
    }

    public Vault(String sealedSalt, String name, String password, boolean isLocked) throws DerivationException, HashingException {
        encryptor = new KeyEncryptor(password, sealedSalt);
        this.name = Objects.requireNonNull(name);
        this.password = HashingUtility.hashPassword(Objects.requireNonNull(password)).orElseThrow(HashingException::new);
        this.isLocked = isLocked;
    }

    public Vault(String sealedSalt, String hashedPassword, String name, String password) throws DerivationException {
        encryptor = new KeyEncryptor(password, sealedSalt);
        this.name = Objects.requireNonNull(name);
        this.password = Objects.requireNonNull(hashedPassword);
        isLocked = false;
    }

    public Vault(String sealedSalt, String hashedPassword, String name, String password, boolean isLocked) throws DerivationException, HashingException, NoSuchAlgorithmException {
        encryptor = new KeyEncryptor(password, sealedSalt);
        this.name = Objects.requireNonNull(name);
        this.password = Objects.requireNonNull(hashedPassword);
        this.isLocked = isLocked;
    }

    public KeyEncryptor getEncryptor() {
        return encryptor;
    }

    public String getName() {
        return name;
    }

    public String getHashedPassword() {
        return password;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

}