package com.asterexcrisys.acm;

import com.asterexcrisys.acm.exceptions.DatabaseException;
import com.asterexcrisys.acm.exceptions.DerivationException;
import com.asterexcrisys.acm.utility.HashingUtility;
import com.asterexcrisys.acm.types.encryption.Vault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("unused")
public class VaultManagerUnitTests {

    private VaultManager manager;

    @BeforeEach
    public void setUp() throws DerivationException, DatabaseException {
        manager = new VaultManager("masterKey", "sealedSalt");
    }

    @AfterEach
    public void tearDown() {
        manager.close();
        manager = null;
    }

    @Test
    public void shouldAddVaultToDatabase() {
        manager.addVault("name1", "password");
        Optional<Vault> vault = manager.getVault("name", "password");
        Optional<String> password = HashingUtility.hashPassword("password");
        assertTrue(vault.isPresent());
        assertTrue(password.isPresent());
        assertEquals(password.get(), vault.get().getHashedPassword());
    }

    @Test
    public void shouldRemoveVaultFromDatabase() {
        manager.addVault("name2", "password");
        Optional<Vault> vault = manager.getVault("name2", "password");
        assertTrue(vault.isPresent());
        manager.removeVault("name2", "password");
        vault = manager.getVault("name2", "password");
        assertTrue(vault.isEmpty());
    }

}