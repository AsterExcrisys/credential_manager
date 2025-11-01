package com.asterexcrisys.acm;

import com.asterexcrisys.acm.exceptions.DatabaseException;
import com.asterexcrisys.acm.services.encryption.GenericEncryptor;
import com.asterexcrisys.acm.utility.HashingUtility;
import com.asterexcrisys.acm.types.encryption.Vault;
import com.asterexcrisys.acm.utility.PathUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import java.nio.file.Paths;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("unused")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class VaultManagerUnitTests {

    private VaultManager manager;

    @BeforeEach
    public void setUp() throws DatabaseException {
        PathUtility.deleteRecursively(Paths.get("./data/"));
        manager = new VaultManager(GenericEncryptor.generateKey().orElseThrow());
    }

    @AfterEach
    public void tearDown() {
        manager.close();
        PathUtility.deleteRecursively(Paths.get("./data/"));
    }

    @Test
    public void shouldAddVaultToDatabase() {
        assertTrue(manager.addVault("name1", "password"));
        Optional<Vault> vault = manager.getVault("name", "password");
        Optional<String> password = HashingUtility.hashPassword("password");
        assertTrue(vault.isPresent());
        assertTrue(password.isPresent());
        assertEquals(password.get(), vault.get().getHashedPassword());
    }

    @Test
    public void shouldRemoveVaultFromDatabase() {
        assertTrue(manager.addVault("name2", "password"));
        Optional<Vault> vault = manager.getVault("name2", "password");
        assertTrue(vault.isPresent());
        assertTrue(manager.removeVault("name2", "password"));
        vault = manager.getVault("name2", "password");
        assertTrue(vault.isEmpty());
    }

}