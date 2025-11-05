package com.asterexcrisys.acm.constants;

@SuppressWarnings("unused")
public final class EncryptionConstants {

    public static final int INITIALIZATION_VECTOR_SIZE = 12;
    public static final int AUTHENTICATION_TAG_SIZE = 128;
    public static final int KEY_SIZE = 256;
    public static final int CHUNK_SIZE = 4096;
    public static final String KEY_GENERATION_ALGORITHM = "AES";
    public static final String ENCRYPTION_TRANSFORMATION = "AES/GCM/NoPadding";

    private EncryptionConstants() {
        // This class should not be instantiable
    }

}