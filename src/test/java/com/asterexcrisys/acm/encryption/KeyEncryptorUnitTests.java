package com.asterexcrisys.acm.encryption;

import com.asterexcrisys.acm.exceptions.DerivationException;
import com.asterexcrisys.acm.services.encryption.KeyEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("unused")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KeyEncryptorUnitTests {

    private KeyEncryptor encryptor;

    @BeforeEach
    public void setUp() throws DerivationException, NoSuchAlgorithmException {
        encryptor = new KeyEncryptor(UUID.randomUUID().toString());
    }

    @Test
    public void shouldEncryptAndDecrypt() {
        assertDoesNotThrow(() -> {
            String encryptedData = encryptor.encrypt("test").orElseThrow();
            String decryptedData = encryptor.decrypt(encryptedData).orElseThrow();
            assertEquals("test", decryptedData);
        });
    }

}