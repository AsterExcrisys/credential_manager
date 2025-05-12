package com.asterexcrisys.acm.services.authentication;

import com.asterexcrisys.acm.services.persistence.VaultDatabase;
import com.asterexcrisys.acm.types.encryption.Vault;
import java.util.Optional;

@SuppressWarnings("unused")
public final class Authentication {

    private Authentication() {
        // This class should not be instantiable
    }

    public static Optional<Vault> authenticate(VaultDatabase database, String name, String password) {
        if (database == null || name == null || password == null || name.isBlank() || password.isBlank()) {
            return Optional.empty();
        }
        return database.getVault(name, password);
    }

}