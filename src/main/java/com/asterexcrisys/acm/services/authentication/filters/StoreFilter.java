package com.asterexcrisys.acm.services.authentication.filters;

import de.fhg.iosb.iad.tpm.TpmEngine;

@SuppressWarnings("unused")
public sealed interface StoreFilter extends Filter<TpmEngine> permits ValidationStoreFilter, AttestationStoreFilter {

    // This is just a marker interface

}