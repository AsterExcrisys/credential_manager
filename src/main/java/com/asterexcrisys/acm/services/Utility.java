package com.asterexcrisys.acm.services;

import com.asterexcrisys.acm.constants.Global;
import com.asterexcrisys.acm.constants.Hashing;
import com.asterexcrisys.acm.types.console.CommandType;
import com.asterexcrisys.acm.types.console.CredentialCommandType;
import com.asterexcrisys.acm.types.console.GenericCommandType;
import com.asterexcrisys.acm.types.console.VaultCommandType;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.crypto.params.Argon2Parameters.Builder;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unused")
public final class Utility {

    private Utility() {
        // This class should not be instantiable
    }

    public static String getCurrentDate() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Global.DATE_FORMAT);
        return now.format(formatter);
    }

    public static boolean deleteRecursively(Path path) {
        if (Files.exists(path) && Files.isDirectory(path)) {
            for (File file : Objects.requireNonNull(path.toFile().listFiles())) {
                if (!deleteRecursively(file.toPath())) {
                    return false;
                }
            }
            return true;
        }
        if (Files.exists(path) && Files.isRegularFile(path)) {
            return path.toFile().delete();
        }
        return false;
    }

    public static boolean isFileInDirectory(Path directory, Path file) {
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            return false;
        }
        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            return false;
        }
        return file.toAbsolutePath().startsWith(directory.toAbsolutePath());
    }

    public static Optional<String> hash(String data) {
        if (data == null || data.isBlank()) {
            return Optional.empty();
        }
        byte[] salt = new byte[Hashing.SALT_SIZE];
        Builder builder = new Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withIterations(Hashing.ITERATION_COUNT)
                .withMemoryAsKB(Hashing.MEMORY_USAGE)
                .withParallelism(Hashing.PARALLELISM_COUNT)
                .withSalt(salt);
        byte[] result = new byte[Hashing.HASH_SIZE];
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(builder.build());
        generator.generateBytes(data.getBytes(StandardCharsets.UTF_8), result, 0, result.length);
        return Optional.of(Base64.getEncoder().encodeToString(result));
    }

    public static boolean verify(String data, String hash) {
        if (data == null || hash == null || data.isBlank() || hash.isBlank()) {
            return false;
        }
        byte[] salt = new byte[Hashing.SALT_SIZE];
        Builder builder = new Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withIterations(Hashing.ITERATION_COUNT)
                .withMemoryAsKB(Hashing.MEMORY_USAGE)
                .withParallelism(Hashing.PARALLELISM_COUNT)
                .withSalt(salt);
        byte[] result = new byte[Hashing.HASH_SIZE];
        Argon2BytesGenerator verifier = new Argon2BytesGenerator();
        verifier.init(builder.build());
        verifier.generateBytes(data.getBytes(StandardCharsets.UTF_8), result, 0, result.length);
        return Base64.getEncoder().encodeToString(result).equals(hash);
    }

    public static Optional<String> derive(String data, byte[] salt) {
        if (data == null || data.isBlank() || salt == null || salt.length == 0) {
            return Optional.empty();
        }
        try {
            KeySpec keySpec = new PBEKeySpec(data.toCharArray(), salt, Hashing.KEY_ITERATION_COUNT, Hashing.KEY_SIZE);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(Hashing.KEY_DERIVATION_ALGORITHM);
            return Optional.ofNullable(Base64.getEncoder().encodeToString(secretKeyFactory.generateSecret(keySpec).getEncoded()));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            return Optional.empty();
        }
    }

    public static final class NonInteractiveShell {

        private NonInteractiveShell() {
            // This class should not be instantiable
        }

        public static Optional<? extends CommandType> fromValue(String value) {
            return VaultCommandType.fromValue(value);
        }

    }

    public static final class InteractiveShell {

        private InteractiveShell() {
            // This class should not be instantiable
        }

        public static Optional<? extends CommandType> fromValue(String value) {
            Optional<GenericCommandType> type = GenericCommandType.fromValue(value);
            if (type.isPresent()) {
                return type;
            }
            return CredentialCommandType.fromValue(value);
        }

    }

}