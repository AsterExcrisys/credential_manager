package com.asterexcrisys.acm.utility;

import com.asterexcrisys.acm.constants.EncryptionConstants;
import com.asterexcrisys.acm.services.encryption.GenericEncryptor;
import com.asterexcrisys.acm.services.encryption.KeyEncryptor;
import com.asterexcrisys.acm.types.encryption.CipherMode;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class EncryptionUtility {

    private static final Logger LOGGER = Logger.getLogger(EncryptionUtility.class.getName());

    private EncryptionUtility() {
        // This class should not be instantiable
    }

    public static byte[] checkPadding(byte[] input, int expectedLength) {
        if (input == null) {
            return new byte[expectedLength];
        }
        if (input.length < expectedLength) {
            byte[] output = new byte[expectedLength];
            System.arraycopy(input, 0, output, 0, input.length);
            return output;
        }
        return Arrays.copyOfRange(input, 0, expectedLength);
    }

    public static Optional<String> generateKey() {
        Optional<SecretKey> key = GenericEncryptor.generateKey();
        if (key.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Base64.getEncoder().encodeToString(key.get().getEncoded()));
    }

    public static Optional<String> deriveKey(String password, byte[] salt) {
        Optional<SecretKey> key = KeyEncryptor.deriveKey(password, salt);
        if (key.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Base64.getEncoder().encodeToString(key.get().getEncoded()));
    }

    public static boolean transformFile(GenericEncryptor encryptor, CipherMode mode, Path targetFile) {
        if (encryptor == null || targetFile == null) {
            return false;
        }
        if (PathUtility.isFileInDirectory(Paths.get("./data/"), targetFile)) {
            return false;
        }
        if (!Files.isReadable(targetFile) || !Files.isWritable(targetFile)) {
            return false;
        }
        Path temporaryFile = targetFile.getParent().resolve(String.format("%s.tmp", UUID.randomUUID()));
        try {
            try (
                    InputStream input = Files.newInputStream(targetFile, StandardOpenOption.READ);
                    OutputStream output = Files.newOutputStream(temporaryFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
            ) {
                byte[] buffer = new byte[EncryptionConstants.CHUNK_SIZE];
                int read;
                while ((read = input.read(buffer)) > 0) {
                    Optional<String> result = switch (mode) {
                        case ENCRYPT -> encryptor.encrypt(Arrays.copyOf(buffer, read));
                        case DECRYPT -> encryptor.decrypt(Arrays.copyOf(buffer, read));
                    };
                    if (result.isEmpty()) {
                        return false;
                    }
                    output.write(result.get().getBytes());
                }
            }
            Files.move(temporaryFile, targetFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            return true;
        } catch (IOException e) {
            LOGGER.warning("Error encrypting file: " + e.getMessage());
            return false;
        }
    }

}