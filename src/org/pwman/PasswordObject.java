package org.pwman;

import javax.crypto.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

public class PasswordObject implements Serializable {

    private final PasswordEncryptor passwordEncryptor;
    private final String password;
    private final String platform;

    public PasswordObject(String password, String platform) throws NullPointerException, EncryptionException {
        this.passwordEncryptor = new PasswordEncryptor();
        this.password = this.passwordEncryptor.encryptPassword(Objects.requireNonNull(password)).orElseThrow(EncryptionException::new);
        this.platform = Objects.requireNonNull(platform);
    }

    public PasswordObject(SecretKey key, byte[] vector, String password, String platform) throws NullPointerException, EncryptionException {
        this.passwordEncryptor = new PasswordEncryptor(Objects.requireNonNull(key), Objects.requireNonNull(vector));
        this.password = this.passwordEncryptor.encryptPassword(Objects.requireNonNull(password)).orElseThrow(EncryptionException::new);
        this.platform = Objects.requireNonNull(platform);
    }

    public Optional<String> getPassword() {
        return this.passwordEncryptor.decryptPassword(this.password);
    }

    public String getPlatform() {
        return this.platform;
    }

    public Optional<String> sealObject(Cipher cipher) {
        if (Objects.isNull(cipher)) {
            return Optional.empty();
        }
        try {
            SealedObject sealedObject = new SealedObject(this, cipher);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            CipherOutputStream cipherStream = new CipherOutputStream(byteStream, cipher);
            ObjectOutputStream objectStream = new ObjectOutputStream(cipherStream);
            objectStream.writeObject(sealedObject);
            cipherStream.close();
            return Optional.ofNullable(Base64.getEncoder().encodeToString(byteStream.toByteArray()));
        } catch (IOException | IllegalBlockSizeException e) {
            return Optional.empty();
        }
    }

}
