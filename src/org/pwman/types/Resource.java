package org.pwman.types;

public interface Resource {

    int KEY_SIZE = 256;
    int VECTOR_SIZE = 16;
    String ENCRYPTION_STANDARD = "AES";
    String ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";
    String GENERATOR_PROVIDER = "SUN";
    String GENERATOR_ALGORITHM = "SHA1PRNG";
    int DEFAULT_LENGTH = 12;
    String LOWERCASE_LETTERS = "abcdefghijklmnopqrstuvwxyz";
    String UPPERCASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String DECIMAL_NUMBERS = "0123456789";
    String SPECIAL_CHARACTERS = "-+.*^$;,!£%&=@#_<>";

}