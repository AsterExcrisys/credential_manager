package com.asterexcrisys.acm.services.authentication.filters;

import com.asterexcrisys.acm.services.persistence.VaultDatabase;

@SuppressWarnings("unused")
public sealed interface DatabaseFilter extends Filter<VaultDatabase> permits VerificationDatabaseFilter {

    // This is just a marker interface

}