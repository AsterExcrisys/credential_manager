package pws.lib;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.Serializable;
import java.security.*;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

public class PasswordEncryptor implements Serializable {

    private final SecretKey key;
    private final byte[] vector;

    public PasswordEncryptor() throws EncryptionException {
        this.key = this.generateKey(Resource.KEY_SIZE).orElseThrow(EncryptionException::new);
        this.vector = this.generateVector();
    }

    public PasswordEncryptor(SecretKey key, byte[] vector) throws NullPointerException, EncryptionException {
        this.key = Objects.requireNonNull(key);
        this.vector = Objects.requireNonNull(vector);
    }

    public Optional<Cipher> getCipher() {
        try {
            Cipher cipher = Cipher.getInstance(Resource.ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, this.key, new IvParameterSpec(this.vector));
            return Optional.of(cipher);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException e) {
            return Optional.empty();
        }
    }

    public Optional<String> encryptPassword(String password) {
        if (Objects.isNull(password)) {
            return Optional.empty();
        }
        byte[] result;
        try {
            Cipher cipher = Cipher.getInstance(Resource.ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, this.key, new IvParameterSpec(this.vector));
            result = cipher.doFinal(password.getBytes());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            return Optional.empty();
        }
        return Optional.ofNullable(Base64.getEncoder().encodeToString(result));
    }

    public Optional<String> decryptPassword(String password) {
        if (Objects.isNull(password)) {
            return Optional.empty();
        }
        byte[] result;
        try {
            Cipher cipher = Cipher.getInstance(Resource.ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, this.key, new IvParameterSpec(this.vector));
            result = cipher.doFinal(Base64.getDecoder().decode(password));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            return Optional.empty();
        }
        return Optional.of(new String(result));
    }

    private Optional<SecretKey> generateKey(int size) {
        KeyGenerator generator;
        try {
            generator = KeyGenerator.getInstance(Resource.ENCRYPTION_STANDARD);
            generator.init(size);
        } catch (NoSuchAlgorithmException | InvalidParameterException e) {
            return Optional.empty();
        }
        return Optional.ofNullable(generator.generateKey());
    }

    private byte[] generateVector() {
        byte[] vector = new byte[Resource.VECTOR_SIZE];
        new SecureRandom().nextBytes(vector);
        return vector;
    }

}