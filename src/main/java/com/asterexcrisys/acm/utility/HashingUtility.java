package com.asterexcrisys.acm.utility;

import com.asterexcrisys.acm.constants.HashingConstants;
import com.asterexcrisys.acm.types.utility.Pair;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.crypto.params.Argon2Parameters.Builder;
import org.bouncycastle.util.Arrays;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class HashingUtility {

    private static final Logger LOGGER = Logger.getLogger(HashingUtility.class.getName());

    private HashingUtility() {
        // This class should not be instantiable
    }

    public static Optional<String> hashPassword(String password) {
        if (password == null || password.isBlank()) {
            return Optional.empty();
        }
        try {
            byte[] salt = new byte[HashingConstants.SALT_SIZE];
            SecureRandom.getInstanceStrong().nextBytes(salt);
            Builder builder = new Builder(Argon2Parameters.ARGON2_id)
                    .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                    .withIterations(HashingConstants.ITERATION_COUNT)
                    .withMemoryAsKB(HashingConstants.MEMORY_USAGE)
                    .withParallelism(HashingConstants.PARALLELISM_COUNT)
                    .withSalt(salt);
            Argon2Parameters parameters = builder.build();
            byte[] hashedData = new byte[HashingConstants.HASH_SIZE];
            Argon2BytesGenerator generator = new Argon2BytesGenerator();
            generator.init(parameters);
            generator.generateBytes(password.getBytes(StandardCharsets.UTF_8), hashedData, 0, hashedData.length);
            Optional<byte[]> result = constructHash(
                    salt,
                    parameters.getVersion(),
                    parameters.getIterations(),
                    parameters.getMemory(),
                    hashedData
            );
            if (result.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(Base64.getEncoder().encodeToString(result.get()));
        } catch (NoSuchAlgorithmException e) {
            LOGGER.severe("Error hashing password: " + e.getMessage());
            return Optional.empty();
        }
    }

    public static boolean verifyPassword(String password, String hash) {
        if (password == null || hash == null || password.isBlank() || hash.isBlank()) {
            return false;
        }
        Optional<Pair<Argon2Parameters, byte[]>> pair = deconstructHash(Base64.getDecoder().decode(hash));
        if (pair.isEmpty()) {
            return false;
        }
        byte[] hashedData = new byte[HashingConstants.HASH_SIZE];
        Argon2BytesGenerator verifier = new Argon2BytesGenerator();
        verifier.init(pair.get().first());
        verifier.generateBytes(password.getBytes(StandardCharsets.UTF_8), hashedData, 0, hashedData.length);
        return Arrays.areEqual(pair.get().second(), hashedData);
    }

    private static Optional<byte[]> constructHash(byte[] salt, int version, int iterationCount, int memoryUsage, byte[] hashedData) {
        if (salt == null || salt.length != HashingConstants.SALT_SIZE) {
            return Optional.empty();
        }
        if (version < 0 || iterationCount < 1 || memoryUsage < 1) {
            return Optional.empty();
        }
        if (hashedData == null || hashedData.length != HashingConstants.HASH_SIZE) {
            return Optional.empty();
        }
        ByteBuffer buffer = ByteBuffer.allocate(salt.length + hashedData.length + 12);
        buffer.put(salt);
        buffer.putInt(version);
        buffer.putInt(iterationCount);
        buffer.putInt(memoryUsage);
        buffer.put(hashedData);
        return Optional.of(buffer.array());
    }

    private static Optional<Pair<Argon2Parameters, byte[]>> deconstructHash(byte[] hash) {
        if (hash == null || hash.length != HashingConstants.SALT_SIZE + HashingConstants.HASH_SIZE + 12) {
            return Optional.empty();
        }
        byte[] salt = new byte[HashingConstants.SALT_SIZE];
        int version;
        int iterationCount;
        int memoryUsage;
        byte[] hashedData = new byte[HashingConstants.HASH_SIZE];
        ByteBuffer buffer = ByteBuffer.wrap(hash).asReadOnlyBuffer();
        buffer.get(salt);
        version = buffer.getInt();
        iterationCount = buffer.getInt();
        memoryUsage = buffer.getInt();
        buffer.get(hashedData);
        Builder builder = new Builder(Argon2Parameters.ARGON2_id);
        builder.withVersion(version);
        builder.withIterations(iterationCount);
        builder.withMemoryAsKB(memoryUsage);
        builder.withParallelism(HashingConstants.PARALLELISM_COUNT);
        builder.withSalt(salt);
        return Optional.of(Pair.of(builder.build(), hashedData));
    }

}