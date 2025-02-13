package pws.lib;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

public class PasswordGenerator {

    private final SecureRandom random;

    public PasswordGenerator() throws NoSuchAlgorithmException, NoSuchProviderException {
        this.random = SecureRandom.getInstance(Resource.GENERATOR_ALGORITHM, Resource.GENERATOR_PROVIDER);
    }

    public PasswordGenerator(byte[] seed) {
        this.random = new SecureRandom(seed);
    }

    public String generate(int length) {
        if (length < 10) {
            return null;
        }
        int unit = Math.floorDiv(length, 10), index;
        int a = 5 * unit, b = 1 * unit, c = 3 * unit, d = length - a - b - c;
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < a; i++) {
            index = Math.abs(random.nextInt() % Resource.LOWERCASE_LETTERS.length());
            password.append(Resource.LOWERCASE_LETTERS.charAt(index));
        }
        for (int i = 0; i < b; i++) {
            index = Math.abs(random.nextInt() % Resource.UPPERCASE_LETTERS.length());
            password.append(Resource.UPPERCASE_LETTERS.charAt(index));
        }
        for (int i = 0; i < c; i++) {
            index = Math.abs(random.nextInt() % Resource.DECIMAL_NUMBERS.length());
            password.append(Resource.DECIMAL_NUMBERS.charAt(index));
        }
        for (int i = 0; i < d; i++) {
            index = Math.abs(random.nextInt() % Resource.SPECIAL_CHARACTERS.length());
            password.append(Resource.SPECIAL_CHARACTERS.charAt(index));
        }
        return shuffle(password.toString());
    }

    public String generate() {
        return generate(Resource.DEFAULT_LENGTH);
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