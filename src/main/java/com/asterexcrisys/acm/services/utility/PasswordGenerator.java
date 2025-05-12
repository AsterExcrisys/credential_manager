package com.asterexcrisys.acm.services.utility;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@SuppressWarnings("unused")
public class PasswordGenerator {

    private static final int DEFAULT_PASSWORD_SIZE = 12;
    private static final String LOWERCASE_LETTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DECIMAL_NUMBERS = "0123456789";
    private static final String SPECIAL_CHARACTERS = "-+.*^$;,!£%&=@#_<>";
    private final SecureRandom random;

    public PasswordGenerator() throws NoSuchAlgorithmException {
        random = SecureRandom.getInstanceStrong();
    }

    public PasswordGenerator(byte[] seed) {
        random = new SecureRandom(seed);
    }

    public String generate(int length) {
        if (length < 10) {
            return null;
        }
        int unit = Math.floorDiv(length, 10), index;
        int a = 4 * unit, b = 2 * unit, c = 3 * unit, d = length - a - b - c;
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < a; i++) {
            index = Math.abs(random.nextInt() % LOWERCASE_LETTERS.length());
            password.append(LOWERCASE_LETTERS.charAt(index));
        }
        for (int i = 0; i < b; i++) {
            index = Math.abs(random.nextInt() % UPPERCASE_LETTERS.length());
            password.append(UPPERCASE_LETTERS.charAt(index));
        }
        for (int i = 0; i < c; i++) {
            index = Math.abs(random.nextInt() % DECIMAL_NUMBERS.length());
            password.append(DECIMAL_NUMBERS.charAt(index));
        }
        for (int i = 0; i < d; i++) {
            index = Math.abs(random.nextInt() % SPECIAL_CHARACTERS.length());
            password.append(SPECIAL_CHARACTERS.charAt(index));
        }
        return shuffle(password.toString());
    }

    public String generate() {
        return generate(DEFAULT_PASSWORD_SIZE);
    }

    private String shuffle(String password) {
        char[] characters = password.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = characters[i];
            characters[i] = characters[j];
            characters[j] = tmp;
        }
        return new String(characters);
    }
    
}