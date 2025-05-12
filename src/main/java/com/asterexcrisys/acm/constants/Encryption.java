package com.asterexcrisys.acm.constants;

@SuppressWarnings("unused")
public final class Encryption {

    public static final int INITIALIZATION_VECTOR_SIZE = 16;
    public static final int AUTHENTICATION_TAG_SIZE = 128;
    public static final int KEY_SIZE = 256;
    public static final String GENERATOR_ALGORITHM = "AES";
    public static final String ENCRYPTION_TRANSFORMATION = "AES/GCM/NoPadding";

    private Encryption() {
        // This class should not be instantiable
    }

}