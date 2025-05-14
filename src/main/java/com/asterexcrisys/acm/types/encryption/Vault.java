package com.asterexcrisys.acm.types.encryption;

import com.asterexcrisys.acm.exceptions.DerivationException;
import com.asterexcrisys.acm.exceptions.HashingException;
import com.asterexcrisys.acm.services.Utility;
import com.asterexcrisys.acm.services.encryption.KeyEncryptor;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

@SuppressWarnings("unused")
public class Vault {

    private final KeyEncryptor encryptor;
    private final String name;
    private final String password;

    public Vault(String name, String password) throws NullPointerException, HashingException, DerivationException, NoSuchAlgorithmException {
        encryptor = new KeyEncryptor(password);
        this.name = Objects.requireNonNull(name);
        this.password = Utility.hash(Objects.requireNonNull(password)).orElseThrow(HashingException::new);
    }

    public Vault(String name, String password, String sealedSalt) throws DerivationException, HashingException, NoSuchAlgorithmException {
        encryptor = new KeyEncryptor(password, sealedSalt);
        this.name = Objects.requireNonNull(name);
        this.password = Utility.hash(Objects.requireNonNull(password)).orElseThrow(HashingException::new);
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

}