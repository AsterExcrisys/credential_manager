package com.asterexcrisys.acm.services.authentication;

import com.asterexcrisys.acm.constants.StorageConstants;
import com.asterexcrisys.acm.services.authentication.filters.DatabaseFilter;
import com.asterexcrisys.acm.services.authentication.filters.StoreFilter;
import com.asterexcrisys.acm.services.persistence.VaultDatabase;
import com.asterexcrisys.acm.types.authentication.FilterResult;
import com.asterexcrisys.acm.types.encryption.Vault;
import com.asterexcrisys.acm.utility.StorageUtility;
import de.fhg.iosb.iad.tpm.TpmEngine;
import de.fhg.iosb.iad.tpm.TpmEngine.TpmEngineException;
import java.util.Optional;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class Authentication {

    private static final Logger LOGGER = Logger.getLogger(Authentication.class.getName());

    private Authentication() {
        // This class should not be instantiable
    }

    public static Optional<Integer> authenticate(TpmEngine engine, StoreFilter... filters) {
        if (filters != null && filters.length > 0) {
            try {
                for (StoreFilter filter : filters) {
                    if (filter == null) {
                        continue;
                    }
                    FilterResult result = filter.filter(engine);
                    if (result == FilterResult.FAILURE) {
                        return Optional.empty();
                    }
                    if (result == FilterResult.SUCCESS) {
                        break;
                    }
                }
            } catch (Exception e) {
                LOGGER.warning("Error processing store filter: " + e.getMessage());
                return Optional.empty();
            }
        }
        return authenticate(engine);
    }

    public static Optional<Integer> authenticate(TpmEngine engine) {
        if (engine == null) {
            return Optional.empty();
        }
        try {
            Optional<byte[]> nonce = StorageUtility.generateNonce();
            if (nonce.isEmpty()) {
                return Optional.empty();
            }
            int policySession = engine.startPcrPolicyAuthSession(StorageConstants.TPM_PCR_SELECTION, nonce.get());
            return Optional.of(policySession);
        } catch (TpmEngineException e) {
            LOGGER.severe("Error authenticating TPM session: " + e.getMessage());
            return Optional.empty();
        }
    }

    public static Optional<Vault> authenticate(VaultDatabase database, String name, String password, DatabaseFilter... filters) {
        if (filters != null && filters.length > 0) {
            try {
                for (DatabaseFilter filter : filters) {
                    if (filter == null) {
                        continue;
                    }
                    FilterResult result = filter.filter(database);
                    if (result == FilterResult.FAILURE) {
                        return Optional.empty();
                    }
                    if (result == FilterResult.SUCCESS) {
                        break;
                    }
                }
            } catch (Exception e) {
                LOGGER.warning("Error processing database filter: " + e.getMessage());
                return Optional.empty();
            }
        }
        return authenticate(database, name, password);
    }

    public static Optional<Vault> authenticate(VaultDatabase database, String name, String password) {
        if (database == null || name == null || password == null || name.isBlank() || password.isBlank()) {
            return Optional.empty();
        }
        return database.getVault(name, password);
    }

}