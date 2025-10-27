package com.asterexcrisys.acm.services.authentication.filters;

import com.asterexcrisys.acm.services.persistence.VaultDatabase;
import com.asterexcrisys.acm.types.authentication.FilterResult;
import java.util.Objects;

@SuppressWarnings("unused")
public final class VerificationDatabaseFilter implements DatabaseFilter {

    private final String vaultName;

    public VerificationDatabaseFilter(String vaultName) {
        this.vaultName = Objects.requireNonNull(vaultName);
    }

    public FilterResult filter(VaultDatabase data) {
        if (data == null) {
            return FilterResult.FAILURE;
        }
        return data.hasVault(vaultName, false)? FilterResult.CONTINUE:FilterResult.FAILURE;
    }

}