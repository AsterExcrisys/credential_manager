package com.asterexcrisys.acm.constants;

@SuppressWarnings("unused")
public final class Hashing {

     public static final int SALT_SIZE = 16;
     public static final int HASH_SIZE = 32;
     public static final int ITERATION_COUNT = 16;
     public static final int MEMORY_USAGE = 262144;
     public static final int PARALLELISM_COUNT = 4;
     public static final int KEY_SIZE = 256;
     public static final int KEY_ITERATION_COUNT = 1000000;
     public static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";

     private Hashing() {
         // This class should not be instantiable
     }

}