package com.asterexcrisys.acm.services.authentication.filters;

import com.asterexcrisys.acm.constants.StorageConstants;
import com.asterexcrisys.acm.types.authentication.FilterResult;
import com.asterexcrisys.acm.utility.StorageUtility;
import de.fhg.iosb.iad.tpm.TpmEngine;
import de.fhg.iosb.iad.tpm.TpmEngine.TpmEngineException;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public final class ValidationStoreFilter implements StoreFilter {

    private final Map<Integer, String> expectedPcrValues;

    public ValidationStoreFilter(Map<Integer, String> expectedPcrValues) {
        this.expectedPcrValues = Objects.requireNonNull(expectedPcrValues);
    }

    public FilterResult filter(TpmEngine data) throws TpmEngineException {
        if (data == null) {
            return FilterResult.FAILURE;
        }
        Map<Integer, String> currentPcrValues = data.getPcrValues(StorageConstants.TPM_PCR_SELECTION);
        if (currentPcrValues == null || currentPcrValues.isEmpty()) {
            return FilterResult.FAILURE;
        }
        if (!StorageUtility.comparePcrValues(currentPcrValues, expectedPcrValues)) {
            return FilterResult.FAILURE;
        }
        return FilterResult.CONTINUE;
    }

}