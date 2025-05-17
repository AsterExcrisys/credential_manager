package com.asterexcrisys.acm.utility;

import com.asterexcrisys.acm.constants.HashingConstants;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@SuppressWarnings("unused")
public final class DatabaseUtility {

    public static boolean backupTo(Path databaseFile, Path backupFile) {
        if (!PathUtility.isFileInDirectory(Paths.get("./data/"), databaseFile)) {
            return false;
        }
        if (PathUtility.isFileInDirectory(Paths.get("./data/"), backupFile)) {
            return false;
        }
        if (!Files.isReadable(databaseFile)) {
            return false;
        }
        if (!Files.isWritable(backupFile)) {
            return false;
        }
        try {
            Files.copy(databaseFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean restoreFrom(Path databaseFile, Path backupFile) {
        if (!PathUtility.isFileInDirectory(Paths.get("./data/"), databaseFile)) {
            return false;
        }
        if (PathUtility.isFileInDirectory(Paths.get("./data/"), backupFile)) {
            return false;
        }
        if (!Files.isWritable(databaseFile)) {
            return false;
        }
        if (!Files.isReadable(backupFile)) {
            return false;
        }
        try {
            Files.copy(backupFile, databaseFile, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean constructExport(Path file, byte[] salt) {
        if (file == null || !Files.exists(file) || !Files.isRegularFile(file) || !Files.isWritable(file)) {
            return false;
        }
        if (salt == null || salt.length != HashingConstants.SALT_SIZE) {
            return false;
        }
        try (RandomAccessFile accessor = new RandomAccessFile(file.toAbsolutePath().toString(), "rwd")) {
            accessor.seek(accessor.length());
            accessor.write(salt);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static Optional<byte[]> deconstructImport(Path file) {
        if (file == null || !Files.exists(file) || !Files.isRegularFile(file) || !Files.isReadable(file)) {
            return Optional.empty();
        }
        try (RandomAccessFile accessor = new RandomAccessFile(file.toAbsolutePath().toString(), "rwd")) {
            accessor.seek(accessor.length() - HashingConstants.SALT_SIZE);
            byte[] salt = new byte[HashingConstants.SALT_SIZE];
            accessor.read(salt);
            accessor.setLength(accessor.length() - HashingConstants.SALT_SIZE);
            return Optional.of(salt);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

}