package com.asterexcrisys.acm.utility;

import com.asterexcrisys.acm.constants.StorageConstants;
import de.fhg.iosb.iad.tpm.TpmEngine;
import de.fhg.iosb.iad.tpm.TpmEngine.TpmEngineException;
import java.security.*;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class StorageUtility {

    private static final Logger LOGGER = Logger.getLogger(StorageUtility.class.getName());

    private StorageUtility() {
        // This class should not be instantiable
    }

    public static Optional<byte[]> generateNonce() {
        return generateNonce(StorageConstants.NONCE_LENGTH);
    }

    public static Optional<byte[]> generateNonce(int length) {
        if (length < 1) {
            return Optional.empty();
        }
        try {
            byte[] nonce = new byte[length];
            SecureRandom.getInstanceStrong().nextBytes(nonce);
            return Optional.of(nonce);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warning("Error generating nonce: " + e.getMessage());
            return Optional.empty();
        }
    }

    public static Optional<Map<Integer, String>> registerPcrValues(TpmEngine engine) {
        try {
            return Optional.ofNullable(engine.getPcrValues(StorageConstants.TPM_PCR_SELECTION));
        } catch (TpmEngineException e) {
            LOGGER.warning("Error registering PCR values: " + e.getMessage());
            return Optional.empty();
        }
    }

    public static boolean comparePcrValues(Map<Integer, String> actual, Map<Integer, String> expected) {
        if (actual == null || expected == null  || actual.isEmpty() || expected.isEmpty() || actual.size() != expected.size()) {
            return false;
        }
        for (Entry<Integer, String> entry : actual.entrySet()) {
            Integer pcrIndex = entry.getKey();
            String currentValue = entry.getValue();
            String expectedValue = expected.get(pcrIndex);
            if (!currentValue.equals(expectedValue)) {
                return false;
            }
        }
        return true;
    }

}