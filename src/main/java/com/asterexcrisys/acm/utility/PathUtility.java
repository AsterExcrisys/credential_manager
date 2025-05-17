package com.asterexcrisys.acm.utility;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@SuppressWarnings("unused")
public final class PathUtility {

    private PathUtility() {
        // This class should not be instantiable
    }

    public static boolean deleteRecursively(Path path) {
        if (path == null) {
            return false;
        }
        if (Files.exists(path) && Files.isDirectory(path)) {
            for (File file : Objects.requireNonNull(path.toFile().listFiles())) {
                if (!deleteRecursively(file.toPath())) {
                    return false;
                }
            }
            return path.toFile().delete();
        }
        if (Files.exists(path) && Files.isRegularFile(path)) {
            return path.toFile().delete();
        }
        return false;
    }

    public static boolean isFileInDirectory(Path directory, Path file) {
        if (directory == null || file == null) {
            return false;
        }
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            return false;
        }
        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            return false;
        }
        return file.toAbsolutePath().startsWith(directory.toAbsolutePath());
    }

}