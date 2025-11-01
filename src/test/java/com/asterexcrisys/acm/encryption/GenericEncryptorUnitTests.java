package com.asterexcrisys.acm.encryption;

import com.asterexcrisys.acm.exceptions.EncryptionException;
import com.asterexcrisys.acm.services.encryption.GenericEncryptor;
import com.asterexcrisys.acm.services.encryption.KeyEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GenericEncryptorUnitTests {

    private GenericEncryptor encryptor;

    @BeforeEach
    public void setUp() throws EncryptionException {
        encryptor = new GenericEncryptor();
    }

    @Test
    public void shouldEncryptAndDecrypt() {
        assertDoesNotThrow(() -> {
            String encryptedData = encryptor.encrypt("test").orElseThrow();
            String decryptedData = encryptor.decrypt(encryptedData).orElseThrow();
            assertEquals("test", decryptedData);
        });
    }

    @Test
    public void shouldEncryptGeneratedKey() {
        assertDoesNotThrow(() -> {
            String firstKey = encryptor.getEncryptedKey(new KeyEncryptor("test1")).orElseThrow();
            String secondKey = encryptor.getEncryptedKey(new KeyEncryptor("test2")).orElseThrow();
            assertNotEquals(encryptor.getDecryptedKey(), firstKey);
            assertNotEquals(encryptor.getDecryptedKey(), secondKey);
            assertNotEquals(firstKey, secondKey);
        });
    }

    @Test
    public void shouldRecoverStateFromSealedKey() {
        assertDoesNotThrow(() -> {
            String previousKey = encryptor.getDecryptedKey();
            GenericEncryptor currentEncryptor = new GenericEncryptor(previousKey);
            assertEquals(previousKey, currentEncryptor.getDecryptedKey());
        });
    }

}