package com.asterexcrisys.acm.services;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.crypto.params.Argon2Parameters.Builder;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;

@SuppressWarnings("unused")
public final class Utility {

    private Utility() {
        // This class should not be instantiable
    }

    public static Optional<String> hash(String data) {
        if (data == null || data.isBlank()) {
            return Optional.empty();
        }
        byte[] salt = new byte[16];
        Builder builder = new Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withIterations(16)
                .withMemoryAsKB(262144)
                .withParallelism(4)
                .withSalt(salt);
        byte[] result = new byte[32];
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(builder.build());
        generator.generateBytes(data.getBytes(StandardCharsets.UTF_8), result, 0, result.length);
        return Optional.of(Base64.getEncoder().encodeToString(result));
    }

    public static boolean verify(String data, String hash) {
        if (data == null || hash == null || data.isBlank() || hash.isBlank()) {
            return false;
        }
        byte[] salt = new byte[16];
        Builder builder = new Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withIterations(16)
                .withMemoryAsKB(262144)
                .withParallelism(4)
                .withSalt(salt);
        Argon2BytesGenerator verifier = new Argon2BytesGenerator();
        verifier.init(builder.build());
        byte[] result = new byte[32];
        verifier.generateBytes(data.getBytes(StandardCharsets.UTF_8), result, 0, result.length);
        return Base64.getEncoder().encodeToString(result).equals(hash);
    }

    public static Optional<String> derive(String data, byte[] salt) {
        if (data == null || data.isBlank() || salt == null || salt.length == 0) {
            return Optional.empty();
        }
        try {
            KeySpec keySpec = new PBEKeySpec(data.toCharArray(), salt, 1000000, 256);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return Optional.ofNullable(Base64.getEncoder().encodeToString(secretKeyFactory.generateSecret(keySpec).getEncoded()));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            return Optional.empty();
        }
    }

    public static String getCurrentDate() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return now.format(formatter);
    }

}