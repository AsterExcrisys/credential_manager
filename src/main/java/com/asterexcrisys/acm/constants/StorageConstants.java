package com.asterexcrisys.acm.constants;

import java.util.Set;

@SuppressWarnings("unused")
public class StorageConstants {

    public static final int NONCE_LENGTH = 16;
    public static final String MASTER_KEY_STORE = "master";
    public static final String KEY_GENERATION_ALGORITHM = "RSA";
    public static final String ENCRYPTION_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    public static final Set<Integer> TPM_PCR_SELECTION = Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);

    private StorageConstants() {
        // This class should not be instantiable
    }

}