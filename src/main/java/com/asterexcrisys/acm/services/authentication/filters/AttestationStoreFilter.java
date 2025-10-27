package com.asterexcrisys.acm.services.authentication.filters;

import com.asterexcrisys.acm.constants.StorageConstants;
import com.asterexcrisys.acm.types.authentication.FilterResult;
import com.asterexcrisys.acm.utility.StorageUtility;
import de.fhg.iosb.iad.tpm.TpmEngine;
import de.fhg.iosb.iad.tpm.TpmEngine.TpmEngineException;
import de.fhg.iosb.iad.tpm.TpmEngine.TpmLoadedKey;
import de.fhg.iosb.iad.tpm.TpmValidator;
import de.fhg.iosb.iad.tpm.TpmValidator.TpmValidationException;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unused")
public final class AttestationStoreFilter implements StoreFilter {

    public FilterResult filter(TpmEngine data) throws TpmEngineException, TpmValidationException {
        if (data == null) {
            return FilterResult.FAILURE;
        }
        TpmLoadedKey quotingKey = data.loadQk();
        if (quotingKey == null) {
            return FilterResult.FAILURE;
        }
        Optional<byte[]> qualifyingData = StorageUtility.generateNonce();
        if (qualifyingData.isEmpty()) {
            return FilterResult.FAILURE;
        }
        byte[] quote = data.quote(quotingKey.handle, qualifyingData.get(), StorageConstants.TPM_PCR_SELECTION);
        Map<Integer, String> pcrValues = data.getPcrValues(StorageConstants.TPM_PCR_SELECTION);
        if (pcrValues == null || pcrValues.isEmpty()) {
            return FilterResult.FAILURE;
        }
        TpmValidator validator = new TpmValidator();
        boolean isValid = validator.validateQuote(quote, qualifyingData.get(), quotingKey.outPublic, pcrValues);
        data.flushKey(quotingKey.handle);
        return isValid? FilterResult.CONTINUE:FilterResult.FAILURE;
    }

}