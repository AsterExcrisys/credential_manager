package com.asterexcrisys.acm;

import com.asterexcrisys.acm.exceptions.DatabaseException;
import com.asterexcrisys.acm.exceptions.DerivationException;
import com.asterexcrisys.acm.exceptions.HashingException;
import com.asterexcrisys.acm.types.encryption.Credential;
import com.asterexcrisys.acm.types.encryption.Vault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("unused")
public class CredentialManagerTest {

    private static Vault vault;
    private CredentialManager manager;

    @BeforeAll
    public static void setAllUp() throws DerivationException, DatabaseException {
        try (VaultManager manager = new VaultManager("masterKey", "sealedKey")) {
            manager.addVault("name", "password");
            manager.getVault("name", "password").ifPresent(value -> vault = value);
        }
    }

    @BeforeEach
    public void setUp() throws DerivationException, DatabaseException, NoSuchAlgorithmException, HashingException {
        manager = new CredentialManager("name", "password", vault.getEncryptor().getSealedSalt());
    }

    @AfterEach
    public void tearDown() {
        manager.close();
        manager = null;
    }

    @Test
    public void shouldAddCredentialToDatabase() {
        manager.addCredential("platform1", "username", "password");
        Optional<Credential> credential = manager.getCredential("platform1");
        assertTrue(credential.isPresent());
        Optional<String> username = credential.get().getDecryptedUsername();
        Optional<String> password = credential.get().getDecryptedPassword();
        assertTrue(username.isPresent());
        assertTrue(password.isPresent());
        assertEquals("username", username.get());
        assertEquals("password", password.get());
    }

    @Test
    public void shouldRemoveCredentialToDatabase() {
        manager.addCredential("platform2", "username", "password");
        Optional<Credential> credential = manager.getCredential("platform2");
        assertTrue(credential.isPresent());
        manager.removeCredential("platform2");
        credential = manager.getCredential("platform2");
        assertTrue(credential.isEmpty());
    }

}