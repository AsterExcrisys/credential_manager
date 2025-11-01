package com.asterexcrisys.acm;

import com.asterexcrisys.acm.exceptions.DatabaseException;
import com.asterexcrisys.acm.exceptions.DerivationException;
import com.asterexcrisys.acm.exceptions.HashingException;
import com.asterexcrisys.acm.services.encryption.GenericEncryptor;
import com.asterexcrisys.acm.types.encryption.Credential;
import com.asterexcrisys.acm.types.encryption.Vault;
import com.asterexcrisys.acm.utility.PathUtility;
import org.junit.jupiter.api.*;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("unused")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CredentialManagerUnitTests {

    private Vault vault;
    private CredentialManager manager;

    @BeforeAll
    public void setAllUp() throws DatabaseException {
        PathUtility.deleteRecursively(Paths.get("./data/"));
        try (VaultManager manager = new VaultManager(GenericEncryptor.generateKey().orElseThrow())) {
            manager.addVault("name", "password");
            vault = manager.getVault("name", "password").orElseThrow();
        }
    }

    @AfterAll
    public void tearAllDown() {
        PathUtility.deleteRecursively(Paths.get("./data/"));
    }

    @BeforeEach
    public void setUp() throws DerivationException, DatabaseException, NoSuchAlgorithmException, HashingException {
        manager = new CredentialManager(vault.getEncryptor().getSealedSalt(), vault.getHashedPassword(), "name", "password");
    }

    @AfterEach
    public void tearDown() {
        manager.removeAllCredentials();
        manager.close();
    }

    @Test
    public void shouldAddCredentialToDatabase() {
        assertTrue(manager.addCredential("platform1", "username", "password"));
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
        assertTrue(manager.addCredential("platform2", "username", "password"));
        Optional<Credential> credential = manager.getCredential("platform2");
        assertTrue(credential.isPresent());
        assertTrue(manager.removeCredential("platform2"));
        credential = manager.getCredential("platform2");
        assertTrue(credential.isEmpty());
    }

    @Test
    public void shouldUpdateCredentialToDatabase() {
        assertTrue(manager.addCredential("platform3", "username1", "password1"));
        Optional<Credential> credential = manager.getCredential("platform3");
        assertTrue(credential.isPresent());
        Optional<String> username = credential.get().getDecryptedUsername();
        Optional<String> password = credential.get().getDecryptedPassword();
        assertTrue(username.isPresent());
        assertTrue(password.isPresent());
        assertEquals("username1", username.get());
        assertEquals("password1", password.get());
        assertTrue(manager.setCredential("platform3", "username2", "password2"));
        credential = manager.getCredential("platform3");
        assertTrue(credential.isPresent());
        username = credential.get().getDecryptedUsername();
        password = credential.get().getDecryptedPassword();
        assertTrue(username.isPresent());
        assertTrue(password.isPresent());
        assertEquals("username2", username.get());
        assertEquals("password2", password.get());
    }

}